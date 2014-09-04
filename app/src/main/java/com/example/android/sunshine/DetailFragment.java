package com.example.android.sunshine;

import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class DetailFragment
    extends Fragment
    implements LoaderCallbacks<Cursor>
{
    private static final String TAG = DetailFragment.class.getSimpleName();

    private static final int LOADER_INDEX = 0;

    public static final String[] COLUMNS = ForecastFragment.FORECAST_COLUMNS;

    private static final int COLUMN_ID          = 0;
    private static final int COLUMN_DATE        = 1;
    private static final int COLUMN_DESCRIPTION = 2;
    private static final int COLUMN_MAXIMUM     = 3;
    private static final int COLUMN_MINIMUM     = 4;
    private static final int COLUMN_SETTING     = 5;
    private static final int COLUMN_COUNT       = 6;

    private View mView;
    private String mDbDate;
    private String mLocation;
    private String mWeather;

    CursorLoader mLoader;

    private String getPreferredLocation() {
        return PreferenceManager
            .getDefaultSharedPreferences(getActivity()).getString(
                    getString(R.string.preference_location_key),
                    getString(R.string.preference_location_default));
    }

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
        final MenuItem mi = menu.findItem(R.id.action_share);
        final ActionProvider ap = MenuItemCompat.getActionProvider(mi);
        if (ap == null) {
            Toast.makeText(getActivity(),
                    getString(R.string.action_share_none), Toast.LENGTH_SHORT)
                .show();
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
        mLocation = getPreferredLocation();
        final Uri uri = WeatherEntry.buildWeatherLocationDate(
                mLocation, mDbDate);
        Log.v(TAG, "onCreateLoader(" + i + ", ...): uri == " + uri);
        mLoader = new CursorLoader(getActivity(), uri, COLUMNS,
                null, null, null);
        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        if (c.moveToFirst()) {
            mWeather     = c.getString(COLUMN_DATE)
                + " - "  + c.getString(COLUMN_DESCRIPTION)
                + " -- " + c.getDouble(COLUMN_MAXIMUM)
                + " / "  + c.getDouble(COLUMN_MINIMUM);
            ((TextView)mView.findViewById(R.id.textview_detail))
                .setText(mWeather);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_detail, container, false);
        final Intent intent = getActivity().getIntent();
        if (intent != null) {
            final Bundle extras = intent.getExtras();
            if (extras != null) {
                final String dbDate = extras.getString(Intent.EXTRA_TEXT);
                if (dbDate != null) {
                    mDbDate = dbDate;
                }
            }
        }
        return mView;
    }

    public DetailFragment() {
        super();
        setHasOptionsMenu(true);
    }
}
