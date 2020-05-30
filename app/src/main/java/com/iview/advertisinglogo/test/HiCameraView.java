package com.iview.advertisinglogo.test;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.iview.advertisinglogo.AdCamera;
import com.iview.advertisinglogo.AdObjectDetect;
import com.iview.advertisinglogo.AndroidCamera2;
import com.iview.advertisinglogo.AutoFitSurfaceView;
import com.iview.advertisinglogo.DetectResult;
import com.iview.advertisinglogo.HIKvisionCamera;
import com.iview.advertisinglogo.IDataCallback;
import com.iview.advertisinglogo.IDetectCallback;
import com.iview.advertisinglogo.IStateCallback;
import com.iview.advertisinglogo.OverlayView;
import com.iview.advertisinglogo.R;
import com.iview.advertisinglogo.rkdetect.RkObjectDetect;

public class HiCameraView extends Activity{
    private final static String TAG = "HiCameraView";

    AdCamera adCamera;
    CameraStateCallback cameraStateCallback;
    CameraDataCallback cameraDataCallback;

    AutoFitSurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    private int previewWidth;
    private int previewHeight;

    OverlayView overlayView;

    AdObjectDetect objectDetect;
    DetectStateCallback detectStateCallback;
    DetectResultCallback detectResultCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initData();
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

    private void initData() {
        Log.e(TAG, "initData");
        //采用屏幕的宽高, 全屏显示
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(dm);

        previewWidth = dm.widthPixels;
        previewHeight = dm.heightPixels;


        initObjectDetect();
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

        overlayView = findViewById(R.id.overlayView);
    }

    public void initObjectDetect() {
        objectDetect = new RkObjectDetect();

        detectStateCallback = new DetectStateCallback();
        objectDetect.init(detectStateCallback, this);

        detectResultCallback = new DetectResultCallback();
        objectDetect.open(detectResultCallback);
    }

    public void initCamera() {

        cameraStateCallback = new CameraStateCallback();
      //  adCamera = new HIKvisionCamera();
        adCamera = new AndroidCamera2();
        adCamera.init(cameraStateCallback, this);
        adCamera.open();
    }

    class CameraStateCallback implements IStateCallback {

        @Override
        public void onOpened() {
            Log.d(TAG, "onOpened");

            Size chooseSize = adCamera.chooseOptimalSize(previewWidth, previewHeight);
            if (chooseSize != null) {
                previewWidth = chooseSize.getWidth();
                previewHeight = chooseSize.getHeight();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    surfaceView.setAspectRatio(previewWidth, previewHeight);
                    Log.e(TAG, "setAspectRatio width:" + previewWidth + ", height:" + previewHeight);
                }
            });


            adCamera.setSurface(surfaceHolder);

            cameraDataCallback = new CameraDataCallback();
            adCamera.startPreview(cameraDataCallback);
        }

        @Override
        public void onError(int error) {

        }
    }

    class CameraDataCallback implements IDataCallback {

        @Override
        public void onDataCallback(byte[] data, int dataType, int width, int height) {
            Log.e(TAG, "onDataCallbakck  dataType:" + dataType + ", width:" + width + ", height:" + height);
            objectDetect.sendImageData(data, dataType, width, height);
        }
    }

    class DetectStateCallback implements IStateCallback {

        @Override
        public void onOpened() {

        }

        @Override
        public void onError(int error) {
            Log.e(TAG, "DetectStateCallback onError:" + error);
            finish();
        }
    }

    class DetectResultCallback implements IDetectCallback {

        @Override
        public void onDetectResult(DetectResult result) {

        }
    }
}
