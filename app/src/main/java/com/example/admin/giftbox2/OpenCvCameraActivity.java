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
import org.opencv.imgproc.Imgproc;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class OpenCvCameraActivity extends AppCompatActivity  implements CameraBridgeViewBase.CvCameraViewListener2 {

    static {
        System.loadLibrary("native-lib");
    }

    CameraBridgeViewBase mCVCamera;
    private static final String TAG = "OpenCvCameraActivity";
    private Mat mRgba;
    private static int Cur_State = 0;
    static boolean imageReady = false;
    static Bitmap giftBitmap;
    static Mat processedBitmap;
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
                    System.out.println("giftBitmap download success");
                    giftBitmap = (Bitmap) msg.obj;
//                    imageView.setImageBitmap(giftBitmap);
//                    region = (File)msg.obj;
                    if (giftBitmap.equals(null)) {
                        System.out.println("image is null");
                    } else {
                        imageReady = true;
                    }
//                    Mat dst = new Mat();
//                    Utils.bitmapToMat(giftBitmap, dst);
//                    Point p1,p2,p3,p4;
                    List<Double> xs = Arrays.asList( points[0], points[2], points[4], points[6] );
                    List<Double> ys = Arrays.asList( points[1], points[3], points[5], points[7] );
                    double minX = xs.stream().min( Comparator.naturalOrder() ).get();
                    double minY = ys.stream().min( Comparator.naturalOrder()).get();
                    double maxX = xs.stream().max( Comparator.naturalOrder()).get();
                    double maxY = ys.stream().max( Comparator.naturalOrder()).get();

//                    Rect rectCrop = new Rect((int) minX, (int)minY , (int)(maxX-minX), (int)(maxY-minY));
//                    Mat croppedGiftImageMat= dst.submat(rectCrop);
//                    Imgproc.resize(croppedGiftImageMat,croppedGiftImageMat,dst.size());
//                    Utils.matToBitmap(croppedGiftImageMat, giftBitmap);
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
        processedBitmap = null;
        mCVCamera = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCVCamera.setCvCameraViewListener(this);

        ContextHolder.initial(this);
        thisContext = ContextHolder.getContext();

        Button mButton = (Button) findViewById(R.id.deal_btn);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Cur_State == 0) {
                    Cur_State = 1;
                    System.out.println("Cur_State:" + Cur_State);
                } else {
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

    // src image is from camera.
    // dst image is from server
    private void preProcess( Mat camera, Mat processedCamera, Mat giftImage ) {
        Core.rotate( camera, processedCamera, Core.ROTATE_90_CLOCKWISE );
        Utils.bitmapToMat(giftBitmap, giftImage);
        Imgproc.resize(processedCamera,processedCamera,giftImage.size());
    }

    private Mat compareKeypoints(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        try {
            Mat camera = inputFrame.rgba();
            Mat processedCamera = new Mat();
            Mat giftImage = new Mat();

            preProcess( camera, processedCamera, giftImage );

            FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.AKAZE);
            MatOfKeyPoint cameraKeypoints = new MatOfKeyPoint();
            Imgproc.cvtColor(processedCamera, processedCamera, Imgproc.COLOR_RGBA2RGB);
            featureDetector.detect(processedCamera, cameraKeypoints);

            MatOfKeyPoint giftKeypoints = new MatOfKeyPoint();
            Imgproc.cvtColor(giftImage, giftImage, Imgproc.COLOR_RGBA2RGB);
            featureDetector.detect(giftImage, giftKeypoints);

            DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
            Mat cameraDescriptor = new Mat();
            extractor.compute(processedCamera, cameraKeypoints, cameraDescriptor);
            Features2d.drawKeypoints(processedCamera, cameraKeypoints, processedCamera);

            Mat giftDescriptor = new Mat();
            extractor.compute(giftImage, giftKeypoints, giftDescriptor);
            Features2d.drawKeypoints(giftImage, giftKeypoints, giftImage);

            MatOfDMatch matches = new MatOfDMatch();
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
            matcher.match(cameraDescriptor, giftDescriptor, matches);

            List<DMatch> mats = matches.toList();
            double max_dist = 0; double min_dist = 100;

            max_dist = mats.stream().mapToDouble( (match) -> match.distance ).max().orElse( 0 );
            min_dist = mats.stream().mapToDouble( (match) -> match.distance ).min().orElse( 0 );
            double range = max_dist - min_dist;
            final double MIN_DIST = min_dist;
            List<DMatch> bestMatchesList = mats.stream().filter( m -> (m.distance - MIN_DIST) < .5 * range)
                    .collect(Collectors.toList());

            MatOfDMatch bestMatchesMat = new MatOfDMatch();
            bestMatchesMat.fromList(bestMatchesList);

            Mat outImage = new Mat();
            Features2d.drawMatches(processedCamera, cameraKeypoints, giftImage, giftKeypoints, bestMatchesMat, outImage);
            Imgproc.resize(outImage,outImage,camera.size());

            if (bestMatchesList.size() > 4){
                Mat[] corners = findHomography(giftKeypoints, cameraKeypoints, bestMatchesList, giftImage);
                Mat templateTransformResult = new Mat();
                Mat templateCorners = new Mat();
                if (corners[0] != null && corners[1] != null ) {
                    templateTransformResult = corners[0];
                    templateCorners = corners[1];
                    templateTransformResult.get(0,1);

                    double threshold = caculateThreshold(giftKeypoints, cameraKeypoints,
                            templateTransformResult,templateCorners, bestMatchesList);
                    findObject(templateTransformResult, templateCorners, outImage, threshold, processedCamera);
                }
            }
            return outImage;
        } catch (Exception e) {
            System.out.println("Exception:" + e.toString());
            return null;
        }
    }

    private double caculateThreshold(MatOfKeyPoint giftKeypoints, MatOfKeyPoint cameraKeypoints,
                                     Mat templateTransformResult, Mat templateCorners, List<DMatch> bestMatchesList) {
        List<KeyPoint> cameraKeypointsList = cameraKeypoints.toList();
        List<KeyPoint> giftKeypointsList = giftKeypoints.toList();

        Mat giftMat = new Mat();
        templateCorners.copyTo(giftMat);
        giftMat.convertTo(giftMat, CvType.CV_32S);

        Mat cameraMat = new Mat();
        templateTransformResult.copyTo(cameraMat);
        cameraMat.convertTo(cameraMat, CvType.CV_32S);

        MatOfPoint giftMatPoints = new MatOfPoint(giftMat);
        MatOfPoint cameraMatPoints = new MatOfPoint(cameraMat);
        LinkedList<Point> giftPoints = new LinkedList();
        LinkedList<Point> cameraPoints = new LinkedList();
        for(int i=0; i<bestMatchesList.size(); i++){
            giftPoints.addLast(giftKeypointsList.get(bestMatchesList.get(i).trainIdx).pt);
            cameraPoints.addLast(cameraKeypointsList.get(bestMatchesList.get(i).queryIdx).pt);
        }
        int k = 0;
        int kPrime = 0;
        for (int i=0; i< giftPoints.size(); i++){
            double insideCamera = Imgproc.pointPolygonTest(new MatOfPoint2f(cameraMatPoints.toArray()),cameraPoints.get(i),false);
            double insideGift = Imgproc.pointPolygonTest(new MatOfPoint2f(giftMatPoints.toArray()),giftPoints.get(i),false);
            System.out.println("insideCamera:" + insideCamera + ", insideGift:" + insideGift);
            if (insideCamera > 0){
                k++;
                if (insideGift < 0){
                    kPrime++;
                }
            }
        }
        double threshold = kPrime/k;
        return  threshold;
    }

    private Mat[] findHomography(MatOfKeyPoint giftKeypoints, MatOfKeyPoint cameraKeypoints, List<DMatch> bestMatchesList, Mat giftImage){
        List<KeyPoint> templateKeyPointList = giftKeypoints.toList();
        List<KeyPoint> originalKeyPointList = cameraKeypoints.toList();
        LinkedList<Point> objectPoints = new LinkedList();
        LinkedList<Point> scenePoints = new LinkedList();
        for(int i=0; i<bestMatchesList.size(); i++){
            objectPoints.addLast(templateKeyPointList.get(bestMatchesList.get(i).trainIdx).pt);
            scenePoints.addLast(originalKeyPointList.get(bestMatchesList.get(i).queryIdx).pt);
        }
        MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
        objMatOfPoint2f.fromList(objectPoints);
        MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
        scnMatOfPoint2f.fromList(scenePoints);
        Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);

        Mat[] corners = new Mat[2];
        if (!homography.empty()){
            Mat templateCorners = new Mat(4, 1, CvType.CV_32FC2);
            Mat templateTransformResult = new Mat(4, 1, CvType.CV_32FC2);//CvType.CV_32FC2
            templateCorners.put(0, 0, new double[]{points[0], points[1]});
            templateCorners.put(1, 0, new double[]{points[2], points[3]});
            templateCorners.put(2, 0, new double[]{points[4], points[5]});
            templateCorners.put(3, 0, new double[]{points[6], points[7]});
//            templateCorners.put(0, 0, new double[]{0, 0});
//            templateCorners.put(1, 0, new double[]{giftImage.cols(), 0});
//            templateCorners.put(2, 0, new double[]{giftImage.cols(), giftImage.rows()});
//            templateCorners.put(3, 0, new double[]{0, giftImage.rows()});
            //使用 perspectiveTransform 将模板图进行透视变以矫正图象得到标准图片
            Core.perspectiveTransform(templateCorners, templateTransformResult, homography);
            corners[0] = templateTransformResult;
            corners[1] = templateCorners;
        }
        return corners;
    }

    /**
     * @param templateTransformResult
     * @param templateCorners
     * @param outImage
     * @param treshold
     * @param processedCamera
     */
    public void findObject(Mat templateTransformResult, Mat templateCorners, Mat outImage,
                           double treshold, Mat processedCamera){

        double[] pointA = templateTransformResult.get(0, 0);
        double[] pointB = templateTransformResult.get(1, 0);
        double[] pointC = templateTransformResult.get(2, 0);
        double[] pointD = templateTransformResult.get(3, 0);

        boolean outOfCamera = pointA[0] > 0 && pointA[0] < processedCamera.cols()
                && pointB[0] > 0 && pointB[0] < processedCamera.cols()
                && pointC[0] > 0 && pointC[0] < processedCamera.cols()
                && pointD[0] > 0 && pointD[0] < processedCamera.cols()
                && pointA[1] > 0 && pointA[1] <processedCamera.rows()
                && pointB[1] > 0 && pointB[1] <processedCamera.rows()
                && pointC[1] > 0 && pointC[1] <processedCamera.rows()
                && pointD[1] > 0 && pointD[1] <processedCamera.rows();

        List<Double> giftXs = Arrays.asList( points[0], points[2], points[4], points[6] );
        List<Double> giftYs = Arrays.asList( points[1], points[3], points[5], points[7] );
        double giftMinX = giftXs.stream().min( Comparator.naturalOrder() ).get();
        double giftMinY = giftYs.stream().min( Comparator.naturalOrder()).get();
        double giftMaxX = giftXs.stream().max( Comparator.naturalOrder()).get();
        double giftMaxY = giftYs.stream().max( Comparator.naturalOrder()).get();
        double giftW = Math.abs(giftMinX-giftMaxX) > Math.abs(giftMinY-giftMaxY) ? Math.abs(giftMinX-giftMaxX) : Math.abs(giftMinY-giftMaxY);//big
        double giftH = Math.abs(giftMinX-giftMaxX) > Math.abs(giftMinY-giftMaxY) ? Math.abs(giftMinY-giftMaxY) : Math.abs(giftMinX-giftMaxX) ;//small
        double giftRatio = giftW/ giftH;

        List<Double> cameraXs = Arrays.asList( points[0], points[2], points[4], points[6] );
        List<Double> cameraYs = Arrays.asList( points[1], points[3], points[5], points[7] );
        double cameraMinX = cameraXs.stream().min( Comparator.naturalOrder() ).get();
        double cameraMinY = cameraYs.stream().min( Comparator.naturalOrder()).get();
        double cameraMaxX = cameraXs.stream().max( Comparator.naturalOrder()).get();
        double cameraMaxY = cameraYs.stream().max( Comparator.naturalOrder()).get();
        double cameraW = Math.abs(cameraMinX-cameraMaxX) > Math.abs(cameraMinY-cameraMaxY) ? Math.abs(cameraMinX-cameraMaxX) : Math.abs(cameraMinY-cameraMaxY);//big
        double cameraH = Math.abs(cameraMinX-cameraMaxX) > Math.abs(cameraMinY-cameraMaxY) ? Math.abs(cameraMinY-cameraMaxY) : Math.abs(cameraMinX-cameraMaxX) ;//small
        double cameraRatio = cameraW/ cameraH;

        boolean radio = cameraRatio < 2 * giftRatio && cameraRatio > .5 * cameraRatio;


        double clockWiseSum = (pointA[0] - pointB[0]) * (pointA[1] + pointB[1]) +
                (pointB[0] - pointC[0]) * (pointB[1] + pointC[1]) +
                (pointC[0] - pointD[0]) * (pointC[1] + pointD[1]) +
                (pointD[0] - pointA[0]) * (pointD[1] + pointA[1]);

        System.out.println("threshold:" + treshold);
        tag = (clockWiseSum > 0) && treshold < .3 && outOfCamera && radio;//(2 * areaTemplate > areaTransform) && (0.5 * areaTemplate < areaTransform);

        int red = tag ? 255 : 0;
        int green = tag ? 0 : 255;
        Imgproc.line(outImage, new Point(pointA),new Point(pointB), new Scalar(red, green, 0), 4);//上 A->B
        Imgproc.line(outImage, new Point(pointB),new Point(pointC), new Scalar(red, green, 0), 4);//右 B->C
        Imgproc.line(outImage, new Point(pointC),new Point(pointD), new Scalar(red, green, 0), 4);//下 C->D
        Imgproc.line(outImage, new Point(pointD),new Point(pointA), new Scalar(red, green, 0), 4);//左 D->A

        Point pointAA = new Point(new double[]{points[0] + processedCamera.cols(), points[1]});
        Point pointBB = new Point(new double[]{points[2] + processedCamera.cols(), points[3]});
        Point pointCC = new Point(new double[]{points[4] + processedCamera.cols(), points[5]});
        Point pointDD = new Point(new double[]{points[6] + processedCamera.cols(), points[7]});
        Imgproc.line(outImage, pointAA,pointBB, new Scalar(255, 255, 0), 4);//上 A->B
        Imgproc.line(outImage, pointBB,pointCC, new Scalar(255, 255, 0), 4);//右 B->C
        Imgproc.line(outImage, pointCC,pointDD, new Scalar(255, 255, 0), 4);//下 C->D
        Imgproc.line(outImage, pointDD,pointAA, new Scalar(255, 255, 0), 4);//左 D->A

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
