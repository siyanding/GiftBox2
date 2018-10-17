package com.example.admin.giftbox2;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class EditProfileActivity extends AppCompatActivity {
    private String username;
    private Context thisContext;
    private EditText passEdit,confirmEdit;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ContextHolder.initial(this);
        thisContext = ContextHolder.getContext();

        passEdit = (EditText) findViewById(R.id.password_edit_Profile);
        confirmEdit = (EditText) findViewById(R.id.ConfirmPassword_edit_Profile);
        saveButton = findViewById(R.id.button_save_Profile);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        System.out.println("username:" + username);
    }

    public void onClick_PassClear(View view) {
        passEdit.setText("");
    }

    public void onClick_ConfirmClear(View view) {
        confirmEdit.setText("");
    }

    public void onClick_PassVisible(View view) {
        if(passEdit.getInputType() == (InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD)){
//            bt_pwd_eye.setBackgroundResource(R.drawable.button_eye_s);
            passEdit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_NORMAL);
        }else{
//            bt_pwd_eye.setBackgroundResource(R.drawable.button_eye_n);
            passEdit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    public void onClick_confirmVisible(View view) {
        if(confirmEdit.getInputType() == (InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD)){
//            bt_pwd_eye.setBackgroundResource(R.drawable.button_eye_s);
            confirmEdit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_NORMAL);
        }else{
//            bt_pwd_eye.setBackgroundResource(R.drawable.button_eye_n);
            confirmEdit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    public void onClickEditProfileEvent(View view) {
        String password = passEdit.getText().toString();
        String confirmPassword = confirmEdit.getText().toString();

        if(isEntered(password) || isEntered(confirmPassword)){
            Toast.makeText(thisContext, "All fields must be filled", Toast.LENGTH_SHORT).show();
        }else if (! password.equals(confirmPassword)){
            Toast.makeText(thisContext, "The two passwords must be the same", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(thisContext, "Update success!", Toast.LENGTH_SHORT).show();
                }else if(callCount[0] < 5){
                    saveProfile(user, newPassword);
                    Toast.makeText(thisContext, "Update failed! Try again", Toast.LENGTH_SHORT).show();
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
