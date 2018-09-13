package com.example.admin.giftbox2;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class SignInActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Bmob.initialize(this, "b2ab2a965d7a4ea905eeba56d4a2fa4d");
    }

    public void onClick_SignInEvent(View view) {
        final TextView textview = (TextView) findViewById(R.id.textView1);

        EditText editTextUser = (EditText) findViewById(R.id.username_edit);
        final String username = editTextUser.getText().toString();

        EditText editTextPass = (EditText) findViewById(R.id.password_edit);
        final String password = editTextPass.getText().toString();

        signIn(username,password,textview);
    }

    public void signIn(final String username, final String password, final TextView textview){
        final int[] callCount = { 0 };
        BmobUser user = new BmobUser();
        user.setUsername(username);
        user.setPassword(password);
        user.login(new SaveListener<BmobUser>() {

            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                if(e==null){
                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    //通过BmobUser user = BmobUser.getCurrentUser()获取登录成功后的本地用户信息
                    //如果是自定义用户对象MyUser，可通过MyUser user = BmobUser.getCurrentUser(MyUser.class)获取自定义用户信息
                }else if(callCount[0] < 5){
                    callCount[0] ++;
                    signIn(username,password,textview);
                    Log.i("bmob", "failed：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

    public void onClick_SignUpEvent(View view) {
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(intent);
    }
}

