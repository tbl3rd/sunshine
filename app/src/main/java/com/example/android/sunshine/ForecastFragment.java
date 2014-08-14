package com.example.android.sunshine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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

    private static void cleanupFetchForecast(
            HttpURLConnection connection,
            BufferedReader reader)
    {
        try {
            if (connection != null) connection.disconnect();
            if (reader != null) reader.close();
        } catch (final IOException ioe) {
            Log.e(LOG_TAG, "Error closing stream", ioe);
        }
    }

    private String fetchForecast(String... url) {
        Log.i(LOG_TAG, "url[0] == " + url[0]);
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            connection = (HttpURLConnection)new URL(url[0]).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            final InputStream is = connection.getInputStream();
            final InputStreamReader isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);
            final StringBuffer buffer = new StringBuffer();
            while (true) {
                final String line = reader.readLine();
                if (line == null) break;
                buffer.append(line + "\n");
            }
            return buffer.toString();
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            cleanupFetchForecast(connection, reader);
        }
        return null;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        final View result
            = inflater.inflate(R.layout.fragment_main, container, false);
        final String url
            = "http://api.openweathermap.org/data/2.5/forecast/daily"
            + "?q=94043&mode=json&units=metric&cnt=7";
        new FetchWeatherTask().execute(url);
        mForecastAdapter = makeForecastAdapter(result);
        return result;
    }


    private class FetchWeatherTask extends AsyncTask<String, Integer, String> {

        protected String doInBackground(String... url) {
            return fetchForecast(url);
        }

        protected void onPostExecute(String forecast) {
            Log.i(LOG_TAG, "onPostExecute() got: " + forecast);
        }
    }
}
