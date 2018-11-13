package com.example.admin.giftbox2;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class StickerAdapter extends BaseAdapter {

    private Context mContext;
    private String imagePath = "http://bmob-cdn-17499.b0.upaiyun.com/2018/11/13/63ee403740d424ea80235d2758ea850d.png";
    private static int[] mImageIds = new int[] { R.drawable.ic_sticker_photo,
            R.drawable.apple, R.drawable.ic_sticker_heart1, R.drawable.ic_sticker_heart1, R.drawable.ic_sticker_heart1,
            R.drawable.ic_sticker_heart2, R.drawable.ic_sticker_heart2, R.drawable.ic_sticker_heart2, R.drawable.ic_sticker_heart2,
            R.drawable.ic_sticker_heart2, R.drawable.ic_sticker_apple2, R.drawable.ic_sticker_apple2, R.drawable.ic_sticker_apple2,
            R.drawable.ic_sticker_apple2, R.drawable.ic_sticker_apple2, R.drawable.ic_sticker_apple1, R.drawable.ic_sticker_apple1,
            R.drawable.ic_sticker_apple1, R.drawable.ic_sticker_apple1, R.drawable.ic_sticker_apple1, R.drawable.ic_sticker_apple3,
            R.drawable.ic_sticker_apple3, R.drawable.ic_sticker_apple3, R.drawable.ic_sticker_apple3, R.drawable.ic_sticker_apple3,
            R.drawable.ic_sticker_heart1, R.drawable.ic_sticker_heart1, R.drawable.ic_sticker_heart1, R.drawable.ic_sticker_heart1,
            R.drawable.ic_sticker_heart2, R.drawable.ic_sticker_heart2, R.drawable.ic_sticker_heart2, R.drawable.ic_sticker_heart2,
            R.drawable.ic_sticker_heart2, R.drawable.ic_sticker_apple2, R.drawable.ic_sticker_apple2, R.drawable.ic_sticker_apple2,
            R.drawable.ic_sticker_apple2, R.drawable.ic_sticker_apple2, R.drawable.ic_sticker_apple1, R.drawable.ic_sticker_apple1,
            R.drawable.ic_sticker_apple1, R.drawable.ic_sticker_apple1, R.drawable.ic_sticker_apple1, R.drawable.ic_sticker_apple3,
            R.drawable.ic_sticker_apple3, R.drawable.ic_sticker_apple3, R.drawable.ic_sticker_apple3, R.drawable.ic_sticker_apple3,
            };

    public StickerAdapter(Context c) {
        mContext = c;
    }

    // 获取图片的总id
    public static int[] getImageIds() {
        return mImageIds;
    }

    // 获取图片的个数
    public int getCount() {
        return mImageIds.length;
    }

    // 获取图片在库中的位置
    public Object getItem(int position) {
        return position;
    }

    // 获取图片ID
    public long getItemId(int position) {
        return mImageIds[position];
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(175, 175));
            imageView.setScaleType(ImageView.ScaleType.CENTER);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setBackgroundResource(mImageIds[position]);
        imageView.setForegroundGravity(View.TEXT_ALIGNMENT_CENTER);
//        imageView.setImageResource(mImageIds[position]);
        if (position < 65) {
            imageView.setTag("[" + position + "]");
        } else if (position < 100) {
            imageView.setTag("[" + (position + 1) + "]");
        } else {
            imageView.setTag("[" + (position + 2) + "]");
        }
        return imageView;
    }

}
