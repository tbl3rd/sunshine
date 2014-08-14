package com.example.android.sunshine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class ForecastFragment extends Fragment {

    private static final String LOG_TAG
        = ForecastFragment.class.getSimpleName();

    ArrayAdapter<String> mForecastAdapter = null;

    // Set up and return adapter for the forecast in rootView.
    //
    private ArrayAdapter<String> makeForecastAdapter(View rootView)
    {
        final List<String> weekForecast = Arrays.asList(
                "Today - Sunny -- 88/63",
                "Tomorrow - Foggy -- 70/40",
                "Weds - Cloudy -- 72/36",
                "Thurs - Asteroids -- 75/65",
                "Fri - Heavy Rain -- 65/56",
                "Sat - HELP TRAPPED IN WEATHERSTATION -- 60/51",
                "Sun - Sunny -- 80/68"
        );
        final ArrayAdapter<String> result = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);
        final ListView lv =
            (ListView)rootView.findViewById(R.id.listview_forecast);
        lv.setAdapter(result);
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

    private static String makeUrl(final String postcode) {
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
            .appendQueryParameter("q", postcode)
            .build().toString();
    }

    private static double getMaxForDay(String forecast, int dayIndex) {
        try {
            final JSONObject reply = new JSONObject(forecast);
            final JSONArray list = reply.getJSONArray("list");
            final JSONObject day = list.getJSONObject(dayIndex);
            final JSONObject temp = day.getJSONObject("temp");
            final double max = temp.getDouble("max");
            Log.v(LOG_TAG, "getMaxForDay() max == " + max);
            return max;
        } catch (Exception e) {
            Log.e(LOG_TAG, "getMaxForDay() catch", e);
        }
        return -1;
    }

    private static void asyncFetchForecast() {
        final String url = makeUrl("02138");
        Log.i(LOG_TAG, "asyncFetchForecast() url == " + url);
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... ignored) {
                return fetchForecast(url);
            }
            protected void onPostExecute(String forecast) {
                Log.i(LOG_TAG, "onPostExecute() got: " + forecast);
                getMaxForDay(forecast, 3);
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
        setHasOptionsMenu(true);
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
