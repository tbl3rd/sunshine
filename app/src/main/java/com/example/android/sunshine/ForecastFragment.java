package com.example.android.sunshine;

import java.util.Date;

import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.sync.SunshineSyncAdapter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


public class ForecastFragment
    extends Fragment
    implements LoaderCallbacks<Cursor>
{
    private static final int LOADER_INDEX = 0;

    private static final String TAG = ForecastFragment.class.getSimpleName();

    private ForecastAdapter mAdapter;
    private ListView mListView;
    private String mLocation;
    private boolean mTwoPane;
    private int mPosition = 0;

    public interface Callback {
        public void onItemSelected(String date);
    }

    void setTwoPane(boolean twoPane) {
        mTwoPane = twoPane;
        if (mAdapter != null) mAdapter.setTwoPane(mTwoPane);
    }

    @Override
    public void onCreate(Bundle saved)
    {
        super.onCreate(saved);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle saved)
    {
        final Activity a = getActivity();
        final View result
            = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView)result.findViewById(R.id.listview_forecast);
        mAdapter = new ForecastAdapter(a, null, 0);
        mAdapter.setTwoPane(mTwoPane);
        if (saved != null) {
            mPosition = saved.getInt(DetailActivity.KEY_POSITION);
            Log.v(TAG, "onCreateView(): mPosition == " + mPosition);
        }
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(
                        AdapterView av, View view, int n, long id)
                {
                    final Cursor cursor = mAdapter.getCursor();
                    Log.v(TAG, "onItemClick(): cursor == " + cursor);
                    Log.v(TAG, "onItemClick(): n == " + n);
                    if (cursor != null && cursor.moveToPosition(n)) {
                        ((Callback)a).onItemSelected(
                                cursor.getString(Utility.COLUMN_DATE));
                        mPosition = n;
                    }
                }
            });
        return result;
    }

    @Override
    public void onSaveInstanceState(Bundle saved) {
        Log.v(TAG, "onSaveInstanceState(): saved == " + saved);
        if (saved != null) {
            saved.putInt(DetailActivity.KEY_POSITION, mPosition);
            Log.v(TAG, "onSaveInstanceState(): mPosition == " + mPosition);
        }
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
            getLoaderManager().restartLoader(LOADER_INDEX, null, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(TAG, "onCreateOptionsMenu()");
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    private Uri getGeoLocationUri(Activity a) {
        final Cursor cursor = (mAdapter == null) ? null
            : mAdapter.getCursor();
        if (cursor == null) {
            if (mLocation == null) {
                mLocation = Utility.getPreferredLocation(a);
            }
            return Uri.fromParts("geo", "0,0", null).buildUpon()
                .appendQueryParameter("q", mLocation).build();
        }
        return Uri.fromParts("geo",
                cursor.getString(Utility.COLUMN_LATITUDE)
                + ","
                + cursor.getString(Utility.COLUMN_LONGITUDE),
                null);
    }

    private void showMap(Activity a) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        final Uri geo = getGeoLocationUri(a);
        Log.v(TAG, "showMap(): geo == " + geo);
        intent.setData(geo);
        if (intent.resolveActivity(a.getPackageManager()) == null) {
            Utility.shortToast(a, R.string.action_map_none);
        } else {
            a.startActivity(intent);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected()");
        switch (item.getItemId()) {
        case R.id.action_map:
            showMap(getActivity());
            return true;
        case R.id.action_refresh:
            SunshineSyncAdapter.syncNow(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);
        Log.i(TAG, "onActivityCreated()");
        getLoaderManager().initLoader(LOADER_INDEX, null, this);
        final boolean contentAuthorityIsConsistent
            = WeatherContract.CONTENT_AUTHORITY.equals(
                    getString(R.string.content_authority));
        if (!contentAuthorityIsConsistent) {
            throw new RuntimeException("content authority mismatch");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        mLocation = Utility.getPreferredLocation(getActivity());
        final Uri uri = WeatherEntry.buildWeatherLocationQueryDate(
                mLocation, new Date());
        Log.v(TAG, "onCreateLoader(" + i + ", ...): uri == " + uri);
        return new CursorLoader(getActivity(), uri, Utility.FORECAST_COLUMNS,
                null, null, WeatherEntry.COLUMN_DATE + " ASC ");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        if (mTwoPane && mListView != null) {
            mListView.post(new Runnable() {
                    @Override
                    public void run() {
                        mListView.performItemClick(
                                mListView.getChildAt(mPosition),
                                mPosition, mAdapter.getItemId(mPosition));
                    }
                });
            mListView.setSelection(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public ForecastFragment() {
        super();
    }
}
