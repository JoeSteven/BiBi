package com.joe.bibi.application;

import android.app.Application;

import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.Follow;

import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Joe on 2016/1/28.
 */
public class BBApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(true);
    }

}
