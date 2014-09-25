package com.example.android.sunshine.service;

import com.example.android.sunshine.FetchWeatherTask;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class SunshineService extends IntentService
{
    static final String TAG = SunshineService.class.getSimpleName();
    static final String PACKAGE = SunshineService.class.getPackage().getName();
    static final String ACTION_FETCH = PACKAGE + ".action.FETCH";
    static final String EXTRA_LOCATION = PACKAGE + ".extra.LOCATION";

    static Intent makeFetchIntent(Context context, String location) {
        Log.v(TAG, "makeFetchIntent(): context == " + context);
        Log.v(TAG, "makeFetchIntent(): location == " + location);
        return new Intent(context, SunshineService.class)
            .setAction(ACTION_FETCH)
            .putExtra(EXTRA_LOCATION, location);
    }

    public static void fetchWeatherNow(Context context, String location) {
        Log.v(TAG, "fetchWeatherNow(): context == " + context);
        Log.v(TAG, "fetchWeatherNow(): location == " + location);
        context.startService(makeFetchIntent(context, location));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent(): intent == " + intent);
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
        Log.v(TAG, "SunshineService()");
    }

    public static class AlarmReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive(): intent == " + intent);
            context.startService(makeFetchIntent(context,
                            intent.getStringExtra(EXTRA_LOCATION)));
        }
    }

    public static void fetchWeatherLater(Context context, String location) {
        Log.v(TAG, "fetchWeatherLater() waiting 5000ms");
        ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE))
            .set(AlarmManager.RTC_WAKEUP, 5000 + System.currentTimeMillis(),
                    PendingIntent.getBroadcast(context, 0,
                            new Intent(context, AlarmReceiver.class)
                            .setAction(ACTION_FETCH)
                            .putExtra(EXTRA_LOCATION, location),
                            PendingIntent.FLAG_ONE_SHOT));
    }
}
