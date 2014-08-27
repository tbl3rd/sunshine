package com.example.android.sunshine.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;


public class WeatherContract {

    public static final String CONTENT_AUTHORITY
        = WeatherContract.class.getPackage().getName();

    public static final Uri BASE_CONTENT_URI
        = new Uri.Builder().scheme("content")
        .authority(CONTENT_AUTHORITY).build();

    public static final String KIND_CURSOR = "vnd.android.cursor";
    public static final String KIND_CURSOR_DIR = KIND_CURSOR + ".dir";
    public static final String KIND_CURSOR_ITEM = KIND_CURSOR + ".item";

    public static final class LocationEntry implements BaseColumns {

        public static final String LOG_TAG
            = LocationEntry.class.getSimpleName();

        public static final String TABLE = "location";
        public static final String COLUMN_SETTING = "setting";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";

        public static final Uri CONTENT_URI
            = BASE_CONTENT_URI.buildUpon().appendPath(TABLE).build();

        public static final String CONTENT_TYPE_DIR
            = KIND_CURSOR_DIR + "/" + CONTENT_AUTHORITY + "/" + TABLE;

        public static final String CONTENT_TYPE_ITEM
            = KIND_CURSOR_ITEM + "/" + CONTENT_AUTHORITY + "/" + TABLE;

        public static Uri buildLocationById(long id) {
            Log.v(LOG_TAG, "CONTENT_URI == " + CONTENT_URI);
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class WeatherEntry implements BaseColumns {

        public static final String LOG_TAG = WeatherEntry.class.getSimpleName();

        public static final String TABLE = "weather";
        public static final String COLUMN_LOCATION_KEY = "location_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_MINIMUM = "minimum";
        public static final String COLUMN_MAXIMUM = "maximum";
        public static final String COLUMN_HUMIDITY = "humidity";
        public static final String COLUMN_PRESSURE = "pressure";
        public static final String COLUMN_WIND = "wind";
        public static final String COLUMN_DIRECTION = "direction";
        public static final String COLUMN_WEATHER_ID = "weather_id";

        public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(TABLE).build();

        public static final String CONTENT_TYPE_DIR =
            KIND_CURSOR_DIR + "/" + CONTENT_AUTHORITY + "/" + TABLE;

        public static final String CONTENT_TYPE_ITEM =
            KIND_CURSOR_ITEM + "/" + CONTENT_AUTHORITY + "/" + TABLE;

        public static Uri buildWeatherById(long id) {
            Log.v(LOG_TAG, "CONTENT_URI == " + CONTENT_URI);
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWeatherLocation(String locationSetting) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithStartDate(
                String locationSetting, String startDate) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting)
                .appendQueryParameter(COLUMN_DATE, startDate).build();
        }

        public static Uri buildWeatherLocationWithDate(
                String locationSetting, String date)
        {
            return CONTENT_URI.buildUpon()
                .appendPath(locationSetting).appendPath(date).build();
        }

        public static String getLocationSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getStartDateFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_DATE);
        }
    }
}
