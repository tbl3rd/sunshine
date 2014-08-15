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

    private static final String[] dummyForecast = {
        "Today - Sunny -- 88/63",
        "Tomorrow - Foggy -- 70/40",
        "Weds - Cloudy -- 72/36",
        "Thurs - Asteroids -- 75/65",
        "Fri - Heavy Rain -- 65/56",
        "Sat - HELP TRAPPED IN WEATHERSTATION -- 60/51",
        "Sun - Sunny -- 80/68"
    };

    ArrayAdapter<String> mForecastAdapter = null;

    // Set up and return adapter for the forecast in rootView.
    //
    private ArrayAdapter<String> makeForecastAdapter(View rootView)
    {
        final ArrayList<String> forecast
            = new ArrayList<String>(Arrays.asList(dummyForecast));
        final ArrayAdapter<String> result = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                forecast);
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

    private static String makeUrl(final String postcode, int days) {
        return new Uri.Builder()
            .scheme("http")
            .authority("api.openweathermap.org")
            .appendPath("data")
            .appendPath("2.5")
            .appendPath("forecast")
            .appendPath("daily")
            .appendQueryParameter("mode", "json")
            .appendQueryParameter("units", "metric")
            .appendQueryParameter("cnt", "" + days)
            .appendQueryParameter("q", postcode)
            .build().toString();
    }

    private static String[] parseWeather(String jsonString, int dayCount) {
        final String[] result = new String[dayCount];
        try {
            final JSONObject forecast = new JSONObject(jsonString);
            final JSONArray list = forecast.getJSONArray("list");
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
                final double hi = temperature.getDouble("max");
                final double lo = temperature.getDouble("min");
                final String hiLo = Math.round(hi) + "/" + Math.round(lo);
                result[i] = day + " - " + description + " -- " + hiLo;
            }
        } catch (final Exception e) {
            Log.e(LOG_TAG, "parseWeather() catch", e);
        }
        return result;
    }

    private void asyncFetchForecast() {
        final int week = 7;
        final String url = makeUrl("02138", week);
        Log.i(LOG_TAG, "asyncFetchForecast() url == " + url);
        new AsyncTask<Void, Void, String[]>() {
            protected String[] doInBackground(Void... ignored) {
                return parseWeather(fetchForecast(url), week);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(LOG_TAG, "onCreateOptionsMenu()");
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(LOG_TAG, "onOptionsItemSelected()");
        final int id = item.getItemId();
        if (id == R.id.action_refresh) {
            asyncFetchForecast();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
