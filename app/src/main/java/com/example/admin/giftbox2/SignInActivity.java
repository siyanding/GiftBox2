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
import android.widget.Toast;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class SignInActivity extends AppCompatActivity{

    private EditText userNameEdit, passwordEdit;
    private Context thisContext;
    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ContextHolder.initial(this);
        thisContext = ContextHolder.getContext();

        userNameEdit = (EditText) findViewById(R.id.username);
        passwordEdit = (EditText) findViewById(R.id.password);
        signInButton = findViewById(R.id.login);

        Bmob.initialize(this, "b2ab2a965d7a4ea905eeba56d4a2fa4d");
    }

    public void onClick_SignInEvent(View view) {
        String username = userNameEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        signIn(username,password);
    }

    public void onClick_PassClear(View view) {
        passwordEdit.setText("");
    }

    public void onClick_UserClear(View view) {
        userNameEdit.setText("");
    }

    public void onClick_SignUpEvent(View view) {
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    public void onClick_PassVisible(View view) {
        if(passwordEdit.getInputType() == (InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD)){
//            bt_pwd_eye.setBackgroundResource(R.drawable.button_eye_s);
            passwordEdit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_NORMAL);
        }else{
//            bt_pwd_eye.setBackgroundResource(R.drawable.button_eye_n);
            passwordEdit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    public void signIn(final String username, final String password){
        final int[] callCount = { 0 };
        BmobUser user = new BmobUser();
        user.setUsername(username);
        user.setPassword(password);
        user.login(new SaveListener<BmobUser>() {

            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                if(e==null){
                    Toast.makeText(thisContext, "Signing in...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    signInButton.setClickable(false);
                    passwordEdit.setText("");
                    userNameEdit.setText("");
                }else if(e.getErrorCode() == 101){
                    Toast.makeText(thisContext, "username or password incorrect", Toast.LENGTH_SHORT).show();
                }else if(e.getErrorCode() == 109){
                    Toast.makeText(thisContext, "username or password missing", Toast.LENGTH_SHORT).show();
                }else{//if(callCount[0] < 5)
//                    callCount[0] ++;
//                    signIn(username,password);
                    Toast.makeText(thisContext, "something wrong, please try again", Toast.LENGTH_SHORT).show();
                    Log.i("bmob", "failed：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }
}

