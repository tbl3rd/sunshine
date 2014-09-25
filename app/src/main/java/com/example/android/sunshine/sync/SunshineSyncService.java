package com.example.android.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by tbl on 9/25/14.
 */
public class SunshineSyncService extends Service
{
    static final String TAG = SunshineSyncService.class.getSimpleName();

    private static final Object sSyncAdapterLock = new Object();
    private static SunshineSyncAdapter sSunshineSyncAdapter = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        synchronized (sSyncAdapterLock) {
            sSunshineSyncAdapter
                = new SunshineSyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }
}
