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

    final static String[] locationColumns = {
        LocationEntry._ID,
        LocationEntry.COLUMN_SETTING,
        LocationEntry.COLUMN_CITY,
        LocationEntry.COLUMN_LATITUDE,
        LocationEntry.COLUMN_LONGITUDE
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
    };

    static ContentValues rowToContentValues(LocationRow row) {
        final ContentValues result = new ContentValues();
        result.put(LocationEntry.COLUMN_CITY, row.city);
        result.put(LocationEntry.COLUMN_SETTING, row.setting);
        result.put(LocationEntry.COLUMN_LATITUDE, row.latitude);
        result.put(LocationEntry.COLUMN_LONGITUDE, row.longitude);
        return result;
    }

    static LocationRow cursorToRow(Cursor c) {
        return new LocationRow(
                c.getString(c.getColumnIndex(LocationEntry.COLUMN_CITY)),
                c.getString(c.getColumnIndex(LocationEntry.COLUMN_SETTING)),
                c.getDouble(c.getColumnIndex(LocationEntry.COLUMN_LATITUDE)),
                c.getDouble(c.getColumnIndex(LocationEntry.COLUMN_LONGITUDE))
        );
    }

    public void testInsertAndReadDb() throws Throwable {
        Log.v(LOG_TAG, "TestDb.testInsertAndReadDb()");
        final WeatherDbHelper dbh = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = dbh.getWritableDatabase();
        final LocationRow rowIn
            = new LocationRow("North Pole", "99705", 64.772, -147.355);
        final ContentValues cv = rowToContentValues(rowIn);
        final long rowId = db.insert(LocationEntry.TABLE, null, cv);
        Log.d(LOG_TAG, "rowId == " + rowId);
        assertTrue(rowId != -1);
        final Cursor cursor = db.query(
                LocationEntry.TABLE, locationColumns,
                null, null, null, null, null);
        assertTrue(cursor.moveToFirst());
        rowIn.assertSame(cursorToRow(cursor));
    }

    public TestDb() {
        super();
        Log.v(LOG_TAG, "TestDb()");
    }
}
