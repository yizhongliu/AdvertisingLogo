package com.iview.advertisinglogo.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.iview.advertisinglogo.R;
import com.iview.commonclient.CommonManager;

public class ManagerActivity extends Activity implements View.OnClickListener, View.OnFocusChangeListener {
    Button motorSettingButton;
    Button imageSwitchButton;
    Button speedSettingButton;
    Button surfaceSwitchButton;
    Button scaleButton;
    Button imageRotateButton;

    TextView versionText;


    boolean bSpecialKey = false;

    private CommonManager commonManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bSpecialKey) {
            commonManager.disconnect();
        }
    }

    private void initView() {

        motorSettingButton = findViewById(R.id.motorSettingButton);
        motorSettingButton.setOnClickListener(this);
        motorSettingButton.setOnFocusChangeListener(this);

        imageSwitchButton = findViewById(R.id.imageSwitchButton);
        imageSwitchButton.setOnClickListener(this);
        imageSwitchButton.setOnFocusChangeListener(this);

        speedSettingButton = findViewById(R.id.speedSettingButton);
        speedSettingButton.setOnClickListener(this);
        speedSettingButton.setOnFocusChangeListener(this);

        surfaceSwitchButton = findViewById(R.id.surfaceSwitchButton);
        surfaceSwitchButton.setOnClickListener(this);
        surfaceSwitchButton.setOnFocusChangeListener(this);

        scaleButton = findViewById(R.id.scaleButton);
        scaleButton.setOnClickListener(this);
        scaleButton.setOnFocusChangeListener(this);

        imageRotateButton = findViewById(R.id.imageRotateButton);
        imageRotateButton.setOnClickListener(this);
        imageRotateButton.setOnFocusChangeListener(this);

        versionText = findViewById(R.id.versionText);
        versionText.setText(getAppInfo());
        versionText.setTextColor(0xffff0000);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.motorSettingButton:
           //     Intent intent = new Intent(this, MotorSettingActivity.class);
                Intent intent = new Intent(this, MotorSetting3PActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
//            case R.id.imageSwitchButton:
//                Intent intent2 = new Intent(this, ImageSelectActivity.class);
//                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent2);
//                break;
            case R.id.speedSettingButton:
                Intent intent3 = new Intent(this, SpeedSettingActivity.class);
                intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent3);
                break;

            case R.id.surfaceSwitchButton:
                Intent cameraViewIntent = new Intent(this, CameraViewActivity.class);
                cameraViewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(cameraViewIntent);
                break;
//            case R.id.scaleButton:
//                Intent intent4 = new Intent(this, ScaleSettingActivity.class);
//                intent4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent4);
//                break;
//            case R.id.imageRotateButton:
//                Intent intent5 = new Intent(this, ImageRotateActivity.class);
//                intent5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent5);
//                break;

        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        Button btn = (Button)view;
        if(b){
            btn.setBackgroundColor(0xffff0000);
        }else {
            btn.setBackgroundColor(0xffffffff);
        }
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("testKey", "onKey:" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_UNKNOWN:
                if (bSpecialKey == false) {
                    bSpecialKey = true;
                    commonManager = CommonManager.getInstance(this);
                    commonManager.connect();
                }

                return true;
                //break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (bSpecialKey) {
                    commonManager.controlMotor(CommonManager.VMotor, 300, CommonManager.VMotorUpDirection, 100, false);
                }
                return true;
                //break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (bSpecialKey) {
                    commonManager.controlMotor(CommonManager.VMotor, 300, CommonManager.VMotorDownDirection, 100, false);
                }
                return true;
                //break;
        }

        return  super.onKeyDown(keyCode, event);
    }


//    @Override
//    public void onBackPressed() {
//        Log.e("testKey", "start mainactivity");
//     //   finish();
//        //     Toast.makeText(this, "按下了back键   onBackPressed()", Toast.LENGTH_SHORT).show();
//    }

    public String getAppInfo() {
        try {
            String pkName = this.getPackageName();
            String versionName = this.getPackageManager().getPackageInfo(
                    pkName, 0).versionName;

            return "Version: " + versionName;
        } catch (Exception e) {
        }
        return null;
    }

}
