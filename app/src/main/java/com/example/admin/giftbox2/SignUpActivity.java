package com.example.admin.giftbox2;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class SignUpActivity extends AppCompatActivity {


    private Context thisContext;
    private EditText editTextUser,editTextPass,editTextConfirm,editTextEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ContextHolder.initial(this);
        thisContext = ContextHolder.getContext();

        editTextUser =(EditText)findViewById(R.id.username_edit);
        editTextPass = (EditText)findViewById(R.id.password_edit);
        editTextConfirm = (EditText)findViewById(R.id.ConfirmPassword_edit);
        editTextEmail = (EditText)findViewById(R.id.email_edit);
    }

    public void onClick_PassClear(View view) {
        editTextPass.setText("");
    }

    public void onClick_UserClear(View view) {
        editTextUser.setText("");
    }

    public void onClick_ConfirmPassClear(View view) {
        editTextUser.setText("");
    }

    public void onClick_EmailClear(View view) {
        editTextUser.setText("");
    }

    public void onClick_PassVisible(View view) {
        if(editTextPass.getInputType() == (InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD)){
//            bt_pwd_eye.setBackgroundResource(R.drawable.button_eye_s);
            editTextPass.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_NORMAL);
        }else{
//            bt_pwd_eye.setBackgroundResource(R.drawable.button_eye_n);
            editTextPass.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    public void onClick_ConfirmPassVisible(View view) {
        if(editTextConfirm.getInputType() == (InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD)){
//            bt_pwd_eye.setBackgroundResource(R.drawable.button_eye_s);
            editTextConfirm.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_NORMAL);
        }else{
//            bt_pwd_eye.setBackgroundResource(R.drawable.button_eye_n);
            editTextConfirm.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    public void onClick_SignUpEvent(View view) {

        editTextUser =(EditText)findViewById(R.id.username_edit);
        editTextPass = (EditText)findViewById(R.id.password_edit);
        editTextConfirm = (EditText)findViewById(R.id.ConfirmPassword_edit);
        editTextEmail = (EditText)findViewById(R.id.email_edit);

        String username = editTextUser.getText().toString();
        String password = editTextPass.getText().toString();
        String confirmPassword = editTextConfirm.getText().toString();
        String email = editTextEmail.getText().toString();

        if(isValid(username, password, confirmPassword, email)){
            signUp(username, password, email);
        }
    }

    public boolean isValid(String username, String password, String confirmPassword, String email){
        if (username.equals("")){
            Toast.makeText(thisContext, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password .equals("")){
            Toast.makeText(thisContext, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (confirmPassword.equals("")){
            Toast.makeText(thisContext, "Confirm password cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (email.equals("")){
            Toast.makeText(thisContext, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirmPassword)){
            Toast.makeText(thisContext, "Two passwords must be the same", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void signUp(String username, String password, String email){
        BmobUser user = new BmobUser();
        user.setUsername(username);
        user.setPassword(password);//BCrypt.hashpw(password, BCrypt.gensalt())
        user.setEmail(email);
        user.signUp(new SaveListener<User>() {
            @Override
            public void done(User s, BmobException e) {
                if(e==null){
                    Toast.makeText(thisContext, "Thank you for join us. " +
                            "You will be log in after 3 seconds", Toast.LENGTH_SHORT).show();
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            startActivity(new Intent(thisContext, MainActivity.class));
                        }
                    }, 3000);
                }else if(e.getErrorCode() == 301){
                    Toast.makeText(thisContext, "This email may used by other users or not valid", Toast.LENGTH_SHORT).show();
                }else if (e.getErrorCode() == 202){
                    Toast.makeText(thisContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(thisContext, "Sign up failed, please try again.(" + e.getMessage() + ")", Toast.LENGTH_SHORT).show();
                    Log.i("bmob", "failed：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }
}
