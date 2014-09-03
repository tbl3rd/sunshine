package com.example.android.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import java.util.ArrayList;
import java.util.Date;


public class ForecastFragment
    extends Fragment
    implements LoaderManager.LoaderCallbacks
{

    private static final String TAG = ForecastFragment.class.getSimpleName();

    private static final String[] FORECAST_COLUMNS = {
        WeatherEntry.TABLE + "." + WeatherEntry._ID,
        WeatherEntry.COLUMN_DATE,
        WeatherEntry.COLUMN_DESCRIPTION,
        WeatherEntry.COLUMN_MAXIMUM,
        WeatherEntry.COLUMN_MINIMUM,
        LocationEntry.COLUMN_SETTING
    };

    private static final int COLUMN_ID          = 0;
    private static final int COLUMN_DATE        = 1;
    private static final int COLUMN_DESCRIPTION = 2;
    private static final int COLUMN_MAXIMUM     = 3;
    private static final int COLUMN_MINIMUM     = 4;
    private static final int COLUMN_SETTING     = 5;
    private static final int COLUMN_COUNT       = 6;

    private String mLocation;

    private static final int FORECAST_LOADER = 0;

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated()");
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        mLocation = PreferenceManager
            .getDefaultSharedPreferences(getActivity()).getString(
                    getString(R.string.preference_location_key),
                    getString(R.string.preference_location_default));
        final Uri uri
            = WeatherEntry.buildWeatherLocationQueryDate(mLocation, new Date());
        Log.v(TAG, "onCreateLoader(" + i + ", ...): uri == " + uri);
        return new CursorLoader(
                getActivity(), uri, FORECAST_COLUMNS, null, null,
                WeatherEntry.COLUMN_DATE + " ASC ");
    }

    @Override
    public void onLoadFinished(Loader loader, Object o) {

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    public ForecastFragment() {
        super();
        assert COLUMN_COUNT == FORECAST_COLUMNS.length;
    }
}
