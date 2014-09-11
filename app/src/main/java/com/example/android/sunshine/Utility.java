package com.example.android.sunshine;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherContract;

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

    /**
     * Helper method to convert the database representation of the
     * date into something to display to users.  As classy and
     * polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dbDate The db formatted date string
     * @return a user-friendly representation of the date.
     */
    //
    // The day string for forecast uses the following logic:
    // For today: "Today, June 8"
    // For tomorrow:  "Tomorrow"
    // For the next 5 days: "Wednesday" (just the day name)
    // For all days after that: "Mon Jun 8"
    //
    public static String getFriendlyDayString(Context context, String dbDate) {
        final Date today = new Date();
        if (WeatherEntry.dbDate(today).equals(dbDate)) {
            return context.getString(
                    R.string.format_full_friendly_date,
                    context.getString(R.string.today),
                    getFormattedMonthDay(context, dbDate));
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DATE, 7);
        if (dbDate.compareTo(WeatherEntry.dbDate(cal.getTime())) < 0) {
            // If the input date is less than a week in the
            // future, just return the day name.
            return getDayName(context, dbDate);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            return new SimpleDateFormat("EEE MMM dd")
                .format(WeatherEntry.dbDate(dbDate));
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dbDate The db formatted date string
     * @return
     */
    public static String getDayName(Context context, String dbDate) {
        final Date inputDate = WeatherEntry.dbDate(dbDate);
        final Date today = new Date();
        // If the date is today, return the localized version of
        // "Today" instead of the actual day name.
        if (WeatherEntry.dbDate(today).equals(dbDate)) {
            return context.getString(R.string.today);
        }
        // If the date is set for tomorrow, the format is "Tomorrow".
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, 1);
        return (WeatherEntry.dbDate(calendar.getTime()).equals(dbDate))
            ? context.getString(R.string.tomorrow)
            : new SimpleDateFormat("EEEE").format(inputDate);
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     *
     * @param context Context to use for resource localization
     * @param dbDate The db formatted date string
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, String dbDate)
    {
        return new SimpleDateFormat("MMMM dd")
            .format(WeatherEntry.dbDate(dbDate));
    }
}
