package com.iview.advertisinglogo.objectdetect;

import android.content.Context;

import com.iview.advertisinglogo.IDetectCallback;
import com.iview.advertisinglogo.IStateCallback;

public class AdObjectDetect {

    public final static int ERROR_INIT_FAIL = -1;

    public Context mContext;

    public IDetectCallback detectCallback;
    public IStateCallback stateCallback;

    public void init(IStateCallback stateCallback, Context context) {
        this.stateCallback = stateCallback;
        mContext = context;
    }

    public void open(IDetectCallback detectCallback) {
        this.detectCallback = detectCallback;
    }

    public void sendImageData(byte[] data, int dataType, int width, int height) {

    }

    public void close() {

    }

    public void release() {

    }

}
