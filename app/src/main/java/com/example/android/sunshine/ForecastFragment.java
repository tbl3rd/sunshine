package com.example.android.sunshine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class ForecastFragment extends Fragment {

    private static final String LOG_TAG
        = ForecastFragment.class.getSimpleName();

    ArrayAdapter<String> mForecastAdapter = null;

    public ForecastFragment() {}

    // Set up and return adapter for the forecast in rootView.
    //
    private ArrayAdapter<String> makeForecastAdapter(View rootView)
    {
        final ArrayAdapter<String> result = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());
        final ListView lv =
            (ListView)rootView.findViewById(R.id.listview_forecast);
        final AdapterView.OnItemClickListener ocl
            = new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> adapter,
                            View view, int n, long ignoredId) {
                        startActivity(
                                new Intent(getActivity(), DetailActivity.class)
                                .putExtra(
                                        Intent.EXTRA_TEXT,
                                        mForecastAdapter.getItem(n)));
                    }
                };
        lv.setAdapter(result);
        lv.setOnItemClickListener(ocl);
        return result;
    }

    private static String fetchForecast(String url) {
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
            Log.e(LOG_TAG, "catch in fetchForecast()", e);
        } finally {
            try {
                if (connection != null) connection.disconnect();
                if (reader != null) reader.close();
            } catch (final Exception e) {
                Log.e(LOG_TAG, "finally in fetchForecast()", e);
            }
        }
        return null;
    }

    private static double celsiusToFahrenheit(final double t) {
        return 32.0 + 1.8 * t;
    }

    private static String hiloToString(final double high, final double low) {
        return Math.round(high) + "/" + Math.round(low);
    }

    private String adjustTemperature(final JSONObject temperature)
        throws JSONException
    {
        final String metric
            = getResources().getString(R.string.preference_metric_setting);
        final String units =
            PreferenceManager.getDefaultSharedPreferences(getActivity())
            .getString("units", metric);
        final double max = temperature.getDouble("max");
        final double min = temperature.getDouble("min");
        if (units == metric) return hiloToString(max, min);
        final double high = celsiusToFahrenheit(max);
        final double low = celsiusToFahrenheit(min);
        return hiloToString(high, low);
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
            Log.e(LOG_TAG, "parseWeather() catch", e);
        }
        return result;
    }

    private String getFetchForecastUrl() {
        final String defaultLocation
            = getResources().getString(R.string.preference_location_default);
        final String location =
            PreferenceManager.getDefaultSharedPreferences(getActivity())
            .getString("location", defaultLocation);
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
            .appendQueryParameter("q", location)
            .build().toString();
    }

    private void asyncFetchForecast() {
        final String url = getFetchForecastUrl();
        Log.i(LOG_TAG, "asyncFetchForecast() url == " + url);
        new AsyncTask<Void, Void, String[]>() {
            protected String[] doInBackground(Void... ignored) {
                return parseWeather(fetchForecast(url));
            }
            protected void onPostExecute(String[] forecast) {
                Log.v(LOG_TAG, "onPostExecute()");
                for (String s: forecast) Log.v(LOG_TAG, s);
                mForecastAdapter.clear();
                mForecastAdapter.addAll(forecast);
            }
        }.execute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        final View result
            = inflater.inflate(R.layout.fragment_main, container, false);
        mForecastAdapter = makeForecastAdapter(result);
        return result;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        asyncFetchForecast();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(LOG_TAG, "onCreateOptionsMenu()");
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(LOG_TAG, "onOptionsItemSelected()");
        switch (item.getItemId()) {
        case R.id.action_refresh:
            asyncFetchForecast();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
