package com.example.admin.giftbox2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

public class GenerateGiftActivity extends AppCompatActivity {

    private Context thisContext;
    private String username;
    private BmobFile backBmobImage;
    private BmobFile giftBmobImage;
    private BmobFile wholeBmobImage;
    private String backBmobUrl;
    private String giftBmobUrl;
    private String wholeBmobUrl;
    private String pointStr;
    private String recipient;
    private String coordinate;
    private int type;
    String drawablePath;
    Hashtable<Integer, String> stickerMap
            = new Hashtable<Integer, String>();
    private String wholePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generete_gift);

        ContextHolder.initial(this);
        thisContext = ContextHolder.getContext();

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        type = intent.getIntExtra("type",0);
        pointStr = intent.getStringExtra("points");
        recipient = intent.getStringExtra("recipient");
        coordinate = intent.getStringExtra("coordinate");

        stickerMap.put(2131165274,"http://bmob-cdn-17499.b0.upaiyun.com/2018/11/16/b2b8c09c4000febe804b1d953f3fb586.png");
        stickerMap.put(2131165275,"http://bmob-cdn-17499.b0.upaiyun.com/2018/11/16/024028b340ef5d0080e17bfb6cdfd5ed.png");
        stickerMap.put(2131165276,"http://bmob-cdn-17499.b0.upaiyun.com/2018/11/16/9d802b4e40082f5680d845c0ca4f81c9.png");
        stickerMap.put(2131165334,"http://bmob-cdn-17499.b0.upaiyun.com/2018/11/16/47b8912340f15d3880672ea61aeb903b.png");
        stickerMap.put(2131165335,"http://bmob-cdn-17499.b0.upaiyun.com/2018/11/16/01b3590c4015790f800bc825ca82db49.png");
        stickerMap.put(2131165379,"http://bmob-cdn-17499.b0.upaiyun.com/2018/11/16/2c7fc0a0407801478049c6d8147c882e.png");
        stickerMap.put(2131165383,"http://bmob-cdn-17499.b0.upaiyun.com/2018/11/16/7d880939400af62280dee673882cba11.png");
        stickerMap.put(2131165384,"http://bmob-cdn-17499.b0.upaiyun.com/2018/11/16/dda99b1c408334c3802e5fae3246a876.png");
        stickerMap.put(2131165385,"http://bmob-cdn-17499.b0.upaiyun.com/2018/11/16/7e8afb4d401a1cca80e3194893a0a883.png");

        drawablePath = intent.getStringExtra("drawablePath");
        System.out.println("GenerateGiftActivity drawablePath:" + drawablePath);
        Mat drawable = new Mat();
        if (type == 1){
            giftBmobImage = new BmobFile(new File(drawablePath));
            Bitmap drawableBitmap = BitmapFactory.decodeFile(drawablePath);
            Utils.bitmapToMat(drawableBitmap, drawable);
            Imgproc.cvtColor(drawable, drawable, Imgproc.COLOR_RGB2RGBA);
        }else if(type == 2){
            drawable = getDrawAble(getResources().getDrawable((int)Long.parseLong(drawablePath)));
            Imgproc.cvtColor(drawable, drawable, Imgproc.COLOR_RGB2RGBA);
        }else{
            Toast.makeText(this, "cannot get gift", Toast.LENGTH_SHORT).show();
        }

        String regionPath = intent.getStringExtra("regionPath");
        backBmobImage = new BmobFile(new File(regionPath));
        Bitmap regionBitmap = BitmapFactory.decodeFile(regionPath);
        Mat region = new Mat();
        Utils.bitmapToMat(regionBitmap, region);
        Imgproc.cvtColor(region, region, Imgproc.COLOR_RGB2RGBA);

        String[] pointStr = intent.getStringExtra("points").trim().split(" ");
        int[] points = new int[8];
        for (int i=0; i<pointStr.length; i++){
            System.out.println("pointsString:" + i + ":" + pointStr[i]);
            points[i] = Integer.parseInt(pointStr[i]);
        }

        Mat result = getThansform(drawable, region, points);
        Core.rotate(result, result, Core.ROTATE_90_CLOCKWISE);
        Bitmap resultBitmap = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, resultBitmap);
        wholePath = saveBitmap(resultBitmap);
        wholeBmobImage = new BmobFile(new File(wholePath));

        ImageView imageView = (ImageView) findViewById(R.id.gift);
        imageView.setImageBitmap(resultBitmap);
    }

    public Mat getThansform(Mat drawable, Mat region, int[] points) {
        Mat drawableCorner = new Mat(4, 1, CvType.CV_32FC2);
        Mat drawableTransformCorner = new Mat(4, 1, CvType.CV_32FC2);

        drawableCorner.put(0, 0, new double[]{0.0, 0.0});
        drawableCorner.put(1, 0, new double[]{drawable.cols(), 0.0});
        drawableCorner.put(2, 0, new double[]{drawable.cols(), drawable.rows()});
        drawableCorner.put(3, 0, new double[]{0.0, drawable.rows()});

        drawableTransformCorner.put(0, 0, new double[]{points[0], points[1]});
        drawableTransformCorner.put(1, 0, new double[]{points[2], points[3]});
        drawableTransformCorner.put(2, 0, new double[]{points[4], points[5]});
        drawableTransformCorner.put(3, 0, new double[]{points[6], points[7]});

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

    public void onClick_SendGiftEvent(View view) {
        uploadBackground();
        TextView tv = (TextView) findViewById(R.id.SendGiftMessage);
        Toast.makeText(thisContext, "Gift sending...", Toast.LENGTH_SHORT).show();
    }

    public void onClick_SendGiftHome(View view) {
        Intent intent = new Intent(GenerateGiftActivity.this, MainActivity.class);
//        intent.putExtra("username",username);
        startActivity(intent);
    }

    public void onClick_SendGiftAnother(View view) {
        Intent intent = new Intent(GenerateGiftActivity.this, ContactActivity.class);
        intent.putExtra("username",username);
        startActivity(intent);
    }

    public void uploadBackground() {
        backBmobImage.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    backBmobUrl = backBmobImage.getFileUrl();
                    Toast.makeText(thisContext, "Uploading background image", Toast.LENGTH_SHORT).show();
                    uploadGift();

                } else {
                    Toast.makeText(thisContext, "Gift upload failed! Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    System.out.println("Bmob image upload failed" + e);
                }
            }
            @Override
            public void onProgress(Integer value) {
                // return the percent of uploading
            }
        });
    }

    public void uploadWhole() {
        wholeBmobImage.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    wholeBmobUrl = wholeBmobImage.getFileUrl();
                    Toast.makeText(thisContext, "Uploading the whole image", Toast.LENGTH_SHORT).show();
                    addGift();
                } else {
                    Toast.makeText(thisContext, "Gift upload failed! Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    System.out.println("Bmob image upload failed" + e);
                }
            }
            @Override
            public void onProgress(Integer value) {
                // return the percent of uploading
            }
        });
    }

    public void uploadGift() {
        System.out.println("upload type:" + type);
        if (type == 1){
            giftBmobImage.uploadblock(new UploadFileListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        giftBmobUrl = giftBmobImage.getFileUrl();
                        Toast.makeText(thisContext, "Uploading gift image", Toast.LENGTH_SHORT).show();
                        uploadWhole();
                    } else {
                        Toast.makeText(thisContext, "Gift upload failed! Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        System.out.println("Bmob image upload failed" + e);
                    }
                }
                @Override
                public void onProgress(Integer value) {
                    // return the percent of uploading
                }
            });
        }else if (type == 2){
            giftBmobUrl = stickerMap.get((int)Long.parseLong(drawablePath));
            uploadWhole();
        }
    }

    public void addGift() {
        Gift giftSending = new Gift();
        giftSending.setBackground(backBmobUrl);
        giftSending.setAttach(giftBmobUrl);
        giftSending.setWhole(wholeBmobUrl);
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
                    Button b1 = (Button) findViewById(R.id.SendGiftHome);
                    b1.setVisibility(View.VISIBLE);
                    Button b2 = (Button) findViewById(R.id.SendGiftAnother);
                    b2.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(thisContext, "send failed!error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.i("bmob", "failed：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

    public String saveBitmap(Bitmap bm) {
        String path ="";
//        Log.e(TAG, "保存图片");
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "giftbox");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
            }
        }
        if (mediaStorageDir.exists()) {
            mediaStorageDir.delete();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File image = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
        try {
            FileOutputStream out = new FileOutputStream(image);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            path = image.getPath();
//            Log.i(TAG, "已经保存");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return path;
    }
}
