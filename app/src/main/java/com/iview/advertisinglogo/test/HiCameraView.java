package com.iview.advertisinglogo.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
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
import com.iview.advertisinglogo.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class HiCameraView extends Activity{
    private final static String TAG = "HiCameraView";

    private static final float TEXT_SIZE_DIP = 18;

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

    List<DetectResult> detectResultList = new ArrayList<>();

    private final Paint boxPaint = new Paint();

    private float textSizePx;
    private BorderedText borderedText;

    private Matrix frameToCanvasMatrix;

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
        overlayView.addCallback(new OverlayView.DrawCallback() {
            @Override
            public void drawCallback(Canvas canvas) {
                draw(canvas);
            }
        });


        boxPaint.setColor(Color.RED);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(12.0f);
        boxPaint.setStrokeCap(Paint.Cap.ROUND);
        boxPaint.setStrokeJoin(Paint.Join.ROUND);
        boxPaint.setStrokeMiter(100);

        textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
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
        adCamera = new HIKvisionCamera();
      //  adCamera = new AndroidCamera2();
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
         //   Log.e(TAG, "onDataCallbakck  dataType:" + dataType + ", width:" + width + ", height:" + height);
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
        public void onDetectResult(List<DetectResult> result) {
            detectResultList = result;
            Log.e(TAG, "onDetect result:" + detectResultList.size());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    overlayView.postInvalidate();
                }
            });

        }
    }

    public synchronized void draw(final Canvas canvas) {
        if (detectResultList == null) {
            return;
        }

        final boolean rotated = 0 % 180 == 90;
        final float multiplier =
                Math.min(canvas.getHeight() / (float) (rotated ? previewWidth : previewHeight),
                        canvas.getWidth() / (float) (rotated ? previewHeight : previewWidth));
        /*
         * camera preview size 会和 显示的坐标 转换?
         * */
        frameToCanvasMatrix =
                ImageUtils.getTransformationMatrix(
                        previewWidth,
                        previewHeight,
                        (int) (multiplier * (rotated ? previewHeight : previewWidth)),
                        (int) (multiplier * (rotated ? previewWidth : previewHeight)),
                        0,
                        false);

        for (final DetectResult recognition : detectResultList) {

            Log.e(TAG, "detect title" + recognition.getTitle());


            RectF trackedPos = recognition.getLocation();
            frameToCanvasMatrix.mapRect(trackedPos);
            final float cornerSize = Math.min(trackedPos.width(), trackedPos.height()) / 8.0f;
            canvas.drawRoundRect(trackedPos, cornerSize, cornerSize, boxPaint);

            final String labelString =
                    !TextUtils.isEmpty(recognition.getTitle())
                            ? String.format("%s %.2f", recognition.getTitle(), recognition.getConfidence())
                            : String.format("%.2f", recognition.getConfidence());
            borderedText.drawText(canvas, trackedPos.left + cornerSize, trackedPos.bottom, labelString);
        }
    }
}
