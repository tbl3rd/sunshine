package com.example.android.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class DetailFragment extends Fragment {

    private String mForecast = "";

    private Intent getShareWeatherIntent() {
        final Intent result = new Intent(Intent.ACTION_SEND);
        final String name = getString(R.string.app_name);
        result.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        result.setType("text/plain");
        result.putExtra(Intent.EXTRA_TEXT, mForecast + " #" + name);
        return result;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflater.inflate(R.menu.detail, menu);
        final MenuItem mi = menu.findItem(R.id.action_share);
        final ActionProvider ap = MenuItemCompat.getActionProvider(mi);
        if (ap == null) {
            final String no = getString(R.string.action_share_none);
            Toast.makeText(getActivity(), no, Toast.LENGTH_SHORT).show();
        } else {
            final ShareActionProvider sap = (ShareActionProvider)ap;
            sap.setShareIntent(getShareWeatherIntent());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView
            = inflater.inflate(R.layout.fragment_detail, container, false);
        final TextView tv
            = (TextView)rootView.findViewById(R.id.textview_detail);
        final Intent intent = getActivity().getIntent();
        if (intent != null) {
            final Bundle extras = intent.getExtras();
            if (extras != null) {
                final String weather = extras.getString(Intent.EXTRA_TEXT);
                if (weather != null) {
                    mForecast = weather;
                    tv.setText(mForecast);
                }
            }
        }
        return rootView;
    }

    public DetailFragment() {
        super();
        setHasOptionsMenu(true);
    }
}
