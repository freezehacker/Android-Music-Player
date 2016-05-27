package com.demo.vita.vitamusicplayer;

import android.util.Log;

/**
 * Created by sjk on 2016/3/19.
 */
public class MyLogger {

    public static final String TAG = "what";

    public static void log(String s) {
        if (s == null) {
            s = "'Null'";
        }
        Log.d(TAG, s);
        System.out.println("JK:" + s);
    }
}
