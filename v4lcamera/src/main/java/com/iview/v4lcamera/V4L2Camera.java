package com.iview.v4lcamera;

public class V4L2Camera {
    private final static String TAG = "V4L2Camera";

    static {
        System.loadLibrary("v4l-android");
    }


}
