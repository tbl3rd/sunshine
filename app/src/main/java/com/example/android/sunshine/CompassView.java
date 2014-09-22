package com.example.android.sunshine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CompassView extends View
{
    private static final String TAG = CompassView.class.getSimpleName();

    private static float stroke(float size) {
        return size / 100.0f;
    }

    private static float center(float size) {
        return size / 2.0f;
    }

    private static float outerRadius(float size) {
        return CompassView.center(size) - 10.0f;
    }

    private static float innerRadius(float size) {
        return 0.75f * CompassView.outerRadius(size);
    }

    private static float textSize(float size) {
        return size / 10.0f;
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        Log.v(TAG, "onDraw(): c == " + c);
        final float size = 200.0f;
        final float center = CompassView.center(size);
        final float strokeWidth = CompassView.stroke(size);
        final float outerRadius = CompassView.outerRadius(size);
        final float innerRadius = CompassView.innerRadius(size);
        final Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(strokeWidth);
        p.setColor(Color.BLACK);
        c.drawCircle(center, center, outerRadius, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(getResources().getColor(R.color.sunshine_blue));
        c.drawCircle(center, center, innerRadius, p);
        final Paint t = new Paint();
        final float textSize = CompassView.textSize(size);
        final float textFudge = 1.0f + textSize / 4.0f;
        final float offset = innerRadius + textSize / 2.0f;
        t.setColor(getResources().getColor(R.color.sunshine_red));
        t.setTextSize(CompassView.textSize(size));
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextAlign(Paint.Align.CENTER);
        final String N = getResources().getString(R.string.direction_north);
        final String S = getResources().getString(R.string.direction_south);
        final String E = getResources().getString(R.string.direction_east);
        final String W = getResources().getString(R.string.direction_west);
        c.drawText(N, center, center - offset + textFudge, t);
        c.drawText(S, center, center + offset + textFudge, t);
        c.drawText(E, center + offset, center + textFudge, t);
        c.drawText(W, center - offset, center + textFudge, t);
        final Paint n = new Paint();
        n.setColor(getResources().getColor(R.color.sunshine_yellow));
        n.setStyle(Paint.Style.FILL);
        Path path = new Path();
        path.moveTo(center, center - innerRadius);
        path.lineTo(center + 10.0f, center);
        path.lineTo(center, center + 10.0f);
        path.lineTo(center - 10.0f, center);
        path.lineTo(center, center - innerRadius);
        path.close();
        c.drawPath(path, n);
   }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measuredWidth
            = (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY)
            ? MeasureSpec.getSize(widthMeasureSpec)
            : widthMeasureSpec;
        final int measuredHeight
            = (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY)
            ? MeasureSpec.getSize(heightMeasureSpec)
            : heightMeasureSpec;
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    public CompassView(Context context) {
        super(context);
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
