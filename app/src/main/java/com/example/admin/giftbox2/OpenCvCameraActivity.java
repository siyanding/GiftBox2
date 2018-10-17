package com.example.admin.giftbox2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class OpenCvCameraActivity extends AppCompatActivity  implements CameraBridgeViewBase.CvCameraViewListener2 {

    static {
        System.loadLibrary("native-lib");
    }

    CameraBridgeViewBase mCVCamera;
    private static final String TAG = "OpenCvCameraActivity";
    private Mat mRgba;
    private static int Cur_State = 0;
    static boolean imageReady = false;
    static Bitmap bitmap;
    protected static final int SUCCESS = 0;
    protected static final int ERROR = 1;
    protected static final int NETWORK_ERROR = 2;
    private  boolean tag = false;
    private Context thisContext;
    private static double[] points = new double[8];


    //use Handle to update main thread(UI thread)
    private static Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    System.out.println("bitmap download success");
                    bitmap = (Bitmap) msg.obj;
//                    imageView.setImageBitmap(bitmap);
//                    region = (File)msg.obj;
                    if (bitmap.equals(null)) {
                        System.out.println("image is null");
                    } else {
                        imageReady = true;
                    }
                    Mat dst = new Mat();
                    Utils.bitmapToMat(bitmap, dst);
//                    Point p1,p2,p3,p4;
                    List<Double> xs = Arrays.asList( points[0], points[2], points[4], points[6] );
                    List<Double> ys = Arrays.asList( points[1], points[3], points[5], points[7] );
                    double minX = xs.stream().min( Comparator.naturalOrder() ).get();
                    double minY = ys.stream().min( Comparator.naturalOrder()).get();
                    double maxX = xs.stream().max( Comparator.naturalOrder()).get();
                    double maxY = ys.stream().max( Comparator.naturalOrder()).get();

                    Rect rectCrop = new Rect((int) minX, (int)minY , (int)(maxX-minX), (int)(maxY-minY));
                    Mat image_output= dst.submat(rectCrop);
                    Imgproc.resize(image_output,image_output,dst.size());
                    Utils.matToBitmap(image_output,bitmap);
                    System.out.println("OpenCvCameraActivity: SUCCESS");
                    break;
                case ERROR:
                    System.out.println("ViewGiftActivity: ERROR");
                    break;
                case NETWORK_ERROR:
                    System.out.println("ViewGiftActivity: NETWORK_ERROR");
                    break;
            }
        };
    };

    /**
     * 通过OpenCV管理Android服务，异步初始化OpenCV
     */
    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    mCVCamera.enableView();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cv_camera);

        mCVCamera = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCVCamera.setCvCameraViewListener(this);

        ContextHolder.initial(this);
        thisContext = ContextHolder.getContext();

        Button mButton = (Button) findViewById(R.id.deal_btn);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Cur_State == 0) {
                    //切换状态
                    Cur_State = 1;
                    System.out.println("Cur_State:" + Cur_State);
                } else {
                    //恢复初始状态
                    Cur_State = 0;
                }
            }

        });
        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("imageUrl");
        String [] pointStr = intent.getStringExtra("points").trim().split(" ");

        for (int i=0; i<pointStr.length; i++){
            System.out.println("pointsString:" + i + ":" + pointStr[i]);
            points[i] = Double.parseDouble(pointStr[i]);
        }
//        System.out.println("pointsPoint:" + points.toString());
        getImage(imagePath);
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();

    }

    private Mat compareKeypoints(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        try {
            Mat src = inputFrame.rgba();
            Mat srcTrans = new Mat();
            Core.rotate(src,srcTrans,Core.ROTATE_90_CLOCKWISE);
//            Imgproc.resize(src,src,src.size());
            Mat dst = new Mat();
            bitmap = Bitmap.createScaledBitmap(bitmap, src.rows(), src.cols(), false);
            Utils.bitmapToMat(bitmap, dst);

            FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.AKAZE);

            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

            System.out.println(
                    src.height() + "x" + src.width() + " <===> " + dst.height() + "x" + dst.width()
            );

            Imgproc.resize(srcTrans,srcTrans,dst.size());
            MatOfKeyPoint keypoint1 = new MatOfKeyPoint();
            Imgproc.cvtColor(srcTrans, srcTrans, Imgproc.COLOR_RGBA2RGB);
            featureDetector.detect(srcTrans, keypoint1);

            MatOfKeyPoint keypoint2 = new MatOfKeyPoint();
            Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGBA2RGB);
            featureDetector.detect(dst, keypoint2);

            DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);

            Mat descriptor1 = new Mat();
            extractor.compute(srcTrans, keypoint1, descriptor1);
            Features2d.drawKeypoints(srcTrans, keypoint1, srcTrans);
//            Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2RGBA);

            Mat descriptor2 = new Mat();
            extractor.compute(dst, keypoint2, descriptor2);
            Features2d.drawKeypoints(dst, keypoint2, dst);
//            Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGB2RGBA);
//--------------------------------------------------------------------
            MatOfDMatch matches = new MatOfDMatch();
//            List<MatOfDMatch> matches = new LinkedList();
// ----------------------------------------------------------------
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

//            List<MatOfDMatch> matchesTest = new ArrayList<>();
            matcher.match(descriptor1, descriptor2, matches);
//            matcher.knnMatch(descriptor1, descriptor2, matches, 2);

//---------------------------------------------------------------------------------------
            List<DMatch> mats = matches.toList();//.toArray()
            double max_dist = 0; double min_dist = 100;

            //-- Quick calculation of max and min distances between keypoints
            for( int i = 0; i < descriptor1.rows(); i++ ){
                double dist = mats.get(i).distance;
                if( dist < min_dist ) min_dist = dist;
                if( dist > max_dist ) max_dist = dist;
            }
            List<DMatch> goodMatch = new LinkedList<>();
            for( int i = 0; i < descriptor1.rows(); i++ ){
                if( mats.get(i).distance < .5 * (max_dist - min_dist) ) { //3*min_dist ){
                    goodMatch.add(mats.get(i));
                }
            }
            MatOfDMatch gm = new MatOfDMatch();
            gm.fromList(goodMatch);
            Mat outImage = new Mat();  src.copyTo(outImage);// new Mat( src.rows(), src.cols(), src.type() );
            Features2d.drawMatches(srcTrans, keypoint1, dst, keypoint2, gm, outImage);
            Imgproc.resize(outImage,outImage,src.size());

            List<KeyPoint> templateKeyPointList = keypoint2.toList();
            List<KeyPoint> originalKeyPointList = keypoint1.toList();
            LinkedList<Point> objectPoints = new LinkedList();
            LinkedList<Point> scenePoints = new LinkedList();
            for(int i=0; i<goodMatch.size(); i++){
                objectPoints.addLast(templateKeyPointList.get(goodMatch.get(i).trainIdx).pt);
                scenePoints.addLast(originalKeyPointList.get(goodMatch.get(i).queryIdx).pt);
            }
            MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
            objMatOfPoint2f.fromList(objectPoints);
            MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
            scnMatOfPoint2f.fromList(scenePoints);
            Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);

            System.out.println("homographyW:" + homography.cols() + ", homographyH:" + homography.rows());

            Mat templateCorners = new Mat(4, 1, CvType.CV_32FC2);
            Mat templateTransformResult = new Mat(4, 1, CvType.CV_32FC2);//pointStr
//            templateCorners.put(0, 0, new double[]{points[0], points[1]});
//            templateCorners.put(1, 0, new double[]{points[2], points[3]});
//            templateCorners.put(2, 0, new double[]{points[4], points[5]});
//            templateCorners.put(3, 0, new double[]{points[6], points[7]});
            templateCorners.put(0, 0, new double[]{0, 0});
            templateCorners.put(1, 0, new double[]{dst.cols(), 0});
            templateCorners.put(2, 0, new double[]{dst.cols(), dst.rows()});
            templateCorners.put(3, 0, new double[]{0, dst.rows()});
            //使用 perspectiveTransform 将模板图进行透视变以矫正图象得到标准图片
            Core.perspectiveTransform(templateCorners, templateTransformResult, homography);

            double[] pointA = templateTransformResult.get(0, 0);
            double[] pointB = templateTransformResult.get(1, 0);
            double[] pointC = templateTransformResult.get(2, 0);
            double[] pointD = templateTransformResult.get(3, 0);

            double clockWiseSum = (pointA[0] - pointB[0]) * (pointA[1] + pointB[1]) +
                    (pointB[0] - pointC[0]) * (pointB[1] + pointC[1]) +
                    (pointC[0] - pointD[0]) * (pointC[1] + pointD[1]) +
                    (pointD[0] - pointA[0]) * (pointD[1] + pointA[1]);
            System.out.println("clockWiseSum: " + clockWiseSum);

//            MatOfPoint contour = iterator.next();
            double areaTemplate = Imgproc.contourArea(templateCorners);
            double areaTransform = Imgproc.contourArea(templateTransformResult);
            if(2 * areaTemplate <= areaTransform || 0.5 * areaTemplate >= areaTransform){
                System.out.println("area: false");
            }else {
                System.out.println("area: true");
            }
            tag = (clockWiseSum > 0) && (2 * areaTemplate > areaTransform) && (0.5 * areaTemplate < areaTransform);

//            Imgproc.line(outImage,new Point(pointA[0]));
            Imgproc.line(outImage, new Point(pointA),new Point(pointB), new Scalar(0, 255, 0), 4);//上 A->B
            Imgproc.line(outImage, new Point(pointB),new Point(pointC), new Scalar(0, 255, 0), 4);//右 B->C
            Imgproc.line(outImage, new Point(pointC),new Point(pointD), new Scalar(0, 255, 0), 4);//下 C->D
            Imgproc.line(outImage, new Point(pointD),new Point(pointA), new Scalar(0, 255, 0), 4);//左 D->A

            return outImage;
        } catch (Exception e) {
            return null;
        }


    }

    /**
     * take care of all image processing, this method will be called every time camera frame refreshed
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if( Cur_State  == 1 && imageReady ) {
//            this.runOnUiThread(new Runnable() {
//                public void run() {
//                    final Toast toast = Toast.makeText(thisContext, "image ready!!!" , Toast.LENGTH_SHORT);
//                    toast.show();
//                }
//            });
            if (tag){
                this.runOnUiThread(new Runnable() {
                    public void run() {
                        final Toast toast = Toast.makeText(thisContext, "find it!!!" , Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
//                Intent intent = new Intent(OpenCvCameraActivity.this, SuccussActivity.class);
//                startActivity(intent);
            }
            return  compareKeypoints(inputFrame);
        } else {
            return inputFrame.rgba();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library not found!");
        }else{
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        }
    }


    public static void getImage(String imagePath) {
        new Thread() {
            public void run() {
                try {
                    URL url = new URL(imagePath);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    System.out.println("respond code --" + conn.getResponseCode());
                    if (conn.getResponseCode() == 200) {

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        InputStream in = conn.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
                        //use handler send message
                        Message msg = Message.obtain();
                        msg.obj = bitmap;//data being sent
                        msg.what = SUCCESS;//handler can have different actions depends on different message
                        handler.sendMessage(msg);
                        in.close();
                    } else {
                        Message msg = Message.obtain();
                        msg.what = ERROR;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    Message msg = Message.obtain();
                    msg.what = NETWORK_ERROR;
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
