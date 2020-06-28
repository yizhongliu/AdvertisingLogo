package com.iview.advertisinglogo.hardware;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.iview.advertisinglogo.utils.StorageUtil;
import com.iview.commonclient.CommonManager;
import com.iview.commonclient.OnCommonListener;
import java.util.ArrayList;

public class MotorManager {

    private final static String TAG = "MotorManager";

    public static final int MSG_MOTOR_INIT = 0;
    public static final int MSG_MOTOR_MOVE_POINT_A = 1;
    public static final int MSG_MOTOR_MOVE_REPEAT = 2;
    public static final int MSG_MOTOR_MOVE_POINT_A2B = 3;
    public static final int MSG_MOTOR_MOVE_POINT_B2A = 4;

    public static final int MSG_MOTOR_MOVE_POINT_A2C = 5;
    public static final int MSG_MOTOR_MOVE_POINT_C2D = 6;

    public static final int MSG_MOTOR_MOVE_POINT_D2C = 7;
    public static final int MSG_MOTOR_MOVE_POINT_C2A = 8;
    public static final int MSG_MOTOR_MOVE_POINT_D2A = 9;



    public static final int STATE_INIT = 0;
    public static final int STATE_POINTA = 1;
    public static final int STATE_POINTB = 2;
    public static final int STATE_RUNNING_A2B = 3;
    public static final int STATE_RUNNING_B2A = 4;

    public static final int STATE_POINTC = 5;
    public static final int STATE_POINTD = 6;

    private static final int VDELAY = 100;
    private static final int CHECK_PERSON_TIME = 100;

    public static final int PROJECTOR_DISABLE = 0;
    public static final int PROJECTOR_ENABLE = 1;

    private boolean bServiceConnect;
    private CommonManager commonManager;
    private CommonListener commonListener;

    //两个点的情况 A, B
    private final static int[] POINT_A = {124800, 5100}; // 第一个水平马达默认值, 的二个垂直马达默认值
    private final static int[] POINT_B = {124800, 38000}; // 第一个水平马达默认值, 的二个垂直马达默认值

    //三个点的情况 A,C,D
    private final static int[] POINT_C = {124800, 38000};
    private final static int[] POINT_D = {80000, 38000};

    int[] pointA = new int[2];
    int[] pointB = new int[2];
    int[] pointC = new int[2];
    int[] pointD = new int[2];

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private boolean bHMotorRunning = false;
    private boolean bVMotorRunning = false;

    private int motorRunningState = STATE_INIT;

    Context context;
    private static MotorManager sMotorManager;


    int hSpeed = 80;
    int vSpeed = 300;

    int hInitSpeed = 20;
    int vInitSpeed = 100;


    private ArrayList<MotorStateCallback> stateCallbacks = new ArrayList<>();

    public MotorManager(Context context) {
        this.context = context;
    }

    public static MotorManager getInstance(Context context) {
        if (sMotorManager == null) {
            synchronized (MotorManager.class) {
                if (sMotorManager == null) {
                    sMotorManager = new MotorManager(context.getApplicationContext());
                }
            }
        }

        return sMotorManager;
    }



    private class CommonListener implements OnCommonListener {

        @Override
        public void onServiceConnect() {
            bServiceConnect = true;
            Log.e(TAG, "onServiceConnect");

            commonManager.setProjectorMode(1);
            executeMotorMove();
        }

        @Override
        public void onMotorStop(int i) {
            Log.e(TAG, "onMotorStop motor:" + i + ",hmotor running:" + bHMotorRunning + ",vmotor running:" + bVMotorRunning + ", state:" + motorRunningState);
            switch (i) {
                case CommonManager.HMotor:
                    bHMotorRunning = false;
                    break;
                case CommonManager.VMotor:
                    bVMotorRunning = false;
                    break;
            }

            if (bHMotorRunning == false && bVMotorRunning == false) {
                if (motorRunningState == STATE_RUNNING_A2B) {
                    setMotorRunningState(STATE_POINTB);

                } else if (motorRunningState == STATE_RUNNING_B2A) {
                    setMotorRunningState(STATE_POINTA);
                }
            }
        }
    }

    public void executeMotorMove() {
        mHandlerThread = new HandlerThread("MediaServiceHandler");
        mHandlerThread.start();

        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_MOTOR_INIT:
                        setMotorRunningState(STATE_INIT);

                        commonManager.controlMotor(CommonManager.HMotor, 1000000, CommonManager.HMotorLeftDirection, hInitSpeed, true);
                        //FIXME: unnormal step.防止马达卡在上限位下不来
                   //     commonManager.controlMotor(CommonManager.VMotor, 6000, CommonManager.VMotorUpDirection, vInitSpeed, false);
                        commonManager.controlMotor(CommonManager.VMotor, 100, CommonManager.VMotorUpDirection, vInitSpeed, true);

                        commonManager.controlMotor(CommonManager.VMotor, 1000000, CommonManager.VMotorDownDirection, vInitSpeed, true);

                        pointA = getSharePointA();
                        pointB = getSharePointB();
                        pointC = getSharePointC();
                        pointD = getSharePointD();

                        Log.e(TAG, "pointA h:" + pointA[0] + " , v:" + pointA[1]);
                        Log.e(TAG, "pointB h:" + pointB[0] + " , v:" + pointB[1]);
                        mHandler.sendEmptyMessage(MSG_MOTOR_MOVE_POINT_A);
                        break;
                    case MSG_MOTOR_MOVE_POINT_A:
                        Log.e(TAG, "MSG_MOTOR_MOVE_POINT_A");
                        commonManager.controlMotor(CommonManager.HMotor, pointA[0], CommonManager.HMotorRightDirection, hInitSpeed, false);
                        commonManager.controlMotor(CommonManager.VMotor, pointA[1], CommonManager.VMotorUpDirection, vInitSpeed, false);

                        setMotorRunningState(STATE_POINTA);
                        break;
                    case MSG_MOTOR_MOVE_POINT_A2B:
                        moveMotorA2B();
                        break;
                    case MSG_MOTOR_MOVE_POINT_B2A:
                        moveMotorB2A();
                        break;
                    case MSG_MOTOR_MOVE_REPEAT:
                        int dir = CommonManager.HMotorRightDirection;
                        int hSteps;
                        int vSteps;
                        if (motorRunningState == STATE_POINTA) {
                            //判断水平马达
                            if (pointB[0] > pointA[0]) {
                                dir = CommonManager.HMotorRightDirection;
                            } else if (pointB[0] < pointA[0]) {
                                dir = CommonManager.HMotorLeftDirection;
                            }

                            hSteps = Math.abs(pointB[0] - pointA[0]);
                            if (hSteps != 0) {
                                Log.e(TAG, "pointA move h steps:" + hSteps + ",dir:" + dir);
                                bHMotorRunning = true;
                                commonManager.controlMotorAsync(CommonManager.HMotor, hSteps, dir, hSpeed, false);
                            }

                            //判断垂直马达
                            if (pointB[1] > pointA[1]) {
                                dir = CommonManager.VMotorUpDirection;
                            } else if (pointB[1] < pointA[1]) {
                                dir = CommonManager.VMotorDownDirection;
                            }

                            vSteps = Math.abs(pointB[1] - pointA[1]);
                            if (vSteps != 0) {
                                bVMotorRunning = true;
                                Log.e(TAG, "pointA move v steps:" + vSteps + ",dir:" + dir);
                                commonManager.controlMotorAsync(CommonManager.VMotor, vSteps, dir, vSpeed, false);
                            }

                            setMotorRunningState(STATE_RUNNING_A2B);
                        } else if (motorRunningState == STATE_POINTB) {
                            //判断水平马达
                            if (pointA[0] > pointB[0]) {
                                dir = CommonManager.HMotorRightDirection;
                            } else if (pointA[0] < pointB[0]) {
                                dir = CommonManager.HMotorLeftDirection;
                            }

                            hSteps = Math.abs(pointB[0] - pointA[0]);
                            if (hSteps != 0) {
                                bHMotorRunning = true;
                                Log.e(TAG, "pointB move h steps:" + hSteps + ",dir:" + dir);
                                commonManager.controlMotorAsync(CommonManager.HMotor, hSteps, dir, hSpeed, false);
                            }

                            //判断垂直马达
                            if (pointA[1] > pointB[1]) {
                                dir = CommonManager.VMotorUpDirection;
                            } else if (pointA[1] < pointB[1]) {
                                dir = CommonManager.VMotorDownDirection;
                            }

                            vSteps = Math.abs(pointB[1] - pointA[1]);
                            if (vSteps != 0) {
                                bVMotorRunning = true;
                                Log.e(TAG, "pointB move v steps:" + vSteps + ",dir:" + dir + ", vSpeed:" + vSpeed);
                                commonManager.controlMotorAsync(CommonManager.VMotor, vSteps, dir, vSpeed, false);
                            }

                            setMotorRunningState(STATE_RUNNING_B2A);
                        }
                        break;

                    case MSG_MOTOR_MOVE_POINT_A2C:
                        moveMotorA2C();
                        break;
                    case MSG_MOTOR_MOVE_POINT_C2D:
                        moveMotorC2D();
                        break;
                    case MSG_MOTOR_MOVE_POINT_D2C:
                        moveMotorA2B();
                        break;
                    case MSG_MOTOR_MOVE_POINT_C2A:
                        moveMotorA2B();
                        break;
                    case MSG_MOTOR_MOVE_POINT_D2A:
                        moveMotorD2A();
                        break;
                }
            }
        };

        mHandler.sendEmptyMessage(MSG_MOTOR_INIT);
    }

    private int[] getSharePointA() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        int hsteps = sharedPreferences.getInt("pointAHstep", POINT_A[0]);
        int vsteps = sharedPreferences.getInt("pointAVstep", POINT_A[1]);

        int[] ret = {hsteps, vsteps};
        return ret;
    }

    private int[] getSharePointB() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        int hsteps = sharedPreferences.getInt("pointBHstep", POINT_B[0]);
        int vsteps = sharedPreferences.getInt("pointBVstep", POINT_B[1]);

        int[] ret = {hsteps, vsteps};
        return ret;
    }

    private int[] getSharePointC() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        int hsteps = sharedPreferences.getInt("pointCHstep", POINT_C[0]);
        int vsteps = sharedPreferences.getInt("pointCVstep", POINT_C[1]);

        int[] ret = {hsteps, vsteps};
        return ret;
    }

    private int[] getSharePointD() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("motorData", Context.MODE_PRIVATE);
        int hsteps = sharedPreferences.getInt("pointDHstep", POINT_D[0]);
        int vsteps = sharedPreferences.getInt("pointDVstep", POINT_D[1]);

        int[] ret = {hsteps, vsteps};
        return ret;
    }

    public void startMotorService() {
        int speedIndex = StorageUtil.getSpeedIndex(context);
        setMotorSpeed(speedIndex);

        bServiceConnect = false;
        commonManager = CommonManager.getInstance(context);
        commonListener = new CommonListener();
        commonManager.regisiterOnCommonListener(commonListener);
        commonManager.connect();
    }

    public void stopMotorService() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.getLooper().quit();
            mHandlerThread = null;
        }


        bServiceConnect = false;
        if (commonManager != null) {
            commonManager.unregisiterOnCommonListener(commonListener);
            commonManager.stopMotorRunning(CommonManager.HMotor);
            commonManager.stopMotorRunning(CommonManager.VMotor);
            commonManager.disconnect();
            commonManager = null;
        }
    }

    public int getVmotorSteps() {
        return commonManager.getMotorSteps(CommonManager.VMotor);
    }

    public void setMotorSpeed(int level) {
        switch (level) {
            case 1:
                hSpeed = 80;
                vSpeed = 300;
                break;
            case 2:
                hSpeed = 50;
                vSpeed = 150;
                break;
            case 3:
                hSpeed = 30;
                vSpeed = 75;
                break;
        }
    }

    public void regisiterOnMotorStateCallback(MotorStateCallback listener) {
        stateCallbacks.add(listener);
    }

    public void unregisiterOnMotorStateCallback(MotorStateCallback listener) {
        stateCallbacks.remove(listener);
    }

    public int getMotorRunningState() {
        return motorRunningState;
    }

    public void setMotorRunningState(int motorRunningState) {
        this.motorRunningState = motorRunningState;

        for (MotorStateCallback l : stateCallbacks) {
            l.onMotorStateChange(motorRunningState);
        }
    }

    public void sendHandleMsg(int msg) {
        mHandler.sendEmptyMessage(msg);
    }

    private void moveMotorA2B() {
        int dir;
        int vSteps;
        int hSteps;

        if (pointB[1] != pointA[1]) {
            //判断垂直马达
            if (pointA[1] > pointB[1]) {
                dir = CommonManager.VMotorDownDirection;
            } else  {
                dir = CommonManager.VMotorUpDirection;
            }

            vSteps = Math.abs(pointB[1] - pointA[1]);
            //bVMotorRunning = true;
            Log.e(TAG, "pointA move v steps:" + vSteps + ",dir:" + dir);
            commonManager.controlMotor(CommonManager.VMotor, vSteps, dir, vSpeed, false);

        }

        if (pointA[0] != pointB[0]) {
            if (pointA[0] > pointB[0]) {
                dir = CommonManager.HMotorLeftDirection;
            } else  {
                dir = CommonManager.HMotorRightDirection;
            }

            hSteps = Math.abs(pointB[0] - pointA[0]);

            //bHMotorRunning = true;
            Log.e(TAG, "pointB move h steps:" + hSteps + ",dir:" + dir);
            commonManager.controlMotor(CommonManager.HMotor, hSteps, dir, hSpeed, false);
        }


        setMotorRunningState(STATE_POINTB);
    }

    private void moveMotorB2A() {
        int dir;
        int vSteps;
        int hSteps;

        if (pointA[0] != pointB[0]) {
            if (pointA[0] > pointB[0]) {
                dir = CommonManager.HMotorRightDirection;
            } else {
                dir = CommonManager.HMotorLeftDirection;
            }

            hSteps = Math.abs(pointB[0] - pointA[0]);

            //bHMotorRunning = true;
            Log.e(TAG, "pointB move h steps:" + hSteps + ",dir:" + dir);
            commonManager.controlMotor(CommonManager.HMotor, hSteps, dir, hSpeed, false);
        }


        if (pointB[1] != pointA[1]) {
            //判断垂直马达
            if (pointA[1] > pointB[1]) {
                dir = CommonManager.VMotorUpDirection;
            } else  {
                dir = CommonManager.VMotorDownDirection;
            }

            vSteps = Math.abs(pointB[1] - pointA[1]);
            //bVMotorRunning = true;
            Log.e(TAG, "pointA move v steps:" + vSteps + ",dir:" + dir);
            commonManager.controlMotor(CommonManager.VMotor, vSteps, dir, vSpeed, false);

        }

        setMotorRunningState(STATE_POINTA);
    }

    private void moveMotorA2C() {
        runTwoPointRouting(pointA, pointC);
        setMotorRunningState(STATE_POINTC);
    }

    private void moveMotorC2D() {
        runTwoPointRouting(pointC, pointD);
        setMotorRunningState(STATE_POINTD);
    }

    private void moveMotorD2A() {
        runTwoPointRouting(pointD, pointA);
        setMotorRunningState(STATE_POINTA);
    }

    public void runTwoPointRouting(int[] src, int[] dst) {
        int dir;
        int vSteps;
        int hSteps;

        Log.e(TAG, "pointsrc h:" + src[0] + " , v:" + src[1]);
        Log.e(TAG, "pointdst h:" + dst[0] + " , v:" + dst[1]);
        if (src[1] != dst[1]) {
            //判断垂直马达
            if (src[1] > dst[1]) {
                dir = CommonManager.VMotorDownDirection;
            } else  {
                dir = CommonManager.VMotorUpDirection;
            }

            vSteps = Math.abs(dst[1] - src[1]);
            //bVMotorRunning = true;
            Log.e(TAG, "pointA move v steps:" + vSteps + ",dir:" + dir);
            commonManager.controlMotor(CommonManager.VMotor, vSteps, dir, vSpeed, false);

        }

        if (src[0] != dst[0]) {
            if (src[0] > dst[0]) {
                dir = CommonManager.HMotorLeftDirection;
            } else  {
                dir = CommonManager.HMotorRightDirection;
            }

            hSteps = Math.abs(dst[0] - src[0]);

            //bHMotorRunning = true;
            Log.e(TAG, "pointB move h steps:" + hSteps + ",dir:" + dir);
            commonManager.controlMotor(CommonManager.HMotor, hSteps, dir, hSpeed, false);
        }
    }

    public void switchProjector(int enable) {
        commonManager.switchProjector(enable);
    }
}
