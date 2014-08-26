package com.example.android.sunshine.test;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherDbHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.suitebuilder.TestSuiteBuilder;
import android.util.Log;
import junit.framework.Test;


public class TestDb extends AndroidTestCase {

    final static String LOG_TAG = TestDb.class.getSimpleName();

    final static String[] locationColumns = {
        LocationEntry._ID,
        LocationEntry.COLUMN_SETTING,
        LocationEntry.COLUMN_CITY,
        LocationEntry.COLUMN_LATITUDE,
        LocationEntry.COLUMN_LONGITUDE
    };

    final static Object[] locationRow = {
        null,
        "99705",
        "North Pole",
        Double.valueOf(64.772),
        Double.valueOf(-147.355)
    };

    final static String[] weatherColumns = {
        WeatherEntry.COLUMN_LOCATION_KEY,
        WeatherEntry.COLUMN_DATE,
        WeatherEntry.COLUMN_DESCRIPTION,
        WeatherEntry.COLUMN_MINIMUM,
        WeatherEntry.COLUMN_MAXIMUM,
        WeatherEntry.COLUMN_HUMIDITY,
        WeatherEntry.COLUMN_PRESSURE,
        WeatherEntry.COLUMN_WIND,
        WeatherEntry.COLUMN_DIRECTION,
        WeatherEntry.COLUMN_WEATHER_ID
    };

    final static Object[] weatherRow = {
        null,
        "20141205",
        "Asteroids",
        Double.valueOf(65),
        Double.valueOf(75),
        Double.valueOf(1.2),
        Double.valueOf(1.3),
        Double.valueOf(5.5),
        Double.valueOf(1.1),
        Long.valueOf(321)
    };

    public Map<String, Object> makeMap(String[] keys, Object[] values) {
        Log.v(LOG_TAG, "makeMap(): keys.length == " + keys.length);
        final int size = keys.length;
        final Map<String, Object> result = new HashMap<String, Object>(size);
        for (int index = 0; index < size; ++index) {
            final String k = keys[index];
            final Object v = values[index];
            Log.v(LOG_TAG, "makeMap(): " + index + ": " + k + ", " + v);
            result.put(k, v);
        }
        return result;
    }

    public ContentValues makeContentValues(Map<String, Object> row) {
        final ContentValues result = new ContentValues();
        for (Map.Entry<String, Object> e: row.entrySet()) {
            final String key = e.getKey();
            final Object value = e.getValue();
            Log.v(LOG_TAG, "makeContentValues(map): " + key + ", " + value);
            if (value instanceof Double) {
                result.put(key, ((Double)value).doubleValue());
            } else if (value instanceof String) {
                result.put(key, (String)value);
            } else if (value instanceof Integer) {
                result.put(key, ((Integer)value).intValue());
            } else if (value instanceof Long) {
                result.put(key, ((Long)value).longValue());
            } else if (value == null) {
                result.putNull(key);
            } else {
                Log.d(LOG_TAG,
                        "makeContentValues(else): " + key + ", " + value);
            }
        }
        return result;
    }

    public static void assertEquals(ContentValues x, ContentValues y) {
        Log.v(LOG_TAG, "assertEquals(): x == " + x + ", y == " + y);
        if (x != y) {
            Log.v(LOG_TAG, "assertEquals(): x.size() == " + x.size());
            Log.v(LOG_TAG, "assertEquals(): y.size() == " + y.size());
            assertEquals(x.size(), y.size());
            final Set<Map.Entry<String, Object>> xs = x.valueSet();
            final Set<Map.Entry<String, Object>> ys = y.valueSet();
            Log.v(LOG_TAG, "assertEquals(): xs == " + xs);
            Log.v(LOG_TAG, "assertEquals(): ys == " + ys);
            final boolean wtf = xs.containsAll(ys);
            Log.v(LOG_TAG, "assertEquals(): wtf == " + wtf);
            assertTrue(wtf);
        }
    }

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
        final WeatherDbHelper dbh = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = dbh.getWritableDatabase();
        final ContentValues locationIn
            = makeContentValues(makeMap(locationColumns, locationRow));
        final long locationRowId
            = db.insert(LocationEntry.TABLE, null, locationIn);
        locationIn.put(LocationEntry._ID, locationRowId);
        Log.d(LOG_TAG, "locationRowId == " + locationRowId);
        assertTrue(locationRowId != -1);
        final Cursor locationCursor = db.query(
                LocationEntry.TABLE, locationColumns,
                null, null, null, null, null);
        assertTrue(locationCursor.moveToFirst());
        final ContentValues locationOut = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(locationCursor, locationOut);
        assertEquals(locationIn, locationOut);
        final ContentValues weatherIn
            = makeContentValues(makeMap(weatherColumns, weatherRow));
        weatherIn.put(WeatherEntry.COLUMN_LOCATION_KEY, locationRowId);
        Log.v(LOG_TAG, "weatherIn == " + weatherIn);
        final long weatherRowId
            = db.insert(WeatherEntry.TABLE, null, weatherIn);
        Log.d(LOG_TAG, "weatherRowId == " + weatherRowId);
        assertTrue(weatherRowId != -1);
        final Cursor weatherCursor = db.query(
                WeatherEntry.TABLE, weatherColumns,
                null, null, null, null, null);
        assertTrue(weatherCursor.moveToFirst());
        final ContentValues weatherOut = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(weatherCursor, weatherOut);
        assertEquals(weatherIn, weatherOut);
    }

    public TestDb() {
        super();
        Log.v(LOG_TAG, "TestDb()");
        final double d = 64.772;
        final Object d0 = Double.valueOf(d);
        final Object d1 = Double.valueOf(d);
        final boolean wtf = d0.equals(d1);
        Log.v(LOG_TAG, "DOUBLES: " + wtf);
    }
}

// junit.framework.AssertionFailedError: expected:
// <setting=99705 longitude=-147.355 latitude=64.772 _id=1 city=North Pole>
// but was:
// <setting=99705 longitude=-147.355 latitude=64.772 _id=1 city=North Pole>
