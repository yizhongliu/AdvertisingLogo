package com.iview.advertisinglogo;

import android.content.Context;
import android.view.SurfaceHolder;

public class AdCamera {

    Context mContext;

    IStateCallback stateCallback;
    IDataCallback dataCallback;

    public final static int ERROR_INIT_FAIL = -1;
    public final static int ERROR_LOGIN_FAIL = -2;
    public final static int ERROR_STATE_ILLEGAL = -3;
    public final static int ERROR_CAPABILITY_UNSUPPORT = -4;
    public final static int ERROR_OPEN_FAIL = -5;
    public final static int ERROR_PREVIEW_FAIL = -6;

    public final static int STATE_IDLE = 0;
    public final static int STATE_INIT = 1;
    public final static int STATE_OPEN = 2;
    public final static int STATE_PREVIEW = 3;
    public final static int STATE_RELEASE = 4;

    int cameraState = STATE_IDLE;

    /*
     *  SDK初始化, 设置状态回调
     */

    public void init(IStateCallback callback, Context context) {
        stateCallback = callback;
        mContext = context;
    }

    /**
     *
     * @param width
     *            [in] preview的宽
     * @param height
     *            [in] preview的高
     */
    public void open(int width, int height) {

    }

    public void startPreview(IDataCallback callback) {
        dataCallback = callback;
    }

    public void stopPreview() {
    }

    public void setSurface(SurfaceHolder holder) {

    }

    public void close() {

    }

    /*
     *  释放SDK资源
     */
    public void release() {

    }
}
