package com.example.android.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FetchWeatherTask {

    private final String TAG = FetchWeatherTask.class.getSimpleName();

    private final ArrayAdapter<String> mForecastAdapter;
    private final Context mContext;

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

    private String[] parseWeather(String jsonString) {
        String[] result = new String[0];
        try {
            final JSONObject forecast = new JSONObject(jsonString);
            final JSONArray list = forecast.getJSONArray("list");
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
            .appendQueryParameter("cnt", "7")
            .appendQueryParameter("q", getLocationPreference())
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

    public void fetch() {
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

    public FetchWeatherTask(Context context, ArrayAdapter<String> forecastAdapter) {
        mContext = context;
        mForecastAdapter = forecastAdapter;
    }
}
