package com.example.admin.giftbox2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class GiftAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Gift> mGifts;
    private DisplayMetrics displayMetrics;
    private int sectionNum;
    private static ImageView userImageView;
    private User currentUser;

    protected static final int SUCCESS = 0;
    protected static final int ERROR = 1;
    protected static final int NETWORK_ERROR = 2;

    public GiftAdapter(Context context, List<Gift> gifts, int sectionNum) {
        mInflater = (LayoutInflater)context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        displayMetrics = context.getResources().getDisplayMetrics();
        mGifts = gifts;
        this.sectionNum = sectionNum;
    }

    @Override
    public int getCount() {
        return mGifts == null ? 0 : mGifts.size();
    }

    @Override
    public Object getItem(int position) {
        return mGifts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = mInflater.inflate(R.layout.fragment_gift, parent,
                    false);
        } else {
            v = convertView;
        }
        Gift item = (Gift) getItem(position);
//        findUser(sectionNum == 1 ? item.getRecipient() : item.getUser());
        userImageView  = (ImageView) v.findViewById(R.id.item_portrait);
        TextView usernameTv = (TextView) v.findViewById(R.id.item_username);
        TextView timeTv = (TextView) v.findViewById(R.id.item_time);
        final CheckBox checkbox = (CheckBox) v.findViewById(R.id.item_check);

//        usernameTv.setWidth(displayMetrics.widthPixels/3);
        usernameTv.setText(sectionNum == 1 ? item.getRecipient() : item.getUser());
        timeTv.setText(timeFormat(item.getCreatedAt().toString().trim()));
        checkbox.setChecked(item.getStatus());
        checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkbox.setChecked(!isChecked);
        });
        return v;
    }

    public String timeFormat(String time){
        SimpleDateFormat bjSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        bjSdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date date = null;
        try {
            date = bjSdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat usFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        usFormat.setTimeZone(TimeZone.getTimeZone("America/Chicago"));
//        System.out.println("北京时间: " + time +"对应的东京时间为:"  + tokyoSdf.format(date));
        return usFormat.format(date);
    }
}
