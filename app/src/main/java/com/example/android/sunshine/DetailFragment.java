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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class DetailFragment
    extends Fragment
    implements LoaderCallbacks<Cursor>
{
    private static final String TAG = DetailFragment.class.getSimpleName();

    private static final int LOADER_INDEX = 0;

    private View mView;
    private String mDbDate;
    private String mLocation;
    private String mWeather;

    CursorLoader mLoader;

    static class ViewHolder {
        final TextView day;
        final TextView date;
        final TextView maximum;
        final TextView minimum;
        final TextView humidity;
        final TextView wind;
        final TextView pressure;
        final ImageView icon;
        final TextView description;
        ViewHolder(View v) {
            day         =  (TextView)v.findViewById(R.id.detail_day);
            date        =  (TextView)v.findViewById(R.id.detail_date);
            maximum     =  (TextView)v.findViewById(R.id.detail_maximum);
            minimum     =  (TextView)v.findViewById(R.id.detail_minimum);
            humidity    =  (TextView)v.findViewById(R.id.detail_humidity);
            wind        =  (TextView)v.findViewById(R.id.detail_wind);
            pressure    =  (TextView)v.findViewById(R.id.detail_pressure);
            icon        = (ImageView)v.findViewById(R.id.detail_icon);
            description =  (TextView)v.findViewById(R.id.detail_description);
        }
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
            Utility.shortToast(getActivity(), R.string.action_share_none);
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
        if (c.moveToFirst()) {
            final Activity a = getActivity();
            final ViewHolder vh = (ViewHolder)mView.getTag();
            final String date
                = Utility.displayDbDate(c.getString(Utility.COLUMN_DATE));
            final String description
                = c.getString(Utility.COLUMN_DESCRIPTION);
            final String maximum
                = Utility.fromCelsius(a, c.getDouble(Utility.COLUMN_MAXIMUM));
            final String minimum
                = Utility.fromCelsius(a, c.getDouble(Utility.COLUMN_MINIMUM));
            final String humidity
                = String.valueOf(c.getDouble(Utility.COLUMN_HUMIDITY));
            final String wind
                = String.valueOf(c.getDouble(Utility.COLUMN_WIND));
            final String pressure
                = String.valueOf(c.getDouble(Utility.COLUMN_PRESSURE));
            vh.day.setText(date);
            vh.date.setText(date);
            vh.maximum.setText(maximum);
            vh.minimum.setText(minimum);
            vh.humidity.setText(humidity);
            vh.wind.setText(wind);
            vh.pressure.setText(pressure);
            vh.icon.setImageResource(R.drawable.ic_launcher);
            vh.description.setText(description);
            mWeather = date + " - " + description
                + " -- "  + maximum + " / " + minimum;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public View onCreateView(LayoutInflater i, ViewGroup c, Bundle saved)
    {
        final View result = i.inflate(R.layout.fragment_detail, c, false);
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
        result.setTag(new ViewHolder(result));
        mView = result;
        return result;
    }

    public DetailFragment() {
        super();
        setHasOptionsMenu(true);
    }
}
