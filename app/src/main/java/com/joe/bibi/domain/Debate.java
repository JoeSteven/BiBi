package com.joe.bibi.domain;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

/**
 * Created by Joe on 2016/2/6.
 */
public class Debate extends BmobObject implements Serializable{
    private String publisher;
    private String title;
    private String desc;
    private Integer positive;
    private Integer negative;
    private Integer total;
    private String image;
    private String avatar;
    private String positiveop;
    private String negativeop;
    private Integer comment;

    public Integer getComment() {
        return comment;
    }

    public void setComment(Integer comment) {
        this.comment = comment;
    }

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

    public Integer getPositive() {
        return positive;
    }

    public void setPositive(Integer positive) {
        this.positive = positive;
    }

    public Integer getNegative() {
        return negative;
    }

    public void setNegative(Integer negative) {
        this.negative = negative;
    }

    public Integer getTotal() {
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
