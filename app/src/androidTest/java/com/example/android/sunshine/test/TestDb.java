package com.example.android.sunshine.test;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherDbHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;


public class TestDb extends AndroidTestCase {

    final static String TAG = TestDb.class.getSimpleName();

    public void testCreateDb() {
        Log.v(TAG, "TestDb.testCreateDb()");
        mContext.deleteDatabase(WeatherDbHelper.DATABASE);
        final WeatherDbHelper helper = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = helper.getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
        helper.close();
    }

    public void testInsertReadDb() {
        Log.v(TAG, "testInsertReadDb()");
        final WeatherDbHelper helper = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final ContentValues locationIn = TestUtility.makeLocationIn();
        final long locationId
            = db.insert(LocationEntry.TABLE, null, locationIn);
        locationIn.put(LocationEntry._ID, locationId);
        assertTrue(locationId != -1);
        Log.d(TAG, "testInsertReadDb(): locationId == " + locationId);
        final Cursor locationCursor = db.query(
                LocationEntry.TABLE, TestUtility.locationColumns,
                null, null, null, null, null);
        assertEquals(locationIn, TestUtility.makeContentValues(locationCursor));
        locationCursor.close();
        final ContentValues weatherIn = TestUtility.makeWeatherIn();
        weatherIn.put(WeatherEntry.COLUMN_LOCATION_KEY, locationId);
        final long weatherId = db.insert(WeatherEntry.TABLE, null, weatherIn);
        Log.d(TAG, "testInsertReadDb(): weatherId == " + weatherId);
        assertTrue(weatherId != -1);
        weatherIn.put(WeatherEntry._ID, weatherId);
        final Cursor weatherCursor = db.query(
                WeatherEntry.TABLE, TestUtility.weatherColumns,
                null, null, null, null, null);
        assertEquals(weatherIn, TestUtility.makeContentValues(weatherCursor));
        weatherCursor.close();
        db.close();
        helper.close();
    }

    public TestDb() {
        super();
        Log.v(TAG, "TestDb()");
    }
}
