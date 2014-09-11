package com.example.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

// Expose a list of weather forecasts from a Cursor to a ListView.
//
public class ForecastAdapter extends CursorAdapter
{

    @Override
    public int getViewTypeCount() {
        final int VIEW_TYPE_COUNT = 2;
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        final int VIEW_TYPE_TODAY = 0;
        final int VIEW_TYPE_FUTURE = 1;
        return (position == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final int[] LAYOUT = {
            R.layout.list_item_today,
            R.layout.list_item_future
        };
        return LayoutInflater.from(context).inflate(
                LAYOUT[getItemViewType(cursor.getPosition())],
                parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((ImageView)view.findViewById(R.id.list_item_weather_icon))
            .setImageResource(R.drawable.ic_launcher);
        ((TextView)view.findViewById(R.id.list_item_date))
            .setText(Utility.friendlyDate(context,
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
