package com.example.admin.giftbox2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

public class SelectActivity extends AppCompatActivity {
    private ChooseArea chooseArea;
    private String imagePath;
    private String username;
    private String coordinate;
    private String recipient;
    private File image;
    private Button button;
    private Context thisContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_avtivity);

        ContextHolder.initial(this);
        thisContext = ContextHolder.getContext();

        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imagePath");
        username = intent.getStringExtra("username");
        coordinate = intent.getStringExtra("coordinate");
        recipient = intent.getStringExtra("recipient");
        image = new File(imagePath);
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
//        ((ImageView)findViewById(R.id.layout2_imageAbove)).setImageBitmap(bm);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_avtivity);
        BottomImageView bottomImageView = (BottomImageView)findViewById(R.id.layout2_bottomView);
        bottomImageView.setImageBitmap(bm);
        ImageView zoomArea = (ImageView)findViewById(R.id.layout2_imageAbove);
        zoomArea.setImageBitmap(bm);
        chooseArea = (ChooseArea)findViewById(R.id.layout2_topView);
        bottomImageView.setZoomView(zoomArea);
        chooseArea.setBottomView(bottomImageView);
        chooseArea.setRegion(new Point(20, 40), new Point(60, 40), new Point(60, 90), new Point(20, 90));
        button = (Button)findViewById(R.id.check);

        EventManager.Event event = new EventManager.Event() {
            @Override
            public void onSomethingHappened(boolean msg) {
                if (msg){
                    button.setVisibility(View.VISIBLE);
                }else {
                    button.setVisibility(View.INVISIBLE);
                }
            }
        };
        EventManager.setEventListener(event);
    }


    public void onClick_CheckEvent(View view){
        Point[] points = chooseArea.getRegion();
        BmobFile bmobImage = new BmobFile(image);

        System.out.println("SelectActivity:" + chooseArea.isFlag());
        uploadImage(bmobImage,points);
        button.setVisibility(View.INVISIBLE);
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
        for (int i=0; i<points.length; i++){
            pointStr += points[i].x + " " + points[i].y + " ";
        }

        Gift giftSending = new Gift();
        if (coordinate.equals("")){
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
                if(e == null){
                    Toast.makeText(thisContext, "send success!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SelectActivity.this, MainActivity.class);
                    intent.putExtra("imagePath", image.getAbsolutePath());
                    intent.putExtra("username",username);
                    startActivity(intent);
                }else if(callCount[0] < 5){
                    addGift(BmobImageUrl, points);
                    Log.i("bmob","failed："+e.getMessage()+","+e.getErrorCode());
                    callCount[0] ++;
                }
            }
        });
    }
}
