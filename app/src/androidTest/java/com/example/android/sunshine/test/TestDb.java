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

    final static String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        Log.v(LOG_TAG, "TestDb.testCreateDb()");
        mContext.deleteDatabase(WeatherDbHelper.DATABASE);
        final WeatherDbHelper helper = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = helper.getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
        helper.close();
    }

    public void testInsertReadDb() throws Throwable {
        Log.v(LOG_TAG, "TestDb.testInsertReadDb()");
        final WeatherDbHelper helper = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final long locationId = Util.insertCheckLocation(db);
        final ContentValues weatherIn = Util.insertWeather(db, locationId);
        final Cursor cursor = db.query(
                WeatherEntry.TABLE, Util.weatherColumns,
                null, null, null, null, null);
        assertTrue(cursor.moveToFirst());
        final ContentValues weatherOut = Util.makeContentValues(cursor);
        assertEquals(weatherIn, weatherOut);
        db.close();
        helper.close();
    }

    public TestDb() {
        super();
        Log.v(LOG_TAG, "TestDb()");
    }
}
