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
        Log.v(TAG, "TestDb.testInsertReadDb()");
        final WeatherDbHelper helper = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final ContentValues locationIn = Util.makeLocationIn();
        final long locationId = db.insert(LocationEntry.TABLE, null, locationIn);
        locationIn.put(LocationEntry._ID, locationId);
        assertTrue(locationId != -1);
        Log.d(TAG, "insertCheckLocation(): locationId == " + locationId);
        final Cursor locationCursor = db.query(
                LocationEntry.TABLE, Util.locationColumns,
                null, null, null, null, null);
        assertEquals(locationIn, Util.makeContentValues(locationCursor));
        locationCursor.close();
        final ContentValues weatherIn = Util.insertWeather(db, locationId);
        final Cursor weatherCursor = db.query(
                WeatherEntry.TABLE, Util.weatherColumns,
                null, null, null, null, null);
        assertEquals(weatherIn, Util.makeContentValues(weatherCursor));
        weatherCursor.close();
        db.close();
        helper.close();
    }

    public TestDb() {
        super();
        Log.v(TAG, "TestDb()");
    }
}
