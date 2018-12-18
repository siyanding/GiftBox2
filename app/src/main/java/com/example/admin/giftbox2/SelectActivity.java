package com.example.admin.giftbox2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

public class SelectActivity extends Activity{//AppCompatActivity
    static {
        System.loadLibrary("native-lib");
    }

    private static final String TAG = "SelectActivity";

    private ChooseArea chooseArea;
    private StickerAdapter stickerAdapter = null;
    private ImageView sticker;
    private GridView stickersGridView = null;
    private File drawableFile;
    private BmobFile portraitBmob;
    private String imagePath;
    private String username;
    private String coordinate;
    private String recipient;
    private File image;
    private Button button;
    private Context thisContext;
    private BottomImageView bottomImageView;
    private ImageView zoomArea;
    private Bitmap bm;
    private Bitmap giftBitmap;
//    private List<String> gifts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_avtivity);

        ContextHolder.initial(this);
        thisContext = ContextHolder.getContext();

        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imagePath");
        System.out.println("SelectActivity imagePath:" + imagePath);
        username = intent.getStringExtra("username");
        coordinate = intent.getStringExtra("coordinate");
        recipient = intent.getStringExtra("recipient");
        image = new File(imagePath);
        bm = BitmapFactory.decodeFile(imagePath);
//        ((ImageView)findViewById(R.id.layout2_imageAbove)).setImageBitmap(bm);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_avtivity);
        bottomImageView = (BottomImageView) findViewById(R.id.layout2_bottomView);
        bottomImageView.setImageBitmap(bm);
//        zoomArea = (ImageView) findViewById(R.id.layout2_imageAbove);
//        zoomArea.setImageBitmap(bm);
        chooseArea = (ChooseArea) findViewById(R.id.layout2_topView);
//        bottomImageView.setZoomView(zoomArea);
        chooseArea.setBottomView(bottomImageView);
        chooseArea.setRegion(new Point(20, 40), new Point(60, 40), new Point(60, 90), new Point(20, 90));
        button = (Button) findViewById(R.id.check);

        EventManager.Event event = new EventManager.Event() {
            @Override
            public void onSomethingHappened(boolean msg) {
                if (msg) {
                    button.setVisibility(View.VISIBLE);
                } else {
                    button.setVisibility(View.INVISIBLE);
                }
            }
        };
        EventManager.setEventListener(event);

        sticker = (ImageView) findViewById(R.id.sticker);
//        stickerAdapter = new StickerAdapter(this, gifts);
        stickerAdapter = new StickerAdapter(thisContext);
        stickersGridView = (GridView) findViewById(R.id.stickers);
        stickersGridView.setAdapter(stickerAdapter);
        stickersGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                stickersGridView.setVisibility(View.INVISIBLE);
                Drawable drawable1 = getResources().getDrawable(
                        (int) stickerAdapter.getItemId(position));
//                drawable = new Mat();
                if (position == 0) {
                    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES), "giftbox");
                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                            Log.d("MyCameraApp", "failed to create directory");
                        }
                    }
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    drawableFile = new File(mediaStorageDir.getPath() + File.separator +
                            "IMG_" + timeStamp + ".jpg");
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    intent.putExtra("recipient", personOnClick.getFriend());
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uri = FileProvider.getUriForFile(thisContext, BuildConfig.APPLICATION_ID+".fileprovider", drawableFile);//BuildConfig.APPLICATION_ID + ".fileProvider"
                    } else {
                        uri = Uri.fromFile(drawableFile);
                    }
                    // set the address for picture
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, 0);
                } else {
                    System.out.println("drawable id 1: " + stickerAdapter.getItemId(1));//9
                    System.out.println("drawable id 2: " + stickerAdapter.getItemId(2));//9
                    System.out.println("drawable id 3: " + stickerAdapter.getItemId(3));//9
                    System.out.println("drawable id 4: " + stickerAdapter.getItemId(4));//9
                    System.out.println("drawable id 5: " + stickerAdapter.getItemId(5));//9
                    System.out.println("drawable id 6: " + stickerAdapter.getItemId(6));//9
                    System.out.println("drawable id 7: " + stickerAdapter.getItemId(7));//9
                    System.out.println("drawable id 8: " + stickerAdapter.getItemId(8));//9
                    System.out.println("drawable id 9: " + stickerAdapter.getItemId(9));//9
//                    sticker.setVisibility(View.VISIBLE);
//                    ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                    Point[] points = chooseArea.getRegion();
                    String pointStr = "";
                    for (int i = 0; i < points.length; i++) {
                        pointStr += points[i].x + " " + points[i].y + " ";
                    }
                    Intent intent = new Intent(SelectActivity.this, GenerateGiftActivity.class);
                    intent.putExtra("type",2);
                    intent.putExtra("drawablePath", stickerAdapter.getItemId(position)+"");
                    intent.putExtra("regionPath", imagePath);
                    intent.putExtra("username",username);
                    intent.putExtra("coordinate",coordinate);
                    intent.putExtra("recipient",recipient);
                    intent.putExtra("points", pointStr);
                    startActivity(intent);
//                    Mat drawable = getDrawAble(drawable1);
//                                    Mat region = new Mat();
//                Utils.bitmapToMat(bm, region);
//                Imgproc.cvtColor(region, region, Imgproc.COLOR_RGB2RGBA);
//                Imgproc.cvtColor(drawable, drawable, Imgproc.COLOR_RGB2RGBA);
//                Mat temp = getThansform(drawable, region);//reSizeGift(drawable,region);
//                Core.rotate(temp, temp, Core.ROTATE_90_CLOCKWISE);
//
//                System.out.println("temp width:" + temp.cols() + "<===> temp height:" + temp.rows());
//                Bitmap bmp = null;
//                try {
//                    bmp = Bitmap.createBitmap(temp.cols(), temp.rows(), Bitmap.Config.ARGB_8888);
//                    Utils.matToBitmap(temp, bmp);
//                } catch (CvException e) {
//                    Log.d("Exception", e.getMessage());
//                }
//                sticker.setImageBitmap(bmp);
//                sticker.bringToFront();

                }
            }
        });
    }

    public Mat getThansform(Mat drawable, Mat region) {
        Imgproc.cvtColor(drawable, drawable, Imgproc.COLOR_RGB2RGBA);
        Imgproc.cvtColor(region, region, Imgproc.COLOR_RGB2RGBA);
        Mat drawableCorner = new Mat(4, 1, CvType.CV_32FC2);
        Mat drawableTransformCorner = new Mat(4, 1, CvType.CV_32FC2);
        Point[] points = chooseArea.getRegion();
        drawableCorner.put(0, 0, new double[]{0.0, 0.0});
        drawableCorner.put(1, 0, new double[]{drawable.cols(), 0.0});
        drawableCorner.put(2, 0, new double[]{drawable.cols(), drawable.rows()});
        drawableCorner.put(3, 0, new double[]{0.0, drawable.rows()});

        drawableTransformCorner.put(0, 0, new double[]{points[0].x, points[0].y});
        drawableTransformCorner.put(1, 0, new double[]{points[1].x, points[1].y});
        drawableTransformCorner.put(2, 0, new double[]{points[2].x, points[2].y});
        drawableTransformCorner.put(3, 0, new double[]{points[3].x, points[3].y});

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(drawableCorner, drawableTransformCorner);

        Imgproc.warpPerspective(drawable, region, perspectiveTransform, region.size(), Imgproc.INTER_LINEAR,
                Core.BORDER_TRANSPARENT, new Scalar(0, 0, 0, 0));
        return region;
    }

    public Mat getDrawAble(Drawable drawable) {
//        if (drawable instanceof BitmapDrawable) {
//            return ((BitmapDrawable)drawable).getBitmap();
//        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        Mat drawMat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(bitmap, drawMat);
        Imgproc.cvtColor(drawMat, drawMat, Imgproc.COLOR_RGBA2RGB);
        return drawMat;
    }

    /**
     * 通过OpenCV管理Android服务，异步初始化OpenCV
     */
    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library not found!");

        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        }
    }


    public void onClick_CheckEvent(View view) {
        stickersGridView.setVisibility(View.VISIBLE);
        button.setVisibility(View.INVISIBLE);
//        Point[] points = chooseArea.getRegion();
//        BmobFile bmobImage = new BmobFile(image);
//
//        System.out.println("SelectActivity:" + chooseArea.isFlag());
//        uploadBackground(bmobImage,points);
//        button.setVisibility(View.INVISIBLE);
    }



    public void uploadImage(BmobFile bmobImage, Point[] points) {
        final int[] callCount = {0};
        bmobImage.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    String BmobImageUrl = bmobImage.getFileUrl();
                    addGift(BmobImageUrl, points);
                } else if (callCount[0] < 5) {
                    System.out.println("Bmob image upload failed" + e);
                    uploadImage(bmobImage, points);
                    callCount[0]++;
                }
            }
            @Override
            public void onProgress(Integer value) {
                // return the percent of uploading
            }
        });
    }

    public void addGift(String BmobImageUrl, Point[] points) {
        final int[] callCount = {0};
        String pointStr = "";
        for (int i = 0; i < points.length; i++) {
            pointStr += points[i].x + " " + points[i].y + " ";
        }

        Gift giftSending = new Gift();
        if (coordinate.equals("")) {
            System.out.println("GPS value false");
        }
        giftSending.setBackground(BmobImageUrl);
        giftSending.setUser(username);
        giftSending.setRecipient(recipient);
        giftSending.setStatus(false);
        giftSending.setCoordinate(coordinate);
        giftSending.setPoints(pointStr);
        giftSending.save(new SaveListener<String>() {

            @Override
            public void done(String giftId, BmobException e) {
                if (e == null) {
                    Toast.makeText(thisContext, "send success!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SelectActivity.this, MainActivity.class);
                    intent.putExtra("imagePath", image.getAbsolutePath());
                    intent.putExtra("username", username);
                    startActivity(intent);
                } else if (callCount[0] < 5) {
                    addGift(BmobImageUrl, points);
                    Log.i("bmob", "failed：" + e.getMessage() + "," + e.getErrorCode());
                    callCount[0]++;
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("SelectActivity: enter onActivity Result" );
        if (resultCode == RESULT_OK && requestCode == 0) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(drawableFile.getPath(), options);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int w = width >= height ? 800 : 600;
                int h = width >= height ? 600 : 800;
                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, false);
                try (FileOutputStream out = new FileOutputStream(drawableFile.getPath())) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                    // PNG is a lossless format, the compression factor (100) is ignored
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Point[] points = chooseArea.getRegion();
                String pointStr = "";
                for (int i = 0; i < points.length; i++) {
                    pointStr += points[i].x + " " + points[i].y + " ";
                }
                Intent intent = new Intent(SelectActivity.this, GenerateGiftActivity.class);
                intent.putExtra("type",1);
                intent.putExtra("drawablePath", drawableFile.getAbsolutePath());
                intent.putExtra("regionPath", imagePath);
                intent.putExtra("username",username);
                intent.putExtra("coordinate",coordinate);
                intent.putExtra("recipient",recipient);
                intent.putExtra("points", pointStr);
//                intent.putExtra("recipient",personOnClick.getFriend());

                startActivity(intent);
//                uploadBackground();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(this, "cannot get data", Toast.LENGTH_LONG).show();
        }
    }

}
