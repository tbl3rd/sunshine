package com.example.android.sunshine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
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
        for (String n: FORECAST_COLUMNS) result.put(n, i++);
        if (result.size() != COLUMN_COUNT) {
            throw new RuntimeException("result.size() != COLUMN_COUNT");
        }
        return result;
    }

    public static final HashMap<String, Integer> columnToIndex
        = makeColumnToIndex();

    public static int indexOfColumn(String column) {
        final Integer index = columnToIndex.get(column);
        return (index == null) ? -1 : index;
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
        return String.valueOf(Math.round(isMetric ? t : (32.0 + 1.8 * t)))
            + "\u00b0" + (isMetric ? "C" : "F");
    }

    public static String displayDbDate(String dbDate) {
        return DateFormat.getDateInstance()
            .format(WeatherEntry.dbDate(dbDate));
    }

    public static void makeShortToast(Context c, int stringResId) {
        Toast.makeText(c, c.getString(stringResId), Toast.LENGTH_SHORT).show();
    }

    // If today, return localized "Today" instead of the day name.
    // If tomorrow, return localized "Tomorrow".
    // Otherwise return the day name.
    //
    public static String getDayName(Context context, String dbDate) {
        final Date inputDate = WeatherEntry.dbDate(dbDate);
        final Date today = new Date();
        if (WeatherEntry.dbDate(today).equals(dbDate)) {
            return context.getString(R.string.today);
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, 1);
        return (WeatherEntry.dbDate(calendar.getTime()).equals(dbDate))
            ? context.getString(R.string.tomorrow)
            : new SimpleDateFormat("EEEE").format(inputDate);
    }

    // Return dbDate as "September 11".
    //
    public static String formatMonthDay(Context context, String dbDate)
    {
        return new SimpleDateFormat("MMMM dd")
            .format(WeatherEntry.dbDate(dbDate));
    }

    // Return a user-friendly representation of dbDate from database.
    //
    // If today, return "Today, September 11".
    // If tomorrow, return "Tomorrow, September 12".
    // If within the week, return "Saturday, September 13".
    // Otherwise, return "Thu Sep 18", and so on.
    //
    public static String friendlyDate(Context context, String dbDate) {
        final Date today = new Date();
        if (WeatherEntry.dbDate(today).equals(dbDate)) {
            return context.getString(
                    R.string.format_full_friendly_date,
                    context.getString(R.string.today),
                    formatMonthDay(context, dbDate));
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, 7);
        if (dbDate.compareTo(WeatherEntry.dbDate(calendar.getTime())) < 0) {
            return context.getString(
                    R.string.format_full_friendly_date,
                    getDayName(context, dbDate),
                    formatMonthDay(context, dbDate));
        } else {
            return new SimpleDateFormat("EEE MMM dd")
                .format(WeatherEntry.dbDate(dbDate));
        }
    }
}
