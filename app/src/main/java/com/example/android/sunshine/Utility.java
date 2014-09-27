package com.example.android.sunshine;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


public class Utility
{
    private static final String TAG = Utility.class.getSimpleName();

    public static final String[] FORECAST_COLUMNS = {
        WeatherEntry.TABLE + "." + WeatherEntry._ID,
        LocationEntry.COLUMN_SETTING,
        WeatherEntry.COLUMN_DATE,
        WeatherEntry.COLUMN_DESCRIPTION,
        WeatherEntry.COLUMN_DIRECTION,
        WeatherEntry.COLUMN_HUMIDITY,
        WeatherEntry.COLUMN_MAXIMUM,
        WeatherEntry.COLUMN_MINIMUM,
        WeatherEntry.COLUMN_PRESSURE,
        WeatherEntry.COLUMN_WEATHER_CODE,
        WeatherEntry.COLUMN_WIND
    };

    public static final int COLUMN_ID           =  0;
    public static final int COLUMN_SETTING      =  1;
    public static final int COLUMN_DATE         =  2;
    public static final int COLUMN_DESCRIPTION  =  3;
    public static final int COLUMN_DIRECTION    =  4;
    public static final int COLUMN_HUMIDITY     =  5;
    public static final int COLUMN_MAXIMUM      =  6;
    public static final int COLUMN_MINIMUM      =  7;
    public static final int COLUMN_PRESSURE     =  8;
    public static final int COLUMN_WEATHER_CODE =  9;
    public static final int COLUMN_WIND         = 10;
    public static final int COLUMN_COUNT        = 11;

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

    public static long getLastNotification(Context c) {
        return getDefaultSharedPreferences(c).getLong(
                c.getString(R.string.pref_last_notification), 0);
    }

    public static String formatCelsius(Context c,
            boolean isMetric, double temperature)
    {
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

    public static int computeWindDirectionId(int[] directionIds, int degrees)
    {
        final int whatever = 3;
        final double compass = 360.0;
        final double sector = compass / directionIds.length;
        final double positive = degrees + sector / 2 + compass * whatever;
        final double normalized = positive % compass;
        final double d = normalized / sector;
        return (directionIds[(int)Math.floor(d)]);
    }

    public static String sayWindDirectionFromDegrees(Context c, int degrees)
    {
        final int[] directionIds = {
            R.string.say_north,
            R.string.say_north_northeast,
            R.string.say_northeast,
            R.string.say_east_northeast,
            R.string.say_east,
            R.string.say_east_southeast,
            R.string.say_southeast,
            R.string.say_south_southeast,
            R.string.say_south,
            R.string.say_south_southwest,
            R.string.say_southwest,
            R.string.say_west_southwest,
            R.string.say_west,
            R.string.say_west_northwest,
            R.string.say_northwest,
            R.string.say_north_northwest
        };
        final int id = computeWindDirectionId(directionIds, degrees);
        return c.getString(R.string.say_wind_format, id);
    }

    public static String windDirectionFromDegrees(Context c, int degrees)
    {
        final int[] directionIds = {
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
        return c.getString(computeWindDirectionId(directionIds, degrees));
    }

    public static String formatWind(Context c, boolean isMetric,
            double wind, int degrees)
    {
        return c.getString(R.string.format_wind,
                c.getString(R.string.wind),
                (isMetric ? wind
                        :   (0.621371 * wind)),
                (isMetric ? c.getString(R.string.wind_kmh)
                        :   c.getString(R.string.wind_mph)),
                windDirectionFromDegrees(c, degrees));
    }

    public static String formatPressure(Context c,
            boolean isMetric, double pressure)
    {
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

    public static void shortToast(Context c, String s) {
        Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
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
    // If today, return "Today".
    // If tomorrow, return "Tomorrow".
    // If within the week, return "Saturday".
    // Otherwise, return "Thu Sep 18", and so on.
    //
    public static String friendlyDayDate(Context context, String dbDate) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 7);
        return (dbDate.compareTo(Utility.dbDate(calendar.getTime())) < 0)
            ? dayName(context, dbDate)
            : (new SimpleDateFormat("EEE MMM dd")
                    .format(Utility.dbDate(dbDate)));
    }

    public static void showMap(Activity a) {
        final String location = Utility.getPreferredLocation(a);
        final Uri geo = new Uri.Builder()
            .scheme("geo")
            .appendPath("0,0")
            .appendQueryParameter("q", location)
            .build();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geo);
        if (intent.resolveActivity(a.getPackageManager()) == null) {
            Utility.shortToast(a, R.string.action_map_none);
        } else {
            a.startActivity(intent);
        }
    }

    // See: http://openweathermap.org/weather-conditions
    //
    static int weatherCodeToIndex(int code) {
        final int hundred = code - (code % 100);
        if (hundred == 200) return 1;  // storm
        if (hundred == 300) return 2;  // light_rain
        if (code    == 500) return 2;  // light_rain
        if (hundred == 500) return 3;  // rain
        if (hundred == 600) return 4;  // snow
        if (hundred == 700) return 5;  // fog
        if (code    == 800) return 6;  // clear
        if (code    == 801) return 7;  // light_clouds
        if (code    == 802) return 7;  // light_clouds
        if (hundred == 800) return 8;  // cloudy
        /* otherwise */     return 0;  // light_clouds
    }

    public static int weatherIcon(int code) {
        final int drawables[] = {
            R.drawable.ic_light_clouds,
            R.drawable.ic_storm,
            R.drawable.ic_light_rain,
            R.drawable.ic_rain,
            R.drawable.ic_snow,
            R.drawable.ic_fog,
            R.drawable.ic_clear,
            R.drawable.ic_light_clouds,
            R.drawable.ic_cloudy
        };
        return drawables[weatherCodeToIndex(code)];
    }

    public static int weatherArt(int code) {
        final int drawables[] = {
            R.drawable.art_light_clouds,
            R.drawable.art_storm,
            R.drawable.art_light_rain,
            R.drawable.art_rain,
            R.drawable.art_snow,
            R.drawable.art_fog,
            R.drawable.art_clear,
            R.drawable.art_light_clouds,
            R.drawable.art_cloudy
        };
        return drawables[weatherCodeToIndex(code)];
    }
}
