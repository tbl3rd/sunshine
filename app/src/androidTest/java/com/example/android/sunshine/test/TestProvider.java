package com.example.android.sunshine.test;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherDbHelper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.suitebuilder.TestSuiteBuilder;
import android.util.Log;
import junit.framework.Test;


public class TestProvider extends AndroidTestCase {

    final static String LOG_TAG = TestProvider.class.getSimpleName();

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
        final int size = keys.length;
        final Map<String, Object> result = new HashMap<String, Object>(size);
        for (int index = 0; index < size; ++index) {
            final String k = keys[index];
            final Object v = values[index];
            result.put(k, v);
        }
        return result;
    }

    public ContentValues makeContentValues(Map<String, Object> row) {
        final ContentValues result = new ContentValues();
        for (Map.Entry<String, Object> e: row.entrySet()) {
            final String key = e.getKey();
            final Object value = e.getValue();
            if (value instanceof Double) {
                result.put(key, (Double)value);
            } else if (value instanceof String) {
                result.put(key, (String)value);
            } else if (value instanceof Integer) {
                result.put(key, (Integer)value);
            } else if (value instanceof Long) {
                result.put(key, (Long)value);
            } else if (value == null) {
                result.putNull(key);
            } else {
                final String vc = (value == null)
                    ? "NULL"
                    : value.getClass().getSimpleName();
                Log.d(LOG_TAG, "makeContentValues(else): "
                        + key + ", " + value + " of " + vc);
            }
        }
        return result;
    }

    public ContentValues makeContentValues(Cursor c) {
        final ContentValues result = new ContentValues();
        final int count = c.getColumnCount();
        for (int index = 0; index < count; ++index) {
            final String name = c.getColumnName(index);
            switch (c.getType(index)) {
            case Cursor.FIELD_TYPE_NULL:
                result.putNull(name);
                break;
            case Cursor.FIELD_TYPE_INTEGER:
                result.put(name, c.getLong(index));
                break;
            case Cursor.FIELD_TYPE_FLOAT:
                result.put(name, c.getDouble(index));
                break;
            case Cursor.FIELD_TYPE_STRING:
                result.put(name, c.getString(index));
                break;
            case Cursor.FIELD_TYPE_BLOB:
                result.put(name, c.getBlob(index));
                break;
            default:
                Log.d(LOG_TAG, "Cursor.getType() == " + c.getType(index));
            }
        }
        return result;
    }

    public void testDeleteDb() throws Throwable {
        Log.v(LOG_TAG, "TestProvider.testDeleteDb()");
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void testGetType() {
        final ContentResolver resolver = mContext.getContentResolver();
        final String location = "94074";
        final String date = "20140612";
        assertEquals(WeatherEntry.CONTENT_TYPE_DIR,
                resolver.getType(WeatherEntry.CONTENT_URI));
        assertEquals(WeatherEntry.CONTENT_TYPE_DIR,
                resolver.getType(WeatherEntry.buildWeatherLocation(location)));
        assertEquals(WeatherEntry.CONTENT_TYPE_ITEM,
                resolver.getType(
                        WeatherEntry.buildWeatherLocationDate(location, date)));
        assertEquals(LocationEntry.CONTENT_TYPE_DIR,
                resolver.getType(LocationEntry.CONTENT_URI));
        assertEquals(LocationEntry.CONTENT_TYPE_ITEM,
                resolver.getType(LocationEntry.buildLocationId(1L)));
    }

    public void testInsertReadProvider() {
        Log.v(LOG_TAG, "testInsertReadProvider()");
        final WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final ContentValues locationIn
            = makeContentValues(makeMap(locationColumns, locationRow));
        final long locationRowId
            = db.insert(LocationEntry.TABLE, null, locationIn);
        locationIn.put(LocationEntry._ID, locationRowId);
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "testInsertReadProvider(): locationRowId == "
                + locationRowId);
        final Cursor locationCursor = db.query(
                LocationEntry.TABLE, locationColumns,
                null, null, null, null, null);
        assertTrue(locationCursor.moveToFirst());
        final ContentValues locationOut = makeContentValues(locationCursor);
        assertEquals(locationIn, locationOut);
        final ContentValues weatherIn
            = makeContentValues(makeMap(weatherColumns, weatherRow));
        weatherIn.put(WeatherEntry.COLUMN_LOCATION_KEY, locationRowId);
        final long weatherRowId
            = db.insert(WeatherEntry.TABLE, null, weatherIn);
        Log.d(LOG_TAG, "weatherRowId == " + weatherRowId);
        assertTrue(weatherRowId != -1);
        final ContentResolver resolver = mContext.getContentResolver();
        final Cursor weatherCursor
            = resolver.query(WeatherEntry.CONTENT_URI,
                    weatherColumns, null, null, null);
        assertTrue(weatherCursor.moveToFirst());
        final ContentValues weatherOut = makeContentValues(weatherCursor);
        assertEquals(weatherIn, weatherOut);
        dbHelper.close();
    }

    public TestProvider() {
        super();
        Log.v(LOG_TAG, "TestProvider()");
    }
}
