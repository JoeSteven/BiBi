package com.joe.bibi.domain;

import cn.bmob.v3.BmobObject;

/**
 * Created by Joe on 2016/2/27.
 */
public class Follow extends BmobObject {
    private String followerId;
    private String debateId;
    private String followerUserName;
    private String followerInstallationId;

    public String getFollowerInstallationId() {
        return followerInstallationId;
    }

    public void setFollowerInstallationId(String followerInstallationId) {
        this.followerInstallationId = followerInstallationId;
    }

    public String getFollowerUserName() {
        return followerUserName;
    }

    public void setFollowerUserName(String followerUserName) {
        this.followerUserName = followerUserName;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public String getDebateId() {
        return debateId;
    }

    public void setDebateId(String debateId) {
        this.debateId = debateId;
    }
}
