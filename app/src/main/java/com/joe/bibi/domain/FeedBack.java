package com.joe.bibi.domain;

import cn.bmob.v3.BmobObject;

/**
 * Created by Joe on 2016/3/6.
 */
public class FeedBack extends BmobObject {
    private String phone;//手机型号
    private String system;//系统情况
    private String feedback;//问题详述
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
