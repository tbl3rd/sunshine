package com.example.android.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class DetailFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView
            = inflater.inflate(R.layout.fragment_detail, container, false);
        final TextView tv
            = (TextView)rootView.findViewById(R.id.textview_detail);
        final Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            final String weather = extras.getString(Intent.EXTRA_TEXT);
            tv.setText(weather);
        }
        return rootView;
    }

    public DetailFragment() {}
}
