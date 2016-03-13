package com.joe.bibi.domain;

import org.json.JSONObject;

/**
 * Created by Joe on 2016/2/27.
 */
public class PushMessage {
    public String alert;//id
    public int tag=0;//类型
    public String type="comment&bibi&621";

    public PushMessage(String alert, int tag) {
        this.alert = alert;
        this.tag = tag;
    }
}
