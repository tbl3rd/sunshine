package com.example.android.sunshine.test;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;


public class Util {

    final static String LOG_TAG = Util.class.getSimpleName();

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
        WeatherEntry._ID,
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

    static public Map<String, Object> makeMap(String[] keys, Object[] values) {
        final int size = keys.length;
        final Map<String, Object> result = new HashMap<String, Object>(size);
        for (int index = 0; index < size; ++index) {
            final String k = keys[index];
            final Object v = values[index];
            result.put(k, v);
        }
        return result;
    }

    static public ContentValues makeContentValues(Map<String, Object> row) {
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

    static public ContentValues makeContentValues(Cursor c) {
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
}
