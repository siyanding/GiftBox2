package com.example.admin.giftbox2;

import cn.bmob.v3.BmobUser;

public class User extends BmobUser {
    private String portrait;

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }
}