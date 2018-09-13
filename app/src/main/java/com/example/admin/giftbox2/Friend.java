package com.example.admin.giftbox2;

import cn.bmob.v3.BmobObject;

public class Friend extends BmobObject {
    private String user;
    private String friend;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }
}