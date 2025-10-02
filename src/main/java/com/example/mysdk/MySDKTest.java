package com.example.mysdk;

import android.util.Log;

public class MySDKTest {
    private static final String TAG = "MySDKTest";
    private String test;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public void logHello() {
        Log.w(TAG, test);
    }
}
