package com.example.android.sunshine.data;

import android.provider.BaseColumns;


public class WeatherContract {

    public static final class LocationEntry implements BaseColumns {
        public static final String TABLE = "location";
        public static final String COLUMN_SETTING = "setting";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
    }

    public static final class WeatherEntry implements BaseColumns {
        public static final String TABLE = "weather";
        public static final String COLUMN_LOCATION_KEY = "location_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_WEATHER_ID = "weather_id";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_MINIMUM = "minimum";
        public static final String COLUMN_MAXIMUM = "maximum";
        public static final String COLUMN_HUMIDITY = "humidity";
        public static final String COLUMN_PRESSURE = "pressure";
        public static final String COLUMN_WIND = "wind";
        public static final String COLUMN_DIRECTION = "direction";
    }
}
