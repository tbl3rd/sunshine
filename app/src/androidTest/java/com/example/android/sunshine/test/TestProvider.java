package com.example.android.sunshine.test;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherDbHelper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;


public class TestProvider extends AndroidTestCase {

    final static String TAG = TestProvider.class.getSimpleName();

    public void testDeleteDb() {
        Log.v(TAG, "TestProvider.testDeleteDb()");
        mContext.deleteDatabase(WeatherDbHelper.DATABASE);
    }

    public void testGetType() {
        final ContentResolver resolver = mContext.getContentResolver();
        assertEquals(WeatherEntry.CONTENT_TYPE_DIR,
                resolver.getType(WeatherEntry.CONTENT_URI));
        assertEquals(WeatherEntry.CONTENT_TYPE_DIR,
                resolver.getType(
                        WeatherEntry.buildWeatherLocation(Util.WHERE)));
        assertEquals(WeatherEntry.CONTENT_TYPE_ITEM,
                resolver.getType(
                        WeatherEntry.buildWeatherLocationDate(
                                Util.WHERE, Util.WHEN)));
        assertEquals(LocationEntry.CONTENT_TYPE_DIR,
                resolver.getType(LocationEntry.CONTENT_URI));
        assertEquals(LocationEntry.CONTENT_TYPE_ITEM,
                resolver.getType(LocationEntry.buildLocationId(1L)));
    }

    public void testInsertReadProvider() {
        Log.v(TAG, "testInsertReadProvider()");
        final ContentResolver resolver = mContext.getContentResolver();
        final WeatherDbHelper helper = new WeatherDbHelper(mContext);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final ContentValues locationIn = Util.makeLocationIn();
        final long locationId = db.insert(LocationEntry.TABLE, null, locationIn);
        locationIn.put(LocationEntry._ID, locationId);
        assertTrue(locationId != -1);
        Log.d(TAG, "testInsertReadProvider(): locationId == " + locationId);
        assertEquals(locationIn,
                Util.makeContentValues(resolver.query(
                                LocationEntry.CONTENT_URI,
                                Util.locationColumns, null, null, null)));
        assertEquals(locationIn,
                Util.makeContentValues(resolver.query(
                                ContentUris.withAppendedId(
                                        LocationEntry.CONTENT_URI, locationId),
                                Util.locationColumns, null, null, null)));
        final ContentValues weatherIn = Util.insertWeather(db, locationId);
        assertEquals(weatherIn,
                Util.makeContentValues(resolver.query(
                                WeatherEntry.CONTENT_URI,
                                Util.weatherColumns, null, null, null)));
        final ContentValues joined = new ContentValues(locationIn);
        joined.putAll(weatherIn);
        assertEquals(joined,
                Util.makeContentValues(resolver.query(
                                WeatherEntry.buildWeatherLocation(Util.WHERE),
                                null, null, null, null)));
        assertEquals(joined,
                Util.makeContentValues(resolver.query(
                                WeatherEntry.buildWeatherLocationDate(
                                        Util.WHERE, Util.WHEN),
                                null, null, null, null)));
        assertEquals(joined,
                Util.makeContentValues(resolver.query(
                                WeatherEntry.buildWeatherLocationQueryDate(
                                        Util.WHERE, Util.WHEN),
                                null, null, null, null)));
        db.close();
        helper.close();
    }

    public TestProvider() {
        super();
        Log.v(TAG, "TestProvider()");
        // mResolver = mContext.getContentResolver(); // WTF: mContext == null
    }
}

// junit.framework.AssertionFailedError: expected:
// <wind=5.5 humidity=1.2 _id=1 pressure=1.3 minimum=65.0 description=Asteroids maximum=75.0 direction=1.1 weather_id=321 date=20140612 location_id=1>
// but was:
// <wind=5.5 pressure=1.3 minimum=65.0 direction=1.1 date=20140612 city=North Pole location_id=1 setting=02138 humidity=1.2 _id=1 maximum=75.0
//           description=Asteroids longitude=-147.355 weather_id=321 latitude=64.772>
