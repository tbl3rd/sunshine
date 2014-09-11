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
    static class ViewHolder {
        final ImageView icon;
        final TextView date;
        final TextView description;
        final TextView maximum;
        final TextView minimum;
        ViewHolder(View v) {
            icon = (ImageView)v.findViewById(R.id.list_item_weather_icon);
            date = (TextView)v.findViewById(R.id.list_item_date);
            description = (TextView)v.findViewById(R.id.list_item_description);
            maximum = (TextView)v.findViewById(R.id.list_item_maximum);
            minimum = (TextView)v.findViewById(R.id.list_item_minimum);
        }
    }

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
        final View result = LayoutInflater.from(context).inflate(
                LAYOUT[getItemViewType(cursor.getPosition())],
                parent, false);
        result.setTag(new ViewHolder(result));
        return result;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder vh = (ViewHolder)view.getTag();
        vh.icon.setImageResource(R.drawable.ic_launcher);
        vh.date.setText(Utility.friendlyDate(context,
                        cursor.getString(Utility.COLUMN_DATE)));
        vh.description.setText(cursor.getString(Utility.COLUMN_DESCRIPTION));
        vh.maximum.setText(Utility.fromCelsius(context,
                        cursor.getDouble(Utility.COLUMN_MAXIMUM)));
        vh.minimum.setText(Utility.fromCelsius(context,
                        cursor.getDouble(Utility.COLUMN_MINIMUM)));
    }

    public ForecastAdapter(Context context, Cursor cursor, boolean autoQuery)
    {
        super(context, cursor, autoQuery);
    }
}
