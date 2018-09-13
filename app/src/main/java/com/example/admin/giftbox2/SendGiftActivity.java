package com.example.admin.giftbox2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SendGiftActivity extends AppCompatActivity {

    private static int IMAGE_MAX_WIDTH  = 480;
    private static int IMAGE_MAX_HEIGHT = 960;
    private  String imagePath;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_gift);

        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imagePath");
        username = intent.getStringExtra("username");

        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = getImageScale(imagePath);
        Bitmap bm = BitmapFactory.decodeFile(imagePath, option);
        ImageView imageView = (ImageView) findViewById(R.id.gift);
        imageView.setImageBitmap(bm);
    }

    private static int getImageScale(String imagePath) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, option);

        int scale = 1;
        while (option.outWidth / scale >= IMAGE_MAX_WIDTH || option.outHeight / scale >= IMAGE_MAX_HEIGHT) {
            scale *= 2;
        }
        return scale;
    }

    public void onClick_SendGiftEvent(View view) {
        TextView tv = (TextView) findViewById(R.id.SendGiftMessage);
        tv.setText("gift sent success!");
        Button b1 = (Button) findViewById(R.id.SendGiftHome);
        b1.setVisibility(View.VISIBLE);
        Button b2 = (Button) findViewById(R.id.SendGiftAnother);
        b2.setVisibility(View.VISIBLE);
    }

    public void onClick_SendGiftHome(View view) {
        Intent intent = new Intent(SendGiftActivity.this, MainActivity.class);
        intent.putExtra("username",username);
        startActivity(intent);
    }

    public void onClick_SendGiftAnother(View view) {
        Intent intent = new Intent(SendGiftActivity.this, ContactActivity.class);
        intent.putExtra("username",username);
        startActivity(intent);
    }
}
