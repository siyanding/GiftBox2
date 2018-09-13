package com.example.admin.giftbox2;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class SearchActivity extends AppCompatActivity {

    private List<String> usernames;
    private SearchView searchView;
    private ListView listView;
    Context thisContext;
    private String username;
    private List<String> friendsName = new ArrayList<>();
    private String onClickFriendName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ContextHolder.initial(this);
        thisContext = ContextHolder.getContext();

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        getFriends();

        searchView = (SearchView) findViewById(R.id.searchView);
        listView = (ListView) findViewById(R.id.listView);
        listView.setTextFilterEnabled(true);
        listView.setOnItemLongClickListener((arg0, arg1, position, arg3) -> {
            onClickFriendName = (String) listView.getItemAtPosition(position);
            listView.setOnCreateContextMenuListener((menu, arg11, arg2) -> {
                menu.setHeaderTitle("Operations");
                menu.add(0,0,0,"Add");
            });
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // this method will be triggered when search button is clicked
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // this method will be triggered when search content changed
            @Override
            public boolean onQueryTextChange(String newText) {
                ListAdapter adapter = listView.getAdapter();
                if (adapter instanceof Filterable) {
                    Filter filter = ((Filterable) adapter).getFilter();
                    if (newText == null || newText.length() == 0) {
                        filter.filter(null);
                    } else {
                        filter.filter(newText);
                    }
                }
                return true;
            }
        });
    }

    public void initSearch(){
        final int[] callCount = { 0 };
        BmobQuery<User> query = new BmobQuery<>();
        System.out.println("SearchActivity:initSearch:"+friendsName.toString());
        query.addWhereNotContainedIn("username", friendsName);
        query.addWhereNotEqualTo("username",username);
        query.setLimit(50);
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> users, BmobException e) {
                if(e==null){
                    usernames = users
                            .stream()
                            .map( p -> p.getUsername() )
                            .collect( Collectors.toList() );
                    listView.setAdapter(
                            new ArrayAdapter<>(thisContext,
                                    android.R.layout.simple_list_item_1,
                                    usernames)
                    );

                }else if(callCount[0] < 5){
                    initSearch();
                    Log.i("bmob","failed："+e.getMessage()+","+e.getErrorCode());
                    callCount[0] ++;
                }
            }
        });
    }

    private void getFriends() {
        final int[] callCount = { 0 };
        BmobQuery<Friend> query = new BmobQuery<>();
        query.addWhereEqualTo("user", username);
        query.setLimit(50);
        query.findObjects(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> friends, BmobException e) {
                if (e == null) {
                    for (int i=0;i<friends.size(); i++){
                        friendsName.add(friends.get(i).getFriend());
                    }
                    initSearch();
                } else if(callCount[0] < 5){
                    Log.i("bmob", "failed：" + e.getMessage() + "," + e.getErrorCode());
                    getFriends();
                    callCount[0] ++;
                }
            }
        });
    }


    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == 0){
            showNormalDialog(username,onClickFriendName);
        }
        return super.onContextItemSelected(item);
    }

    private void showNormalDialog(final String user, final String friend){

        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(thisContext);
        normalDialog.setIcon(R.drawable.friends);
        normalDialog.setTitle("Add");
        normalDialog.setMessage("Do you sure you want to add "+friend+"?");
        normalDialog.setPositiveButton("Yes",
                (dialog, which) -> {
                    AddANewFriend(user, friend);
                    System.out.println("showNormalDialog:user,"+user+"friend:"+friend);
                    dialog.dismiss();

                });
        normalDialog.setNegativeButton("No",
                (dialog, which) -> {
                    dialog.dismiss();
                });
        normalDialog.show();
    }

    public void AddANewFriend(final String user, final String friend){
        final int[] callCount = { 0 };
        Friend friendTemp = new Friend();
        friendTemp.setUser(user);
        friendTemp.setFriend(friend);
        friendTemp.save(new SaveListener<String>() {

            @Override
            public void done(String objectId, BmobException e) {
                if(e==null){
                    TextView tv = (TextView) findViewById(R.id.ConfirmMessage);
                    tv.setText(friend+"is your friend now");
                }else if(callCount[0] < 5){
                    AddANewFriend(user,friend);
                    callCount[0]++;
                    Log.i("bmob","failed："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }
}
