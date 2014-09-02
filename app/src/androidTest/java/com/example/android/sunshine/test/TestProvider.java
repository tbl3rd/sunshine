package com.example.android.sunshine.test;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherDbHelper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;


public class TestProvider extends AndroidTestCase {

    final static String TAG = TestProvider.class.getSimpleName();

    public void testDeleteAllRecords() {
        Log.v(TAG, "testDeleteAllRecords()");
        final ContentResolver resolver = mContext.getContentResolver();
        final int weatherCount
            = resolver.delete(WeatherEntry.CONTENT_URI, null, null);
        Log.v(TAG, "testDeleteAllRecords(): weatherCount == " + weatherCount);
        final int locationCount
            = resolver.delete(LocationEntry.CONTENT_URI, null, null);
        Log.v(TAG, "testDeleteAllRecords(): locationCount == " + locationCount);
        final Cursor weatherCursor = resolver.query(
                WeatherEntry.CONTENT_URI, null, null, null,null
        );
        assertEquals(weatherCursor.getCount(), 0);
        final Cursor locationCursor = resolver.query(
                LocationEntry.CONTENT_URI, null, null, null,null
        );
        assertEquals(locationCursor.getCount(), 0);
        Log.v(TAG, "TestProvider.testDeleteAllRecords()");
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
        joined.remove(LocationEntry._ID);
        joined.remove(WeatherEntry._ID);
        final ContentValues where = Util.makeContentValues(
                resolver.query(
                        WeatherEntry.buildWeatherLocation(Util.WHERE),
                        null, null, null, null));
        where.remove(WeatherEntry._ID);
        assertEquals(joined, where);
        final ContentValues whereWhen =
            Util.makeContentValues(resolver.query(
                            WeatherEntry.buildWeatherLocationDate(
                                    Util.WHERE, Util.WHEN),
                            null, null, null, null));
        whereWhen.remove(WeatherEntry._ID);
        assertEquals(joined, whereWhen);
        final ContentValues whereWhenQuery =
            Util.makeContentValues(resolver.query(
                            WeatherEntry.buildWeatherLocationQueryDate(
                                    Util.WHERE, Util.WHEN),
                            null, null, null, null));
        whereWhenQuery.remove(WeatherEntry._ID);
        assertEquals(joined, whereWhenQuery);
    }

    public void testInsertReadProvider() {
        Log.v(TAG, "testInsertReadProvider()");
        final ContentResolver resolver = mContext.getContentResolver();
        final ContentValues locationIn = Util.makeLocationIn();
        final long locationId = ContentUris.parseId(
                resolver.insert(LocationEntry.CONTENT_URI, locationIn));
        locationIn.put(LocationEntry._ID, locationId);
        Log.v(TAG, "testInsertReadProvider(): locationIn == " + locationIn);
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
        Log.v(TAG, "testInsertReadProvider(): weatherIn == " + weatherIn);
        assertEquals(weatherIn,
                Util.makeContentValues(resolver.query(
                                WeatherEntry.CONTENT_URI,
                                Util.weatherColumns, null, null, null)));
        final ContentValues joined = new ContentValues(locationIn);
        joined.putAll(weatherIn);
        assertJoined(joined, resolver);
    }

    public void testUpdateProvider() {
        Log.v(TAG, "testUpdateProvider()");
        testDeleteAllRecords();
        final ContentResolver resolver = mContext.getContentResolver();
        final ContentValues location = Util.makeLocationIn();
        Log.v(TAG, "testUpdateProvider(): location == " + location);
        final long id = ContentUris.parseId(
                resolver.insert(LocationEntry.CONTENT_URI, location));
        location.put(LocationEntry._ID, id);
        Log.d(TAG, "testUpdateProvider(): id == " + id);
        assertTrue(id != -1);
        assertLocation(location, id, resolver);
        location.put(LocationEntry.COLUMN_CITY, "OpinionVille");
        Log.v(TAG, "testUpdateProvider(): location == " + location);
        final int count = resolver.update(
                LocationEntry.CONTENT_URI, location, null, null);
        Log.d(TAG, "testUpdateProvider(): count == " + count);
        assertEquals(count, 1);
        assertEquals(location, Util.makeContentValues(resolver.query(
                                ContentUris.withAppendedId(
                                        LocationEntry.CONTENT_URI, id),
                                Util.locationColumns, null, null, null)));
    }

    public TestProvider() {
        super();
        Log.v(TAG, "TestProvider()");
        // mResolver = mContext.getContentResolver(); // WTF: mContext == null
    }
}
