package com.example.android.sunshine;

import com.example.android.sunshine.ForecastFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public String getLocationPreference() {
        final String key = getString(R.string.preference_location_key);
        final String or = getString(R.string.preference_location_default);
        return PreferenceManager.getDefaultSharedPreferences(this)
            .getString(key, or);
    }

    public String getUnitsPreference() {
        final String key = getString(R.string.preference_units_key);
        final String or = getString(R.string.preference_units_default);
        return PreferenceManager.getDefaultSharedPreferences(this)
            .getString(key, or);
    }

    public void showMap() {
        final String location = getLocationPreference();
        final Uri geo = new Uri.Builder()
            .scheme("geo")
            .appendPath("0,0")
            .appendQueryParameter("q", location)
            .build();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geo);
        if (intent.resolveActivity(getPackageManager()) == null) {
            final String noMap = getString(R.string.action_map_none);
            Toast.makeText(this, noMap, Toast.LENGTH_SHORT).show();
        } else {
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, new ForecastFragment())
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

    // Inflate the menu; this adds items to the action bar if it is present.
    //
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
            showMap();
            return true;
        case R.id.action_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
