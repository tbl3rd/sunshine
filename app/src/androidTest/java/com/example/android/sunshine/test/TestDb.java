package com.example.android.sunshine.test;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherDbHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.suitebuilder.TestSuiteBuilder;
import android.util.Log;
import junit.framework.Test;


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

    public void testInsertAndReadDb() throws Throwable {
        Log.v(LOG_TAG, "TestDb.testInsertAndReadDb()");
        final String testCity = "North Pole";
        final String testLocation = "99705";
        final double testLatitude = 64.772;
        final double testLongitude = -147.355;
        final WeatherDbHelper dbh = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = dbh.getWritableDatabase();
        final ContentValues cv = new ContentValues();
        cv.put(LocationEntry.COLUMN_LOCATION_CITY, testCity);
        cv.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocation);
        cv.put(LocationEntry.COLUMN_LOCATION_LATITUDE, testLatitude);
        cv.put(LocationEntry.COLUMN_LOCATION_LONGITUDE, testLongitude);
        final long rowId = db.insert(LocationEntry.TABLE_NAME, null, cv);
        assertTrue(rowId != -1);
        Log.d(LOG_TAG, "rowId == " + rowId);
        final String[] columns = {
            LocationEntry._ID,
            LocationEntry.COLUMN_LOCATION_SETTING,
            LocationEntry.COLUMN_LOCATION_CITY,
            LocationEntry.COLUMN_LOCATION_LATITUDE,
            LocationEntry.COLUMN_LOCATION_LONGITUDE
        };
        final Cursor cursor = db.query(
                LocationEntry.TABLE_NAME, columns,
                null, null, null, null, null);
        if (cursor.moveToFirst()) {
            final int cityIndex
                = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_CITY);
            final String city = cursor.getString(cityIndex);
            final int locationIndex
                = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
            final String location = cursor.getString(locationIndex);
            final int latitudeIndex
                = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_LATITUDE);
            final double latitude = cursor.getDouble(latitudeIndex);
            final int longitudeIndex
                = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_LONGITUDE);
            final double longitude = cursor.getDouble(longitudeIndex);
            assertEquals(testCity, city);
            assertEquals(testLocation, location);
            assertEquals(testLatitude, latitude);
            assertEquals(testLongitude, longitude);
        }
    }

    public TestDb() {
        super();
        Log.v(LOG_TAG, "TestDb()");
    }
}
