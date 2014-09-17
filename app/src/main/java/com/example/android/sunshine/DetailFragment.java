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

    private ShareActionProvider mShareActionProvider;
    private View mView;
    private String mDbDate;
    private String mLocation;
    private String mWeather;

    static class ViewHolder {
        final ImageView icon;
        final TextView  day;        
        final TextView  date;       
        final TextView  maximum;    
        final TextView  minimum;    
        final TextView  humidity;   
        final TextView  wind;       
        final TextView  pressure;   
        final TextView  description;
        ViewHolder(View v) {
            icon        = (ImageView)v.findViewById(R.id.detail_icon);
            day         =  (TextView)v.findViewById(R.id.detail_day);
            date        =  (TextView)v.findViewById(R.id.detail_date);
            maximum     =  (TextView)v.findViewById(R.id.detail_maximum);
            minimum     =  (TextView)v.findViewById(R.id.detail_minimum);
            humidity    =  (TextView)v.findViewById(R.id.detail_humidity);
            wind        =  (TextView)v.findViewById(R.id.detail_wind);
            pressure    =  (TextView)v.findViewById(R.id.detail_pressure);
            description =  (TextView)v.findViewById(R.id.detail_description);
        }
    }

    public String getDate() {
        final Bundle args = getArguments();
        Log.v(TAG, "getDate(): args == " + args);
        return getArguments().getString(DetailActivity.KEY_DATE);
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
    public void onResume() {
        super.onResume();
        final Bundle args = getArguments();
        final boolean restart = (args != null)
            && args.containsKey(DetailActivity.KEY_DATE)
            && (mLocation != null)
            && !mLocation.equals(Utility.getPreferredLocation(getActivity()));
        if (restart) {
            getLoaderManager().restartLoader(LOADER_INDEX, null, this);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);
        final MenuItem mi = menu.findItem(R.id.action_share);
        final ActionProvider ap = MenuItemCompat.getActionProvider(mi);
        mShareActionProvider = (ShareActionProvider)ap;
        if (mShareActionProvider != null && mWeather != null) {
            mShareActionProvider.setShareIntent(getShareIntent());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated()");
        if (savedInstanceState != null) {
            mLocation
                = savedInstanceState.getString(DetailActivity.KEY_LOCATION);
        }
        final Bundle args = getArguments();
        if (args != null && args.containsKey(DetailActivity.KEY_DATE)) {
            getLoaderManager().initLoader(LOADER_INDEX, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        final Activity a = getActivity();
        final String date = getArguments().getString(DetailActivity.KEY_DATE);
        mLocation = Utility.getPreferredLocation(a);
        final Uri uri = WeatherEntry.buildWeatherLocationDate(
                mLocation, mDbDate);
        Log.v(TAG, "onCreateLoader(" + i + ", ...): uri == " + uri);
        return new CursorLoader(a, uri, Utility.FORECAST_COLUMNS,
                null, null, WeatherEntry.COLUMN_DATE + " ASC ");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        if (c != null && c.moveToFirst()) {
            final Activity a = getActivity();
            final boolean isMetric = Utility.isMetric(a);
            final String dbDate = c.getString(Utility.COLUMN_DATE);
            final String day = Utility.dayName(a, dbDate);
            final String date = Utility.displayDbDate(dbDate);
            final String description = c.getString(Utility.COLUMN_DESCRIPTION);
            final String maximum
                = Utility.formatCelsius(a, isMetric,
                        c.getDouble(Utility.COLUMN_MAXIMUM));
            final String minimum
                = Utility.formatCelsius(a, isMetric,
                        c.getDouble(Utility.COLUMN_MINIMUM));
            final String humidity
                = Utility.formatHumidity(a,
                        c.getDouble(Utility.COLUMN_HUMIDITY));
            final String wind
                = Utility.formatWind(a, isMetric,
                        c.getDouble(Utility.COLUMN_WIND),
                        c.getInt(Utility.COLUMN_DIRECTION));
            final String pressure
                = Utility.formatPressure(a, isMetric,
                        c.getDouble(Utility.COLUMN_PRESSURE));
            final ViewHolder vh = (ViewHolder)mView.getTag();
            final int code = c.getInt(Utility.COLUMN_WEATHER_CODE);
            Log.v(TAG, "onLoadFinished(): code == " + code);
            vh.icon.setImageResource(Utility.weatherArt(code));
            vh.day.setText(day);
            vh.date.setText(date);
            vh.maximum.setText(maximum);
            vh.minimum.setText(minimum);
            vh.humidity.setText(humidity);
            vh.wind.setText(wind);
            vh.pressure.setText(pressure);
            vh.description.setText(description);
            mWeather = date + " - " + description
                + " -- "  + maximum + " / " + minimum;
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(getShareIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(DetailActivity.KEY_LOCATION, mLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater i, ViewGroup c, Bundle saved)
    {
        final View result = i.inflate(R.layout.fragment_detail, c, false);
        final Bundle args = getArguments();
        if (args  != null) {
            mDbDate   =  args.getString(DetailActivity.KEY_DATE);
        }
        if (saved != null) {
            mLocation = saved.getString(DetailActivity.KEY_LOCATION);
        }
        result.setTag(new ViewHolder(result));
        mView = result;
        Log.v(TAG, "onCreateView(): result == " + result);
        return result;
    }

    public DetailFragment() {
        super();
        Log.v(TAG, "DetailFragment()");
        setHasOptionsMenu(true);
    }
}
