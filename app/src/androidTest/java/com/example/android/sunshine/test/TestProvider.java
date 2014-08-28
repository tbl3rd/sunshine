package com.example.android.sunshine.test;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherDbHelper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;


public class TestProvider extends AndroidTestCase {

    final static String LOG_TAG = TestProvider.class.getSimpleName();

    public void testDeleteDb() {
        Log.v(LOG_TAG, "TestProvider.testDeleteDb()");
        mContext.deleteDatabase(WeatherDbHelper.DATABASE);
    }

    public void testGetType() {
        final ContentResolver resolver = mContext.getContentResolver();
        assertEquals(WeatherEntry.CONTENT_TYPE_DIR,
                resolver.getType(WeatherEntry.CONTENT_URI));
        assertEquals(WeatherEntry.CONTENT_TYPE_DIR,
                resolver.getType(WeatherEntry.buildWeatherLocation("02138")));
        assertEquals(WeatherEntry.CONTENT_TYPE_ITEM,
                resolver.getType(
                        WeatherEntry.buildWeatherLocationDate(
                                "02138", "20140612")));
        assertEquals(LocationEntry.CONTENT_TYPE_DIR,
                resolver.getType(LocationEntry.CONTENT_URI));
        assertEquals(LocationEntry.CONTENT_TYPE_ITEM,
                resolver.getType(LocationEntry.buildLocationId(1L)));
    }

    public void testInsertReadProvider() {
        Log.v(LOG_TAG, "testInsertReadProvider()");
        final WeatherDbHelper helper = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final long locationId = Util.insertCheckLocation(db);
        final ContentValues weatherIn = Util.insertWeather(db, locationId);
        final ContentResolver resolver = mContext.getContentResolver();
        final Cursor cursor = resolver.query(WeatherEntry.CONTENT_URI,
                Util.weatherColumns, null, null, null);
        final ContentValues weatherOut = Util.makeContentValues(cursor);
        assertEquals(weatherIn, weatherOut);
        db.close();
        helper.close();
    }

    public TestProvider() {
        super();
        Log.v(LOG_TAG, "TestProvider()");
        // mResolver = mContext.getContentResolver(); // WTF: mContext == null
    }
}
