package com.example.android.sunshine.test;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherDbHelper;
import com.example.android.sunshine.test.Util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;


public class TestDb extends AndroidTestCase {

    final static String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        Log.v(LOG_TAG, "TestDb.testCreateDb()");
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        final SQLiteDatabase db
            = new WeatherDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() throws Throwable {
        Log.v(LOG_TAG, "TestDb.testInsertReadDb()");
        final WeatherDbHelper dbh = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = dbh.getWritableDatabase();
        final ContentValues locationIn = Util.makeLocationIn();
        final long locationRowId
            = db.insert(LocationEntry.TABLE, null, locationIn);
        locationIn.put(LocationEntry._ID, locationRowId);
        Log.d(LOG_TAG, "testInsertReadDb(): locationRowId == "
                + locationRowId);
        assertTrue(locationRowId != -1);
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
        Log.d(LOG_TAG, "testInsertReadDb(): weatherRowId == "
                + weatherRowId);
        assertTrue(weatherRowId != -1);
        weatherIn.put(WeatherEntry._ID, weatherRowId);
        final Cursor weatherCursor = db.query(
                WeatherEntry.TABLE, Util.weatherColumns,
                null, null, null, null, null);
        assertTrue(weatherCursor.moveToFirst());
        final ContentValues weatherOut
            = Util.makeContentValues(weatherCursor);
        assertEquals(weatherIn, weatherOut);
    }

    public TestDb() {
        super();
        Log.v(LOG_TAG, "TestDb()");
    }
}
