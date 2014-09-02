package com.example.android.sunshine;

import java.util.ArrayList;

import android.content.Intent;
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

    private static final String TAG = ForecastFragment.class.getSimpleName();

    ArrayAdapter<String> mForecastAdapter = null;

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

    private void fetchForecast() {
        FetchWeatherTask.fetch(getActivity(), mForecastAdapter);
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
        fetchForecast();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(TAG, "onCreateOptionsMenu()");
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected()");
        switch (item.getItemId()) {
        case R.id.action_refresh:
            fetchForecast();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ForecastFragment() { super(); }
}
