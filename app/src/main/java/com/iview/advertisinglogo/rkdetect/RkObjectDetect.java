package com.iview.advertisinglogo.rkdetect;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;

import com.iview.advertisinglogo.AdObjectDetect;
import com.iview.advertisinglogo.DetectResult;
import com.iview.advertisinglogo.IDetectCallback;
import com.iview.advertisinglogo.IStateCallback;
import com.iview.advertisinglogo.rkdetect.Util.net.TCPClient.TCPClientCallback;
import com.iview.advertisinglogo.rkdetect.Util.net.TCPClient.TCPClientConnect;
import com.iview.advertisinglogo.utils.ImageUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static android.os.SystemClock.sleep;
import static org.opencv.core.CvType.CV_8UC1;

public class RkObjectDetect extends AdObjectDetect {
    private final static String TAG = "RkObjectDetect";

    private static final String[] CLASSES = {"person", "bicycle", "car", "motorbike ", "aeroplane ", "bus ", "train", "truck ", "boat", "traffic light",
            "fire hydrant", "stop sign ", "parking meter", "bench", "bird", "cat", "dog ", "horse ", "sheep", "cow",
            "elephant", "bear", "zebra ", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee",
            "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard",
            "tennis racket", "bottle", "wine glass", "cup", "fork", "knife ", "spoon", "bowl", "banana", "apple",
            "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza ", "donut", "cake", "chair", "sofa",
            "pottedplant", "bed", "diningtable", "toilet ", "tvmonitor", "laptop  ", "mouse    ", "remote ", "keyboard ",
            "cell phone", "microwave ", "oven ", "toaster", "sink", "refrigerator ", "book", "clock", "vase",
            "scissors ", "teddy bear ", "hair drier", "toothbrush "};

    private static boolean computingDetection = false;

    TCPClientConnect mBaseTcpClient;

    String ip = "192.168.180.8";
    int port = 8002;

    long lastDetectTime = System.currentTimeMillis();

    private static boolean bOpencvInit = false;

    int imageWidth;
    int imageHeight;

    static{
        if(OpenCVLoader.initDebug()){
           bOpencvInit = true;
           Log.e(TAG, "opencv init success");
        }
    }

    @Override
    public void init(IStateCallback stateCallback, Context context) {
        super.init(stateCallback, context);

        if (!bOpencvInit) {
            stateCallback.onError(ERROR_INIT_FAIL);
        }
    }

    @Override
    public void open(IDetectCallback detectCallback) {
        super.open(detectCallback);

        initTcp();
    }

    private void initTcp() {

        if (mBaseTcpClient == null) {
            mBaseTcpClient = new TCPClientConnect();
            mBaseTcpClient.setCallback(new TCPClientCallback() {
                @Override
                public void tcp_connected() {
                    Log.d(TAG, "tcp_connected: " + ip);
                }

                @Override
                public void tcp_receive(List<byte[]> buffer) {
                    List<Object[]> resultObject = new ArrayList<>();
                    try {
                        for (int i = 0; i < buffer.size(); i++) {
                            Object[] oo = ParseData.parse(buffer.get(i), true);
                            if (oo != null) {
                                resultObject.add(oo);
                            }
                        }

                        computingDetection = false;

                        detectCallback.onDetectResult(generateDetectResult(resultObject));

            //            cameraFrameBufferQueue.setDetectResult(generateDetectResult(resultObject));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    sleep(1);
                }

                @Override
                public void tcp_disconnect() {
                    Log.d(TAG, "tcp_disconnect: " + ip);
                }
            });
            mBaseTcpClient.setAddress(ip, port);
            mBaseTcpClient.setTimeOut(10000);
            new Thread(mBaseTcpClient).start();
        }
    }

    @Override
    public void sendImageData(byte[] data, int dataType, int width, int height) {

        if (dataType == ImageUtils.YV12) {
            //  width * height + width * height /4 + width * height / 4 = width * (height + height /2 )
            Mat mat = new Mat(height + height / 2, width, CV_8UC1);
            int re =  mat.put(0,0, data);
            final Mat dst = new Mat();
            Imgproc.cvtColor(mat , dst, Imgproc.COLOR_YUV2BGR_YV12);

            imageWidth = width;
            imageHeight = height;

            Log.d(TAG, "setImage width:" + imageWidth + "imageHeight :" + imageHeight);

            if (!computingDetection ||
                    ((System.currentTimeMillis() - lastDetectTime) > 10000)) {

                new Thread() {
                    @Override
                    public void run() {
                        computingDetection = true;
                        lastDetectTime = System.currentTimeMillis();

                        Imgproc.resize(dst, dst, new Size(416, 416));
                        byte[] sendData = setJpgData(dst);

                        mBaseTcpClient.write(sendData);
                    }
                }.start();


            }

        }
    }

    private byte[] setJpgData(Mat mat) {
        int len = 16;
        byte[] data = mat2Byte(mat, ".jpg");
        String str2 = String.format("%01$-" + len + "s", String.valueOf(data.length));
        byte[] jpgData_t = new byte[str2.getBytes().length + data.length];

        System.arraycopy(str2.getBytes(), 0, jpgData_t, 0, str2.getBytes().length);
        System.arraycopy(data, 0, jpgData_t, str2.getBytes().length, data.length);
        return jpgData_t;
    }

    /**
     * Mat转换成byte数组
     *
     * @param matrix        要转换的Mat
     * @param fileExtension 格式为 ".jpg", ".png", etc
     * @return
     */
    public static byte[] mat2Byte(Mat matrix, String fileExtension) {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(fileExtension, matrix, mob);
        byte[] byteArray = mob.toArray();
        return byteArray;
    }


    List<DetectResult> generateDetectResult(List<Object[]> resultObject) {
        List<DetectResult> detectResultList = new ArrayList<>();
//        Log.d(TAG, "generateDetectResult resultObject.size(): " + resultObject.size());
        if (resultObject.size() == 3) {
            Object[] boxes = resultObject.get(0);
            Object[] classes = resultObject.get(1);
            Object[] scores = resultObject.get(2);
            int[] boxesShare = (int[]) boxes[ParseData.SHAPE];
            int[] classesShare = (int[]) classes[ParseData.SHAPE];
            int[] scoresShare = (int[]) scores[ParseData.SHAPE];
            float[] boxesData = (float[]) boxes[ParseData.DATA_ARRAY];
            long[] classesData = (long[]) classes[ParseData.DATA_ARRAY];
            float[] scoresData = (float[]) scores[ParseData.DATA_ARRAY];

            float[] data_t = new float[boxesShare[1]];

            for (int i = 0; i < boxesShare[0]; i++) {
                System.arraycopy(boxesData, i * boxesShare[1], data_t, 0, boxesShare[1]);

                float x = data_t[0];
                float y = data_t[1];
                float width = data_t[2];
                float height = data_t[3];


                x = x * imageWidth;
                y = y * imageHeight;
                width = x + width * imageWidth;
                height = y + height * imageHeight;

                RectF location = new RectF();
                location.set(x, y, width, height);

                detectResultList.add(new DetectResult(null, CLASSES[(int)classesData[i]], scoresData[i], location));
                Log.d(TAG, "generateDetectResult:" + i + " = " + detectResultList.get(i).toString());
            }
        }

        return detectResultList;
    }

}
