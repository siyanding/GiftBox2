package com.example.admin.giftbox2;

import android.os.Message;
import android.os.Handler;

public class SleepThread implements Runnable {

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//这里写handler要处理的工作，例如页面的文字的改变，
        }
    };


    @Override
    public void run() {
        while (true) {
            try {
                //线程休眠一秒钟
                Thread.sleep(3000);
                //通过Message对象来发送消息
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
