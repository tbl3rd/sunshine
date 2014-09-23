package com.example.android.sunshine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
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

    // The draw size of this view.
    //
    private static final int   SIZE        = 200;

    // Values derived from size.
    //
    private static final float CENTER      = SIZE /  2;
    private static final float TEXTSIZE    = SIZE / 10;
    private static final float NEEDLEBASE  = SIZE / 20;
    private static final float OUTERRADIUS = CENTER      * 90 / 100;
    private static final float INNERRADIUS = OUTERRADIUS * 75 / 100;
    private static final float STROKEWIDTH = 1 + SIZE     / 100;
    private static final float YTEXTFUDGE  = 1 + TEXTSIZE /   4;
    private static final float OFFSET      = INNERRADIUS + TEXTSIZE / 2;

    final Resources mResources = getContext().getResources();

    // Draw the 4 primary compass directions on c using r.
    //
    static void drawDirections(Canvas c, Resources r) {
        final String N = r.getString(R.string.direction_north);
        final String S = r.getString(R.string.direction_south);
        final String E = r.getString(R.string.direction_east);
        final String W = r.getString(R.string.direction_west);
        final Paint paint = new Paint();
        paint.setColor(r.getColor(R.color.sunshine_red));
        paint.setTextSize(TEXTSIZE);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        c.drawText(N, CENTER,          CENTER - OFFSET + YTEXTFUDGE, paint);
        c.drawText(S, CENTER,          CENTER + OFFSET + YTEXTFUDGE, paint);
        c.drawText(E, CENTER + OFFSET, CENTER + YTEXTFUDGE,          paint);
        c.drawText(W, CENTER - OFFSET, CENTER + YTEXTFUDGE,          paint);
    }

    // Return a bitmap of the outer ring of the compass using r.
    //
    static Bitmap newOuterBitmap(Resources r) {
        final Bitmap result
            = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        final Canvas c = new Canvas(result);
        final Paint fill = new Paint();
        final Paint line = new Paint();
        fill.setColor(r.getColor(R.color.sunshine_light_blue));
        fill.setStyle(Paint.Style.FILL);
        line.setColor(Color.GRAY);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(STROKEWIDTH);
        c.drawCircle(CENTER, CENTER, OUTERRADIUS, fill);
        c.drawCircle(CENTER, CENTER, OUTERRADIUS, line);
        drawDirections(c, r);
        return result;
    }
    final Bitmap mOuterBitmap = newOuterBitmap(mResources);
    final Paint  mOuterBitmapPaint = new Paint();

    // Return the draw path for a compass needle pointing north.
    //
    static Path newDrawNeedlePath() {
        final Path result = new Path();
        result.moveTo(CENTER,              CENTER - INNERRADIUS);
        result.lineTo(CENTER + NEEDLEBASE, CENTER              );
        result.lineTo(CENTER,              CENTER + NEEDLEBASE );
        result.lineTo(CENTER - NEEDLEBASE, CENTER              );
        result.lineTo(CENTER,              CENTER - INNERRADIUS);
        result.close();
        return result;
    }
    final Path mDrawNeedlePath = newDrawNeedlePath();

    static Paint newDrawNeedlePaint(Resources r) {
        final Paint result = new Paint();
        result.setColor(r.getColor(R.color.sunshine_yellow));
        result.setStyle(Paint.Style.FILL);
        return result;
    }
    final Paint mDrawNeedlePaint = newDrawNeedlePaint(mResources);

    final Matrix mDrawNeedleRotate = new Matrix();

    // Draw the compass needle on canvas pointing at degrees.
    //
    void drawNeedle(Canvas canvas, int degrees) {
        mDrawNeedleRotate.setRotate(degrees, CENTER, CENTER);
        mDrawNeedlePath.transform(mDrawNeedleRotate);
        canvas.drawPath(mDrawNeedlePath, mDrawNeedlePaint);
    }

    static Paint newDrawInnerPaint(Resources r) {
        final Paint result = new Paint();
        result.setColor(r.getColor(R.color.sunshine_blue));
        result.setStyle(Paint.Style.FILL);
        return result;
    }
    final Paint mDrawInnerPaint = newDrawInnerPaint(mResources);

    // Draw the inner circle of the compass with needle at degrees.
    //
    void drawInner(Canvas canvas, int degrees) {
        canvas.drawCircle(CENTER, CENTER, INNERRADIUS, mDrawInnerPaint);
        drawNeedle(canvas, degrees);
    }

    // Set the direction of the compass pointer to degrees.
    //
    private int mDegrees = 0;
    private boolean mDegreesSet = false;
    public int setDirectionDegrees(int degrees) {
        Log.v(TAG, "setDirectionDegrees(): degrees == " + degrees);
        final int result = mDegrees;
        final int whatever = 3;
        final int compass = 360;
        mDegrees = (degrees + whatever * compass) % compass;
        if (!mDegreesSet || result != mDegrees) {
            mDegreesSet = true;
            invalidate();
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.v(TAG, "onDraw(): canvas == " + canvas);
        canvas.drawBitmap(mOuterBitmap, 0, 0, mOuterBitmapPaint);
        if (mDegreesSet) drawInner(canvas, mDegrees);
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
