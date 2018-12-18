package com.example.admin.giftbox2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class ContactActivity extends AppCompatActivity {

    private ListView listView;
    private Friend personOnClick;
    private String username;
    private File image;
    private Context thisContext;
    private BmobFile bmobImage;
    private String BmobImageUrl;
    private String coordinate;
    private ArrayList<Friend> totalFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        ContextHolder.initial(this);
        thisContext = ContextHolder.getContext();
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(thisContext);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        for(int i=0; i<5; i++){
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            coordinate = location.getLongitude() + " " + location.getLatitude();
                            System.out.println("point[0]" + coordinate);
                        }
                    });
        }
        User currentUser = BmobUser.getCurrentUser(User.class);
        username = currentUser.getUsername();
        listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemLongClickListener((arg0, arg1, position, arg3) -> {
            personOnClick = (Friend) listView.getItemAtPosition(position);
            listView.setOnCreateContextMenuListener((menu, arg11, arg2) -> {
                menu.setHeaderTitle("Operations");
                menu.add(0, 0, 0, "Send Message");
                menu.add(0, 1, 0, "Delete");
            });
            return false;
        });

        contactSetUp();

        EditText searchEt = (EditText) findViewById(R.id.search_contact);
        TextWatcher searchTw = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int textlength = searchEt.getText().length();
                ArrayList<Friend> friendsSort = new ArrayList<>();
                for (int i = 0; i < totalFriends.size(); i++){
                    if (textlength <= totalFriends.get(i).getFriend().length()){
                        String friend= searchEt.getText().toString();
                        if(totalFriends.get(i).getFriend().toString().contains(friend)){
                            friendsSort.add(totalFriends.get(i));
                        }
                    }
                }
                ListAdapter adapter = new FriendAdapter(thisContext, friendsSort);
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

    public void onClickAddFriendEvent(View view) {
        Intent intent = new Intent(ContactActivity.this, SearchActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void sendMessage() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "giftbox");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        image = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("recipient", personOnClick.getFriend());
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(thisContext, BuildConfig.APPLICATION_ID+".fileprovider", image);//BuildConfig.APPLICATION_ID + ".fileProvider"
        } else {
            uri = Uri.fromFile(image);
        }
        // set the address for picture
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, 0);
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            sendMessage();
        } else {
            deleteFriend();
        }
        return super.onContextItemSelected(item);
    }

    public void contactSetUp() {
        final int[] callCount = {0};
        BmobQuery<Friend> query = new BmobQuery<>();
        query.addWhereEqualTo("user", username);
        query.setLimit(50);
        query.findObjects(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> friends, BmobException e) {
                if (e == null) {
                    totalFriends = new ArrayList<>(friends);
                    System.out.println("ContactActivity:totalFriends:" + totalFriends.size());
                    ListAdapter adapter = new FriendAdapter(thisContext, friends);
                    listView.setAdapter(adapter);
                } else if (callCount[0] < 5) {
                    contactSetUp();
                    Log.i("bmob", "failed：" + e.getMessage() + "," + e.getErrorCode());
                    callCount[0]++;
                }
            }
        });
    }

    public void deleteFriend() {
        final int[] callCountDelete = {0};
        String deleteId = personOnClick.getObjectId();
        Friend friendToDelete = new Friend();
        friendToDelete.setObjectId(deleteId);
        friendToDelete.delete(new UpdateListener() {

            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Log.i("bmob", "success");
                    contactSetUp();
                } else if (callCountDelete[0] < 5) {
                    Log.i("bmob", "failed：" + e.getMessage() + "," + e.getErrorCode());
                    callCountDelete[0]++;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int result, Intent data) {
        super.onActivityResult(requestCode, result, data);

        if (requestCode == 0 && result == RESULT_OK) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(image.getPath(), options);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int w = width >= height ? 800 : 600;
                int h = width >= height ? 600 : 800;
                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, false);
                try (FileOutputStream out = new FileOutputStream(image.getPath())) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                    // PNG is a lossless format, the compression factor (100) is ignored
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bmobImage = new BmobFile(image);
                Intent intent = new Intent(ContactActivity.this, SelectActivity.class);
                intent.putExtra("imagePath", image.getAbsolutePath());
                intent.putExtra("username",username);
                intent.putExtra("coordinate",coordinate);
                intent.putExtra("recipient",personOnClick.getFriend());

                startActivity(intent);
//                uploadBackground();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addGift() {
        final int[] callCount = {0};
        Gift giftSending = new Gift();
        if (coordinate.equals("")){
            System.out.println("GPS value false");
        }
        giftSending.setBackground(BmobImageUrl);
        giftSending.setUser(username);
        giftSending.setRecipient(personOnClick.getFriend());
        giftSending.setStatus(false);
        giftSending.setCoordinate(coordinate);
        giftSending.save(new SaveListener<String>() {

            @Override
            public void done(String giftId, BmobException e) {
                if(e == null){
                    Intent intent = new Intent(ContactActivity.this, SendGiftActivity.class);
                    intent.putExtra("imagePath", image.getAbsolutePath());
                    intent.putExtra("username",username);
                    startActivity(intent);
                }else if(callCount[0] < 5){
                    addGift();
                    Log.i("bmob","failed："+e.getMessage()+","+e.getErrorCode());
                    callCount[0] ++;
                }
            }
        });
    }
}
