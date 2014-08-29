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

    public void assertLocation(ContentValues location, long id,
            ContentResolver resolver)
    {
        assertEquals(location,
                Util.makeContentValues(resolver.query(
                                LocationEntry.CONTENT_URI,
                                Util.locationColumns, null, null, null)));
        assertEquals(location,
                Util.makeContentValues(resolver.query(
                                ContentUris.withAppendedId(
                                        LocationEntry.CONTENT_URI, id),
                                Util.locationColumns, null, null, null)));
    }

    public void assertJoined(ContentValues joined, ContentResolver resolver)
    {
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
    }

    public void testInsertReadProvider() {
        Log.v(TAG, "testInsertReadProvider()");
        final ContentResolver resolver = mContext.getContentResolver();
        final ContentValues locationIn = Util.makeLocationIn();
        final long locationId = ContentUris.parseId(
                resolver.insert(LocationEntry.CONTENT_URI, locationIn));
        locationIn.put(LocationEntry._ID, locationId);
        assertTrue(locationId != -1);
        Log.d(TAG, "testInsertReadProvider(): locationId == " + locationId);
        assertLocation(locationIn, locationId, resolver);
        final ContentValues weatherIn = Util.makeWeatherIn();
        weatherIn.put(WeatherEntry.COLUMN_LOCATION_KEY, locationId);
        final long weatherId = ContentUris.parseId(
                resolver.insert(WeatherEntry.CONTENT_URI, weatherIn));
        Log.d(TAG, "testInsertReadProvider(): weatherId == " + weatherId);
        assertTrue(weatherId != -1);
        weatherIn.put(WeatherEntry._ID, weatherId);
        assertEquals(weatherIn,
                Util.makeContentValues(resolver.query(
                                WeatherEntry.CONTENT_URI,
                                Util.weatherColumns, null, null, null)));
        final ContentValues joined = new ContentValues(locationIn);
        joined.putAll(weatherIn);
        assertJoined(joined, resolver);
    }

    public TestProvider() {
        super();
        Log.v(TAG, "TestProvider()");
        // mResolver = mContext.getContentResolver(); // WTF: mContext == null
    }
}
