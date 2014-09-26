package com.example.android.sunshine.sync;

import com.example.android.sunshine.DetailActivity;
import com.example.android.sunshine.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter
{
    static final String TAG = SunshineSyncAdapter.class.getSimpleName();

    public static final int HOURLY = 60 * 60;
    public static final int NOTBEFORE = HOURLY / 3;

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
            configurePeriodicSync(context, HOURLY, NOTBEFORE);
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
        getSyncAccount(context);
    }

    @Override
    public void onPerformSync(
            Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult)
    {
        Log.v(TAG, "onPerformSync(): account == " + account);
        SunshineFetchWeather.fetch(mContext);
    }

    SunshineSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        mContext = context;
    }
}
