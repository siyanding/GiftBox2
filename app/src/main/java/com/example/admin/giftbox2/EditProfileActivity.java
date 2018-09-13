package com.example.admin.giftbox2;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class EditProfileActivity extends AppCompatActivity {
    private TextView textview;
    private String username;
    private Context thisContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ContextHolder.initial(this);
        thisContext = ContextHolder.getContext();

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        System.out.println("username:" + username);
    }

    public void onClickEditProfileEvent(View view) {
        textview = (TextView)findViewById(R.id.textView_Edit_Profile);

        EditText editTextPass =(EditText)findViewById(R.id.password_edit_Profile);
        String password = editTextPass.getText().toString();

        EditText editTextConfirmPass =(EditText)findViewById(R.id.ConfirmPassword_edit_Profile);
        String confirmPassword = editTextConfirmPass.getText().toString();

        if(isEntered(password) || isEntered(confirmPassword)){
            textview.setText("All fields must be filled");
        }else if (! password.equals(confirmPassword)){
            textview.setText("The two passwords must be the same");
        }else {
            updateProfile(username, password);
        }
    }


    public void updateProfile(String username, final String newPassword){
        final int[] callCount = { 0 };
        BmobQuery<User> query = new BmobQuery<>();
        query.addWhereEqualTo("username", username);
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> users, BmobException e) {
                if(e == null){
                    System.out.println("EditProfile: query success");
                    saveProfile(users.get(0),newPassword);
                }else if(callCount[0] < 5){
                    Log.i("bmob","failed："+e.getMessage()+","+e.getErrorCode());
                    saveProfile(users.get(0),newPassword);
                    callCount[0] ++;
                }
            }
        });
    }

    public void saveProfile(final BmobUser user, final String newPassword){
        final int[] callCount = { 0 };
        user.setPassword(newPassword);
        user.update(user.getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    textview.setText("Update success!");
                }else if(callCount[0] < 5){
                    saveProfile(user, newPassword);
                    textview.setText("Update failed!");
                    Log.i("bmob","update failed："+e.getMessage()+","+e.getErrorCode());
                    callCount[0] ++;
                }
            }
        });
    }
    public boolean isEntered(String str){
        return str == null || str.length() == 0;
    }
}