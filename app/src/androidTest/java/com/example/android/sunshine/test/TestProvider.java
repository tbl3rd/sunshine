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

    public long insertLocation(SQLiteDatabase db) {
        Log.v(LOG_TAG, "insertLocation()");
        final ContentValues in = Util.makeLocationIn();
        final long result = db.insert(LocationEntry.TABLE, null, in);
        in.put(LocationEntry._ID, result);
        assertTrue(result != -1);
        Log.d(LOG_TAG, "insertLocation(): result == " + result);
        final Cursor cursor = db.query(
                LocationEntry.TABLE, Util.locationColumns,
                null, null, null, null, null);
        assertTrue(cursor.moveToFirst());
        final ContentValues out = Util.makeContentValues(cursor);
        assertEquals(in, out);
        return result;
    }

    public long insertWeather(SQLiteDatabase db, long locationId) {
        Log.v(LOG_TAG, "insertWeather()");
        final ContentValues in = Util.makeWeatherIn();
        in.put(WeatherEntry.COLUMN_LOCATION_KEY, locationId);
        final long result = db.insert(WeatherEntry.TABLE, null, in);
        Log.d(LOG_TAG, "insertWeather(): result == " + result);
        assertTrue(result != -1);
        in.put(WeatherEntry._ID, result);
        final ContentResolver resolver = mContext.getContentResolver();
        final Cursor cursor = resolver.query(WeatherEntry.CONTENT_URI,
                Util.weatherColumns, null, null, null);
        assertTrue(cursor.moveToFirst());
        final ContentValues out = Util.makeContentValues(cursor);
        assertEquals(in, out);
        return result;
    }

    public void testInsertReadProvider() {
        Log.v(LOG_TAG, "testInsertReadProvider()");
        final WeatherDbHelper helper = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final long locationId = insertLocation(db);
        final long weatherIdIgnored = insertWeather(db, locationId);
        db.close();
        helper.close();
    }

    public TestProvider() {
        super();
        Log.v(LOG_TAG, "TestProvider()");
        // mResolver = mContext.getContentResolver(); // TBL: mContext == null
    }
}
