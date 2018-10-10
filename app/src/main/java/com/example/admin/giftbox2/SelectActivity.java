package com.example.admin.giftbox2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class SelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_avtivity);

        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("imagePath");
        String username = intent.getStringExtra("username");

        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        ((ImageView)findViewById(R.id.image)).setImageBitmap(bm);
    }
}
