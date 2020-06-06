package com.iview.advertisinglogo.setting;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.iview.advertisinglogo.R;
import com.iview.commonclient.CommonManager;
import com.iview.commonclient.OnCommonListener;

public class MotorSettingActivity extends Activity {
    private final static String TAG = "MotorSettingActivity";

    private boolean bServiceConnect;
    private CommonManager commonManager;
    private CommonListener commonListener;

    private TextView infoText;
    private SETTING_STATE state;

    public enum SETTING_STATE {
        INIT, POINT_A, POINT_B, END
    }

    int[] pointA = new int[2];
    int[] pointB = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motor_setting);

        initView();
        initData();

        state = SETTING_STATE.INIT;
    }

    @Override
    protected void onPause() {
        super.onPause();

        bServiceConnect = false;
        if (commonManager != null) {
            commonManager.unregisiterOnCommonListener(commonListener);
            commonManager.disconnect();
            commonManager = null;
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");


    }

    private void initView() {
        infoText = findViewById(R.id.infoText);
      //  infoText.setText("INIT");
        infoText.setTextColor(0xffff0000);
    }

    public void initData() {
        bServiceConnect = false;
        commonManager = CommonManager.getInstance(this);
        commonListener = new CommonListener();
        commonManager.regisiterOnCommonListener(commonListener);
        commonManager.connect();
    }

    private class CommonListener implements OnCommonListener {

        @Override
        public void onServiceConnect() {
            bServiceConnect = true;
            Log.e(TAG, "onServiceConnect");

            new Thread(new Runnable() {
                @Override
                public void run() {
//                    commonManager.controlMotor(CommonManager.HMotor, 1000000, CommonManager.HMotorLeftDirection, 30, true);
//                    commonManager.controlMotor(CommonManager.VMotor, 1000000, CommonManager.VMotorDownDirection, 50, true);
                    state = SETTING_STATE.POINT_A;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            infoText.setText("一");
                            infoText.setTextColor(0xffff0000);
                        }
                    });
                }
            }).start();
        }

        @Override
        public void onMotorStop(int i) {
            Log.e(TAG, "onMotorStop");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (state == SETTING_STATE.POINT_A) {
                    int hsteps = commonManager.getMotorSteps(CommonManager.HMotor);
                    int vsteps = commonManager.getMotorSteps(CommonManager.VMotor);

                    if (vsteps < 100) {
                        Log.e(TAG, "vstep can't less than 100");
                        Toast.makeText(this, "第一巡航点不能设置太小", Toast.LENGTH_LONG).show();
                        break;
                    } else {
                        pointA[0] = hsteps;
                        pointA[1] = vsteps;

                        state = SETTING_STATE.POINT_B;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                infoText.setText("二");
                                infoText.setTextColor(0xffff0000);
                            }
                        });
                    }
                  //  setPointA(hsteps, vsteps);

                } else if (state == SETTING_STATE.POINT_B) {
                    int hsteps = commonManager.getMotorSteps(CommonManager.HMotor);
                    int vsteps = commonManager.getMotorSteps(CommonManager.VMotor);

                    if (Math.abs(vsteps - pointA[1]) < 300) {
                        Log.e(TAG, "vstep can't less than 100");
                        Toast.makeText(this, "两个巡航点不能相等", Toast.LENGTH_LONG).show();
                        break;
                    } else {
                        setPointA(pointA[0], pointA[1]);
                        setPointB(hsteps, vsteps);
                        state = SETTING_STATE.END;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                infoText.setText("DONE");
                                infoText.setTextColor(0xffff0000);
                            }
                        });
                    }


                }

                break;
//            case KeyEvent.KEYCODE_BACK:
//                Intent intent3 = new Intent(this, ManagerActivity.class);
//                intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent3);
//                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setPointA(int hsteps, int vsteps) {
        SharedPreferences sharedPreferences= getSharedPreferences("motorData", Context.MODE_PRIVATE);
        //步骤2： 实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("pointAHstep", hsteps);
        editor.putInt("pointAVstep",  vsteps);

        editor.commit();
    }

    public void setPointB(int hsteps, int vsteps) {
        SharedPreferences sharedPreferences= getSharedPreferences("motorData", Context.MODE_PRIVATE);
        //步骤2： 实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("pointBHstep", hsteps);
        editor.putInt("pointBVstep",  vsteps);

        editor.commit();
    }

}
