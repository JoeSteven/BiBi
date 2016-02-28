package com.joe.bibi.domain;

import java.io.Serializable;

import cn.bmob.im.bean.BmobChatUser;

/**
 * Created by Joe on 2016/1/27.
 */
public class BBUser extends BmobChatUser implements Serializable{
    private String Desc;//简介
    private String Tag;//标签
    private String AvatarName;
    private String AvatarUrl;

    public String getAvatarUrl() {
        return AvatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        AvatarUrl = avatarUrl;
    }

    public String getAvatarName() {
        return AvatarName;
    }

    public void setAvatarName(String avatarName) {
        AvatarName = avatarName;
    }

    public String getTag() {
        return Tag;
    }

    public void setTag(String tag) {
        Tag = tag;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }


}
