package com.iview.advertisinglogo.task;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.util.Size;

import com.iview.advertisinglogo.DetectResult;
import com.iview.advertisinglogo.IDataCallback;
import com.iview.advertisinglogo.IDetectCallback;
import com.iview.advertisinglogo.IStateCallback;
import com.iview.advertisinglogo.camera.AdCamera;
import com.iview.advertisinglogo.camera.HIKvisionCamera;
import com.iview.advertisinglogo.hardware.MotorManager;
import com.iview.advertisinglogo.hardware.MotorStateCallback;
import com.iview.advertisinglogo.objectdetect.AdObjectDetect;
import com.iview.advertisinglogo.objectdetect.rkdetect.RkObjectDetect;

import java.util.List;

public class TasksPresenter implements TasksContract.Presenter {
    private final static String TAG = "TasksPresente";

    private final static int CASE_TWO_POINT = 0;
    private final static int CASE_THREE_POINT = 1;

    private TasksContract.View view;
    private Context context;

    AdCamera adCamera;
    CameraStateCallback cameraStateCallback;
    CameraDataCallback cameraDataCallback;
    boolean bCameraOpen = false;

    AdObjectDetect objectDetect;
    DetectStateCallback detectStateCallback;
    DetectResultCallback detectResultCallback;
    boolean bObjectDetectOpen = false;

    MotorManager motorManager;
    OnMotorState onMotorState;
    boolean bMotorInit = false;


    boolean bTaskRunning = false;
    AdvertisingTask advertisingTask;

    int runningCase = CASE_THREE_POINT;

    public TasksPresenter(TasksContract.View view) {
        this.view = view;
        view.setPresenter(this);
        advertisingTask = new AdvertisingTask(view, this);
    }

    @Override
    public void start() {
        startObjectDetect();
        startCamera();
        startMotorManager();
    }

    @Override
    public void stop() {
        switchProjector(MotorManager.PROJECTOR_ENABLE);
        stopCamera();
        stopObjectDetect();
        stopMotorManager();
    }


    @Override
    public void setViewContext(Context context) {
        this.context = context;
    }

    @Override
    public void showAdvertisngImageComplete() {
        advertisingTask.runNextStep();
    }

    @Override
    public void showIndicateImageComplete() {
        advertisingTask.runNextStep();
    }

    @Override
    public void showProductImageComplete() {
        advertisingTask.runNextStep();
    }

    @Override
    public void runForward() {
        motorManager.sendHandleMsg(MotorManager.MSG_MOTOR_MOVE_POINT_A2B);
    }

    @Override
    public void runBackward() {
        switch (runningCase) {
            case CASE_TWO_POINT:
                motorManager.sendHandleMsg(MotorManager.MSG_MOTOR_MOVE_POINT_B2A);
                break;
            case CASE_THREE_POINT:
                motorManager.sendHandleMsg(MotorManager.MSG_MOTOR_MOVE_POINT_D2A);
                break;
        }
    }

    @Override
    public void runFRouting1() {
        motorManager.sendHandleMsg(MotorManager.MSG_MOTOR_MOVE_POINT_A2C);
    }

    @Override
    public void runFRouting2() {
        motorManager.sendHandleMsg(MotorManager.MSG_MOTOR_MOVE_POINT_C2D);
    }

    @Override
    public void runBRouting1() {

    }

    @Override
    public void runBRouting2() {

    }

    @Override
    public boolean isTaskActive() {
        return bTaskRunning;
    }

    @Override
    public void switchProjector(int enable) {
        motorManager.switchProjector(enable);
    }

    public void startObjectDetect() {
        objectDetect = new RkObjectDetect();

        detectStateCallback = new DetectStateCallback();
        objectDetect.init(detectStateCallback, context);

        detectResultCallback = new DetectResultCallback();
        objectDetect.open(detectResultCallback);
    }

    public void stopObjectDetect() {
        synchronized (TasksPresenter.class) {
            bObjectDetectOpen = false;
            objectDetect.close();
            objectDetect.release();
        }
    }

    public void startCamera() {
        cameraStateCallback = new CameraStateCallback();
        adCamera = new HIKvisionCamera();
        adCamera.init(cameraStateCallback, context);
        adCamera.open();
    }

    public void stopCamera() {
        bCameraOpen = false;
        adCamera.stopPreview();
        adCamera.close();
        adCamera.release();
    }

    public void startMotorManager() {
        motorManager = MotorManager.getInstance(context);
        onMotorState = new OnMotorState();
        motorManager.regisiterOnMotorStateCallback(onMotorState);
        motorManager.startMotorService();
    }

    public void stopMotorManager() {
        motorManager.unregisiterOnMotorStateCallback(onMotorState);
        motorManager.stopMotorService();

    }

    class CameraStateCallback implements IStateCallback {

        @Override
        public void onOpened() {
            Log.d(TAG, "onOpened");
            bCameraOpen = true;

            adCamera.setSurface(null);

            cameraDataCallback = new CameraDataCallback();
            adCamera.startPreview(cameraDataCallback);
        }

        @Override
        public void onError(int error) {
            Log.e(TAG, "CameraStateCallback onError :" + error);
            throw new RuntimeException();

        }
    }

    class CameraDataCallback implements IDataCallback {
        @Override
        public void onDataCallback(byte[] data, int dataType, int width, int height) {
            synchronized (TasksPresenter.class) {
                if (bObjectDetectOpen) {
                    objectDetect.sendImageData(data, dataType, width, height);
                }
            }
        }
    }

    class DetectStateCallback implements IStateCallback {
        @Override
        public void onOpened() {
            bObjectDetectOpen = true;
        }

        @Override
        public void onError(int error)  {
            Log.e(TAG, "DetectStateCallback onError:" + error);
            throw new RuntimeException();
        }
    }

    class DetectResultCallback implements IDetectCallback {

        @Override
        public void onDetectResult(List<DetectResult> result) {
            for (DetectResult result1 : result) {
                if (result1.getTitle().equals("person")) {
                    if ((!bTaskRunning)
                            && (motorManager.getMotorRunningState() == MotorManager.STATE_POINTA)) {
                        bTaskRunning = true;
                        advertisingTask.start();
                    }

                    break;
                }
            }
        }
    }

    public class OnMotorState implements MotorStateCallback {

        @Override
        public void onMotorStateChange(int state) {
            Log.e(TAG, "onMotorStateChange: " + state);
            switch (state) {
                case MotorManager.STATE_POINTA: {
                    if (bMotorInit = false) {
                        bMotorInit = true;
                    }

                    advertisingTask.reset();
                    bTaskRunning = false;
                }
                break;

                case MotorManager.STATE_POINTB: {
                    advertisingTask.runNextStep();
                }
                break;

                case MotorManager.STATE_POINTC: {
                    advertisingTask.runNextStep();
                }
                break;

                case MotorManager.STATE_POINTD: {
                    advertisingTask.runNextStep();
                }
                break;

                case MotorManager.STATE_RUNNING_A2B:
                case MotorManager.STATE_RUNNING_B2A: {
                }
                break;
            }
        }
    }
}
