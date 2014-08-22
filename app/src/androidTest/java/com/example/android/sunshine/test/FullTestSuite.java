package com.example.android.sunshine.test;

import junit.framework.Test;
import android.test.suitebuilder.TestSuiteBuilder;
import android.util.Log;


public class FullTestSuite {

    final static String LOG_TAG = FullTestSuite.class.getSimpleName();

    public static Test suite() {
        Log.v(LOG_TAG, "Test.suite()");
        return new TestSuiteBuilder(FullTestSuite.class)
            .includeAllPackagesUnderHere().build();
    }

    public FullTestSuite() {
        super();
        Log.v(LOG_TAG, "FullTestSuite()");
    }
}
