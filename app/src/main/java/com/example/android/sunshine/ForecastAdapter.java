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
        ((ImageView)view.findViewById(R.id.list_item_weather_icon))
            .setImageResource(R.drawable.ic_launcher);
        ((TextView)view.findViewById(R.id.list_item_date))
            .setText(Utility.getFriendlyDayString(context,
                            cursor.getString(Utility.COLUMN_DATE)));
        ((TextView)view.findViewById(R.id.list_item_description))
            .setText(cursor.getString(Utility.COLUMN_DESCRIPTION));
        ((TextView)view.findViewById(R.id.list_item_maximum))
            .setText(Utility.fromCelsius(context,
                            cursor.getDouble(Utility.COLUMN_MAXIMUM)));
        ((TextView)view.findViewById(R.id.list_item_minimum))
            .setText(Utility.fromCelsius(context,
                            cursor.getDouble(Utility.COLUMN_MINIMUM)));
    }

    public ForecastAdapter(Context context, Cursor cursor, boolean autoQuery)
    {
        super(context, cursor, autoQuery);
    }
}
