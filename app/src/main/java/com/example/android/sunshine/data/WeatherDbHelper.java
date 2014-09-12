package com.example.android.sunshine.data;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class WeatherDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE = "weather.db";

    private static final int VERSION = 1;

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String INTEGER = " INTEGER NOT NULL, ";
        final String REAL    = " REAL NOT NULL, ";
        final String TEXT    = " TEXT NOT NULL, ";
        final String LOCATION
            = "CREATE TABLE " + LocationEntry.TABLE + " ("
            + LocationEntry._ID                + " INTEGER PRIMARY KEY, "
            + LocationEntry.COLUMN_SETTING     + " TEXT UNIQUE NOT NULL, "
            + LocationEntry.COLUMN_CITY        + TEXT
            + LocationEntry.COLUMN_LATITUDE    + REAL
            + LocationEntry.COLUMN_LONGITUDE   + REAL
            + "UNIQUE ("
            + LocationEntry.COLUMN_SETTING
            + ") ON CONFLICT IGNORE" + ");";
        final String WEATHER
            = "CREATE TABLE " + WeatherEntry.TABLE + " ("
            + WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WeatherEntry.COLUMN_LOCATION_KEY + INTEGER
            + WeatherEntry.COLUMN_DATE         + TEXT
            + WeatherEntry.COLUMN_DESCRIPTION  + TEXT
            + WeatherEntry.COLUMN_WEATHER_ID   + INTEGER
            + WeatherEntry.COLUMN_MINIMUM      + REAL
            + WeatherEntry.COLUMN_MAXIMUM      + REAL
            + WeatherEntry.COLUMN_HUMIDITY     + REAL
            + WeatherEntry.COLUMN_PRESSURE     + REAL
            + WeatherEntry.COLUMN_WIND         + REAL
            + WeatherEntry.COLUMN_DIRECTION    + INTEGER
            + "FOREIGN KEY ("
            + WeatherEntry.COLUMN_LOCATION_KEY
            + ") REFERENCES "
            + LocationEntry.TABLE + " (" + LocationEntry._ID + "), "
            + "UNIQUE ("
            + WeatherEntry.COLUMN_DATE + ", "
            + WeatherEntry.COLUMN_LOCATION_KEY
            + ") ON CONFLICT REPLACE);";
        db.execSQL(LOCATION);
        db.execSQL(WEATHER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String DROP = "DROP TABLE IF EXISTS ";
        db.execSQL(DROP + LocationEntry.TABLE);
        db.execSQL(DROP + WeatherEntry.TABLE);
        onCreate(db);
    }

    public WeatherDbHelper(Context context) {
        super(context, DATABASE, null, VERSION);
    }
}
