package com.example.android.sunshine.test;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
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
        64.772,
        -147.355
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
        65.0,
        75.0,
        1.2,
        1.3,
        5.5,
        1.1,
        321
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

    public ContentValues makeContentValues(Cursor c) {
        final ContentValues result = new ContentValues();
        final int count = c.getColumnCount();
        Log.v(LOG_TAG, "makeContentValues(c): count == " + count);
        for (int index = 0; index < count; ++index) {
            final String name = c.getColumnName(index);
            Log.v(LOG_TAG, "makeContentValues(cursor): " + name + ", " + index);
            final int kind = c.getType(index);
            Log.v(LOG_TAG, "makeContentValues(cursor): kind == " + kind);
            switch (kind) {
            case Cursor.FIELD_TYPE_NULL:
                Log.v(LOG_TAG, "makeContentValues(cursor): putNull()");
                result.putNull(name);
                break;
            case Cursor.FIELD_TYPE_INTEGER:
                Log.v(LOG_TAG, "makeContentValues(c): " + c.getLong(index));
                result.put(name, c.getLong(index));
                break;
            case Cursor.FIELD_TYPE_FLOAT:
                Log.v(LOG_TAG, "makeContentValues(c): " + c.getDouble(index));
                result.put(name, c.getDouble(index));
                break;
            case Cursor.FIELD_TYPE_STRING:
                Log.v(LOG_TAG, "makeContentValues(c): " + c.getString(index));
                result.put(name, c.getString(index));
                break;
            case Cursor.FIELD_TYPE_BLOB:
                Log.v(LOG_TAG, "makeContentValues(c): " + c.getBlob(index));
                result.put(name, c.getBlob(index));
                break;
            default:
                Log.d(LOG_TAG, "Cursor.getType() == " + c.getType(index));
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
            for (Map.Entry<String, Object> e : x.valueSet()) {
                final String k = e.getKey();
                final String xs = e.getValue().toString();
                final String ys = y.get(k).toString();
                Log.v(LOG_TAG, "assertEquals(): k == " + k);
                Log.v(LOG_TAG, "assertEquals(): xs == " + xs);
                Log.v(LOG_TAG, "assertEquals(): ys == " + ys);
                assertEquals(xs, ys);
            }
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
        assertEquals(locationIn, makeContentValues(locationCursor));
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
        assertEquals(weatherIn, makeContentValues(weatherCursor));
    }

    public TestDb() {
        super();
        Log.v(LOG_TAG, "TestDb()");
    }
}

// WTF, Java.  ContentValues equality must be pointer-wise.
//
// I/TestRunner( 2551): junit.framework.AssertionFailedError: expected:
// <setting=99705 longitude=-147.355 latitude=64.772 _id=1 city=North Pole>
// but was:
// <setting=99705 longitude=-147.355 latitude=64.772 _id=1 city=North Pole>
