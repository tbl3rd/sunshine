package com.example.android.sunshine;

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
            args.putString("date", date);
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.weather_detail_container, df).commit();
        } else {
            startActivity(new Intent(this, DetailActivity.class)
                    .putExtra("date", date));
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
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.v(TAG, "onRestart()");
        super.onRestart();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume()");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()");
        super.onDestroy();
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
