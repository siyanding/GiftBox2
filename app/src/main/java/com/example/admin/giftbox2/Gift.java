package com.example.admin.giftbox2;

import cn.bmob.v3.BmobObject;

public class Gift extends BmobObject {

    private String background;
    private String user;
    private String attach;
    private String coordinate;
    private String comment;
    private String recipient;
    private Boolean status;
    private String points;
    private String whole;

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public String getWhole() {
        return whole;
    }

    public void setWhole(String whole) {
        this.whole = whole;
    }



    public String getPoints() { return points; }

    public void setPoints(String points) { this.points = points; }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
