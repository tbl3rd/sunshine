package com.example.android.sunshine.data;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class WeatherDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "weather.db";

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String LOCATION
            = "CREATE TABLE " + LocationEntry.TABLE + " ("
            + LocationEntry._ID + " INTEGER PRIMARY KEY, "
            + LocationEntry.COLUMN_SETTING + " TEXT UNIQUE NOT NULL, "
            + LocationEntry.COLUMN_CITY + " TEXT NOT NULL, "
            + LocationEntry.COLUMN_LATITUDE + " TEXT NOT NULL, "
            + LocationEntry.COLUMN_LONGITUDE + " TEXT NOT NULL, "
            + "UNIQUE ("
            + LocationEntry.COLUMN_SETTING
            + ") ON CONFLICT IGNORE" + ");";
        final String WEATHER
            = "CREATE TABLE " + WeatherEntry.TABLE + " ("
            + WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WeatherEntry.COLUMN_LOCATION_KEY + " INTEGER NOT NULL, "
            + WeatherEntry.COLUMN_DATE + " TEXT NOT NULL, "
            + WeatherEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, "
            + WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL, "
            + WeatherEntry.COLUMN_MINIMUM + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_MAXIMUM + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_WIND + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_DIRECTION + " REAL NOT NULL, "
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
        final String LOCATION = "DROP TABLE IF EXISTS " + LocationEntry.TABLE;
        final String WEATHER = "DROP TABLE IF EXISTS " + WeatherEntry.TABLE;
        db.execSQL(LOCATION);
        db.execSQL(WEATHER);
        onCreate(db);
    }

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
