package com.iview.advertisinglogo;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

public class ZoomImageView extends ImageView {

    private int actWidth;
    private int actHeight;

    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void initView() {

        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(dm);

        actWidth = dm.widthPixels;
        actHeight = dm.heightPixels;
    }

    /**
     * 进行缩放
     * @param scale
     */
    public void scaleImage(float scale, float inputScale, float hDeta, float yDeta2) {
        final int width = actWidth;
        final int newWidth = (int) (width * scale);
        final int height = actHeight;
        final int newHeight = (int) (height * getHeightScale(scale, inputScale, hDeta, yDeta2));
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = newHeight;
        params.width = newWidth;

        Log.e("scale", "scale:" + scale  + ",inputScale:" + inputScale);
        Log.e("scale", "height:" + params.height  + ", width:" + params.width);
        setLayoutParams(params);
    }


    public float getHeightScale(float scale, float inputScale, float hDeta, float yDeta2) {
        float ret = 1;
        float deta = 1.0f;
        if (inputScale < 0.1f) {
            deta = 1.0f;
        } else if (inputScale < 0.2f) {
            deta =  1.0f + hDeta ;
        } else if (inputScale < 0.3f) {
            deta =  1.0f + hDeta * 1.1f;
        } else if (inputScale < 0.4f) {
            deta = 1.0f + hDeta * 1.2f;
        } else if (inputScale < 0.5f) {
            deta = 1.0f + hDeta * 1.4f + yDeta2;
        }else if (inputScale < 0.6f) {
            deta =  1.0f + hDeta * 1.6f + yDeta2 * 1.2f;
        } else if (inputScale < 0.7f) {
            deta =  1.0f + hDeta * 1.8f + yDeta2 * 1.6f;
        } else if (inputScale < 0.8f) {
            deta = 1.0f + hDeta * 2.0f + yDeta2 * 1.8f;
        } else if (inputScale < 0.9f) {
            deta = 1.0f + hDeta * 2.5f + yDeta2 * 2.2f;
        } else {
            deta = 1.0f + hDeta * 3.0f + yDeta2 * 3.0f;
        }

        ret = scale / deta;

        return ret;
    }

    public void scaleImage2(float xScale, float yScale) {
        final int width = actWidth;
        final int newWidth = (int) (width * xScale);
        final int height = actHeight;
        final int newHeight = (int) (height * yScale);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = newHeight;
        params.width = newWidth;

        Log.e("scale", "xScale:" + xScale  + ",yScale:" + yScale);
        Log.e("scale", "height:" + params.height  + ", width:" + params.width);
        setLayoutParams(params);
    }

    public void reset() {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = actHeight;
        params.width = actWidth;
        setLayoutParams(params);
    }
}
