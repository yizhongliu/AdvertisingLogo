package com.iview.advertisinglogo.setting;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.iview.advertisinglogo.R;
import com.iview.advertisinglogo.utils.StorageUtil;


public class SpeedSettingActivity extends Activity implements View.OnClickListener, View.OnFocusChangeListener {
    //public class ManagerActivity extends Activity {
    Button speedlowButton;
    Button speedmiddleButton;
    Button speedhighButton;

    TextView speedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed);

        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void initView() {
        speedlowButton = findViewById(R.id.speedlow);
        speedlowButton.setOnClickListener(this);
        speedlowButton.setOnFocusChangeListener(this);

        speedmiddleButton = findViewById(R.id.speedmiddle);
        speedmiddleButton.setOnClickListener(this);
        speedmiddleButton.setOnFocusChangeListener(this);

        speedhighButton = findViewById(R.id.speedhigh);
        speedhighButton.setOnClickListener(this);
        speedhighButton.setOnFocusChangeListener(this);

        speedText = findViewById(R.id.speedText);
        int speedIndex = StorageUtil.getSpeedIndex(this);
        if (speedIndex == 1) {
            speedText.setText("低速");
        } else if (speedIndex == 2) {
            speedText.setText("中速");
        } else if (speedIndex == 3) {
            speedText.setText("高速");
        }
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.speedlow:
                setSpeed(1);
                speedText.setText("低速");
                break;
            case R.id.speedmiddle:
                setSpeed(2);
                speedText.setText("中速");
                break;
            case R.id.speedhigh:
                setSpeed(3);
                speedText.setText("高速");
                break;
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

    public void setSpeed(int speed) {
        SharedPreferences sharedPreferences= getSharedPreferences("motorData", Context.MODE_PRIVATE);
        //步骤2： 实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("speed", speed);

        editor.commit();
    }

}
