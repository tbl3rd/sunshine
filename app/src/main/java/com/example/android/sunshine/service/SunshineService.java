package com.example.android.sunshine.service;

import com.example.android.sunshine.FetchWeatherTask;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;


public class SunshineService extends IntentService
{
    static final String TAG = SunshineService.class.getSimpleName();
    static final String PACKAGE = SunshineService.class.getPackage().getName();
    static final String ACTION_FETCH = PACKAGE + ".action.FETCH";
    static final String EXTRA_LOCATION = PACKAGE + ".extra.LOCATION";

    public static void fetchWeather(Context context, String location)
    {
        context.startService(new Intent(context, SunshineService.class)
                .setAction(ACTION_FETCH)
                .putExtra(EXTRA_LOCATION, location));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH.equals(action)) {
                final String location = intent.getStringExtra(EXTRA_LOCATION);
                FetchWeatherTask.fetch(this, location);
            } else {
                throw new UnsupportedOperationException(action);
            }
        }
    }

    public SunshineService() {
        super("SunshineService");
    }
}
