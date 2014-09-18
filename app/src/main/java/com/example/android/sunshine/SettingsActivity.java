package com.example.android.sunshine;

import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;


public class SettingsActivity
    extends PreferenceActivity
    implements Preference.OnPreferenceChangeListener,
               Preference.OnPreferenceClickListener
{
    private static final String TAG = SettingsActivity.class.getSimpleName();

    boolean mBindingPreference = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        final String locKey = getString(R.string.preference_location_key);
        final String unitsKey = getString(R.string.preference_units_key);
        bindPreferenceSummaryToValue(findPreference(locKey));
        bindPreferenceSummaryToValue(findPreference(unitsKey));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String stringValue = value.toString();
        if (!mBindingPreference) {
            final boolean locationPreferenceChanged
                = preference.getKey().equals(
                        getString(R.string.preference_location_key));
            if (locationPreferenceChanged) {
                FetchWeatherTask.fetch(this);
            } else {
                getContentResolver().notifyChange(
                        WeatherEntry.CONTENT_URI, null);
            }
        }
        if (preference instanceof ListPreference) {
            final ListPreference listPreference = (ListPreference)preference;
            final int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            preference.setSummary(stringValue);
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Log.v(TAG, "onPreferenceClick(): preference == " + preference);
        if (preference instanceof EditTextPreference) {
            Log.v(TAG, "onPreferenceClick(): fnord");
            final EditTextPreference etp = (EditTextPreference)preference;
            etp.getEditText().setSelection(etp.getText().length());
        }
        return true;
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        mBindingPreference = true;
        preference.setOnPreferenceChangeListener(this);
        preference.setOnPreferenceClickListener(this);
        onPreferenceChange(preference,
                PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));
        mBindingPreference = false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent()
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
