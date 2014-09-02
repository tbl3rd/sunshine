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

    private long addLocation(String setting, String city,
            double latitude, double longitude)
    {
        final ContentResolver resolver = mContext.getContentResolver();
        final ContentValues location = new ContentValues();
        location.put(LocationEntry.COLUMN_SETTING, setting);
        location.put(LocationEntry.COLUMN_CITY, city);
        location.put(LocationEntry.COLUMN_LATITUDE, latitude);
        location.put(LocationEntry.COLUMN_LONGITUDE, longitude);
        Log.v(TAG, "addLocation(): location == " + location);
        try {
            final Uri uri = resolver.insert(LocationEntry.CONTENT_URI, location);
            Log.v(TAG, "addLocation(insert): uri == " + uri);
            return ContentUris.parseId(uri);
        } catch (final SQLException sqle) {
            Log.v(TAG, "addLocation(update): caught " + sqle);
            final int count = resolver.update(
                    LocationEntry.CONTENT_URI, location, null, null);
            Log.v(TAG, "addLocation(update): count == " + count);
            return count;
        }
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

// {
//     "city": {
//         "coord": {
//             "lat": 42.3792,
//             "lon": -71.1326
//         },
//         "country": "United States of America",
//         "id": "4931972",
//         "name": "Cambridge",
//         "population": 0
//     },
//     "cnt": 7,
//     "cod": "200",
//     "list": [
//         {
//             "clouds": 36,
//             "deg": 204,
//             "dt": 1409673600,
//             "humidity": 66,
//             "pressure": 1021.47,
//             "rain": 1,
//             "speed": 3.17,
//             "temp": {
//                 "day": 31.1,
//                 "eve": 30.48,
//                 "max": 31.1,
//                 "min": 27.1,
//                 "morn": 31.1,
//                 "night": 27.1
//             },
//             "weather": [
//                 {
//                     "description": "light rain",
//                     "icon": "10d",
//                     "id": 500,
//                     "main": "Rain"
//                 }
//             ]
//         },
//         {
//             "clouds": 0,
//             "deg": 335,
//             "dt": 1409760000,
//             "humidity": 67,
//             "pressure": 1023.9,
//             "speed": 3.5,
//             "temp": {
//                 "day": 26.2,
//                 "eve": 27.91,
//                 "max": 27.91,
//                 "min": 22.38,
//                 "morn": 25.69,
//                 "night": 22.38
//             },
//             "weather": [
//                 {
//                     "description": "sky is clear",
//                     "icon": "01d",
//                     "id": 800,
//                     "main": "Clear"
//                 }
//             ]
//         },
//         {
//             "clouds": 0,
//             "deg": 319,
//             "dt": 1409846400,
//             "humidity": 61,
//             "pressure": 1032.8,
//             "speed": 0.61,
//             "temp": {
//                 "day": 25.56,
//                 "eve": 27.1,
//                 "max": 27.1,
//                 "min": 18.99,
//                 "morn": 18.99,
//                 "night": 22.61
//             },
//             "weather": [
//                 {
//                     "description": "sky is clear",
//                     "icon": "01d",
//                     "id": 800,
//                     "main": "Clear"
//                 }
//             ]
//         },
//         {
//             "clouds": 0,
//             "deg": 217,
//             "dt": 1409932800,
//             "humidity": 0,
//             "pressure": 1024.82,
//             "speed": 3.8,
//             "temp": {
//                 "day": 26.97,
//                 "eve": 25.58,
//                 "max": 26.97,
//                 "min": 20.93,
//                 "morn": 20.93,
//                 "night": 24.32
//             },
//             "weather": [
//                 {
//                     "description": "sky is clear",
//                     "icon": "01d",
//                     "id": 800,
//                     "main": "Clear"
//                 }
//             ]
//         },
//         {
//             "clouds": 0,
//             "deg": 217,
//             "dt": 1410019200,
//             "humidity": 0,
//             "pressure": 1017.94,
//             "speed": 3.13,
//             "temp": {
//                 "day": 29.06,
//                 "eve": 26.88,
//                 "max": 29.06,
//                 "min": 24.39,
//                 "morn": 24.39,
//                 "night": 24.67
//             },
//             "weather": [
//                 {
//                     "description": "sky is clear",
//                     "icon": "01d",
//                     "id": 800,
//                     "main": "Clear"
//                 }
//             ]
//         },
//         {
//             "clouds": 22,
//             "deg": 203,
//             "dt": 1410105600,
//             "humidity": 0,
//             "pressure": 1014.13,
//             "rain": 25.74,
//             "speed": 5.14,
//             "temp": {
//                 "day": 27.57,
//                 "eve": 22.27,
//                 "max": 27.57,
//                 "min": 20.85,
//                 "morn": 24.45,
//                 "night": 20.85
//             },
//             "weather": [
//                 {
//                     "description": "heavy intensity rain",
//                     "icon": "10d",
//                     "id": 502,
//                     "main": "Rain"
//                 }
//             ]
//         },
//         {
//             "clouds": 54,
//             "deg": 27,
//             "dt": 1410192000,
//             "humidity": 0,
//             "pressure": 1024.19,
//             "rain": 0.53,
//             "speed": 4.33,
//             "temp": {
//                 "day": 19.04,
//                 "eve": 17.1,
//                 "max": 19.04,
//                 "min": 16.17,
//                 "morn": 17.05,
//                 "night": 16.17
//             },
//             "weather": [
//                 {
//                     "description": "light rain",
//                     "icon": "10d",
//                     "id": 500,
//                     "main": "Rain"
//                 }
//             ]
//         }
//     ],
//     "message": 0.1056
// }
