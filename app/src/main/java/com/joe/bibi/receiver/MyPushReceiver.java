package com.joe.bibi.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.PowerManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.joe.bibi.R;
import com.joe.bibi.activity.CommentDetailActivity;
import com.joe.bibi.activity.MessageActivity;
import com.joe.bibi.application.BBApplication;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.Comment;
import com.joe.bibi.domain.PushMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.bean.BmobRecent;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.config.BmobConstant;
import cn.bmob.im.util.BmobJsonUtil;
import cn.bmob.im.util.BmobUtils;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Joe on 2016/2/27.
 */
public class MyPushReceiver extends BroadcastReceiver {
    private Context context;
    private BBApplication application;
    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context=context;
        this.application=BBApplication.getInstance();
        if(intent.getAction().equals("cn.bmob.push.action.MESSAGE")){
            Log.e("BB", "客户端收到推送内容：" + intent.getStringExtra("msg"));
            String json=intent.getStringExtra("msg");
            if(json.contains("comment&bibi&621")){
                //评论消息
                showCommentMessage(parseData(json));
            }else{
            }
        }
    }
    private void showCommentMessage(final PushMessage msg) {
        BmobQuery<Comment> query=new BmobQuery<Comment>();
        query.addWhereEqualTo("objectId", msg.alert);
        query.findObjects(context, new FindListener<Comment>() {
            @Override
            public void onSuccess(List<Comment> list) {
                if(list.size()>0){
                    Comment comment=list.get(0);
                    Log.d("BB",comment.getContent());
                    if(application.isCommentAllowed)
                    {
                        showNotification(context, comment,msg.tag);
                    }
                }

            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    private PushMessage parseData(String json) {
        Gson gson=new Gson();
        PushMessage pm=gson.fromJson(json, PushMessage.class);
        return pm;
    }
    private void showNotification(Context context, Comment comment, int tag) {

        wakePhoneAndUnlock();
        Intent notificationIntent = new Intent(context,CommentDetailActivity.class);
        notificationIntent.putExtra("comment", comment);
        PendingIntent contentIntent = PendingIntent.getActivity(context,comment.getObjectId().hashCode(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder=new Notification.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        if(tag!=0){
            builder.setTicker("有人回复了你");
            builder.setContentTitle("有人回复了你");
        }else{
            builder.setTicker("有新的评论");
            builder.setContentTitle(comment.getTitle());
        }

        //设置通知声音
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(comment.getTitle()); //设置下拉列表里的标题
        builder.setContentText(comment.getContent());//设置上下文内容
        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);
        Notification notification=builder.build();
        if(application.isVoiceAllowed){
            notification.defaults |= 1;
        }
        if(application.isVibrateAllowed){
            notification.defaults |= 2;
        }
        //设置为不同的ID才能有多条通知，否则会被最新的通知覆盖掉
        notificationManager.notify(comment.getObjectId().hashCode(),notification);
    }

    //点亮屏幕并解锁
    private void wakePhoneAndUnlock() {
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "WakeLock");
        mWakelock.acquire();//唤醒屏幕
        mWakelock.release();//释放
    }
}
