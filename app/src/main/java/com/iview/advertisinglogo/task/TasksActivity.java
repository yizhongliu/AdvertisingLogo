package com.iview.advertisinglogo.task;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.iview.advertisinglogo.R;
import com.iview.advertisinglogo.ZoomImageView;
import com.iview.advertisinglogo.hardware.MotorManager;
import com.iview.advertisinglogo.setting.ManagerActivity;
import com.iview.advertisinglogo.utils.StorageUtil;

import java.util.StringTokenizer;

import static android.widget.ImageView.ScaleType.FIT_XY;

public class TasksActivity extends Activity implements TasksContract.View {
    private final static String  TAG = "TasksActivity";

    private final static int MSG_SHOW_ADVERTISING_IMAGE_DONE = 0;
    private final static int MSG_SHOW_INDICATE_IMAGE_DONE = 1;
    private final static int MSG_SHOW_PRODUCT_IMAGE_DONE = 2;

    private TasksContract.Presenter presenter;

    private ZoomImageView zoomImageView;

    int currentSteps = 0;
    int scaleIndex = 6;
    float scalePoint = 0.0f;
    float xDeta = 0.02f;
    float xDeta2 = 0.02f;
    float yDeta = 0.02f;
    float yDeta2 = 0.0f;

    boolean bWarningShow = false;
    int[] warnImages = new int[15];
    int warnImageSize = 0;
    int warnImageIndex = 0;

    @Override
    public void setPresenter(TasksContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        initData();
        initView();
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
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        presenter = new TasksPresenter(this);
        presenter.setViewContext(this);
        presenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        presenter.stop();
    }

    private void initData() {
        warnImages[0] = R.drawable.indicate1;
        warnImages[1] = R.drawable.indicate2;
        warnImageSize = 2;
    }

    private void initView() {
        zoomImageView = findViewById(R.id.zoomImage);
    }

    @Override
    public void showAdvertisngImage() {
        Log.e(TAG, "showAdvertisngImage");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                zoomImageView.setImageResource(R.drawable.advertising);
                zoomImageView.setVisibility(View.VISIBLE);

                final int[] pointA = StorageUtil.getSharePointA(TasksActivity.this);
                final int[] pointB = StorageUtil.getSharePointB(TasksActivity.this);

                int currentSteps = MotorManager.getInstance(TasksActivity.this).getVmotorSteps();

                float scale;
                if (pointA[1] > pointB[1]) {
                    scale = (float) (currentSteps - pointB[1]) / (pointA[1] - pointB[1]);
                } else {
                    scale = (float) (currentSteps - pointA[1]) / (pointB[1] - pointA[1]);
                }

                float actScale = (float) 1 / (1 + scale / (scaleIndex + scalePoint));

                actScale = getWidthScale(actScale, scale, xDeta, xDeta2);
                Log.e(TAG, "actScale:" + actScale);
                zoomImageView.setScaleType(FIT_XY);
                zoomImageView.scaleImage(actScale, scale, yDeta, yDeta2);

                mHandler.sendEmptyMessageDelayed(MSG_SHOW_ADVERTISING_IMAGE_DONE, 10000);
            }
        });
    }

    @Override
    public void showIndicateImage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "showIndicateImage");
                zoomImageView.setImageResource(R.drawable.indicate1);
            }
        });


        if (!bWarningShow) {
            Log.e(TAG, "!bWarningShow");
            bWarningShow = true;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "onLoopStart running");

                    final int[] pointA = StorageUtil.getSharePointA(TasksActivity.this);
                    final int[] pointB = StorageUtil.getSharePointB(TasksActivity.this);

                    while (bWarningShow) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                int currentSteps = MotorManager.getInstance(TasksActivity.this).getVmotorSteps();

                                //Method1
                                float scale;
                                if (pointA[1] > pointB[1]) {
                                    scale = (float) (currentSteps - pointB[1]) / (pointA[1] - pointB[1]);
                                } else {
                                    scale = (float) (currentSteps - pointA[1]) / (pointB[1] - pointA[1]);
                                }

                                float actScale = (float) 1 / (1 + scale / (scaleIndex + scalePoint));

                                actScale = getWidthScale(actScale, scale, xDeta, xDeta2);
                                Log.e(TAG, "actScale:" + actScale);
                                zoomImageView.setScaleType(FIT_XY);
                                zoomImageView.scaleImage(actScale, scale, yDeta, yDeta2);

                                warnImageIndex = (++warnImageIndex) % warnImageSize;
                                zoomImageView.setImageResource(warnImages[warnImageIndex]);
                            }
                        });

                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }).start();
        }

        mHandler.sendEmptyMessage(MSG_SHOW_INDICATE_IMAGE_DONE);
    }

    @Override
    public void showProductImage() {
        Log.e(TAG, "showProductImage");
        bWarningShow = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                zoomImageView.setImageResource(R.drawable.advertising);

                mHandler.sendEmptyMessageDelayed(MSG_SHOW_PRODUCT_IMAGE_DONE, 10000);
            }
        });

    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_SHOW_ADVERTISING_IMAGE_DONE: {
                    presenter.showAdvertisngImageComplete();
                    break;
                }

                case MSG_SHOW_INDICATE_IMAGE_DONE: {
                    presenter.showIndicateImageComplete();
                    break;
                }

                case MSG_SHOW_PRODUCT_IMAGE_DONE: {
                    Log.d(TAG, "msg MSG_SHOW_PRODUCT_IMAGE_DONE");
                    zoomImageView.setVisibility(View.INVISIBLE);
                    presenter.showProductImageComplete();
                    break;
                }
            }

            return false;
        }
    });

    public float getWidthScale(float scale, float inputScale, float vDeta, float vDeta2) {
        float ret = 1;
        float deta = 1.0f;
        if (inputScale < 0.1f) {
            deta = 1.0f;
        } else if (inputScale < 0.2f) {
            deta =  1.0f + vDeta ;
        } else if (inputScale < 0.3f) {
            deta =  1.0f + vDeta * 1.1f;
        } else if (inputScale < 0.4f) {
            deta = 1.0f + vDeta * 1.2f;
        } else if (inputScale < 0.5f) {
            deta = 1.0f + vDeta * 1.4f + vDeta2;
        }else if (inputScale < 0.6f) {
            deta =  1.0f + vDeta * 1.6f + vDeta2 * 1.2f;
        } else if (inputScale < 0.7f) {
            deta =  1.0f + vDeta * 1.8f + vDeta2 * 1.6f;
        } else if (inputScale < 0.8f) {
            deta = 1.0f + vDeta * 2.0f + vDeta2 * 1.8f;
        } else if (inputScale < 0.9f) {
            deta = 1.0f + vDeta * 2.5f + vDeta2 * 2.2f;
        } else {
            deta = 1.0f + vDeta * 3.0f + vDeta2 * 2.8f;
        }

        ret = scale / deta;

        return ret;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                handleDpadCenter();
                break;

        }
        return super.onKeyDown(keyCode, event);
    }

    public void handleDpadCenter() {
        if (!presenter.isTaskActive()) {
            Intent intent = new Intent(this, ManagerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }
}

