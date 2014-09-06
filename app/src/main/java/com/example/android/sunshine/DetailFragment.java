package com.example.android.sunshine;

import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class DetailFragment
    extends Fragment
    implements LoaderCallbacks<Cursor>
{
    private static final String TAG = DetailFragment.class.getSimpleName();

    private static final int LOADER_INDEX = 0;

    private String mDbDate;
    private String mLocation;
    private String mWeather;

    CursorLoader mLoader;

    private Intent getShareIntent() {
        final Intent result = new Intent(Intent.ACTION_SEND);
        final String name = getString(R.string.app_name);
        result.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        result.setType("text/plain");
        result.putExtra(Intent.EXTRA_TEXT, mWeather + " #" + name);
        return result;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final ActionProvider ap = MenuItemCompat.getActionProvider(
                menu.findItem(R.id.action_share));
        if (ap == null) {
            Utility.makeShortToast(getActivity(), R.string.action_share_none);
        } else {
            ((ShareActionProvider)ap).setShareIntent(getShareIntent());
        }
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
        final Uri uri = WeatherEntry.buildWeatherLocationDate(
                mLocation, mDbDate);
        Log.v(TAG, "onCreateLoader(" + i + ", ...): uri == " + uri);
        mLoader = new CursorLoader(getActivity(), uri,
                Utility.FORECAST_COLUMNS, null, null, null);
        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        final Activity a = getActivity();
        final SimpleAdapter adapter = makeAdapter(a);
        final View result
            = inflater.inflate(R.layout.list_item_forecast, container, false);
        adapter.setViewBinder(Utility.makeWeatherBinder(a));
        result.setAdapter(adapter);
        final Intent intent = a.getIntent();
        if (intent != null) {
            final Bundle extras = intent.getExtras();
            if (extras != null) {
                final String dbDate = extras.getString(Intent.EXTRA_TEXT);
                if (dbDate != null) {
                    mDbDate = dbDate;
                }
            }
        }
        return result;
    }

    public DetailFragment() {
        super();
        setHasOptionsMenu(true);
    }
}
