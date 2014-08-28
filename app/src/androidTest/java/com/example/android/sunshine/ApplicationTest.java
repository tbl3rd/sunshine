package com.example.android.sunshine;

import com.example.android.sunshine.data.WeatherDbHelper;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.test.ApplicationTestCase;
import android.util.Log;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">
 *     Testing Fundamentals
 * </a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    final static String LOG_TAG = ApplicationTest.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        Log.v(LOG_TAG, "testCreateDb()");
        mContext.deleteDatabase(WeatherDbHelper.DATABASE);
        final SQLiteDatabase db
            = new WeatherDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public ApplicationTest() { super(Application.class); }
}
