package com.example.android.sunshine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CompassView extends View
{
    private static final String TAG = CompassView.class.getSimpleName();

    private Canvas mCanvas;
    private float mSize = 200.0f;
    private int mDegrees;

    private static float stroke(float size) {
        return 1.0f + size / 100.0f;
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

    void drawOuter() {
        final float radius = CompassView.outerRadius(mSize);
        final float center = CompassView.center(mSize);
        final float strokeWidth = CompassView.stroke(mSize);
        final Paint fill = new Paint();
        final Paint outline = new Paint();
        fill.setColor(getResources().getColor(R.color.sunshine_light_blue));
        fill.setStyle(Paint.Style.FILL);
        mCanvas.drawCircle(center, center, radius, fill);
        outline.setColor(Color.GRAY);
        outline.setStyle(Paint.Style.STROKE);
        outline.setStrokeWidth(strokeWidth);
        mCanvas.drawCircle(center, center, radius, outline);
    }

    void drawNeedle() {
        final float center = CompassView.center(mSize);
        final float radius = CompassView.innerRadius(mSize);
        final Paint paint = new Paint();
        final Path path = new Path();
        final Matrix rotate = new Matrix();
        paint.setColor(getResources().getColor(R.color.sunshine_yellow));
        paint.setStyle(Paint.Style.FILL);
        path.moveTo(center, center - radius);
        path.lineTo(center + 10.0f, center);
        path.lineTo(center, center + 10.0f);
        path.lineTo(center - 10.0f, center);
        path.lineTo(center, center - radius);
        path.close();
        rotate.setRotate(mDegrees, center, center);
        path.transform(rotate);
        mCanvas.drawPath(path, paint);
    }

    void drawInner() {
        final float radius = CompassView.innerRadius(mSize);
        final float center = CompassView.center(mSize);
        final Paint fill = new Paint();
        fill.setColor(getResources().getColor(R.color.sunshine_blue));
        fill.setStyle(Paint.Style.FILL);
        mCanvas.drawCircle(center, center, radius, fill);
        drawNeedle();
    }

    void drawDirections() {
        final float center = CompassView.center(mSize);
        final float radius = CompassView.innerRadius(mSize);
        final float textSize = CompassView.textSize(mSize);
        final float textFudge = 1.0f + textSize / 4.0f;
        final float offset = radius + textSize / 2.0f;
        final String N = getResources().getString(R.string.direction_north);
        final String S = getResources().getString(R.string.direction_south);
        final String E = getResources().getString(R.string.direction_east);
        final String W = getResources().getString(R.string.direction_west);
        final Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.sunshine_red));
        paint.setTextSize(CompassView.textSize(mSize));
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        mCanvas.drawText(N, center, center - offset + textFudge, paint);
        mCanvas.drawText(S, center, center + offset + textFudge, paint);
        mCanvas.drawText(E, center + offset, center + textFudge, paint);
        mCanvas.drawText(W, center - offset, center + textFudge, paint);
    }

    public int setDirectionDegrees(int degrees) {
        Log.v(TAG, "setDirectionDegrees(): degrees == " + degrees);
        final int result = mDegrees;
        final int whatever = 3;
        final int compass = 360;
        mDegrees = (degrees + whatever * compass) % compass;
        if (result != mDegrees) invalidate();
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.v(TAG, "onDraw(): canvas == " + canvas);
        mCanvas = canvas;
        drawOuter();
        drawDirections();
        drawInner();
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
        Log.v(TAG, "onMeasure(): measuredWidth == " + measuredWidth);
        Log.v(TAG, "onMeasure(): measuredHeight == " + measuredHeight);
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
