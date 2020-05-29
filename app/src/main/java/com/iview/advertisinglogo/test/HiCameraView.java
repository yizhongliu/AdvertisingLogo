package com.iview.advertisinglogo.test;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.iview.advertisinglogo.AdCamera;
import com.iview.advertisinglogo.AndroidCamera2;
import com.iview.advertisinglogo.HIKvisionCamera;
import com.iview.advertisinglogo.IStateCallback;
import com.iview.advertisinglogo.R;

public class HiCameraView extends Activity{
    private final static String TAG = "HiCameraView";

    AdCamera adCamera;
    CameraStateCallback cameraStateCallback;

    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        adCamera.stopPreview();
        adCamera.close();
        adCamera.release();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            //      | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public void initView() {
        surfaceView = findViewById(R.id.cameraSurface);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                Log.e(TAG, "Surface create");
                initCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
    }

    public void initCamera() {

        //采用屏幕的宽高
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        cameraStateCallback = new CameraStateCallback();
      //  adCamera = new HIKvisionCamera();
        adCamera = new AndroidCamera2();
        adCamera.init(cameraStateCallback, this);
        adCamera.open(width, height);
    }

    class CameraStateCallback implements IStateCallback {

        @Override
        public void onOpened() {
            Log.d(TAG, "onOpened");
            adCamera.setSurface(surfaceHolder);
            adCamera.startPreview(null);
        }

        @Override
        public void onError(int error) {

        }
    }
}
