package com.joe.bibi.receiver;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.joe.bibi.R;
import com.joe.bibi.activity.MessageActivity;
import com.joe.bibi.application.BBApplication;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.PushMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.config.BmobConstant;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.EventListener;
import cn.bmob.im.inteface.OnReceiveListener;
import cn.bmob.im.util.BmobJsonUtil;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Joe on 2016/2/25.
 */
public class MyMessageReceiver extends BroadcastReceiver {
    public static ArrayList<EventListener> ehList = new ArrayList<EventListener>();
    private Context context;
    public static int mNewNum = 0;
    private BBUser currentUser;
    private BBApplication application;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        this.application=BBApplication.getInstance();
        currentUser=BBUser.getCurrentUser(context,BBUser.class);
        if(intent.getAction().equals("cn.bmob.push.action.MESSAGE")){
            Log.d("BB", "客户端收到推送内容：" + intent.getStringExtra("msg"));
            String json=intent.getStringExtra("msg");
            String msg=parseData(json);
            if(!TextUtils.isEmpty(msg)){
            }else{
                //其他消息
                parseMessage(json);
            }
        }
    }
    private String parseData(String json) {
        Gson gson=new Gson();
        PushMessage pm=gson.fromJson(json, PushMessage.class);
        return pm.alert;
    }
    private void parseMessage(String json) {
        JSONObject jo;

        try {
            jo = new JSONObject(json);
            final String toId = BmobJsonUtil.getString(jo, BmobConstant.PUSH_KEY_TOID);
            String targetId=BmobJsonUtil.getString(jo, BmobConstant.PUSH_KEY_TOID);
            String tag = BmobJsonUtil.getString(jo, BmobConstant.PUSH_KEY_TAG);
            if(tag.equals(BmobConfig.TAG_ADD_CONTACT)){
                if(targetId.equals(BBUser.getCurrentUser(context, BBUser.class).getObjectId())) {
                    handleAddRequest(jo, json);
                }
            }
            else if(tag.equals(BmobConfig.TAG_ADD_AGREE)){

                //如果当前消息的对象是当前用户才显示，否则不显示
                if(targetId.equals(BBUser.getCurrentUser(context, BBUser.class).getObjectId()));
                {
                    addFriendAfterAgree(jo,json);
                }
            }else if(tag.equals(BmobConfig.TAG_READED)){//已读回执
                String conversionId = BmobJsonUtil.getString(jo,BmobConstant.PUSH_READED_CONVERSIONID);
                String msgTime = BmobJsonUtil.getString(jo,BmobConstant.PUSH_READED_MSGTIME);
                if(currentUser!=null){
                    //更改某条消息的状态
                    BmobChatManager.getInstance(context).updateMsgStatus(conversionId, msgTime);
                    if(toId.equals(currentUser.getObjectId())){
                        if (ehList.size() > 0) {// 有监听的时候，传递下去--便于修改界面
                            for (EventListener handler : ehList)
                                handler.onReaded(conversionId, msgTime);
                        }
                    }
                }
            }else{
                String fromId = BmobJsonUtil.getString(jo, BmobConstant.PUSH_KEY_TARGETID);
                //增加消息接收方的ObjectId--目的是解决多账户登陆同一设备时，无法接收到非当前登陆用户的消息。

                String msgTime = BmobJsonUtil.getString(jo,BmobConstant.PUSH_READED_MSGTIME);
                Log.e("BB","fromId为空"+(fromId==null)+"-msgReceiver");
                if(fromId!=null){
                    BmobChatManager.getInstance(context).createReceiveMsg(json, new OnReceiveListener() {
                        @Override
                        public void onSuccess(BmobMsg bmobMsg) {
                            Log.e("BB","成功-msgReceiver");
                            if (ehList.size() > 0) {// 有监听的时候，传递下去
                                for (int i = 0; i < ehList.size(); i++) {
                                    Log.e("BB","有监听，调用OnMessage-msgReceiver");
                                    (ehList.get(i)).onMessage(bmobMsg);
                                }
                            }else{
                                if(application.isMessageAllowed){
                                    mNewNum++;
                                    showMsgNotify(context,bmobMsg);
                                }
                            }
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            Log.e("BB","失败-msgReceiver");
                        }
                    });
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //请求添加好友同意后
    private void addFriendAfterAgree(JSONObject jo, final String json) {
        final String username = BmobJsonUtil.getString(jo, BmobConstant.PUSH_KEY_TARGETUSERNAME);
        final String nick= BmobJsonUtil.getString(jo,BmobConstant.PUSH_KEY_TARGETNICK);
        final String toId=BmobJsonUtil.getString(jo, BmobConstant.PUSH_KEY_TOID);
        //收到对方的同意请求之后，就得添加对方为好友--已默认添加同意方为好友，并保存到本地好友数据库
        BmobUserManager.getInstance(context).addContactAfterAgree(username, new FindListener<BmobChatUser>() {
            @Override
            public void onSuccess(List<BmobChatUser> list) {
                if(application.isMessageAllowed){
                    showOtherNotify(nick, toId, nick + "同意添加你为好友", true);
                }
                //创建一个临时验证会话--用于在会话界面形成初始会话
                BmobMsg.createAndSaveRecentAfterAgree(context, json);
                BBApplication.getInstance().addContactList(list);
            }

            @Override
            public void onError(int i, String s) {

            }
        });

    }

    //处理添加好友请求
    private void handleAddRequest(JSONObject jo, String json) {
        String toId=BmobJsonUtil.getString(jo, BmobConstant.PUSH_KEY_TOID);
        BmobInvitation message = BmobChatManager.getInstance(context).saveReceiveInvite(json, toId);
        if(application.isMessageAllowed){
            showOtherNotify(message.getNick(), toId, message.getNick() + "请求添加好友", false);
        }

    }

    private void showOtherNotify(String username,String toId,String ticker,boolean isChat) {
        wakePhoneAndUnlock();
        Intent intent=new Intent(context,MessageActivity.class);
        intent.putExtra("isshowchat", isChat);
        BmobNotifyManager.getInstance(context).showNotifyWithExtras(application.isVoiceAllowed, application.isVibrateAllowed, R.mipmap.ic_launcher, ticker, username, ticker.toString(), intent);
    }

    public void showMsgNotify(Context context,BmobMsg msg) {
        wakePhoneAndUnlock();
        // 更新通知栏
        int icon = R.mipmap.ic_launcher;
        String trueMsg = "";
        if(msg.getMsgType()==BmobConfig.TYPE_TEXT && msg.getContent().contains("\\ue")){
            trueMsg = "[表情]";
        }else if(msg.getMsgType()==BmobConfig.TYPE_IMAGE){
            trueMsg = "[图片]";
        }else if(msg.getMsgType()==BmobConfig.TYPE_VOICE){
            trueMsg = "[语音]";
        }else if(msg.getMsgType()==BmobConfig.TYPE_LOCATION){
            trueMsg = "[位置]";
        }else{
            trueMsg = msg.getContent();
        }
        CharSequence tickerText = msg.getBelongNick() + ":" + trueMsg;
        String contentTitle = msg.getBelongNick()+ " (" + mNewNum + "条新消息)";

        Intent intent = new Intent(context, MessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("isshowchat", true);


        BmobNotifyManager.getInstance(context).showNotifyWithExtras(application.isVoiceAllowed, application.isVibrateAllowed,icon, tickerText.toString(), contentTitle, tickerText.toString(),intent);
    }

    //点亮屏幕并解锁
    private void wakePhoneAndUnlock() {
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "WakeLock");
        mWakelock.acquire();//唤醒屏幕
        mWakelock.release();//释放
    }
}
