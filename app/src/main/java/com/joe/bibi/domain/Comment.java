package com.joe.bibi.domain;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

/**
 * Created by Joe on 2016/2/26.
 */
public class Comment extends BmobObject implements Serializable{
    private String BelongTo;//属于哪个话题
    private String Content;//评论内容
    private String UserName;//评论人的用户名
    private String Avatar;//评论人头像
    private String Nick;//评论人的昵称
    private Integer Like;//评论的赞同数
    private Integer UnLike;//评论的差评数
    private Integer Point;//评论所支持的观点 三种 正方，反方 中立
    private String Title;

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public static final int POSITIVE_COMMENT=0;
    public static final int NEGATIVE_COMMENT=2;
    public static final int NEUTRAL_COMMENT=1;

    public String getBelongTo() {
        return BelongTo;
    }

    public void setBelongTo(String belongTo) {
        BelongTo = belongTo;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getAvatar() {
        return Avatar;
    }

    public void setAvatar(String avatar) {
        Avatar = avatar;
    }

    public String getNick() {
        return Nick;
    }

    public void setNick(String nick) {
        Nick = nick;
    }

    public Integer getLike() {
        return Like;
    }

    public void setLike(Integer like) {
        Like = like;
    }

    public Integer getUnLike() {
        return UnLike;
    }

    public void setUnLike(Integer unLike) {
        UnLike = unLike;
    }

    public Integer getPoint() {
        return Point;
    }

    public void setPoint(Integer point) {
        Point = point;
    }
}
