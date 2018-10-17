package com.example.admin.giftbox2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String username;
    private TextView textView, textViewHeader;
    private long mExitTime = 0;
    private ListView listView;
    private Gift selectedGift;
    private Context thisContext;
    private ArrayList<Gift> totalGifts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ContextHolder.initial(this);
        thisContext = ContextHolder.getContext();

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show());
        Intent intent = getIntent();

        username = intent.getStringExtra("username");
        System.out.println("username-Main:" + username);
//        textView = (TextView) findViewById(R.id.textView1);
////        textView.setText(username);
//
//        textViewHeader = (TextView) findViewById(R.id.textViewHeader);
//        textViewHeader.setText(username + "yeah!");


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listView = (ListView)findViewById(R.id.main_list);
        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            selectedGift = (Gift) listView.getItemAtPosition(position);
            viewGift();
        });
        giftSetUp();

        EditText searchEt = (EditText) findViewById(R.id.search_MainGift);
        TextWatcher searchTw = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int textlength = searchEt.getText().length();
                ArrayList<Gift> giftSort = new ArrayList<>();
                for (int i = 0; i < totalGifts.size(); i++){
                    String queryText = totalGifts.get(i).getUser();
                    if (textlength <= queryText.length()){
                        String friend= searchEt.getText().toString();
                        if(queryText.toString().contains(friend)){
                            giftSort.add(totalGifts.get(i));
                        }
                    }
                }
                ListAdapter adapter = new GiftAdapter(thisContext, giftSort,0);
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
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = new Intent();
        switch (id){
            case  R.id.nav_gifts :
                intent = new Intent(MainActivity.this, GiftActivity.class);
                break;
            case R.id.nav_Edit_Profile :
                intent = new Intent(MainActivity.this, EditProfileActivity.class);
                break;
            case R.id.nav_friends :
                intent = new Intent(MainActivity.this, ContactActivity.class);
                break;
            case R.id.nav_logout :
                BmobUser.logOut();
                intent = new Intent(MainActivity.this, SignInActivity.class);
                break;
        }
        intent.putExtra("username",username);
        startActivity(intent);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {//
				// 如果两次按键时间间隔大于2000毫秒，则不退出
				Toast.makeText(this, "press to log out", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();// 更新mExitTime
			} else {
                BmobUser.logOut();
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

    private void giftSetUp() {
        BmobQuery<Gift> query = new BmobQuery<>();
        query.addWhereEqualTo( "recipient",username);
        query.setLimit(50);
        query.findObjects(new FindListener<Gift>() {
            @Override
            public void done(List<Gift> gifts, BmobException e) {
                if(e==null){
                    System.out.println("GiftActivity:user:" + username);
                    System.out.println("++++++++"+gifts.size());
                    totalGifts = new ArrayList<>(gifts);
                    ListAdapter adapter = new GiftAdapter(thisContext,gifts,0);
                    listView.setAdapter(adapter);

                }else{
                    Log.i("bmob","failed："+e.getMessage()+","+e.getErrorCode());
                    giftSetUp();
                }
            }
        });
    }

    public void viewGift(){
        Intent intent;
        if(selectedGift.getStatus()){
            //load image
            intent = new Intent(thisContext, ViewGiftActivity.class);
        }else{
            //navigate map
            intent = new Intent(thisContext, NavigateActivity.class);
        }
        intent.putExtra("imageUrl",selectedGift.getBackground());
        intent.putExtra("coordinate",selectedGift.getCoordinate());
        intent.putExtra("points",selectedGift.getPoints());
        System.out.println("imageUrl:"+ selectedGift.getBackground());
        startActivity(intent);
    }
}