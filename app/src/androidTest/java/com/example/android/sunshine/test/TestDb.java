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
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        final SQLiteDatabase db
            = new WeatherDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public long insertLocation(SQLiteDatabase db)
        throws Throwable
    {
        Log.v(LOG_TAG, "TestDb.insertLocation()");
        final ContentValues in = Util.makeLocationIn();
        final long result = db.insert(LocationEntry.TABLE, null, in);
        in.put(LocationEntry._ID, result);
        Log.d(LOG_TAG, "insertLocation(): result == " + result);
        assertTrue(result != -1);
        final Cursor cursor = db.query(
                LocationEntry.TABLE, Util.locationColumns,
                null, null, null, null, null);
        assertTrue(cursor.moveToFirst());
        final ContentValues out = Util.makeContentValues(cursor);
        assertEquals(in, out);
        return result;
    }

    public long insertWeather(SQLiteDatabase db, long locationId)
        throws Throwable
    {
        Log.v(LOG_TAG, "TestDb.insertWeather()");
        final ContentValues in = Util.makeWeatherIn();
        in.put(WeatherEntry.COLUMN_LOCATION_KEY, locationId);
        final long result = db.insert(WeatherEntry.TABLE, null, in);
        Log.d(LOG_TAG, "insertWeather(): result == " + result);
        assertTrue(result != -1);
        in.put(WeatherEntry._ID, result);
        final Cursor cursor = db.query(
                WeatherEntry.TABLE, Util.weatherColumns,
                null, null, null, null, null);
        assertTrue(cursor.moveToFirst());
        final ContentValues out = Util.makeContentValues(cursor);
        assertEquals(in, out);
        return result;
    }

    public void testInsertReadDb() throws Throwable {
        Log.v(LOG_TAG, "TestDb.testInsertReadDb()");
        final WeatherDbHelper helper = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final long locationId = insertLocation(db);
        final long weatherIdIgnored = insertWeather(db, locationId);
        helper.close();
    }

    public TestDb() {
        super();
        Log.v(LOG_TAG, "TestDb()");
    }
}
