package com.example.admin.giftbox2;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class FriendAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Friend> mPersons;
    private DisplayMetrics displayMetrics;

    public FriendAdapter(Context context, List<Friend> persons) {
        mInflater = (LayoutInflater)context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        displayMetrics = context.getResources().getDisplayMetrics();
        mPersons = persons;
    }

    @Override
    public int getCount() {
        return (mPersons == null) ? 0 : mPersons.size();
    }

    @Override
    public Object getItem(int position) {
        return mPersons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        System.out.println("I am Groot");
        View v;
        if (convertView == null) {
            v = mInflater.inflate(R.layout.item_contact, parent,
                    false);
        } else {
            v = convertView;
        }
        Friend item = (Friend) getItem(position);
        ImageView imageView1 = (ImageView) v.findViewById(R.id.item_portrait);
        TextView tv2 = (TextView) v.findViewById(R.id.item_user);
        imageView1.setMaxWidth(displayMetrics.widthPixels/4);
        tv2.setWidth(displayMetrics.widthPixels/4);
        tv2.setText(item.getFriend());

        return v;
    }
}
