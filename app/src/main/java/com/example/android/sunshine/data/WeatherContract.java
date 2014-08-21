package com.example.android.sunshine.data;

import android.provider.BaseColumns;


public class WeatherContract {

    public static final class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_LOCATION_SETTING = "location_setting";
        public static final String COLUMN_LOCATION_CITY = "location_city";
        public static final String COLUMN_LOCATION_LATITUDE = "latitude";
        public static final String COLUMN_LOCATION_LONGITUDE = "longitude";
    }

    public static final class WeatherEntry implements BaseColumns {
        public static final String TABLE_NAME = "weather";
        public static final String COLUMN_LOCATION_KEY = "location_id";
        public static final String COLUMN_DATETEXT = "date";
        public static final String COLUMN_WEATHER_ID = "weather_id";
        public static final String COLUMN_SHORT_DESCRIPTION
            = "short_description";
        public static final String COLUMN_MINIMUM_TEMPERATURE = "min";
        public static final String COLUMN_MAXIMUM_TEMPERATURE = "max";
        public static final String COLUMN_HUMIDITY = "humidity";
        public static final String COLUMN_PRESSURE = "pressure";
        public static final String COLUMN_WIND_SPEED = "wind";
        public static final String COLUMN_DEGREES = "degrees";
    }
}
