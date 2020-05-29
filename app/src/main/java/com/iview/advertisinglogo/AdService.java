package com.iview.advertisinglogo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class AdService  extends Service {
    private final static String TAG = "AdService";

    MyBinder myBinder;

    AdCamera adCamera;
    CameraStateCallback cameraStateCallback;

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {
        public AdService getService() {
            return AdService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        myBinder = new MyBinder();
    }


    public void initCamera() {
        cameraStateCallback = new CameraStateCallback();
        adCamera = new HIKvisionCamera();
        adCamera.init(cameraStateCallback, this);
    }


    class CameraStateCallback  implements IStateCallback {

        @Override
        public void onOpened() {
      //      adCamera.open();
        }

        @Override
        public void onError(int error) {

        }
    }
}
