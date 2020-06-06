package com.iview.advertisinglogo.task;

import android.content.Context;

import com.iview.advertisinglogo.base.BasePresenter;
import com.iview.advertisinglogo.base.BaseView;

public interface TasksContract {
    interface View extends BaseView<Presenter> {
        void showAdvertisngImage();
        void showIndicateImage();
        void showProductImage();
    }

    interface Presenter extends BasePresenter {
        void setViewContext(Context context);

        void showAdvertisngImageComplete();
        void showIndicateImageComplete();
        void showProductImageComplete();

        void runForward();
        void runBackward();

        boolean isTaskActive();

        void switchProjector(int enable);
    }
}
