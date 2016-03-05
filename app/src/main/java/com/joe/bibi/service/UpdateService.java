package com.joe.bibi.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.joe.bibi.domain.Comment;
import com.joe.bibi.domain.Debate;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by Joe on 2016/3/2.
 */
public class UpdateService extends Service {
    private int num=0;//记录当前更新状态
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("BB","开启服务"+num+"-UpdateService");
        String nick=intent.getStringExtra("Nick");
        String publisher=intent.getStringExtra("publisher");
        final String mAvatarUrl=intent.getStringExtra("AvatarUrl");
        BmobQuery<Comment> commentBmobQuery=new BmobQuery<Comment>();
        commentBmobQuery.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
        commentBmobQuery.addWhereEqualTo("Nick",nick);
        commentBmobQuery.findObjects(this, new FindListener<Comment>() {
            @Override
            public void onSuccess(List<Comment> list) {
                Log.e("BB","更新评论成功"+list.size()+"-UpdateService");
                if(list.size()>0){
                    for (Comment c : list) {
                        c.setAvatar(mAvatarUrl);
                        c.update(UpdateService.this, c.getObjectId(), new UpdateListener() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(int i, String s) {

                            }
                        });
                    }
                }
                updateDone();
            }

            @Override
            public void onError(int i, String s) {
                Log.e("BB","更新评论失败-UpdateService");
                updateDone();
            }
        });
        BmobQuery<Debate> query=new BmobQuery<Debate>();
        query.addWhereEqualTo("publisher",publisher);
        query.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
        query.findObjects(this, new FindListener<Debate>() {
            @Override
            public void onSuccess(List<Debate> list) {
                Log.e("BB","更新辩题成功"+list.size()+"-UpdateService");
                if(list.size()>0){
                    for (Debate debate:list) {
                        debate.setAvatar(mAvatarUrl);
                        debate.update(UpdateService.this, debate.getObjectId(), new UpdateListener() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onFailure(int i, String s) {

                            }
                        });
                    }
                }
                Log.e("BB","更新辩题完成-UpdateService");
                updateDone();
            }

            @Override
            public void onError(int i, String s) {
                Log.e("BB","更新辩题失败-UpdateService");
                updateDone();
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateDone() {
        num++;
        Log.e("BB","更新完成数"+num+"-UpdateService");
        if(num==2){
            stopSelf();
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
