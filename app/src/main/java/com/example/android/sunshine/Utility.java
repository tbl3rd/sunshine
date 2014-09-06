package com.example.android.sunshine;

import java.text.DateFormat;
import java.util.HashMap;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class Utility
{
    private static final String TAG = Utility.class.getSimpleName();

    public static final String[] FORECAST_COLUMNS = {
        WeatherEntry.TABLE + "." + WeatherEntry._ID,
        WeatherEntry.COLUMN_DATE,
        WeatherEntry.COLUMN_DESCRIPTION,
        WeatherEntry.COLUMN_MAXIMUM,
        WeatherEntry.COLUMN_MINIMUM,
        LocationEntry.COLUMN_SETTING
    };

    public static final int COLUMN_ID          = 0;
    public static final int COLUMN_DATE        = 1;
    public static final int COLUMN_DESCRIPTION = 2;
    public static final int COLUMN_MAXIMUM     = 3;
    public static final int COLUMN_MINIMUM     = 4;
    public static final int COLUMN_SETTING     = 5;
    public static final int COLUMN_COUNT       = 6;

    static HashMap<String, Integer> makeColumnToIndex() {
        Log.v(TAG, "makeColumnToIndex()");
        if (COLUMN_COUNT != FORECAST_COLUMNS.length) {
            throw new RuntimeException(
                    "COLUMN_COUNT != FORECAST_COLUMNS.length");
        }
        int i = 0;
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        for (String n: FORECAST_COLUMNS) result.put(n, Integer.valueOf(i++));
        if (result.size() != COLUMN_COUNT) {
            throw new RuntimeException("result.size() != COLUMN_COUNT");
        }
        return result;
    }

    public static final HashMap<String, Integer> columnToIndex
        = makeColumnToIndex();

    public static int indexOfColumn(String column) {
        final Integer index = columnToIndex.get(column);
        return (index == null) ? -1 : index.intValue();
    }

    public static SharedPreferences getDefaultSharedPreferences(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }

    public static String getPreferredLocation(Context c) {
        return getDefaultSharedPreferences(c).getString(
                c.getString(R.string.preference_location_key),
                c.getString(R.string.preference_location_default));
    }

    public static String fromCelsius(Context c, double t) {
        final String metric = c.getString(R.string.preference_units_default);
        final String units = getDefaultSharedPreferences(c)
            .getString(c.getString(R.string.preference_units_key), metric);
        final boolean isMetric = units.equals(metric);
        return String.valueOf(Math.round(isMetric ? t : (32.0 + 1.8 * t)));
    }

    public static String displayDbDate(String dbDate) {
        return DateFormat.getDateInstance()
            .format(WeatherEntry.dbDate(dbDate));
    }

    public static void makeShortToast(Context c, int stringResId) {
        Toast.makeText(c, c.getString(stringResId), Toast.LENGTH_SHORT).show();
    }

    public static SimpleCursorAdapter makeSimpleCursorAdapter(Activity a) {
        return new SimpleCursorAdapter(
                a, R.layout.list_item_forecast, null,
                new String[] {
                    WeatherEntry.COLUMN_DATE,
                    WeatherEntry.COLUMN_DESCRIPTION,
                    WeatherEntry.COLUMN_MAXIMUM,
                    WeatherEntry.COLUMN_MINIMUM
                },
                new int[] {
                    R.id.list_item_date_textview,
                    R.id.list_item_forecast_textview,
                    R.id.list_item_high_textview,
                    R.id.list_item_low_textview
                },
                0);
    }

    public static SimpleCursorAdapter.ViewBinder makeWeatherBinder(
            final Activity a)
    {
        return new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View v, Cursor c, int n)
            {
                final TextView tv = (TextView)v;
                switch (n) {
                case Utility.COLUMN_MAXIMUM:
                case Utility.COLUMN_MINIMUM:
                    tv.setText(Utility.fromCelsius(a, c.getDouble(n)));
                    return true;
                case Utility.COLUMN_DATE:
                    tv.setText(Utility.displayDbDate(c.getString(n)));
                    return true;
                }
                return false;
            }
        };
    }

}

// [6:27pm] dnolen: tangrammer: w/ iOS you'll have better luck with the JavaScriptCore bridge and ClojureScript
// [6:27pm] dnolen: tangrammer: I've tried it works great and I know other people are experimenting with it as well
// [6:28pm] dnolen: tangrammer: it does limit you to iOS 7, but you could take the Ejecta approach for earlier OSs
// [6:38pm] dnolen: tangrammer: core.async on iOS works great
// [6:38pm] dnolen: tangrammer: you can also do multithreaded CLJS on iOS
