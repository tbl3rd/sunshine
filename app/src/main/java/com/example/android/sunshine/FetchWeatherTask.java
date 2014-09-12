package com.example.android.sunshine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;


public class FetchWeatherTask {

    private final String TAG = FetchWeatherTask.class.getSimpleName();

    private final Context mContext;

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

    private long addLocation(ContentResolver resolver,
            String setting, String city, double latitude, double longitude)
    {
        long result = findLocation(resolver, setting);
        if (result < 0) {
            final ContentValues location = new ContentValues();
            location.put(LocationEntry.COLUMN_SETTING, setting);
            location.put(LocationEntry.COLUMN_CITY, city);
            location.put(LocationEntry.COLUMN_LATITUDE, latitude);
            location.put(LocationEntry.COLUMN_LONGITUDE, longitude);
            final Uri uri
                = resolver.insert(LocationEntry.CONTENT_URI, location);
            Log.v(TAG, "addLocation(): uri == " + uri);
            result = ContentUris.parseId(uri);
        }
        Log.v(TAG, "addLocation(): result == " + result);
        return result;
    }

    private long parseLocation(
            ContentResolver resolver, String setting, JSONObject forecast)
        throws JSONException
    {
        final JSONObject city = forecast.getJSONObject("city");
        final JSONObject coord = city.getJSONObject("coord");
        final String name = city.getString("name");
        final double lat = coord.getDouble("lat");
        final double lon = coord.getDouble("lon");
        final long result = addLocation(resolver, setting, name, lat, lon);
        Log.v(TAG, "parseLocation(): result == " + result);
        return result;
    }

    private ContentValues makeWeather(long locationId, JSONObject day)
        throws JSONException
    {
        final ContentValues result = new ContentValues();
        result.put(WeatherEntry.COLUMN_LOCATION_KEY, locationId);
        result.put(WeatherEntry.COLUMN_DATE, Utility.dbDate(
                        new Date(day.getLong("dt") * 1000)));
        final JSONObject weather
            = day.getJSONArray("weather").getJSONObject(0);
        result.put(WeatherEntry.COLUMN_DESCRIPTION,
                weather.getString("description"));
        final JSONObject temperature = day.getJSONObject("temp");
        result.put(WeatherEntry.COLUMN_MAXIMUM, temperature.getDouble("max"));
        result.put(WeatherEntry.COLUMN_MINIMUM, temperature.getDouble("min"));
        result.put(WeatherEntry.COLUMN_HUMIDITY, day.getDouble("humidity"));
        result.put(WeatherEntry.COLUMN_PRESSURE, day.getDouble("pressure"));
        result.put(WeatherEntry.COLUMN_WIND, day.getDouble("speed"));
        result.put(WeatherEntry.COLUMN_DIRECTION, day.getInt("deg"));
        result.put(WeatherEntry.COLUMN_WEATHER_ID, weather.getString("id"));
        return result;
    }

    private void parseWeather(String setting, String json) {
        final ContentResolver resolver = mContext.getContentResolver();
        Log.v(TAG, "parseWeather(): json == " + json);
        try {
            final JSONObject forecast = new JSONObject(json);
            final long locationId = parseLocation(resolver, setting, forecast);
            final JSONArray list = forecast.getJSONArray("list");
            final int length = list.length();
            final ContentValues[] w = new ContentValues[length];
            for (int i = 0; i < length; ++i) {
                final JSONObject day = list.getJSONObject(i);
                w[i] = makeWeather(locationId, day);
            }
            final int count = resolver.bulkInsert(WeatherEntry.CONTENT_URI, w);
            Log.v(TAG, "parseWeather(): count == " + count);
        } catch (final Exception e) {
            Log.e(TAG, "parseWeather() catch", e);
        }
    }

    private String getFetchForecastUrl(String location) {
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
            .appendQueryParameter("q", location)
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
            final StringBuilder builder = new StringBuilder();
            while (true) {
                final String line = reader.readLine();
                if (line == null) break;
                builder.append(line).append("\n");
            }
            return builder.toString();
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
        final String location = Utility.getPreferredLocation(mContext);
        final String url = getFetchForecastUrl(location);
        Log.i(TAG, "fetch() url == " + url);
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... ignored) {
                Log.v(TAG, "doInBackground()");
                parseWeather(location, fetchForecast(url));
                return null;
            }
        }.execute();
    }

    private FetchWeatherTask(Context context) {
        Log.v(TAG, "constructor: context == " + context);
        mContext = context;
    }

    static public void fetch(Context context) {
        new FetchWeatherTask(context).fetch();
    }
}
