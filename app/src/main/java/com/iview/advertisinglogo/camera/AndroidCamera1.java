package com.iview.advertisinglogo.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;

import com.iview.advertisinglogo.IDataCallback;
import com.iview.advertisinglogo.IStateCallback;
import com.iview.advertisinglogo.utils.ImageUtils;

import java.io.IOException;
import java.util.List;

public class AndroidCamera1 extends AdCamera {
    private final static String TAG = "AndroidCamera1";

    private int mCameraId;
    private Camera camera;
    private PreviewCallback previewCallback;

    @Override
    public void init(IStateCallback callback, Context context) {
        super.init(callback, context);

        //选择获取到的第一个camera
        mCameraId = chooseCamera();
        if (mCameraId == -1) {
            Log.e(TAG, "Can't find camera");
            callback.onError(ERROR_INIT_FAIL);
        }

        previewCallback = new PreviewCallback();
    }

    @Override
    public void open() {
        camera = Camera.open(mCameraId);

        if (camera != null) {
            stateCallback.onOpened();
        } else {
            stateCallback.onError(ERROR_OPEN_FAIL);
        }

    }

    @Override
    public void close() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**
     * 设置预览 Surface。
     */
    @Override
    public void setSurface(SurfaceHolder previewSurface) {
        if (camera != null && previewSurface != null) {
            try {
                camera.setPreviewDisplay(previewSurface);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开始预览。
     */
    @Override
    public void startPreview(IDataCallback callback) {
        super.startPreview(callback);

        if (camera != null) {
            camera.setPreviewCallback(previewCallback);
            camera.startPreview();
            Log.d(TAG, "startPreview() called");
        }
    }

    /**
     * 停止预览。
     */
    @Override
    public void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
            Log.d(TAG, "stopPreview() called");
        }
    }

    @Override
    public Size chooseOptimalSize(int desireWidth, int desireHeight) {
        Camera.Parameters parameters = camera.getParameters();

        List<Camera.Size> cameraSizes = parameters.getSupportedPreviewSizes();
        Size[] sizes = new Size[cameraSizes.size()];
        int i = 0;
        for (Camera.Size size : cameraSizes) {
            sizes[i++] = new Size(size.width, size.height);
        }
        mPreviewSize = chooseOptimalSize(sizes, desireWidth, desireHeight);
        parameters.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        camera.setParameters(parameters);

        return mPreviewSize;
    }



    private int chooseCamera() {
        Camera.CameraInfo ci = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            /*跟手机不同, 直接返回第一个camera*/
            return i;
        }
        return -1; // No camera found
    }

    private class PreviewCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // 在使用完 Buffer 之后记得回收复用。
            dataCallback.onDataCallback(data, ImageUtils.NV21, mPreviewSize.getWidth(), mPreviewSize.getHeight());
        }
    }
}
