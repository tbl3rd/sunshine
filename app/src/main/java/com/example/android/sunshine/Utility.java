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

    // Format used for storing dates in the database.  ALso used for
    // converting those strings back into date objects for
    // comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the
     * date into something to display to users.  As classy and
     * polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of
     *                the form specified in Utility.DATE_FORMAT
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, String dateStr) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        final Date todayDate = new Date();
        final String todayStr = WeatherContract.getDbDateString(todayDate);
        final Date inputDate = WeatherContract.getDateFromDb(dateStr);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (todayStr.equals(dateStr)) {
            final String today = context.getString(R.string.today);
            return context.getString(
                    R.string.format_full_friendly_date,
                    today,
                    getFormattedMonthDay(context, dateStr));
        } else {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(todayDate);
            cal.add(Calendar.DATE, 7);
            final String weekFutureString
                = WeatherContract.getDbDateString(cal.getTime());

            if (dateStr.compareTo(weekFutureString) < 0) {
                // If the input date is less than a week in the
                // future, just return the day name.
                return getDayName(context, dateStr);
            } else {
                // Otherwise, use the form "Mon Jun 3"
                final SimpleDateFormat shortenedDateFormat
                    = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(inputDate);
            }
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of
     *                the form specified in Utility.DATE_FORMAT
     * @return
     */
    public static String getDayName(Context context, String dateStr) {
        final SimpleDateFormat dbDateFormat
            = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            final Date inputDate = dbDateFormat.parse(dateStr);
            final Date todayDate = new Date();
            // If the date is today, return the localized version of
            // "Today" instead of the actual day name.
            if (WeatherContract.getDbDateString(todayDate).equals(dateStr)) {
                return context.getString(R.string.today);
            } else {
                // If the date is set for tomorrow, the format is "Tomorrow".
                final Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, 1);
                final Date tomorrowDate = cal.getTime();
                if (WeatherContract.getDbDateString(tomorrowDate).equals(
                                dateStr)) {
                    return context.getString(R.string.tomorrow);
                } else {
                    // Otherwise, the format is just the day of the
                    // week (e.g "Wednesday".
                    return new SimpleDateFormat("EEEE").format(inputDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // It couldn't process the date correctly.
            return "";
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of
     *                the form specified in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, String dateStr)
    {
        final SimpleDateFormat dbDateFormat
            = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            final Date inputDate = dbDateFormat.parse(dateStr);
            final SimpleDateFormat monthDayFormat
                = new SimpleDateFormat("MMMM dd");
            final String monthDayString = monthDayFormat.format(inputDate);
            return monthDayString;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns true if metric unit should be used, or false if
     * imperial units should be used.
     */
    public static boolean isMetric(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(context.getString(R.string.pref_units_key),
                    context.getString(R.string.pref_units_metric)).equals(
                            context.getString(R.string.pref_units_metric));
    }
}
