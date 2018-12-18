package com.example.admin.giftbox2;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class GiftActivity extends AppCompatActivity {

    private Gift selectedGift;
    private String username;
    private Context thisContext;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift);
        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        ContextHolder.initial(this);
        thisContext = ContextHolder.getContext();

        BmobUser currentUser = BmobUser.getCurrentUser();
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        if (username == ""){
            username = currentUser.getUsername();
            System.out.println("currentErin:" + username);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gift, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent = new Intent(GiftActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static class PlaceholderFragment extends Fragment{
        private static final String ARG_SECTION_NUMBER = "section_number";
        private View rootView;
        private ListView listView;
        private ArrayList<Gift> totalGifts;
        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            int sectionNum = getArguments().getInt(ARG_SECTION_NUMBER);
            rootView = inflater.inflate(R.layout.rootview_gift, container, false);
            listView = (ListView)rootView.findViewById(R.id.listview);
            TextView titleUser = rootView.findViewById(R.id.title_user);
            titleUser.setText(sectionNum == 1 ? "sent to" : "sent by");
            listView.setOnItemClickListener((adapterView, view, position, id) -> {
                ((GiftActivity)getActivity()).selectedGift = (Gift) listView.getItemAtPosition(position);
                viewGift(sectionNum);
            });
            giftSetUp(sectionNum);

            EditText searchEt = (EditText) rootView.findViewById(R.id.search_gift);
            TextWatcher searchTw = new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int textlength = searchEt.getText().length();
                    int section = getArguments().getInt(ARG_SECTION_NUMBER);

                    ArrayList<Gift> giftSort = new ArrayList<>();
                    for (int i = 0; i < totalGifts.size(); i++){
                        String queryText = section == 1 ?  totalGifts.get(i).getRecipient() : totalGifts.get(i).getUser();
                        if (textlength <= queryText.length()){
                            String friend= searchEt.getText().toString();
                            if(queryText.toString().contains(friend)){
                                giftSort.add(totalGifts.get(i));
                            }
                        }
                    }
                    ListAdapter adapter = new GiftAdapter(((GiftActivity)getActivity()).thisContext, giftSort,getArguments().getInt(ARG_SECTION_NUMBER));
                    listView.setAdapter(adapter);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            };
            searchEt.addTextChangedListener(searchTw);
            return rootView;
        }

        private void giftSetUp(int sectionNum) {
            System.out.println("GiftActivity1:"+sectionNum);
            BmobQuery<Gift> query = new BmobQuery<>();
            query.addWhereEqualTo( sectionNum == 1 ? "user" : "recipient",((GiftActivity)getActivity()).username);
            query.setLimit(50);
            query.findObjects(new FindListener<Gift>() {
                @Override
                public void done(List<Gift> gifts, BmobException e) {
                    if(e==null){
                        System.out.println("GiftActivity:user:"+((GiftActivity)getActivity()).username);
                        System.out.println("++++++++"+gifts.size());
                        totalGifts = new ArrayList<>(gifts);
                        ListAdapter adapter = new GiftAdapter(((GiftActivity)getActivity()).thisContext,gifts,sectionNum);
                        listView.setAdapter(adapter);

                    }else{
                        Log.i("bmob","failed："+e.getMessage()+","+e.getErrorCode());
                        giftSetUp(sectionNum);
                    }
                }
            });
        }

        public void viewGift(int sectionNum){
            Intent intent;
            if(((GiftActivity)getActivity()).selectedGift.getStatus() || sectionNum == 1){
                //load image
                intent = new Intent(((GiftActivity)getActivity()).thisContext, ViewGiftActivity.class);
            }else{
                //navigate map
                intent = new Intent(((GiftActivity)getActivity()).thisContext, NavigateActivity.class);
            }
            intent.putExtra("imageUrl",((GiftActivity)getActivity()).selectedGift.getBackground());
            intent.putExtra("coordinate",((GiftActivity)getActivity()).selectedGift.getCoordinate());
            intent.putExtra("points",((GiftActivity)getActivity()).selectedGift.getPoints());
            intent.putExtra("whole",((GiftActivity)getActivity()).selectedGift.getWhole());
            intent.putExtra("objectId",((GiftActivity)getActivity()).selectedGift.getObjectId());
            System.out.println("imageUrl:"+ ((GiftActivity)getActivity()).selectedGift.getBackground());
            startActivity(intent);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0 ? "Sent" : "Received";
        }
    }
}
