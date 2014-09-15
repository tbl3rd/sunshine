package com.example.android.sunshine;

import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


public class SettingsActivity
    extends PreferenceActivity
    implements Preference.OnPreferenceChangeListener
{
    boolean mBindingPreference = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        // TODO: Add preferences from XML
        addPreferencesFromResource(R.xml.preferences);
        // For all preferences, attach an OnPreferenceChangeListener
        // so the UI summary can be updated when the preference
        // changes.  TODO: Add preferences
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

    /**
     * Attaches a listener so the summary is always updated with the
     * preference value.  Also fires the listener once, to initialize
     * the summary (so it shows up before the value is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        mBindingPreference = true;
        preference.setOnPreferenceChangeListener(this);
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
