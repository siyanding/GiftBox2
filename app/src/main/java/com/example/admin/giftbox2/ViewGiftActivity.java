package com.example.admin.giftbox2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ViewGiftActivity extends AppCompatActivity {
    private static ImageView imageView;
//    private static String imagePath;
    protected static final int SUCCESS = 0;
    protected static final int ERROR = 1;
    protected static final int NETWORK_ERROR = 2;
    private static final String TAG = "ViewGiftActivity";
    private static Button b1;

    //use Handle to update main thread(UI thread)
    private static Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    imageView.setImageBitmap(bitmap);

                    b1.setVisibility(View.VISIBLE);
                    System.out.println("ViewGiftActivity: SUCCESS");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_gift);

        Intent intent = getIntent();
        String wholePath = intent.getStringExtra("whole");

        System.out.println("wholePath:" + wholePath);
        getImage(wholePath);

        imageView = (ImageView) findViewById(R.id.ViewGift);
        b1 = (Button) findViewById(R.id.SendGiftHome);
    }

    public void onClick_SendGiftHome(View view) {
        Intent intent = new Intent(ViewGiftActivity.this, MainActivity.class);
        startActivity(intent);
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
