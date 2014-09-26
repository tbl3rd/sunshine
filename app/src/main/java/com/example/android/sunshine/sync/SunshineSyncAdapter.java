package com.example.android.sunshine.sync;

import com.example.android.sunshine.DetailActivity;
import com.example.android.sunshine.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;


public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter
{
    static final String TAG = SunshineSyncAdapter.class.getSimpleName();

    final Context mContext;

    public static Account getSyncAccount(Context context) {
        Log.v(TAG, "getSyncAccount(): context == " + context);
        final AccountManager am
            = (AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);
        final Account result = new Account(context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));
        if (null == am.getPassword(result)) {
            am.addAccountExplicitly(result, "", null);
        }
        return result;
    }

    public static void syncNow(Context context) {
        Log.v(TAG, "syncNow(): context == " + context);
        final Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    @Override
    public void onPerformSync(
            Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult)
    {
        Log.v(TAG, "onPerformSync(): account == " + account);
        SunshineFetchWeather.fetch(mContext);
    }

    public SunshineSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        mContext = context;
    }
}
