package com.joe.bibi.domain;

import cn.bmob.v3.BmobObject;

/**
 * Created by Joe on 2016/2/6.
 */
public class Debate extends BmobObject {
    private String publisher;
    private String title;
    private String desc;
    private int positive;
    private int negative;
    private int total;
    private String image;
    private String avatar;
    private String positiveop;
    private String negativeop;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPositiveop() {
        return positiveop;
    }

    public void setPositiveop(String positiveop) {
        this.positiveop = positiveop;
    }

    public String getNegativeop() {
        return negativeop;
    }

    public void setNegativeop(String negativeop) {
        this.negativeop = negativeop;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getPositive() {
        return positive;
    }

    public void setPositive(int positive) {
        this.positive = positive;
    }

    public int getNegative() {
        return negative;
    }

    public void setNegative(int negative) {
        this.negative = negative;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        total = total;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
