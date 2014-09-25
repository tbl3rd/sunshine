package com.example.android.sunshine.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by tbl on 9/25/14.
 */
public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter
{
    static final String TAG = SunshineSyncAdapter.class.getSimpleName();

    public static Account getSyncAccount(Context context) {
        final AccountManager am
            = (AccountManager)context.getSystemService(TAG);
        return null;
    }

    @Override
    public void onPerformSync(
            Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult)
    {

    }

    public SunshineSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
    }
}
