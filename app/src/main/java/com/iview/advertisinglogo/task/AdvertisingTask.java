package com.iview.advertisinglogo.task;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.iview.advertisinglogo.hardware.MotorManager;


/*
* 业务逻辑
* 1 先展示广告图片
* 2 显示指示标示, 同时移动马达从A点到B点, 先移动垂直方向,再移动水平方向
* 3 到达B点后, 显示商品图片信息
* 4 隐藏图片,同时马达按原路径返回
* */
public class AdvertisingTask {
    private final static String TAG = "AdvertisingTask";

    TasksContract.View view;
    TasksContract.Presenter presenter;

    int step = 0;

    public AdvertisingTask(TasksContract.View view, TasksContract.Presenter presenter) {
        this.view = view;
        this.presenter = presenter;
    }

    public void reset() {
        step = 0;
        presenter.switchProjector(MotorManager.PROJECTOR_DISABLE);
    }

    public void start() {
        Log.d(TAG, "start");
        presenter.switchProjector(MotorManager.PROJECTOR_ENABLE);
        view.showAdvertisngImage();
//        step++;
//        mHandler.sendEmptyMessageDelayed(step, 10000);
    }

    public void runNextStep() {
        step++;
        Log.e(TAG, "runNextStep :" + step);
        mHandler.sendEmptyMessage(step);
    }

    public void runStep1() {
        Log.d(TAG, "runStep1");
        view.showIndicateImage();
    }

    public void runStep2() {
        Log.d(TAG, "runStep2");
        presenter.runForward();
    }
    public void runStep3() {
        Log.d(TAG, "runStep3");
        view.showProductImage();
    }

    public void runStep4() {
        Log.d(TAG, "runStep4");
        presenter.runBackward();
    }


    //新建Handler对象。
    Handler mHandler = new Handler(){

        //handleMessage为处理消息的方法
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    runStep1();
                    break;
                case 2:
                    runStep2();
                    break;
                case 3:
                    runStep3();
                    break;
                case 4:
                    runStep4();
                    break;
            }
        }
    };

}
