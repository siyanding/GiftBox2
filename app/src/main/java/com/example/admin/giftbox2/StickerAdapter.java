package com.example.admin.giftbox2;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class StickerAdapter extends BaseAdapter {

    private Context mContext;
    private static int[] mImageIds = new int[] { R.drawable.ic_sticker_photo,
            R.drawable.apple, R.drawable.apple1, R.drawable.apple2, R.drawable.heart1,
            R.drawable.heart2, R.drawable.strawberry1, R.drawable.unicorn1,
            R.drawable.unicorn2, R.drawable.unicorn3,
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
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(175, 175));
            imageView.setScaleType(ImageView.ScaleType.CENTER);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setBackgroundResource(mImageIds[position]);//.setBackgroundResource(mImageIds[position]);
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
