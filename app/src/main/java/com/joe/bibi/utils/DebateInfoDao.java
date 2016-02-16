package com.joe.bibi.utils;

import android.app.Activity;

import com.joe.bibi.domain.Debate;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Joe on 2016/2/6.
 */
public class DebateInfoDao {
    private Activity mActivity;
    public DebateInfoDao(Activity activity) {
        this.mActivity=activity;
    }

    public ArrayList<Debate> getAllInfo(){
        BmobQuery<Debate> query = new BmobQuery<Debate>();
        final ArrayList<Debate> list = new ArrayList<Debate>();
        query.findObjects(mActivity, new FindListener<Debate>() {
            @Override
            public void onSuccess(List<Debate> list) {

            }

            @Override
            public void onError(int i, String s) {

            }
        });
        return null;
    }
    class myFindListener<T> extends FindListener<T>{

        public myFindListener() {

        }

        @Override
        public void onSuccess(List<T> list) {

        }

        @Override
        public void onError(int i, String s) {

        }
    }
}
