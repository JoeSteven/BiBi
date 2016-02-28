package com.joe.bibi.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.joe.bibi.R;
import com.joe.bibi.activity.CommentDetailActivity;
import com.joe.bibi.activity.HomeActivity;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.Comment;
import com.joe.bibi.domain.PushMessage;
import com.joe.bibi.utils.ToastUtils;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Joe on 2016/2/27.
 */
public class MyPushReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if(intent.getAction().equals("cn.bmob.push.action.MESSAGE")){
            Log.d("BB", "客户端收到推送内容：" + intent.getStringExtra("msg"));
            String json=intent.getStringExtra("msg");
            String msg=parseData(json);
            BmobQuery<Comment> query=new BmobQuery<Comment>();
            query.addWhereEqualTo("objectId", msg);
            query.findObjects(context, new FindListener<Comment>() {
                @Override
                public void onSuccess(List<Comment> list) {
                    if(list.size()>0){
                        Comment comment=list.get(0);
                        Log.d("BB",comment.getContent());
                        showNotification(context, comment);
                    }

                }

                @Override
                public void onError(int i, String s) {

                }
            });

        }
    } private String parseData(String json) {
        Gson gson=new Gson();
        PushMessage pm=gson.fromJson(json, PushMessage.class);
        return pm.alert;
    }

    private void showNotification(Context context, Comment comment) {
        Intent notificationIntent = new Intent(context,CommentDetailActivity.class);
        notificationIntent.putExtra("comment", comment);
        PendingIntent contentIntent = PendingIntent.getActivity(context,comment.getObjectId().hashCode(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder=new Notification.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        builder.setTicker("您有新的评论");
        //设置通知声音
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle("己见——倾听不同的声音"); //设置下拉列表里的标题
        builder.setContentText("有新评论 "+comment.getTitle());//设置上下文内容
        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);
        Notification notification=builder.build();
        //设置为不同的ID才能有多条通知，否则会被最新的通知覆盖掉
        notificationManager.notify(comment.getObjectId().hashCode(),notification);
    }
}
