package com.iview.advertisinglogo.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class StorageUtil {

    private final static int[] POINT_A = {124800, 5100}; // 第一个水平马达默认值, 的二个垂直马达默认值
    private final static int[] POINT_B = {124800, 38000}; // 第一个水平马达默认值, 的二个垂直马达默认值

    //三个点的情况 A,C,D
    private final static int[] POINT_C = {124800, 38000};
    private final static int[] POINT_D = {80000, 38000};

    public static int getImageIndex(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        int index = sharedPreferences.getInt("imageIndex", 2);

        return index;
    }

    public static int getSpeedIndex(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        int index = sharedPreferences.getInt("speed", 1);

        return index;
    }

    public static boolean getSurfaceShowFlag(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        boolean flag = sharedPreferences.getBoolean("surface", false);

        return flag;
    }

    public static void setSurfaceShowFlag(Context context, boolean flag) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("surface", flag);

        editor.commit();
    }

    public static int[] getSharePointA(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        int hsteps = sharedPreferences.getInt("pointAHstep", POINT_A[0]);
        int vsteps = sharedPreferences.getInt("pointAVstep", POINT_A[1]);

        int[] ret = {hsteps, vsteps};
        return ret;
    }

    public static int[] getSharePointB(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        int hsteps = sharedPreferences.getInt("pointBHstep", POINT_B[0]);
        int vsteps = sharedPreferences.getInt("pointBVstep", POINT_B[1]);

        int[] ret = {hsteps, vsteps};
        return ret;
    }

    public static int[] getSharePointC(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        int hsteps = sharedPreferences.getInt("pointCHstep", POINT_A[0]);
        int vsteps = sharedPreferences.getInt("pointCVstep", POINT_A[1]);

        int[] ret = {hsteps, vsteps};
        return ret;
    }

    public static int[] getSharePointD(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        int hsteps = sharedPreferences.getInt("pointDHstep", POINT_B[0]);
        int vsteps = sharedPreferences.getInt("pointDVstep", POINT_B[1]);

        int[] ret = {hsteps, vsteps};
        return ret;
    }

    public static int getScale(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        int ret = sharedPreferences.getInt("scale", 6);

        return ret;
    }

    public static void setScale(Context context, int scale) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("scale", scale);

        editor.commit();

    }

    public static float getScalePoint(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        float ret = sharedPreferences.getFloat("scalePoint", 0.0f);

        return ret;
    }

    public static void setScalePoint(Context context, float scale) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putFloat("scalePoint", scale);

        editor.commit();

    }

    public static float getDeta(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        float ret = sharedPreferences.getFloat("deta", 0.02f);

        return ret;
    }

    public static void setDeta(Context context, float deta) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putFloat("deta", deta);

        editor.commit();

    }

    public static float getDeta2(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        float ret = sharedPreferences.getFloat("deta2", 0.0f);

        return ret;
    }

    public static void setDeta2(Context context, float deta) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putFloat("deta2", deta);

        editor.commit();

    }


    public static float getXDeta(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        float ret = sharedPreferences.getFloat("xdeta", 0.02f);

        return ret;
    }

    public static void setXDeta(Context context, float deta) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putFloat("xdeta", deta);

        editor.commit();
    }

    public static float getXDeta2(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        float ret = sharedPreferences.getFloat("xdeta2", 0);

        return ret;
    }

    public static void setXDeta2(Context context, float deta) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putFloat("xdeta2", deta);

        editor.commit();
    }

    public static float getStartTheta(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        float ret = sharedPreferences.getFloat("StartTheta", 0.0f);

        return ret;
    }

    public static void setStartTheta(Context context, float deta) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putFloat("StartTheta", deta);

        editor.commit();

    }

    public static float getRotateDegree(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        float ret = sharedPreferences.getFloat("rotateDegree", 0.0f);

        return ret;
    }

    public static void setRotateDegree(Context context, float deta) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putFloat("rotateDegree", deta);

        editor.commit();

    }
}
