package com.example.android.sunshine;

import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new PlaceholderFragment())
                .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        ArrayAdapter<String> mForecastAdapter = null;

        public PlaceholderFragment() {
        }

        // Set up and return adapter for the forecast in rootView.
        //
        private ArrayAdapter<String> makeForecastAdapter(View rootView) {
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

        @Override
        public View onCreateView(
                LayoutInflater inflater,
                ViewGroup container,
                Bundle savedInstanceState) {
            final View result
                = inflater.inflate(R.layout.fragment_main, container, false);
            mForecastAdapter = makeForecastAdapter(result);
            return result;
        }
    }
}
