package com.example.android.sunshine.data;

import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherContract.LocationEntry;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;


public class WeatherProvider extends ContentProvider {

    private static final String TAG = WeatherProvider.class.getSimpleName();

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private static UriMatcher makeUriMatcher() {
        final String auth = WeatherContract.CONTENT_AUTHORITY;
        final String weather = WeatherContract.WeatherEntry.TABLE;
        final String location = WeatherContract.LocationEntry.TABLE;
        final UriMatcher result = new UriMatcher(UriMatcher.NO_MATCH);
        result.addURI(auth, weather,            WEATHER);
        result.addURI(auth, weather   + "/*",   WEATHER_WITH_LOCATION);
        result.addURI(auth, weather   + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);
        result.addURI(auth, location,           LOCATION);
        result.addURI(auth, location  + "/#",   LOCATION_ID);
        return result;
    }

    private static UriMatcher sMatcher = makeUriMatcher();

    private Context mContext;
    private WeatherDbHelper mDbHelper;

    private static SQLiteQueryBuilder makeJoinOnLocationId() {
        final SQLiteQueryBuilder result = new SQLiteQueryBuilder();
        result.setTables(
                WeatherEntry.TABLE + " INNER JOIN " + LocationEntry.TABLE
                + " ON "
                + WeatherEntry.TABLE + "." + WeatherEntry.COLUMN_LOCATION_KEY
                + " = "
                + LocationEntry.TABLE + "." + LocationEntry._ID);
        return result;
    }

    private static SQLiteQueryBuilder sJoinOnLocationId
        = makeJoinOnLocationId();

    private static String getSelector(String path, String query) {
        final int inPath  = (path == null)  ? 0 : 1;
        final int inQuery = (query == null) ? 0 : 2;
        switch (inPath + inQuery) {
        case 0:  return LocationEntry.TABLE + "."
                +       LocationEntry.COLUMN_SETTING + " = ? ";
        case 1:  return LocationEntry.TABLE + "."
                +       LocationEntry.COLUMN_SETTING + " = ? AND "
                +       WeatherEntry.COLUMN_DATE + " = ? ";
        default: return LocationEntry.TABLE + "."
                +       LocationEntry.COLUMN_SETTING + " = ? AND "
                +       WeatherEntry.COLUMN_DATE + " >= ? ";
        }
    }

    private Cursor getWeatherByLocationSetting(
            Uri uri, String[] projection, String sortOrder)
    {
        final String setting = WeatherEntry.getLocationSettingFromUri(uri);
        final String datePath = WeatherEntry.getDateFromUriPath(uri);
        final String dateQuery = WeatherEntry.getDateFromUriQuery(uri);
        final String date = (datePath == null) ? dateQuery : datePath;
        Log.v(TAG, "getWeatherByLocationSetting(): setting == " + setting);
        Log.v(TAG, "getWeatherByLocationSetting(): date == " + date);
        final String selector = getSelector(datePath, dateQuery);
        Log.v(TAG, "getWeatherByLocationSetting(): selector == " + selector);
        final String[] args = (date == null)
            ? (new String[]{ setting })
            : (new String[]{ setting, date });
        return sJoinOnLocationId.query(
                mDbHelper.getReadableDatabase(), projection,
                selector, args, null, null, null, null);
    }

    @Override
    public Cursor query(Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder)
    {
        Cursor result = null;
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        switch (sMatcher.match(uri)) {
        case WEATHER:
            result = db.query(WeatherEntry.TABLE, projection,
                    selection, selectionArgs, null, null, sortOrder);
            break;
        case WEATHER_WITH_LOCATION:
        case WEATHER_WITH_LOCATION_AND_DATE:
            result = getWeatherByLocationSetting(uri, projection, sortOrder);
            break;
        case LOCATION:
            result = db.query(LocationEntry.TABLE, projection,
                    selection, selectionArgs, null, null, sortOrder);
            break;
        case LOCATION_ID: {
            final String select
                = LocationEntry.TABLE + "."
                + LocationEntry._ID + " = ? ";
            final String[] args = { String.valueOf(ContentUris.parseId(uri)) };
            result = db.query(LocationEntry.TABLE, projection,
                    select, args, null, null, sortOrder);
            break;
        }
        default:
            throw new UnsupportedOperationException("URI == " + uri);
        }
        result.setNotificationUri(mContext.getContentResolver(), uri);
        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result = null;
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (sMatcher.match(uri)) {
        case WEATHER: {
            final long id = db.insert(WeatherEntry.TABLE, null, values);
            if (id > 0) {
                result = WeatherEntry.buildWeatherId(id);
            } else {
                throw new android.database.SQLException(
                        WeatherEntry.TABLE + " id == " + id);
            }
            break;
        }
        case LOCATION: {
            final long id = db.insert(LocationEntry.TABLE, null, values);
            if (id > 0) {
                result = LocationEntry.buildLocationId(id);
            } else {
                throw new android.database.SQLException(
                        LocationEntry.TABLE + " id == " + id);
            }
            break;
        }
        default:
            throw new UnsupportedOperationException("URI == " + uri);
        }
        getContext().getContentResolver().notifyChange(result, null);
        return result;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int result = 0;
        switch (sMatcher.match(uri)) {
        case WEATHER:
            try {
                db.beginTransaction();
                for (ContentValues cv : values) {
                    final long id = db.insert(WeatherEntry.TABLE, null, cv);
                    if (id != -1) ++result;
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            mContext.getContentResolver().notifyChange(uri, null);
            break;
        default:
            result += super.bulkInsert(uri, values);
        }
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int result = 0;
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (sMatcher.match(uri)) {
        case WEATHER:
            result += db.delete(WeatherEntry.TABLE, selection, selectionArgs);
            break;
        case LOCATION:
            result += db.delete(LocationEntry.TABLE, selection, selectionArgs);
            break;
        default:
            throw new UnsupportedOperationException("URI == " + uri);
        }
        if (result > 0) mContext.getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public int update(Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs)
    {
        int result = 0;
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Log.v(TAG, "update(): uri == " + uri);
        Log.v(TAG, "update(): values == " + values);
        switch (sMatcher.match(uri)) {
        case WEATHER:
            result += db.update(
                    WeatherEntry.TABLE, values, selection, selectionArgs);
            break;
        case LOCATION:
            result += db.update(
                    LocationEntry.TABLE, values, selection, selectionArgs);
            break;
        default:
            throw new UnsupportedOperationException("URI == " + uri);
        }
        if (result > 0) mContext.getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public String getType(Uri uri) {
        switch (sMatcher.match(uri)) {
        case LOCATION:
            return LocationEntry.CONTENT_TYPE_DIR;
        case LOCATION_ID:
            return LocationEntry.CONTENT_TYPE_ITEM;
        case WEATHER:
            return WeatherEntry.CONTENT_TYPE_DIR;
        case WEATHER_WITH_LOCATION:
            return WeatherEntry.CONTENT_TYPE_DIR;
        case WEATHER_WITH_LOCATION_AND_DATE:
            return WeatherEntry.CONTENT_TYPE_ITEM;
        default:
            throw new UnsupportedOperationException("URI == " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mDbHelper = new WeatherDbHelper(mContext);
        return true;
    }
}
