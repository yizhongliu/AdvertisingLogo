package com.iview.advertisinglogo.camera;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;

import com.iview.advertisinglogo.IDataCallback;
import com.iview.advertisinglogo.IStateCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdCamera {
    private final static String TAG = "AdCamera";

    /**
     * The camera preview size will be chosen to be the smallest frame by pixel size capable of
     * containing a DESIRED_SIZE x DESIRED_SIZE square.
     */
    public static final int MINIMUM_PREVIEW_SIZE = 320;


    /**
     * The {@link android.util.Size} of camera preview.
     */
    public Size mPreviewSize;

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

    Context mContext;

    IStateCallback stateCallback;
    IDataCallback dataCallback;

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
    public void open() {

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


    public Size chooseOptimalSize(int desireWidth, int desireHeight) {
        return null;
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the minimum of both, or an exact match if possible.
     *
     * @param choices The list of sizes that the camera supports for the intended output class
     * @param width The minimum desired width
     * @param height The minimum desired height
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    protected static Size chooseOptimalSize(final Size[] choices, final int width, final int height) {
        final int minSize = Math.max(Math.min(width, height), MINIMUM_PREVIEW_SIZE);
        final Size desiredSize = new Size(width, height);

        // Collect the supported resolutions that are at least as big as the preview Surface
        boolean exactSizeFound = false;
        final List<Size> bigEnough = new ArrayList<Size>();
        final List<Size> tooSmall = new ArrayList<Size>();
        for (final Size option : choices) {
            if (option.equals(desiredSize)) {
                // Set the size but don't return yet so that remaining sizes will still be logged.
                exactSizeFound = true;
            }

            if (option.getHeight() >= minSize && option.getWidth() >= minSize) {
                bigEnough.add(option);
            } else {
                tooSmall.add(option);
            }
        }

        Log.i(TAG, "Desired size: " + desiredSize + ", min size: " + minSize + "x" + minSize);
        Log.i(TAG, "Valid preview sizes: [" + TextUtils.join(", ", bigEnough) + "]");
        Log.i(TAG, "Rejected preview sizes: [" + TextUtils.join(", ", tooSmall) + "]");

        if (exactSizeFound) {
            Log.i(TAG,"Exact size match found.");
            return desiredSize;
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            final Size chosenSize = Collections.min(bigEnough, new CompareSizesByArea());
            Log.i(TAG, "Chosen size: " + chosenSize.getWidth() + "x" + chosenSize.getHeight());
            return chosenSize;
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }
}
