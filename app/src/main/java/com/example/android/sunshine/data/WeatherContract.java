package com.example.android.sunshine.data;

import java.util.Date;
import java.util.List;

import com.example.android.sunshine.MainActivity;
import com.example.android.sunshine.Utility;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;


public class WeatherContract {

    private static final String TAG = WeatherContract.class.getSimpleName();

    public static final String CONTENT_AUTHORITY
        = MainActivity.class.getPackage().getName();

    public static final Uri BASE_CONTENT_URI
        = new Uri.Builder().scheme("content")
        .authority(CONTENT_AUTHORITY).build();

    public static final String CURSOR_TYPE = "vnd.android.cursor";
    public static final String CURSOR_TYPE_DIR = CURSOR_TYPE + ".dir";
    public static final String CURSOR_TYPE_ITEM = CURSOR_TYPE + ".item";

    public static final class LocationEntry implements BaseColumns {

        public static final String TAG = LocationEntry.class.getSimpleName();

        public static final String TABLE = "location";
        public static final String COLUMN_SETTING = "setting";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";

        public static final Uri CONTENT_URI
            = BASE_CONTENT_URI.buildUpon().appendPath(TABLE).build();

        public static final String CONTENT_TYPE_DIR
            = CURSOR_TYPE_DIR + "/" + CONTENT_AUTHORITY + "/" + TABLE;

        public static final String CONTENT_TYPE_ITEM
            = CURSOR_TYPE_ITEM + "/" + CONTENT_AUTHORITY + "/" + TABLE;

        public static Uri buildLocationId(long id) {
            Log.v(TAG, "CONTENT_URI == " + CONTENT_URI);
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class WeatherEntry implements BaseColumns {

        public static final String TAG = WeatherEntry.class.getSimpleName();

        public static final String TABLE               = "weather";
        public static final String COLUMN_LOCATION_KEY = "location_id";
        public static final String COLUMN_DATE         = "date";
        public static final String COLUMN_DESCRIPTION  = "description";
        public static final String COLUMN_MINIMUM      = "minimum";
        public static final String COLUMN_MAXIMUM      = "maximum";
        public static final String COLUMN_HUMIDITY     = "humidity";
        public static final String COLUMN_PRESSURE     = "pressure";
        public static final String COLUMN_WIND         = "wind";
        public static final String COLUMN_DIRECTION    = "direction";
        public static final String COLUMN_WEATHER_CODE = "weather_code";

        public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(TABLE).build();

        public static final String CONTENT_TYPE_DIR =
            CURSOR_TYPE_DIR + "/" + CONTENT_AUTHORITY + "/" + TABLE;

        public static final String CONTENT_TYPE_ITEM =
            CURSOR_TYPE_ITEM + "/" + CONTENT_AUTHORITY + "/" + TABLE;

        public static Uri buildWeatherId(long id) {
            Log.v(TAG, "CONTENT_URI == " + CONTENT_URI);
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWeatherLocation(String locationSetting) {
            return CONTENT_URI.buildUpon()
                .appendPath(locationSetting)
                .build();
        }

        public static Uri buildWeatherLocationQueryDate(
                String locationSetting, Date date)
        {
            return CONTENT_URI.buildUpon()
                .appendPath(locationSetting)
                .appendQueryParameter(COLUMN_DATE, Utility.dbDate(date))
                .build();
        }

        public static Uri buildWeatherLocationQueryDate(
                String locationSetting, String date)
        {
            return CONTENT_URI.buildUpon()
                .appendPath(locationSetting)
                .appendQueryParameter(COLUMN_DATE, date)
                .build();
        }

        public static Uri buildWeatherLocationDate(
                String locationSetting, Date date)
        {
            return CONTENT_URI.buildUpon()
                .appendPath(locationSetting)
                .appendPath(Utility.dbDate(date))
                .build();
        }

        public static Uri buildWeatherLocationDate(
                String locationSetting, String date)
        {
            return CONTENT_URI.buildUpon()
                .appendPath(locationSetting)
                .appendPath(date)
                .build();
        }

        public static String getLocationSettingFromUri(Uri uri) {
            Log.v(TAG, "getLocationSettingFromUri(): uri == " + uri);
            final List<String> segments = uri.getPathSegments();
            return (segments.size() < 2) ? null : segments.get(1);
        }

        public static String getDateFromUriPath(Uri uri) {
            Log.v(TAG, "getDateFromUriPath(): uri == " + uri);
            final List<String> segments = uri.getPathSegments();
            return (segments.size() < 3) ? null : segments.get(2);
        }

        public static String getDateFromUriQuery(Uri uri) {
            Log.v(TAG, "getDateFromUriQuery(): uri == " + uri);
            return uri.getQueryParameter(COLUMN_DATE);
        }
    }
}
