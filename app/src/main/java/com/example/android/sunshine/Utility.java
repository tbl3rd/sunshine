package com.example.android.sunshine;

import java.text.DateFormat;
import java.text.ParsePosition;
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
        LocationEntry.COLUMN_SETTING,
        WeatherEntry.COLUMN_HUMIDITY,
        WeatherEntry.COLUMN_WIND,
        WeatherEntry.COLUMN_DIRECTION,
        WeatherEntry.COLUMN_PRESSURE
    };

    public static final int COLUMN_ID          =  0;
    public static final int COLUMN_DATE        =  1;
    public static final int COLUMN_DESCRIPTION =  2;
    public static final int COLUMN_MAXIMUM     =  3;
    public static final int COLUMN_MINIMUM     =  4;
    public static final int COLUMN_SETTING     =  5;
    public static final int COLUMN_HUMIDITY    =  6;
    public static final int COLUMN_WIND        =  7;
    public static final int COLUMN_DIRECTION   =  8;
    public static final int COLUMN_PRESSURE    =  9;
    public static final int COLUMN_COUNT       = 10;

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

    public static boolean isMetric(Context c) {
        final String metric = c.getString(R.string.preference_units_default);
        final String units = getDefaultSharedPreferences(c)
            .getString(c.getString(R.string.preference_units_key), metric);
        return units.equals(metric);
    }

    public static String temperatureFromCelsius(Context c, double temperature)
    {
        final boolean isMetric = Utility.isMetric(c);
        return c.getString(R.string.format_temperature,
                (isMetric ? temperature
                        :   (32.0 + 1.8 * temperature)),
                (isMetric ? c.getString(R.string.celsius)
                        :   c.getString(R.string.fahrenheit)));
    }

    public static String formatHumidity(Context c, double humidity) {
        Log.v(TAG, "formatHumidity(): humidity == " + humidity);
        return c.getString(R.string.format_humidity,
                c.getString(R.string.humidity), humidity);
    }

    public static String windDirectionFromDegrees(Context c, double degrees)
    {
        final int[] directionId = {
            R.string.direction_north,
            R.string.direction_north_northeast,
            R.string.direction_northeast,
            R.string.direction_east_northeast,
            R.string.direction_east,
            R.string.direction_east_southeast,
            R.string.direction_southeast,
            R.string.direction_south_southeast,
            R.string.direction_south,
            R.string.direction_south_southwest,
            R.string.direction_southwest,
            R.string.direction_west_southwest,
            R.string.direction_west,
            R.string.direction_west_northwest,
            R.string.direction_northwest,
            R.string.direction_north_northwest
        };
        final double compass = 360.0;
        final double sector = compass / directionId.length;
        final double positive = degrees + 3 * compass;
        final double normalized = positive % compass;
        final double d = normalized + sector / 2;
        final int index = (int)Math.floor(d / sector);
        return c.getString(directionId[index]);
    }

    public static String windFromKmH(Context c, double wind, double degrees)
    {
        final boolean isMetric = Utility.isMetric(c);
        return c.getString(R.string.format_wind,
                c.getString(R.string.wind),
                (isMetric ? wind
                        :   (0.621371 * wind)),
                (isMetric ? c.getString(R.string.wind_kmh)
                        :   c.getString(R.string.wind_mph)),
                windDirectionFromDegrees(c, degrees));
    }

    public static String pressureFromHpa(Context c, double pressure) {
        final boolean isMetric = Utility.isMetric(c);
        return c.getString(R.string.format_pressure,
                c.getString(R.string.pressure),
                (isMetric ? pressure
                        :   (0.0293 * pressure)),
                (isMetric ? c.getString(R.string.pressure_hpa)
                        :   c.getString(R.string.pressure_inhg)));
    }

    // Convert a COLUMN_DATE string from and to a java.util.Date.
    //
    public static String dbDate(Date date) {
        return new SimpleDateFormat("yyyyMMdd").format(date);
    }
    public static Date dbDate(String date) {
        return new SimpleDateFormat("yyyyMMdd")
            .parse(date, new ParsePosition(0));
    }

    public static String displayDbDate(String dbDate) {
        return DateFormat.getDateInstance().format(Utility.dbDate(dbDate));
    }

    public static void shortToast(Context c, int stringId) {
        Toast.makeText(c, c.getString(stringId), Toast.LENGTH_SHORT).show();
    }

    // If today, return localized "Today" instead of the day name.
    // If tomorrow, return localized "Tomorrow".
    // Otherwise return the day name.
    //
    public static String dayName(Context context, String dbDate) {
        final Date today = new Date();
        if (Utility.dbDate(today).equals(dbDate)) {
            return context.getString(R.string.today);
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, 1);
        return (Utility.dbDate(calendar.getTime()).equals(dbDate))
            ? context.getString(R.string.tomorrow)
            : new SimpleDateFormat("EEEE").format(Utility.dbDate(dbDate));
    }

    // Return dbDate as "September 11".
    //
    public static String formatMonthDay(String dbDate)
    {
        return new SimpleDateFormat("MMMM dd").format(Utility.dbDate(dbDate));
    }

    // Return a user-friendly representation of dbDate from database.
    //
    // If today, return "Today, September 11".
    // If tomorrow, return "Tomorrow, September 12".
    // If within the week, return "Saturday, September 13".
    // Otherwise, return "Thu Sep 18", and so on.
    //
    public static String friendlyDayDate(Context context, String dbDate) {
        final Date today = new Date();
        if (Utility.dbDate(today).equals(dbDate)) {
            return context.getString(
                    R.string.format_friendly_date,
                    context.getString(R.string.today),
                    formatMonthDay(dbDate));
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, 7);
        if (dbDate.compareTo(Utility.dbDate(calendar.getTime())) < 0) {
            return context.getString(
                    R.string.format_friendly_date,
                    dayName(context, dbDate),
                    formatMonthDay(dbDate));
        } else {
            return new SimpleDateFormat("EEE MMM dd")
                .format(Utility.dbDate(dbDate));
        }
    }
}
