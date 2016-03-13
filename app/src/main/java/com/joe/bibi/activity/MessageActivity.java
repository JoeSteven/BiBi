package com.joe.bibi.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.joe.bibi.R;
import com.joe.bibi.application.BBApplication;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.receiver.MyMessageReceiver;
import com.joe.bibi.utils.FaceTextUtils;
import com.joe.bibi.utils.PrefUtils;
import com.joe.bibi.utils.TimeUtil;
import com.joe.bibi.utils.ToastUtils;

import org.xutils.x;

import java.util.List;

import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.bean.BmobRecent;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.EventListener;
import cn.bmob.v3.listener.UpdateListener;

public class MessageActivity extends AppCompatActivity implements EventListener {

    private TextView mShowAdd;
    private TextView mShowChat;
    private ListView mListView;
    private List<BmobInvitation> mInvitations;
    private boolean isShowChat;
    private AddAdapter mAddAdapter;
    private ChatAdapter mChatAdapter;
    private List<BmobRecent> mRecents;
    private NewMessageReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        isShowChat=getIntent().getBooleanExtra("isshowchat", true);
        ActionBar ab=getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        initView();
        initData();
        initNewMessageBroadCast();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        mShowAdd = (TextView) findViewById(R.id.tv_add_message);
        mShowChat = (TextView) findViewById(R.id.tv_chat_message);
        mListView = (ListView) findViewById(R.id.lv_message);
        if(!isShowChat){
            mShowAdd.setTextColor(getResources().getColor(R.color.MainBlue));
            mShowChat.setTextColor(getResources().getColor(R.color.LightBlack));
        }
    }

    private void initData() {
        //获取邀请信息
        mInvitations = BmobDB.create(this).queryBmobInviteList();
        mRecents = BmobDB.create(this).queryRecents();
        mAddAdapter = new AddAdapter();
        mChatAdapter = new ChatAdapter();
        initAdapter();
        initListener();
    }

    private void initListener() {
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                if(isShowChat){
                    final BmobRecent recent= (BmobRecent) mChatAdapter.getItem(position);
                    AlertDialog.Builder builder=new AlertDialog.Builder(MessageActivity.this);
                    builder.setTitle("删除会话？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //删除会话及聊天记录
                            BmobDB.create(MessageActivity.this).deleteRecent(recent.getTargetid());
                            BmobDB.create(MessageActivity.this).deleteMessages(recent.getTargetid());

                            mRecents.remove(position);
                            mChatAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                            ToastUtils.make(MessageActivity.this,"删除成功");
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }else{
                    final BmobInvitation invitation= (BmobInvitation) mListView.getAdapter().getItem(position);
                    AlertDialog.Builder builder=new AlertDialog.Builder(MessageActivity.this);
                    builder.setTitle("删除好友请求？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BmobDB.create(MessageActivity.this).deleteInviteMsg(invitation.getFromid(), Long.toString(invitation.getTime()));
                            mInvitations.remove(position);
                            mAddAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                            ToastUtils.make(MessageActivity.this,"删除成功");
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
                return false;
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!isShowChat) return;
                BmobRecent recent = (BmobRecent) mChatAdapter.getItem(position);
                //重置未读消息
                BmobDB.create(MessageActivity.this).resetUnread(recent.getTargetid());
                //组装聊天对象
                BBUser user = new BBUser();
                user.setAvatar(recent.getAvatar());
                user.setNick(recent.getNick());
                user.setUsername(recent.getUserName());
                user.setObjectId(recent.getTargetid());
                Intent intent = new Intent(MessageActivity.this, ChatActivity.class);
                intent.putExtra("user", user);
                mListView.setAdapter(mChatAdapter);
                startActivity(intent);
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }
    private void initNewMessageBroadCast(){
        // 注册接收消息广播 更新小圆点
        receiver = new NewMessageReceiver();
        IntentFilter intentFilter = new IntentFilter(BmobConfig.BROADCAST_NEW_MESSAGE);
        intentFilter.setPriority(3);
        registerReceiver(receiver, intentFilter);
    }


    private class NewMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("BB","收到广播");
            if(isShowChat){
                mListView.setAdapter(mChatAdapter);
            }
            // 记得把广播给终结掉
            abortBroadcast();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // 新消息到达，重新刷新界面
        MyMessageReceiver.ehList.add(this);// 监听推送的消息
        // 有可能锁屏期间，在聊天界面出现通知栏，这时候需要清除通知和清空未读消息数
        BmobNotifyManager.getInstance(this).cancelNotify();
        //清空消息未读数-这个要在刷新之后
        MyMessageReceiver.mNewNum=0;
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        MyMessageReceiver.ehList.remove(this);// 监听推送的消息
    }
    private void changeUI(){
        initAdapter();
    }
    private void initAdapter() {
        if(isShowChat){
            mListView.setAdapter(mChatAdapter);
        }else {
            mListView.setAdapter(mAddAdapter);
        }
    }

    public void showAdd(View v){
        mShowAdd.setTextColor(getResources().getColor(R.color.MainBlue));
        mShowChat.setTextColor(getResources().getColor(R.color.LightBlack));
        isShowChat=false;
        changeUI();
    }
    public void showChat(View v){
        mShowAdd.setTextColor(getResources().getColor(R.color.LightBlack));
        mShowChat.setTextColor(getResources().getColor(R.color.MainBlue));
        isShowChat=true;
        changeUI();
    }
    class ChatAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mRecents.size();
        }

        @Override
        public Object getItem(int position) {
            return mRecents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v=convertView;
            ViewHolder holder=null;
            BmobRecent recent=mRecents.get(position);
            if(v==null){
                v=View.inflate(MessageActivity.this,R.layout.item_chat_message,null);
                holder=new ViewHolder();
                holder.nickChat= (TextView) v.findViewById(R.id.tv_nick_chat);
                holder.msg= (TextView) v.findViewById(R.id.tv_msg_chat);
                holder.date= (TextView) v.findViewById(R.id.tv_date_chat);
                holder.unread= (TextView) v.findViewById(R.id.tv_unread_chat);
                holder.avatarChat= (CircularImageView) v.findViewById(R.id.iv_avatar_chat);
                v.setTag(holder);
            }else{
                holder= (ViewHolder) v.getTag();
            }

            x.image().bind(holder.avatarChat, recent.getAvatar());
            holder.nickChat.setText(recent.getNick());
            holder.date.setText(TimeUtil.getChatTime(recent.getTime()));
            if(recent.getType()==BmobConfig.TYPE_TEXT){
                SpannableString spannableString = FaceTextUtils.toSpannableString(MessageActivity.this, recent.getMessage());
                holder.msg.setText(spannableString);
            }else if(recent.getType()==BmobConfig.TYPE_IMAGE){
                holder.msg.setText("[图片]");
            } else if(recent.getType()==BmobConfig.TYPE_VOICE){
                holder.msg.setText("[语音]");
            }
            int num = BmobDB.create(MessageActivity.this).getUnreadCount(recent.getTargetid());
            if(num>0){
                holder.unread.setVisibility(View.VISIBLE);
                if(num>100){
                    holder.unread.setText("99¨");
                }
                holder.unread.setText(num+"");
            }
            return v;
        }
    }

    class AddAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mInvitations.size();
        }

        @Override
        public Object getItem(int position) {
            return mInvitations.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;
            View v=convertView;
            final BmobInvitation invitation=mInvitations.get(position);
            if(v==null){
                v=View.inflate(MessageActivity.this,R.layout.item_add_message,null);
                holder=new ViewHolder();
                holder.nickAdd= (TextView) v.findViewById(R.id.tv_nick_add);
                holder.avatarAdd= (CircularImageView) v.findViewById(R.id.iv_avatar_add);
                holder.agreeAdd= (Button) v.findViewById(R.id.bt_agree_add);
                holder.agreedAdd= (TextView) v.findViewById(R.id.tv_agreed_add);
                v.setTag(holder);
            }else{
                holder= (ViewHolder) v.getTag();
            }

            x.image().bind(holder.avatarAdd, invitation.getAvatar());
            holder.nickAdd.setText(invitation.getNick());
            int status=invitation.getStatus();
            if(status== BmobConfig.INVITE_ADD_NO_VALI_RECEIVED||status==BmobConfig.INVITE_ADD_NO_VALIDATION){
                final ViewHolder finalHolder = holder;
                holder.agreeAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        agreeRequest(invitation, finalHolder.agreeAdd,finalHolder.agreedAdd);
                    }
                });
            }else if(status==BmobConfig.INVITE_ADD_AGREE){
                holder.agreeAdd.setVisibility(View.GONE);
                holder.agreedAdd.setVisibility(View.VISIBLE);
            }
            return v;
        }

        //同意添加好友
        private void agreeRequest(BmobInvitation invitation, final Button v, final TextView agreedAdd) {
            final ProgressDialog progress = new ProgressDialog(MessageActivity.this);
            progress.setMessage("正在添加...");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
            try {
                //同意添加好友
                BmobUserManager.getInstance(MessageActivity.this).agreeAddContact(invitation, new UpdateListener() {

                    @Override
                    public void onSuccess() {
                        // TODO Auto-generated method stub
                        progress.dismiss();
                        v.setVisibility(View.GONE);
                        agreedAdd.setVisibility(View.VISIBLE);
                        //保存到application中的ContactList（即保存在内存中）
                        ToastUtils.make(MessageActivity.this,"添加好友成功");
                        BBApplication.getInstance().updateContactList();
                    }

                    @Override
                    public void onFailure(int arg0, final String arg1) {
                        // TODO Auto-generated method stub
                        progress.dismiss();
                        ToastUtils.make(MessageActivity.this,"添加失败");
                    }
                });
            } catch (final Exception e) {
                progress.dismiss();
                ToastUtils.make(MessageActivity.this, "添加失败"+e);
            }
        }
    }
    class ViewHolder{
        TextView nickAdd;
        CircularImageView avatarAdd;
        Button agreeAdd;
        TextView agreedAdd;
        //会话
        TextView nickChat;
        TextView msg;
        TextView date;
        CircularImageView avatarChat;
        TextView unread;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onMessage(BmobMsg bmobMsg) {
        if(isShowChat){
            mRecents = BmobDB.create(this).queryRecents();
            mChatAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onReaded(String s, String s1) {

    }

    @Override
    public void onNetChange(boolean b) {

    }

    @Override
    public void onAddUser(BmobInvitation bmobInvitation) {

    }

    @Override
    public void onOffline() {

    }
}
