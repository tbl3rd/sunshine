package com.example.android.sunshine.test;

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

    public void testCreateDb() throws Throwable {
        Log.v(LOG_TAG, "TestDb.testCreateDb()");
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        final SQLiteDatabase db
            = new WeatherDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    static class LocationRow {
        public final String city;
        public final String setting;
        public final double latitude;
        public final double longitude;
        void assertSame(LocationRow that) {
            assertEquals(that.city, city);
            assertEquals(that.setting, setting);
            assertEquals(that.latitude, latitude);
            assertEquals(that.longitude, longitude);
        }
        public LocationRow(String cit, String set, double lat, double lon) {
            city = cit; setting = set; latitude = lat; longitude = lon;
        }
    }

    static ContentValues locationRowToContentValues(LocationRow row) {
        final ContentValues result = new ContentValues();
        result.put(LocationEntry.COLUMN_CITY, row.city);
        result.put(LocationEntry.COLUMN_SETTING, row.setting);
        result.put(LocationEntry.COLUMN_LATITUDE, row.latitude);
        result.put(LocationEntry.COLUMN_LONGITUDE, row.longitude);
        return result;
    }

    static LocationRow cursorToLocationRow(Cursor c) {
        return new LocationRow(
                c.getString(c.getColumnIndex(LocationEntry.COLUMN_CITY)),
                c.getString(c.getColumnIndex(LocationEntry.COLUMN_SETTING)),
                c.getDouble(c.getColumnIndex(LocationEntry.COLUMN_LATITUDE)),
                c.getDouble(c.getColumnIndex(LocationEntry.COLUMN_LONGITUDE))
        );
    }

    static class WeatherRow {
        public final long locationId;
        public final String date;
        public final String description;
        public final double minimum;
        public final double maximum;
        public final double humidity;
        public final double pressure;
        public final double wind;
        public final double direction;
        public final long weatherId;
        void assertSame(WeatherRow that) {
            assertEquals(that.locationId, locationId);
            assertEquals(that.date, date);
            assertEquals(that.description, description);
            assertEquals(that.minimum, minimum);
            assertEquals(that.maximum, maximum);
            assertEquals(that.humidity, humidity);
            assertEquals(that.pressure, pressure);
            assertEquals(that.wind, wind);
            assertEquals(that.direction, direction);
            assertEquals(that.weatherId, weatherId);
        }
        public WeatherRow(
                long locId, String date, String desc, double min, double max,
                double hum, double pres, double wind, double dir, long wId)
        {
            this.locationId = locId;
            this.date = date;
            this.description = desc;
            this.minimum = min;
            this.maximum = max;
            this.humidity = hum;
            this.pressure = pres;
            this.wind = wind;
            this.direction = dir;
            this.weatherId = wId;
        }
    }

    static ContentValues weatherRowToContentValues(WeatherRow row) {
        final ContentValues result = new ContentValues();
        result.put(WeatherEntry.COLUMN_LOCATION_KEY, row.locationId);
        result.put(WeatherEntry.COLUMN_DATE, row.date);
        result.put(WeatherEntry.COLUMN_DESCRIPTION, row.description);
        result.put(WeatherEntry.COLUMN_MINIMUM, row.minimum);
        result.put(WeatherEntry.COLUMN_MAXIMUM, row.maximum);
        result.put(WeatherEntry.COLUMN_HUMIDITY, row.humidity);
        result.put(WeatherEntry.COLUMN_PRESSURE, row.pressure);
        result.put(WeatherEntry.COLUMN_WIND, row.wind);
        result.put(WeatherEntry.COLUMN_DIRECTION, row.direction);
        result.put(WeatherEntry.COLUMN_WEATHER_ID, row.weatherId);
        return result;
    }

    static WeatherRow cursorToWeatherRow(Cursor c) {
        return new WeatherRow(
                c.getLong(c.getColumnIndex(WeatherEntry.COLUMN_LOCATION_KEY)),
                c.getString(c.getColumnIndex(WeatherEntry.COLUMN_DATE)),
                c.getString(c.getColumnIndex(WeatherEntry.COLUMN_DESCRIPTION)),
                c.getDouble(c.getColumnIndex(WeatherEntry.COLUMN_MINIMUM)),
                c.getDouble(c.getColumnIndex(WeatherEntry.COLUMN_MAXIMUM)),
                c.getDouble(c.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY)),
                c.getDouble(c.getColumnIndex(WeatherEntry.COLUMN_PRESSURE)),
                c.getDouble(c.getColumnIndex(WeatherEntry.COLUMN_WIND)),
                c.getDouble(c.getColumnIndex(WeatherEntry.COLUMN_DIRECTION)),
                c.getLong(c.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID))
        );
    }

    public void testInsertAndReadDb() throws Throwable {
        Log.v(LOG_TAG, "TestDb.testInsertAndReadDb()");
        final WeatherDbHelper dbh = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = dbh.getWritableDatabase();
        final LocationRow locationIn
            = new LocationRow("North Pole", "99705", 64.772, -147.355);
        final long locationRowId = db.insert(LocationEntry.TABLE, null,
                locationRowToContentValues(locationIn));
        Log.d(LOG_TAG, "locationRowId == " + locationRowId);
        assertTrue(locationRowId != -1);
        final Cursor locationCursor = db.query(
                LocationEntry.TABLE, locationColumns,
                null, null, null, null, null);
        assertTrue(locationCursor.moveToFirst());
        locationIn.assertSame(cursorToLocationRow(locationCursor));
        final WeatherRow weatherIn = new WeatherRow(
                locationRowId, "20141205", "Asteroids",
                65, 75, 1.2, 1.3, 5.5, 1.1, 321);
        final long weatherRowId = db.insert(WeatherEntry.TABLE, null,
                weatherRowToContentValues(weatherIn));
        final Cursor weatherCursor = db.query(
                WeatherEntry.TABLE, weatherColumns,
                null, null, null, null, null);
        assertTrue(weatherCursor.moveToFirst());
        weatherIn.assertSame(cursorToWeatherRow(weatherCursor));
    }

    public TestDb() {
        super();
        Log.v(LOG_TAG, "TestDb()");
    }
}
