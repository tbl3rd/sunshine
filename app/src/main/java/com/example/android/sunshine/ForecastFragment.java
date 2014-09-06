package com.example.android.sunshine;

import java.util.Date;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
    private static final int LOADER_INDEX = 0;

    private static final String TAG = ForecastFragment.class.getSimpleName();

    private String mLocation;

    SimpleCursorAdapter mAdapter;

    private SimpleCursorAdapter makeSimpleCursorAdapter() {
        return new SimpleCursorAdapter(
                getActivity(), R.layout.list_item_forecast, null,
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

    private SimpleCursorAdapter.ViewBinder makeViewBinder() {
        return new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View v, Cursor c, int n)
            {
                final TextView tv = (TextView)v;
                switch (n) {
                case Utility.COLUMN_MAXIMUM:
                case Utility.COLUMN_MINIMUM:
                    tv.setText(Utility.fromCelsius(getActivity(),
                                    c.getDouble(n)));
                    return true;
                case Utility.COLUMN_DATE:
                    tv.setText(Utility.displayDbDate(c.getString(n)));
                    return true;
                }
                return false;
            }
        };
    }

    private AdapterView.OnItemClickListener makeOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView av, View view, int n, long id)
            {
                startActivity(new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT,
                                ((SimpleCursorAdapter)av.getAdapter())
                                .getCursor().getString(Utility.COLUMN_DATE)));
            }
        };
    }

    private SimpleCursorAdapter makeForecastAdapter(View v)
    {
        final SimpleCursorAdapter result = makeSimpleCursorAdapter();
        final ListView lv = (ListView)v.findViewById(R.id.listview_forecast);
        Log.v(TAG, "makeForecastAdapter(): lv == " + lv);
        lv.setAdapter(result);
        lv.setOnItemClickListener(makeOnItemClickListener());
        result.setViewBinder(makeViewBinder());
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
        mAdapter = makeForecastAdapter(result);
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
            =  mLocation == null
            || mLocation.equals(Utility.getPreferredLocation(getActivity()));
        if (!sameLocation) {
            getLoaderManager().initLoader(LOADER_INDEX, null, this);
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
        getLoaderManager().restartLoader(LOADER_INDEX, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        mLocation = Utility.getPreferredLocation(getActivity());
        final Uri uri = WeatherEntry.buildWeatherLocationQueryDate(
                mLocation, new Date());
        Log.v(TAG, "onCreateLoader(" + i + ", ...): uri == " + uri);
        return new CursorLoader(
                getActivity(), uri, Utility.FORECAST_COLUMNS, null, null,
                WeatherEntry.COLUMN_DATE + " ASC ");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public ForecastFragment() {
        super();
    }
}
