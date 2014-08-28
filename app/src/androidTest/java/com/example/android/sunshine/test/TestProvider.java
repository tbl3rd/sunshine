package com.example.android.sunshine.test;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherDbHelper;

import android.content.ContentResolver;
import android.content.ContentUris;
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
        final ContentResolver resolver = mContext.getContentResolver();
        final WeatherDbHelper helper = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final ContentValues locationIn = Util.makeLocationIn();
        final long locationId = db.insert(LocationEntry.TABLE, null, locationIn);
        locationIn.put(LocationEntry._ID, locationId);
        assertTrue(locationId != -1);
        Log.d(LOG_TAG, "testInsertReadProvider(): locationId == " + locationId);
        assertEquals(locationIn,
                Util.makeContentValues(
                        resolver.query(
                                LocationEntry.CONTENT_URI,
                                Util.locationColumns, null, null, null)));
        assertEquals(locationIn,
                Util.makeContentValues(
                        resolver.query(
                                ContentUris.withAppendedId(
                                        LocationEntry.CONTENT_URI, locationId),
                                Util.locationColumns, null, null, null)));
        final ContentValues weatherIn = Util.insertWeather(db, locationId);
        assertEquals(weatherIn,
                Util.makeContentValues(
                        resolver.query(
                                WeatherEntry.CONTENT_URI,
                                Util.weatherColumns, null, null, null)));
        db.close();
        helper.close();
    }

    public TestProvider() {
        super();
        Log.v(LOG_TAG, "TestProvider()");
        // mResolver = mContext.getContentResolver(); // WTF: mContext == null
    }
}
