package com.example.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

// Expose a list of weather forecasts from a Cursor to a ListView.
//
public class ForecastAdapter extends CursorAdapter
{
    private static final String TAG = ForecastAdapter.class.getSimpleName();

    final int VIEW_TYPE_TODAY = 0;
    final int VIEW_TYPE_FUTURE = 1;

    private boolean mTwoPane;
    void setTwoPane(boolean twoPane) {
        mTwoPane = twoPane;
    }

    @Override
    public int getViewTypeCount() {
        final int VIEW_TYPE_COUNT = 2;
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (mTwoPane)      return VIEW_TYPE_FUTURE;
        if (position == 0) return VIEW_TYPE_TODAY;
        /**/               return VIEW_TYPE_FUTURE;
    }

    static class ViewHolder {
        final ImageView icon;
        final TextView date;
        final TextView description;
        final TextView maximum;
        final TextView minimum;
        ViewHolder(View v) {
            icon        = (ImageView)v.findViewById(R.id.list_icon);
            date        =  (TextView)v.findViewById(R.id.list_date);
            description =  (TextView)v.findViewById(R.id.list_description);
            maximum     =  (TextView)v.findViewById(R.id.list_maximum);
            minimum     =  (TextView)v.findViewById(R.id.list_minimum);
        }
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
        final boolean isMetric = Utility.isMetric(context);
        final int code = cursor.getInt(Utility.COLUMN_WEATHER_CODE);
        final int drawable
            = (VIEW_TYPE_TODAY == getItemViewType(cursor.getPosition()))
            ? Utility.weatherArt(code)
            : Utility.weatherIcon(code);
        vh.icon.setImageResource(drawable);
        vh.date.setText(Utility.friendlyDayDate(context,
                        cursor.getString(Utility.COLUMN_DATE)));
        vh.description.setText(
                cursor.getString(Utility.COLUMN_DESCRIPTION));
        vh.maximum.setText(Utility.formatCelsius(context, isMetric,
                        cursor.getDouble(Utility.COLUMN_MAXIMUM)));
        vh.minimum.setText(Utility.formatCelsius(context, isMetric,
                        cursor.getDouble(Utility.COLUMN_MINIMUM)));
    }

    public ForecastAdapter(Context context, Cursor cursor, int flags)
    {
        super(context, cursor, flags);
    }
}
