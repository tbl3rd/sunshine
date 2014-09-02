package com.example.android.sunshine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.android.sunshine.data.WeatherContract.LocationEntry;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;


public class FetchWeatherTask {

    private final String TAG = FetchWeatherTask.class.getSimpleName();

    private final Context mContext;
    private final ArrayAdapter<String> mForecastAdapter;
    private final String locationPreference;

    private String getString(int resourceId) {
        return mContext.getString(resourceId);
    }

    private SharedPreferences sharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    private String getLocationPreference() {
        final String key = getString(R.string.preference_location_key);
        final String or = getString(R.string.preference_location_default);
        return sharedPreferences().getString(key, or);
    }

    private String getUnitsPreference() {
        final String key = getString(R.string.preference_units_key);
        final String or = getString(R.string.preference_units_default);
        return sharedPreferences().getString(key, or);
    }

    private static double celsiusToFahrenheit(double t) {
        return 32.0 + 1.8 * t;
    }

    private static String highlowString(double high, double low) {
        return Math.round(high) + "/" + Math.round(low);
    }

    private String adjustTemperature(final JSONObject temperature)
        throws JSONException
    {
        final String units = getUnitsPreference();
        final double max = temperature.getDouble("max");
        final double min = temperature.getDouble("min");
        final String metric = getString(R.string.preference_units_default);
        if (units == metric) return highlowString(max, min);
        final double high = celsiusToFahrenheit(max);
        final double low = celsiusToFahrenheit(min);
        return highlowString(high, low);
    }

    private static long findLocation(ContentResolver resolver, String setting) {
        final Cursor cursor = resolver.query(
                LocationEntry.CONTENT_URI,
                new String[] { LocationEntry._ID },
                LocationEntry.COLUMN_SETTING + " = ? ",
                new String[] { setting }, null);
        long result = -1;
        if (cursor.moveToFirst()) {
            final int index = cursor.getColumnIndex(LocationEntry._ID);
            if (index >= 0) result = cursor.getLong(index);
        }
        return result;
    }

    private long addLocation(String setting, String city,
            double latitude, double longitude)
    {
        final ContentResolver resolver = mContext.getContentResolver();
        long result = findLocation(resolver, setting);
        if (result < 0) {
            final ContentValues location = new ContentValues();
            location.put(LocationEntry.COLUMN_SETTING, setting);
            location.put(LocationEntry.COLUMN_CITY, city);
            location.put(LocationEntry.COLUMN_LATITUDE, latitude);
            location.put(LocationEntry.COLUMN_LONGITUDE, longitude);
            final Uri uri = resolver.insert(LocationEntry.CONTENT_URI, location);
            Log.v(TAG, "addLocation(): uri == " + uri);
            result = ContentUris.parseId(uri);
        }
        Log.v(TAG, "addLocation(): result == " + result);
        return result;
    }

    private void parseLocation(JSONObject forecast)
        throws JSONException
    {
        final JSONObject city = forecast.getJSONObject("city");
        final JSONObject coord = city.getJSONObject("coord");
        final String name = city.getString("name");
        final double lat = coord.getDouble("lat");
        final double lon = coord.getDouble("lon");
        final long id = addLocation(locationPreference, name, lat, lon);
        Log.v(TAG, "parseLocation(): id == " + id);
    }

    private String[] parseWeather(String jsonString) {
        Log.v(TAG, "parseWeather(): jsonString == " + jsonString);
        String[] result = new String[0];
        try {
            final JSONObject forecast = new JSONObject(jsonString);
            final JSONArray list = forecast.getJSONArray("list");
            parseLocation(forecast);
            result = new String[list.length()];
            for (int i = 0; i < list.length(); ++i) {
                final JSONObject forDay = list.getJSONObject(i);
                final Date date = new Date(forDay.getLong("dt") * 1000);
                final String day
                    = new SimpleDateFormat("E, MMM d").format(date).toString();
                final String description
                    = forDay
                    .getJSONArray("weather")
                    .getJSONObject(0)
                    .getString("main");
                final JSONObject temperature = forDay.getJSONObject("temp");
                final String highLow = adjustTemperature(temperature);
                result[i] = day + " - " + description + " -- " + highLow;
            }
        } catch (final Exception e) {
            Log.e(TAG, "parseWeather() catch", e);
        }
        return result;
    }

    private String getFetchForecastUrl() {
        return new Uri.Builder()
            .scheme("http")
            .authority("api.openweathermap.org")
            .appendPath("data")
            .appendPath("2.5")
            .appendPath("forecast")
            .appendPath("daily")
            .appendQueryParameter("mode", "json")
            .appendQueryParameter("units", "metric")
            .appendQueryParameter("cnt", "14")
            .appendQueryParameter("q", locationPreference)
            .build().toString();
    }

    private String fetchForecast(String url) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            final InputStream is = connection.getInputStream();
            final InputStreamReader isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);
            final StringBuffer buffer = new StringBuffer();
            while (true) {
                final String line = reader.readLine();
                if (line == null) break;
                buffer.append(line).append("\n");
            }
            return buffer.toString();
        } catch (final Exception e) {
            Log.e(TAG, "catch in fetchForecast()", e);
        } finally {
            try {
                if (connection != null) connection.disconnect();
                if (reader != null) reader.close();
            } catch (final Exception e) {
                Log.e(TAG, "finally in fetchForecast()", e);
            }
        }
        return null;
    }

    private void fetch() {
        final String url = getFetchForecastUrl();
        Log.i(TAG, "fetch() url == " + url);
        new AsyncTask<Void, Void, String[]>() {
            protected String[] doInBackground(Void... ignored) {
                Log.v(TAG, "doInBackground()");
                return parseWeather(fetchForecast(url));
            }
            protected void onPostExecute(String[] forecast) {
                Log.v(TAG, "onPostExecute()");
                mForecastAdapter.clear();
                mForecastAdapter.addAll(forecast);
            }
        }.execute();
    }

    private FetchWeatherTask(Context context, ArrayAdapter<String> adapter) {
        Log.v(TAG, "constructor: context == " + context);
        Log.v(TAG, "constructor: adapter == " + adapter);
        mContext = context;
        mForecastAdapter = adapter;
        locationPreference = getLocationPreference();
    }

    static public void fetch(Context context, ArrayAdapter<String> adapter) {
        new FetchWeatherTask(context, adapter).fetch();
    }
}
