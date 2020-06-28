package com.iview.advertisinglogo.camera;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;

import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.hikvision.netsdk.RealPlayCallBack;
import com.iview.advertisinglogo.IDataCallback;
import com.iview.advertisinglogo.IStateCallback;
import com.iview.advertisinglogo.camera.AdCamera;
import com.iview.advertisinglogo.utils.ImageUtils;

import org.MediaPlayer.PlayM4.Player;
import org.MediaPlayer.PlayM4.PlayerCallBack;

import static com.hikvision.netsdk.HCNetSDK.NET_DVR_STREAMDATA;
import static com.hikvision.netsdk.HCNetSDK.NET_DVR_SYSHEAD;
import static org.MediaPlayer.PlayM4.Constants.T_YV12;


public class HIKvisionCamera extends AdCamera {
    private final static String TAG = "HIKvisionManager";

    private final static int MSG_LOGIN = 0;

    private NET_DVR_DEVICEINFO_V30 m_oNetDvrDeviceInfoV30 = null;

    private int m_iLogID = -1; // return by NET_DVR_Login_v30
    private int m_iPlayID = -1; // return by NET_DVR_RealPlay_V30
    private int m_iPlaybackID = -1; // return by NET_DVR_PlayBackByTime

    private int m_iPort = -1; // play port
    private int m_iStartChan = 0; // start channel no
    private int m_iChanNum = 0; // channel number

    private NetRealPlayCallBack netRealPlayCallBack;
    private NetPlayerDecodeCB netPlayerDecodeCB;

    //登录重试的次数. 海康摄像头上电到初始化完成需要一定的时间, 防止因为摄像头未初始化完成而登录失败.
    private int connectCount = 0;

    SurfaceHolder holder;
    Size previewSize = new Size(1280, 720);


    /**
     * @fn initeSdk
     * @author zhuzhenlei
     * @brief SDK init
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return true - success;false - fail
     */
    public boolean initeSdk() {
        // init net sdk
        if (!HCNetSDK.getInstance().NET_DVR_Init()) {
            Log.e(TAG, "HCNetSDK init is failed!");
            return false;
        }
        return true;
    }

    /**
     * @fn loginNormalDevice
     * @author zhuzhenlei
     * @brief login on device
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return login ID
     */
    public int loginNormalDevice() {
        // get instance
        m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
        if (null == m_oNetDvrDeviceInfoV30) {
            Log.e(TAG, "HKNetDvrDeviceInfoV30 new is failed!");
            return -1;
        }
        String strIP = "192.168.0.100";
        int nPort = 8000;
        String strUser = "admin";
        String strPsd = "123ABCabc";

        // call NET_DVR_Login_v30 to login on, port 8000 as default
        int iLogID = HCNetSDK.getInstance().NET_DVR_Login_V30(strIP, nPort,
                strUser, strPsd, m_oNetDvrDeviceInfoV30);
        if (iLogID < 0) {
            Log.e(TAG, "NET_DVR_Login is failed!Err:"
                    + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return -1;
        }
        if (m_oNetDvrDeviceInfoV30.byChanNum > 0) {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartChan;
            m_iChanNum = m_oNetDvrDeviceInfoV30.byChanNum;
        } else if (m_oNetDvrDeviceInfoV30.byIPChanNum > 0) {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartDChan;
            m_iChanNum = 1/*m_oNetDvrDeviceInfoV30.byIPChanNum
                    + m_oNetDvrDeviceInfoV30.byHighDChanNum * 256*/;
        }

//        if (m_iChanNum > 1) {
//            ChangeSingleSurFace(false);
//        } else {
//            ChangeSingleSurFace(true);
//        }
        Log.i(TAG, "NET_DVR_Login is Successful!");

        return iLogID;
    }

    /**
     * @fn loginDevice
     * @author zhangqing
     * @brief login on device
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return login ID
     */
    public int loginDevice() {
        m_iLogID = loginNormalDevice();

        return m_iLogID;
    }


    //获取到数据后的调用海康播放器的回调
    public class NetRealPlayCallBack implements RealPlayCallBack {

        @Override
        public void fRealDataCallBack(int i, int datatyep, byte[] bytes, int dwBuffSize) {
            //RealHandle
            switch (datatyep) {
                case NET_DVR_SYSHEAD:
                    m_iPort = Player.getInstance().getPort();
                    if (m_iPort < 0) {
                        Log.e(TAG, "get valid port fail");
                        break;
                    }

                    //dwBufSize
                    if (dwBuffSize > 0) {

                        if (!Player.getInstance().openStream(m_iPort, bytes, dwBuffSize, previewSize.getWidth() * previewSize.getHeight())) {
                            Log.e(TAG, "openStream failed with error code:"
                                    + Player.getInstance().getLastError(m_iPort));
                            break;
                        }

//                        if (!Player.getInstance().setHardDecode(m_iPort, 1)) {
//                            Log.e(TAG, "setHardDecode failed with error code:"
//                                    + Player.getInstance().getLastError(m_iPort));
//                            break;
//                        }

                        if (!Player.getInstance().setDecodeCB(m_iPort, netPlayerDecodeCB)) {
                            Log.e(TAG, "setDecodeCB failed with error code:"
                                    + Player.getInstance().getLastError(m_iPort));
                            break;
                        }

                        if (!Player.getInstance().play(m_iPort, null)) {
                            Log.e(TAG, "play failed with error code:"
                                    + Player.getInstance().getLastError(m_iPort));
                            break;
                        }

                        Log.e(TAG, "start player success");
                    }
                    break;
                case NET_DVR_STREAMDATA:
                    if (dwBuffSize > 0 && m_iPort != -1) {
                        boolean bInData = Player.getInstance().inputData(m_iPort, bytes, dwBuffSize);

                    }
                    break;
            }
        }
    }


    //海康播放器解码后的回调
    public class NetPlayerDecodeCB implements PlayerCallBack.PlayerDecodeCB {

        @Override
        public void onDecode(int nPort, byte[] data, int nDataLen, int nWidth,
                             int nHeight, int nFrameTime, int nDataType, int Reserved) {
     //       Log.d(TAG, "on Decode: width:" + nWidth + ", height:" + nHeight);
            //yv12 image arranged in “Y0-Y1-......””V0-V1....””U0-U1-.....” format
            synchronized (this) {
                if (nDataType == T_YV12) {
                    if (dataCallback != null) {
                        dataCallback.onDataCallback(data, ImageUtils.YV12, nWidth, nHeight);
                    }
                }
            }
        }

    }


    @Override
    public void init(IStateCallback callback, Context context) {
        super.init(callback, context);

        if (cameraState != STATE_IDLE) {
            stateCallback.onError(ERROR_STATE_ILLEGAL);
            return;
        }

        if (!initeSdk()) {
            Log.e(TAG, "HIKVision SDKinit fail !!!!!!!!!!!");
            stateCallback.onError(ERROR_INIT_FAIL);
            return;
        }

        netRealPlayCallBack = new NetRealPlayCallBack();
        netPlayerDecodeCB = new NetPlayerDecodeCB();

        cameraState = STATE_INIT;
    }

    @Override
    public void open() {
        if (cameraState != STATE_INIT) {
            stateCallback.onError(ERROR_STATE_ILLEGAL);
            return;
        }

        m_iLogID = loginNormalDevice();
        if (m_iLogID < 0) {
            Log.e(TAG, "This device logins failed! Try login again in 3s ");
            handler.sendEmptyMessageDelayed(MSG_LOGIN, 3000);
        } else {
            cameraState = STATE_OPEN;
            stateCallback.onOpened();
        }
    }

    @Override
    public void startPreview(IDataCallback callback) {
        synchronized (this) {
            super.startPreview(callback);

            if (cameraState != STATE_OPEN) {
                stateCallback.onError(ERROR_STATE_ILLEGAL);
                return;
            }

            if (m_iPlaybackID >= 0) {
                Log.i(TAG, "Please stop palyback first");
                return;
            }

            Log.i(TAG, "m_iStartChan:" + m_iStartChan);

            NET_DVR_PREVIEWINFO previewInfo = new NET_DVR_PREVIEWINFO();
            previewInfo.lChannel = m_iStartChan;
            previewInfo.dwStreamType = 0; // substream
            previewInfo.bBlocked = 1;
            previewInfo.hHwnd = holder;
            previewInfo.byPreviewMode = 0;

            // HCNetSDK start preview
            // preview数据交由 海康播放器解码netRealPlayCallBack)
            m_iPlayID = HCNetSDK.getInstance().NET_DVR_RealPlay_V40(m_iLogID,
                    previewInfo, netRealPlayCallBack);
            if (m_iPlayID < 0) {
                Log.e(TAG, "NET_DVR_RealPlay is failed!Err:"
                        + HCNetSDK.getInstance().NET_DVR_GetLastError());
                return;
            }

            cameraState = STATE_PREVIEW;

            Log.i(TAG,"NetSdk Play sucess ***********************3***************************");
        }
    }

    @Override
    public void stopPreview() {
        synchronized (this) {
            super.stopPreview();

            if (cameraState != STATE_PREVIEW) {
                stateCallback.onError(ERROR_STATE_ILLEGAL);
                return;
            }

            if (m_iPlayID < 0) {
                Log.e(TAG, "m_iPlayID < 0");
                return;
            }

            // net sdk stop preview
            if (!HCNetSDK.getInstance().NET_DVR_StopRealPlay(m_iPlayID)) {
                Log.e(TAG, "StopRealPlay is failed!Err:"
                        + HCNetSDK.getInstance().NET_DVR_GetLastError());
                return;
            }

            m_iPlayID = -1;

            cameraState = STATE_OPEN;
        }
    }


    @Override
    public void setSurface(SurfaceHolder holder) {
        this.holder = holder;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {      //判断标志位
                case MSG_LOGIN:
                    if (connectCount < 3) {
                        m_iLogID = loginNormalDevice();
                        if (m_iLogID < 0) {
                            Log.e(TAG, "This device logins failed! Try login again in 3s ");
                            handler.sendEmptyMessageDelayed(MSG_LOGIN, 3000);
                        } else {
                            cameraState = STATE_OPEN;
                            stateCallback.onOpened();
                        }
                    } else {
                        stateCallback.onError(ERROR_LOGIN_FAIL);
                    }

                    break;
            }
        }
    };

    @Override
    public void close() {
        if (cameraState != STATE_OPEN) {
            stateCallback.onError(ERROR_STATE_ILLEGAL);
            return;
        }

        if (m_iLogID != -1) {
            if (!HCNetSDK.getInstance().NET_DVR_Logout_V30(m_iLogID)) {
                Log.e(TAG, " NET_DVR_Logout is failed!");
                return;
            }
            m_iLogID = -1;
        }

        cameraState = STATE_INIT;
    }

    @Override
    public void release() {
        if (cameraState != STATE_INIT) {
            stateCallback.onError(ERROR_STATE_ILLEGAL);
            return;
        }
        HCNetSDK.getInstance().NET_DVR_Cleanup();
    }

    @Override
    public Size chooseOptimalSize(int desireWidth, int desireHeight) {

        // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
        // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
        // garbage capture data.
        Size[] outputSize = new Size[1];
        outputSize[0] = previewSize;
        mPreviewSize = chooseOptimalSize(outputSize,
                desireWidth, desireHeight);

        return mPreviewSize;
    }

}
