package com.example.android.sunshine.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;



/**
 * Created by tbl on 9/25/14.
 */
public class SunshineAuthenticator extends AbstractAccountAuthenticator
{
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
            String accountType)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
            String accountType, String authTokenType, String[] requiredFeatures,
            Bundle options)
        throws NetworkErrorException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
            Account account, Bundle options)
        throws NetworkErrorException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
        throws NetworkErrorException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
        throws NetworkErrorException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
            Account account, String[] features)
        throws NetworkErrorException
    {
        throw new UnsupportedOperationException();
    }

    public SunshineAuthenticator(Context context) {
        super(context);
    }
}
