package com.example.android.sunshine;

import java.text.DateFormat;
import java.util.Date;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


public class ForecastFragment
    extends Fragment
    implements LoaderCallbacks<Cursor>
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

    SimpleCursorAdapter mForecastAdapter;

    private static String getPreferredLocation(Activity a) {
        return PreferenceManager
            .getDefaultSharedPreferences(a).getString(
                    a.getString(R.string.preference_location_key),
                    a.getString(R.string.preference_location_default));
    }

    private static double fromCelsius(Activity a, double t) {
        final String metric = a.getString(R.string.preference_units_default);
        final String units
            = PreferenceManager.getDefaultSharedPreferences(a)
            .getString(a.getString(R.string.preference_units_key), metric);
        final boolean isMetric = units.equals(metric);
        return Math.round(isMetric ? t : (32.0 + 1.8 * t));
    }

    private static SimpleCursorAdapter makeSimpleCursorAdapter(Activity a) {
        return new SimpleCursorAdapter(a, R.layout.list_item_forecast, null,
                new String[] {
                    WeatherEntry.COLUMN_DATE,
                    WeatherEntry.COLUMN_DESCRIPTION,
                    WeatherEntry.COLUMN_MAXIMUM,
                    WeatherEntry.COLUMN_MINIMUM
                },
                new int[] {
                    R.id.list_item_date_textview,
                    R.id.list_item_forecast_textview,
                    R.id.list_item_high_textview,
                    R.id.list_item_low_textview
                },
                0);
    }

    private static SimpleCursorAdapter.ViewBinder
        makeViewBinder(final Activity a)
    {
        return new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View v, Cursor c, int n)
            {
                final TextView tv = (TextView)v;
                switch (n) {
                case COLUMN_MAXIMUM:
                case COLUMN_MINIMUM:
                    tv.setText(String.valueOf(fromCelsius(a, c.getDouble(n))));
                    return true;
                case COLUMN_DATE:
                    tv.setText(DateFormat.getDateInstance().format(
                                    WeatherEntry.dbDate(c.getString(n))));
                    return true;
                }
                return false;
            }
        };
    }

    private static AdapterView.OnItemClickListener
        makeOnItemClickListener(final Activity a)
    {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView av, View view, int n, long id)
            {
                final Cursor c
                    = ((SimpleCursorAdapter)av.getAdapter()).getCursor();
                final String extra
                    = DateFormat.getDateInstance().format(
                            WeatherEntry.dbDate(c.getString(COLUMN_DATE)))
                    + " - " + a.getString(COLUMN_DESCRIPTION) + " -- "
                    + fromCelsius(a, c.getDouble(COLUMN_MAXIMUM)) + " / "
                    + fromCelsius(a, c.getDouble(COLUMN_MINIMUM));
                a.startActivity(new Intent(a, DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, extra));
            }
        };
    }

    private static SimpleCursorAdapter makeForecastAdapter(Activity a, View v)
    {
        final SimpleCursorAdapter result = makeSimpleCursorAdapter(a);
        final ListView lv = (ListView)v.findViewById(R.id.listview_forecast);
        Log.v(TAG, "makeForecastAdapter(): lv == " + lv);
        lv.setAdapter(result);
        lv.setOnItemClickListener(makeOnItemClickListener(a));
        result.setViewBinder(makeViewBinder(a));
        return result;
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
        Log.v(TAG, "onCreateView(): result == " + result);
        mForecastAdapter = makeForecastAdapter(getActivity(), result);
        return result;
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        final boolean sameLocation
            = mLocation == null
            || mLocation.equals(getPreferredLocation(getActivity()));
        if (!sameLocation) {
            getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        }
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
            FetchWeatherTask.fetch(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated()");
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        mLocation = getPreferredLocation(getActivity());
        final Uri uri = WeatherEntry.buildWeatherLocationQueryDate(
                mLocation, new Date());
        Log.v(TAG, "onCreateLoader(" + i + ", ...): uri == " + uri);
        return new CursorLoader(
                getActivity(), uri, FORECAST_COLUMNS, null, null,
                WeatherEntry.COLUMN_DATE + " ASC ");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public ForecastFragment() {
        super();
        if (COLUMN_COUNT != FORECAST_COLUMNS.length) {
            throw new RuntimeException(
                    "COLUMN_COUNT != FORECAST_COLUMNS.length");
        }
    }
}
