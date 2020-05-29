package com.iview.advertisinglogo;

public class AdObjectDetect {

    IDetectCallback detectCallback;
    IStateCallback stateCallback;

    private void init(IStateCallback stateCallback) {
        this.stateCallback = stateCallback;
    }

    private void open(IDetectCallback detectCallback) {
        this.detectCallback = detectCallback;
    }

    private void sendImageData(byte[] data, int dataType, int width, int height) {

    }

    public void close() {

    }

    public void release() {

    }

}
