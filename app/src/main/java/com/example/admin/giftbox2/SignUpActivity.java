package com.example.admin.giftbox2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class SignUpActivity extends AppCompatActivity {

    private TextView textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

    }

    public void onClick_SignUpEvent(View view) {
        textview = (TextView)findViewById(R.id.textViewSignUp);

        EditText editTextUser =(EditText)findViewById(R.id.username_edit);
        final String username = editTextUser.getText().toString();

        EditText editTextPass =(EditText)findViewById(R.id.password_edit);
        final String password = editTextPass.getText().toString();

        EditText editTextConfirmPass =(EditText)findViewById(R.id.ConfirmPassword_edit);
        final String ConfirmPassword = editTextConfirmPass.getText().toString();

        EditText editTextEmail =(EditText)findViewById(R.id.email_edit);
        final String email = editTextEmail.getText().toString();

        if(! password.equals(ConfirmPassword)){
            textview.setText("two passwords are not the same");
        }
        signUp(username, password, email);
    }

    public void signUp(final String username, final String password, final String email){
        final int[] callCount = { 0 };
        BmobUser user = new BmobUser();
        user.setUsername(username);
        user.setPassword(password);//BCrypt.hashpw(password, BCrypt.gensalt())
        user.setEmail(email);
        user.signUp(new SaveListener<User>() {
            @Override
            public void done(User s, BmobException e) {
                if(e==null){
                    textview.setText("sign Up success," +
                            "a confirm email will send to your email address," +
                            "go to sign in page now");
                    Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                    startActivity(intent);
                    System.out.println("sign up success");
                }else if(callCount[0] < 5){
                    callCount[0] ++;
                    signUp(username,password,email);
                    Log.i("bmob", "failed：" + e.getMessage() + "," + e.getErrorCode());

                }
            }
        });
    }
}
