package com.example.admin.giftbox2;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class GiftAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Gift> mGifts;
    private DisplayMetrics displayMetrics;
    private int sectionNum;

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

        ImageView imageView1 = (ImageView) v.findViewById(R.id.item_gift);
//        TextView tv2 = (TextView) v.findViewById(R.id.item_comment);
        TextView tv3 = (TextView) v.findViewById(R.id.item_recipient);
        final CheckBox checkbox = (CheckBox) v.findViewById(R.id.item_check);

        imageView1.setMaxWidth(displayMetrics.widthPixels/3);
//        tv2.setWidth(displayMetrics.widthPixels/4);
        tv3.setWidth(displayMetrics.widthPixels/3);
        checkbox.setWidth(displayMetrics.widthPixels/3);
//        tv2.setText(item.getComment());
        tv3.setText(sectionNum == 1 ? item.getRecipient() : item.getUser());
        checkbox.setChecked(item.getStatus());
        checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkbox.setChecked(!isChecked);
        });
        return v;
    }
}
