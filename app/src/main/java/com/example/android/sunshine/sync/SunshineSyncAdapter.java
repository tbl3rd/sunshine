package com.example.android.sunshine.sync;

import java.util.Calendar;
import java.util.Date;

import com.example.android.sunshine.MainActivity;
import com.example.android.sunshine.R;
import com.example.android.sunshine.Utility;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;


public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter
{
    static final String TAG = SunshineSyncAdapter.class.getSimpleName();

    // public static final int HOURLY = 60 * 60;
    public static final int MINUTELY = 30;
    public static final int NOTBEFORE = MINUTELY / 3;

    private static final int WEATHER_NOTIFICATION_ID = 3004;

    final Context mContext;

    // Return a valid account for R.string.app_name or create a new one.
    // Configure this sync adapter when returning a new account.
    //
    static Account getSyncAccount(Context context) {
        Log.v(TAG, "getSyncAccount(): context == " + context);
        final AccountManager am
            = (AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);
        final Account result = new Account(context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));
        Log.v(TAG, "getSyncAccount(): result == " + result);
        if (null == am.getPassword(result)) {
            am.addAccountExplicitly(result, "", null);
            ContentResolver.setSyncAutomatically(result,
                    context.getString(R.string.content_authority), true);
            configurePeriodicSync(context, MINUTELY, NOTBEFORE);
        }
        return result;
    }

    // Configure this to sync periodically at least every interval
    // seconds but not before notBefore seconds of the next sync.
    //
    static void configurePeriodicSync(Context context,
            int interval, int notBefore)
    {
        Log.v(TAG, "configurePeriodicSync(): interval == " + interval);
        final String ca = context.getString(R.string.content_authority);
        final Account account = getSyncAccount(context);
        Log.v(TAG, "configurePeriodicSync(): account == " + account);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            ContentResolver.addPeriodicSync(account,
                    ca, new Bundle(), interval);
        } else {
            ContentResolver.requestSync(new SyncRequest.Builder()
                    .syncPeriodic(interval, notBefore)
                    .setSyncAdapter(account, ca)
                    .build());
        }
    }

    public static void syncNow(Context context) {
        Log.v(TAG, "syncNow(): context == " + context);
        final Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    // Start this periodic sync adapter.
    //
    public static void start(Context context) {
        Log.v(TAG, "start(): context == " + context);
        getSyncAccount(context);
    }

    final static String notificationText(Context context, Cursor cursor) {
        final int code = cursor.getInt(Utility.COLUMN_WEATHER_CODE);
        final double maximum = cursor.getDouble(Utility.COLUMN_MAXIMUM);
        final double minimum = cursor.getDouble(Utility.COLUMN_MINIMUM);
        final boolean isMetric = Utility.isMetric(context);
        final String result = String.format(
                context.getString(R.string.format_notification),
                cursor.getString(Utility.COLUMN_DESCRIPTION),
                Utility.formatCelsius(context, isMetric, maximum),
                Utility.formatCelsius(context, isMetric, minimum));
        Log.v(TAG, "notificationText(): result == " + result);
        return result;
    }

    private static void notify(Context context, Cursor cursor) {
        final int code = cursor.getInt(Utility.COLUMN_WEATHER_CODE);
        final PendingIntent pending = TaskStackBuilder.create(context)
            .addParentStack(MainActivity.class)
            .addNextIntent(new Intent(context, MainActivity.class))
            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationManager nm = (NotificationManager)context
            .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(WEATHER_NOTIFICATION_ID,
                new NotificationCompat.Builder(context)
                .setContentIntent(pending)
                .setContentText(notificationText(context, cursor))
                .setContentTitle(context.getString(R.string.app_name))
                .setSmallIcon(Utility.weatherIcon(code))
                .build());
        Log.v(TAG, "notify(): code == " + code);
    }

    private static void maybeNotifyWeather(Context context) {
        Log.v(TAG, "maybeNotifyWeather()");
        final long last = Utility.getLastNotification(context);
        Log.v(TAG, "maybeNotifyWeather(): last == " + last);
        final long nowMs = System.currentTimeMillis();
        if (nowMs - last > 1000 * 60) {
            final Uri uri = WeatherEntry.buildWeatherLocationDate(
                    Utility.getPreferredLocation(context),
                    Utility.dbDate(new Date()));
            final Cursor cursor = context.getContentResolver().query(
                    uri, Utility.FORECAST_COLUMNS, null, null, null);
            if (cursor.moveToFirst()) {
                Utility.setLastNotification(context, nowMs);
                notify(context, cursor);
            }
        }
    }

    static String yesterday() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -1);
        return Utility.dbDate(calendar.getTime());
    }

    @Override
    public void onPerformSync(
            Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult)
    {
        Log.v(TAG, "onPerformSync(): account == " + account);
        SunshineFetchWeather.fetch(mContext);
        if (Utility.notificationsOn(mContext)) maybeNotifyWeather(mContext);
        final String yesterday = SunshineSyncAdapter.yesterday();
        Log.v(TAG, "onPerformSync(): yesterday == " + yesterday);
        try {
            final int count = provider.delete(WeatherEntry.CONTENT_URI,
                    (WeatherEntry.COLUMN_DATE + " < ? "),
                    (new String[] { yesterday }));
            Log.v(TAG, "onPerformSync(): count == " + count);
        } catch (final Exception e) {
            Log.d(TAG, "onPerformSync(): caught " + e);
        }
    }

    SunshineSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        mContext = context;
    }
}
