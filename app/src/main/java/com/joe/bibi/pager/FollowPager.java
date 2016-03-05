package com.joe.bibi.pager;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.Debate;
import com.joe.bibi.domain.Follow;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Joe on 2016/2/2.
 */
public class FollowPager extends BasePager {

    private List<String> mFollowedIdList;

    public FollowPager(Activity mActivity) {
        super(mActivity);
        mOrder="-total";
        mFollowedIdList = new ArrayList<String>();
    }
    @Override
    public void initTitle() {
        mTitle="关注辩题";
    }

    @Override
    public void initData() {
        //先查Follow表，找到表中的数据
        BmobQuery<Follow> followQuery=new BmobQuery<Follow>();
        followQuery.addWhereEqualTo("followerId",BmobUser.getCurrentUser(mActivity,BBUser.class).getObjectId());
        followQuery.setLimit(10000);
        followQuery.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
        Log.d("BB","查找Follow-FollowPager");
        followQuery.findObjects(mActivity, new FindListener<Follow>() {
            @Override
            public void onSuccess(List<Follow> list) {
                if(list.size()>0){
                    mFollowedIdList.clear();
                    for (Follow f:list) {
                        mFollowedIdList.add(f.getDebateId());
                    }
                }
                initDataParent();
            }

            @Override
            public void onError(int i, String s) {

            }
        });


    }

    private void initDataParent() {
        if(isInitData==true) return;
        query.setLimit(mLimit);
        query.addWhereContainedIn("objectId",mFollowedIdList);
        if(isRefreshing){
            query.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
        }else{
            query.setCachePolicy(BmobQuery.CachePolicy.CACHE_THEN_NETWORK);
        }
        final ArrayList<Debate> list = new ArrayList<Debate>();
        query.order(mOrder);

        query.findObjects(mActivity, new FindListener<Debate>() {
            @Override
            public void onSuccess(List<Debate> list) {
                isInitData = true;
                debates= list;
                if(debates.size()==0){
                    mNullHint.setText("当前没有关注辩题哦");
                    mNullHint.setVisibility(View.VISIBLE);
                }else{
                    adapter = new myAdapter();
                    mListView.setAdapter(adapter);
                }
                initListener();
                mRefresh.setRefreshing(false);
                mLoading.setVisibility(View.GONE);
                isLoadingMore=false;
                if(mListView.isLoadingMore){
                    mListView.LoadingDone();
                }
            }

            @Override
            public void onError(int i, String s) {
                if(i==9009) return;
                Snackbar.make(container,"加载数据失败，请检查下网络",Snackbar.LENGTH_SHORT).show();
                mRefresh.setRefreshing(false);
                mLoading.setVisibility(View.GONE);
                isLoadingMore=false;
                if(mListView.isLoadingMore){
                    mListView.LoadingDone();
                }
            }
        });
    }

}
