package com.example.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link ListView}.
 */
public class ForecastAdapter extends CursorAdapter
{
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context)
            .inflate(R.layout.list_item_forecast, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Read weather icon ID from cursor
        final int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);

        // Use placeholder image for now
        final ImageView iconView
            = (ImageView)view.findViewById(R.id.list_item_icon);
        iconView.setImageResource(R.drawable.ic_launcher);

        // Read date from cursor
        final String dateString
            = cursor.getString(ForecastFragment.COL_WEATHER_DATE);

        // Find TextView and set formatted date on it
        final TextView dateView
            = (TextView)view.findViewById(R.id.list_item_date_textview);
        dateView.setText(Utility.getFriendlyDayString(context, dateString));

        // Read weather forecast from cursor
        final String description
            = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        // Find TextView and set weather forecast on it
        final TextView descriptionView
            = (TextView)view.findViewById(R.id.list_item_forecast_textview);
        descriptionView.setText(description);

        // Read user preference for metric or imperial temperature units
        final boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        final float high
            = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);

        // TODO: Find TextView and set formatted high temperature on it

        // Read low temperature from cursor
        final float low
            = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);

        // TODO: Find TextView and set formatted low temperature on it
    }

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }
}
