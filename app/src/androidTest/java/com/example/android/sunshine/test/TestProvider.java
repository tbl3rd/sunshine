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

    public void testDeleteDb() throws Throwable {
        Log.v(LOG_TAG, "TestProvider.testDeleteDb()");
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
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
        final WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final ContentValues locationIn = Util.makeLocationIn();
        final long locationRowId
            = db.insert(LocationEntry.TABLE, null, locationIn);
        locationIn.put(LocationEntry._ID, locationRowId);
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "testInsertReadProvider(): locationRowId == "
                + locationRowId);
        final Cursor locationCursor = db.query(
                LocationEntry.TABLE, Util.locationColumns,
                null, null, null, null, null);
        assertTrue(locationCursor.moveToFirst());
        final ContentValues locationOut
            = Util.makeContentValues(locationCursor);
        assertEquals(locationIn, locationOut);
        final ContentValues weatherIn = Util.makeWeatherIn();
        weatherIn.put(WeatherEntry.COLUMN_LOCATION_KEY, locationRowId);
        final long weatherRowId
            = db.insert(WeatherEntry.TABLE, null, weatherIn);
        Log.d(LOG_TAG, "weatherRowId == " + weatherRowId);
        assertTrue(weatherRowId != -1);
        weatherIn.put(WeatherEntry._ID, weatherRowId);
        final ContentResolver resolver = mContext.getContentResolver();
        final Cursor weatherCursor
            = resolver.query(WeatherEntry.CONTENT_URI,
                    Util.weatherColumns, null, null, null);
        assertTrue(weatherCursor.moveToFirst());
        final ContentValues weatherOut
            = Util.makeContentValues(weatherCursor);
        assertEquals(weatherIn, weatherOut);
        dbHelper.close();
    }

    public TestProvider() {
        super();
        Log.v(LOG_TAG, "TestProvider()");
    }
}
