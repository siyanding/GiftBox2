package com.example.admin.giftbox2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String username;
    private long mExitTime = 0;
    private ListView listView;
    private Gift selectedGift;
    private Context thisContext;
    private ArrayList<Gift> totalGifts;
    private String potrait;
    private static ImageView portraitImageView;
    protected static final int SUCCESS = 0;
    protected static final int ERROR = 1;
    protected static final int NETWORK_ERROR = 2;

    public static final int REQUEST_CAMERA = 3;
    public static final int REQUEST_ALBUM = 4;
    public static final int REQUEST_CROP = 5;
    private File portraitFile;
    private BmobFile portraitBmob;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ContextHolder.initial(this);
        thisContext = ContextHolder.getContext();

        Intent intent = getIntent();
//        username = intent.getStringExtra("username");
        currentUser = BmobUser.getCurrentUser(User.class);
        username = currentUser.getUsername();
        potrait = currentUser.getPortrait();
        if (potrait != ""){
            getImage(potrait);
        }
        System.out.println("username-Main:" + username);
        System.out.println("objectID-Main:" + currentUser.getObjectId());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        portraitImageView = (ImageView) header.findViewById(R.id.portrait_imageView);
        portraitImageView.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                onClickPicker(v);
            }
        });

        listView = (ListView)findViewById(R.id.main_list);
        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            selectedGift = (Gift) listView.getItemAtPosition(position);
            viewGift();
        });
        giftSetUp();

        EditText searchEt = (EditText) findViewById(R.id.search_MainGift);
        TextWatcher searchTw = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int textlength = searchEt.getText().length();
                ArrayList<Gift> giftSort = new ArrayList<>();
                for (int i = 0; i < totalGifts.size(); i++){
                    String queryText = totalGifts.get(i).getUser();
                    if (textlength <= queryText.length()){
                        String friend= searchEt.getText().toString();
                        if(queryText.toString().contains(friend)){
                            giftSort.add(totalGifts.get(i));
                        }
                    }
                }
                ListAdapter adapter = new GiftAdapter(thisContext, giftSort,0);
                listView.setAdapter(adapter);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        searchEt.addTextChangedListener(searchTw);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = new Intent();
        switch (id){
            case  R.id.nav_gifts :
                intent = new Intent(MainActivity.this, GiftActivity.class);
                break;
            case R.id.nav_Edit_Profile :
                intent = new Intent(MainActivity.this, EditProfileActivity.class);
                break;
            case R.id.nav_friends :
                intent = new Intent(MainActivity.this, ContactActivity.class);
                break;
            case R.id.nav_logout :
                BmobUser.logOut();
                intent = new Intent(MainActivity.this, SignInActivity.class);
                break;
        }
        intent.putExtra("username",username);
        startActivity(intent);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {//
				// 如果两次按键时间间隔大于2000毫秒，则不退出
				Toast.makeText(this, "press to log out", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();// 更新mExitTime
			} else {
                BmobUser.logOut();
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

    private void giftSetUp() {
        BmobQuery<Gift> query = new BmobQuery<>();
        query.addWhereEqualTo( "recipient",username);
        query.setLimit(50);
        query.findObjects(new FindListener<Gift>() {
            @Override
            public void done(List<Gift> gifts, BmobException e) {
                if(e==null){
                    System.out.println("GiftActivity:user:" + username);
                    System.out.println("++++++++"+gifts.size());
                    totalGifts = new ArrayList<>(gifts);
                    ListAdapter adapter = new GiftAdapter(thisContext,gifts,0);
                    listView.setAdapter(adapter);

                }else{
                    Log.i("bmob","failed："+e.getMessage()+","+e.getErrorCode());
                    giftSetUp();
                }
            }
        });
    }

    public void viewGift(){
        Intent intent;
        if(selectedGift.getStatus()){
            //load image
            intent = new Intent(thisContext, ViewGiftActivity.class);
        }else{
            //navigate map
            intent = new Intent(thisContext, NavigateActivity.class);
        }
        intent.putExtra("imageUrl",selectedGift.getBackground());
        intent.putExtra("coordinate",selectedGift.getCoordinate());
        intent.putExtra("points",selectedGift.getPoints());
        intent.putExtra("whole",selectedGift.getWhole());
        intent.putExtra("objectId",selectedGift.getObjectId());
        System.out.println("imageUrl:"+ selectedGift.getBackground());
        startActivity(intent);
    }

    public void onClickPicker(View v) {
        new AlertDialog.Builder(this)
                .setTitle("Change Portrait")
                .setItems(new String[]{"Take a photo"}, new DialogInterface.OnClickListener() {//new String[]{"Take a photo", "Select from album"}
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            selectCamera();
                        } else {
                            selectAlbum();
                        }
                    }
                })
                .create()
                .show();
    }

    private void selectCamera() {
        createImageFile();
        if (!portraitFile.exists()) {
            return;
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(thisContext, BuildConfig.APPLICATION_ID+".fileprovider", portraitFile);//BuildConfig.APPLICATION_ID + ".fileProvider"
        } else {
            uri = Uri.fromFile(portraitFile);
        }
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }
    private void selectAlbum() {
        Intent albumIntent = new Intent(Intent.ACTION_PICK);
        albumIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(albumIntent, REQUEST_ALBUM);
    }
    private void cropImage(Uri uri){
        Intent cropApps = new Intent("com.android.camera.action.CROP");
        cropApps.setType("image/*");
        List<ResolveInfo> list = this.getPackageManager().queryIntentActivities(cropApps, 0);
        int size = list.size();
        if (size == 0){
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
        }else{
            ResolveInfo res = list.get(0);
            Intent cropIntent = new Intent();
            cropIntent.setClassName(res.activityInfo.packageName, res.activityInfo.name);
            cropIntent.setDataAndType(uri, "image/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
            }
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("outputX", 800);
            cropIntent.putExtra("outputY", 800);
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra("return-data", true);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(portraitFile));
            startActivityForResult(cropIntent, REQUEST_CROP);
        }
    }
    private void createImageFile() {
        portraitFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
        try {
            portraitFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "出错啦", Toast.LENGTH_SHORT).show();
        }
        portraitBmob = new BmobFile(portraitFile);
    }

    public void uploadImage() {
        final int[] callCount = {0};
        portraitBmob.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    updatePortrait();
                } else if (callCount[0] < 5) {
                    System.out.println("Bmob image upload failed" + e);
                    uploadImage();
                    callCount[0]++;
                }
            }

            @Override
            public void onProgress(Integer value) {
                // return the percent of uploading
            }
        });
    }

    public void updatePortrait(){
        String userId = currentUser.getObjectId();
        User updateUser = new User();
        updateUser.setValue("portrait",portraitBmob.getFileUrl());
        updateUser.update(userId, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    Log.i("bmob","更新成功");
                }else{
                    Log.i("bmob","更新失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RESULT_OK != resultCode) {
            return;
        }
        Uri portraitUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            portraitUri = FileProvider.getUriForFile(thisContext, BuildConfig.APPLICATION_ID+".fileprovider", portraitFile);//BuildConfig.APPLICATION_ID + ".fileProvider"
        } else {
            portraitUri = Uri.fromFile(portraitFile);
        }
        switch (requestCode) {
            case REQUEST_CAMERA:
                cropImage(portraitUri);
                break;
            case REQUEST_ALBUM:
                createImageFile();
                if (!portraitFile.exists()) {
                    return;
                }
                Uri uri = data.getData();
                if (uri != null) {
                    cropImage(uri);
                }
                break;
            case REQUEST_CROP:
                portraitImageView.setImageURI(portraitUri);
                uploadImage();
                break;
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

    //use Handle to update main thread(UI thread)
    private static Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    System.out.println("giftBitmap download success");
                    Bitmap portraitBitmap = (Bitmap) msg.obj;
                    portraitImageView.setImageBitmap(portraitBitmap);
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
}