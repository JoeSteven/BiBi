package com.joe.bibi.application;

import android.app.Application;
import android.text.TextUtils;

import com.joe.bibi.domain.BBUser;
import com.joe.bibi.utils.ConsUtils;
import com.joe.bibi.utils.PrefUtils;

import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.im.BmobChat;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Joe on 2016/1/28.
 */
public class BBApplication extends Application{
    public static BBApplication mInstance;

    public List<BmobChatUser> getContactList() {
        return ContactList;
    }

    public void setContactList(List<BmobChatUser> contactList) {
        ContactList = contactList;
    }

    public List<BmobChatUser> ContactList;

    public boolean isCommentAllowed;
    public boolean isVoiceAllowed;
    public boolean isVibrateAllowed;
    public boolean isMessageAllowed;
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(true);
        initBmob();
        mInstance=this;
        isCommentAllowed=PrefUtils.getBoolean(this, ConsUtils.IS_COMMENT_ALLOWED,true);
        isVoiceAllowed=PrefUtils.getBoolean(this, ConsUtils.IS_VOICE_ALLOWED, true);
        isVibrateAllowed=PrefUtils.getBoolean(this, ConsUtils.IS_VIBRATE_ALLOWED,true);
        isMessageAllowed=PrefUtils.getBoolean(this, ConsUtils.IS_MESSAGE_ALLOWED,true);
        BmobUserManager.getInstance(this).queryCurrentContactList(new FindListener<BmobChatUser>() {
            @Override
            public void onSuccess(List<BmobChatUser> list) {
                setContactList(list);
            }

            @Override
            public void onError(int i, String s) {
                setContactList(new ArrayList<BmobChatUser>());
            }
        });
    }
    private void initBmob() {
        BmobChat.DEBUG_MODE = true;
        //BmobIM SDK初始化--只需要这一段代码即可完成初始化
        BmobChat.getInstance(this).init(ConsUtils.APPLICATION_ID);
        // 使用推送服务时的初始化操作
        BmobInstallation.getCurrentInstallation(this).save();
        // 启动推送服务
        BmobChat.DEBUG_MODE = true;
        //BmobIM SDK初始化--只需要这一段代码即可完成初始化
        BmobChat.getInstance(this).init(ConsUtils.APPLICATION_ID);
    }
    public static BBApplication getInstance(){
        return mInstance;
    }

    public void updateContactList(){
        BmobUserManager.getInstance(this).queryCurrentContactList(new FindListener<BmobChatUser>() {
            @Override
            public void onSuccess(List<BmobChatUser> list) {
                setContactList(list);
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    public void addContactList(List<BmobChatUser> user){
        ContactList.addAll(user);
    }
}
