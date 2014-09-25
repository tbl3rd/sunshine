package com.example.android.sunshine;

import com.example.android.sunshine.sync.SunshineSyncAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity
    extends ActionBarActivity
    implements ForecastFragment.Callback
{
    private static final String TAG = MainActivity.class.getSimpleName();
    boolean mTwoPane;

    @Override
    public void onItemSelected(String date) {
        if (mTwoPane) {
            final Bundle args = new Bundle();
            final DetailFragment df = new DetailFragment();
            args.putString(DetailActivity.KEY_DATE, date);
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.weather_detail_container, df).commit();
        } else {
            startActivity(new Intent(this, DetailActivity.class)
                    .putExtra(DetailActivity.KEY_DATE, date));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTwoPane = findViewById(R.id.weather_detail_container) != null;
        if (mTwoPane && savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.weather_detail_container, new DetailFragment())
                .commit();
        }
        ((ForecastFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast))
            .setTwoPane(mTwoPane);
        SunshineSyncAdapter.syncNow(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long as
    // you specify a parent activity in AndroidManifest.xml.
    //
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_map:
            Utility.showMap(this);
            return true;
        case R.id.action_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
