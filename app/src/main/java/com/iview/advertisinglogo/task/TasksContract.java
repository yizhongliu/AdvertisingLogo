package com.iview.advertisinglogo.task;

import android.content.Context;

import com.iview.advertisinglogo.base.BasePresenter;
import com.iview.advertisinglogo.base.BaseView;

public interface TasksContract {
    interface View extends BaseView<Presenter> {
        void showAdvertisngImage(boolean bRotate);
        void showIndicateImage(float angle, boolean stepNext);
        void showProductImage(boolean bRotate);
    }

    interface Presenter extends BasePresenter {
        void setViewContext(Context context);

        void showAdvertisngImageComplete();
        void showIndicateImageComplete();
        void showProductImageComplete();

        void runForward();
        void runBackward();

        void runFRouting1();
        void runFRouting2();
        void runBRouting1();
        void runBRouting2();

        boolean isTaskActive();

        void switchProjector(int enable);
    }
}
