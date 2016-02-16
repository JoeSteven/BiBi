package com.joe.bibi.application;

import android.app.Application;

import org.xutils.x;

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
