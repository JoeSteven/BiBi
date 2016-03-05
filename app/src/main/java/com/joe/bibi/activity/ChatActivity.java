package com.joe.bibi.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.joe.bibi.R;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.receiver.MyMessageReceiver;
import com.joe.bibi.utils.CommonUtils;
import com.joe.bibi.utils.NetUtils;
import com.joe.bibi.utils.TimeUtil;
import com.joe.bibi.utils.ToastUtils;
import com.joe.bibi.view.NewRecordPlayClickListener;

import org.xutils.x;

import java.util.List;

import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobDownloadManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.BmobRecordManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.DownloadListener;
import cn.bmob.im.inteface.EventListener;
import cn.bmob.im.inteface.OnRecordChangeListener;
import cn.bmob.im.inteface.UploadListener;

public class ChatActivity extends AppCompatActivity implements EventListener{

    private ListView mListView;
    private EditText mInput;
    private Button mSend;
    private BBUser mTargetUser;
    private String mTargetId;
    private int mCurrentPage=0;//读取消息的当前页数
    private List<BmobMsg> mMsgs;
    private SwipeRefreshLayout mRefresh;
    private String currentObjectId;
    private Drawable[] drawable_Anims;// 话筒动画
    //6种Item的类型
    //文本
    private final int TYPE_RECEIVER_TXT = 0;
    private final int TYPE_SEND_TXT = 1;
    //图片
    private final int TYPE_SEND_IMAGE = 2;
    private final int TYPE_RECEIVER_IMAGE = 3;
    //语音
    private final int TYPE_SEND_VOICE =4;
    private final int TYPE_RECEIVER_VOICE = 5;
    private BBUser mCurrentUser;
    private MsgAdapter mAdapter;
    private NewMessageReceiver receiver;
    private BmobChatManager manager;
    private Button mVoice;
    private Button mSpeak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mTargetUser = (BBUser) getIntent().getSerializableExtra("user");
        mTargetId = mTargetUser.getObjectId();
        mCurrentUser = BBUser.getCurrentUser(this, BBUser.class);
        currentObjectId = mCurrentUser.getObjectId();
        manager = BmobChatManager.getInstance(this);

        //注册广播接收器
        initNewMessageBroadCast();
        initView();

    }

    private void initView() {
        ActionBar ab=getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(mTargetUser.getNick());
        mListView = (ListView) findViewById(R.id.lv_chat);
        mInput = (EditText) findViewById(R.id.et_keyBoard_chat);
        mSend = (Button) findViewById(R.id.iv_send_chat);
        mRefresh = (SwipeRefreshLayout) findViewById(R.id.srl_refresh_chat);
        mRefresh.setColorSchemeResources(R.color.MainBlue, R.color.Green, R.color.LightRed);
        mVoice = (Button) findViewById(R.id.iv_speak_chat);
        mSpeak = (Button) findViewById(R.id.bt_speak_chat);
        initVoiceView();
        initData();
        initUI();
        initListener();
    }

    private void initData() {
        mMsgs = BmobDB.create(this).queryMessages(mTargetId,mCurrentPage);
    }


    private void initListener() {
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefresh.setRefreshing(true);
                mCurrentPage++;
                int total = BmobDB.create(ChatActivity.this).queryChatTotalCount(mTargetId);
                int currents = mAdapter.getCount();
                if (total <= currents) {
                    ToastUtils.make(ChatActivity.this,"聊天记录加载完了哦!");
                    mCurrentPage--;
                } else {
                    initData();
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(mAdapter.getCount() - currents - 1);
                }
                mRefresh.setRefreshing(false);
            }
        });
    }
    //展示界面
    private void initUI() {
        mAdapter = new MsgAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setSelection(mAdapter.getCount() - 1);
    }


    class MsgAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mMsgs.size();
        }

        @Override
        public Object getItem(int position) {
            return mMsgs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BmobMsg msg=mMsgs.get(position);
            ViewHolder holder=null;
            if(convertView==null){
                convertView=getViewByType(msg.getMsgType(),position);
                holder=new ViewHolder();
                holder.Avatar= (CircularImageView) convertView.findViewById(R.id.iv_avatar_text_chat);
                holder.message= (TextView) convertView.findViewById(R.id.tv_message_text_chat);
                holder.time=(TextView) convertView.findViewById(R.id.tv_time_text_chat);
                holder.load= (ProgressBar) convertView.findViewById(R.id.load_text_chat);
                holder.voice= (ImageView) convertView.findViewById(R.id.iv_voice);
                holder.voiceLong= (TextView) convertView.findViewById(R.id.tv_voice_length);
                holder.failed= (ImageView) convertView.findViewById(R.id.iv_fail_resend);
                convertView.setTag(R.id.holder_of_convert,holder);
            }else{
                //如果该复用条目与现有条目类型(发送，文本，图片等)一样，直接复用
                int type= (int) convertView.getTag(R.id.type_of_convert);
                if (getItemViewType(position)==type){
                    holder= (ViewHolder) convertView.getTag(R.id.holder_of_convert);
                }else{
                    convertView=getViewByType(msg.getMsgType(),position);
                    holder=new ViewHolder();
                    holder.Avatar= (CircularImageView) convertView.findViewById(R.id.iv_avatar_text_chat);
                    holder.message= (TextView) convertView.findViewById(R.id.tv_message_text_chat);
                    holder.time=(TextView) convertView.findViewById(R.id.tv_time_text_chat);
                    holder.load= (ProgressBar) convertView.findViewById(R.id.load_text_chat);
                    holder.voice= (ImageView) convertView.findViewById(R.id.iv_voice);
                    holder.voiceLong= (TextView) convertView.findViewById(R.id.tv_voice_length);
                    holder.failed= (ImageView) convertView.findViewById(R.id.iv_fail_resend);
                    convertView.setTag(R.id.holder_of_convert,holder);
                }
            }
            initHolderByType(holder, position, msg);
            return convertView;
        }


        private void initHolderByType(final ViewHolder holder, int position, final BmobMsg msg) {
            int type=msg.getMsgType();
            if(getItemViewType(position)==TYPE_SEND_TXT
//				||getItemViewType(position)==TYPE_SEND_IMAGE//图片单独处理
                    ||getItemViewType(position)==TYPE_SEND_VOICE){//只有自己发送的消息才有重发机制
                //状态描述
                if(msg.getStatus()==BmobConfig.STATUS_SEND_SUCCESS){//发送成功
                    holder.load.setVisibility(View.INVISIBLE);
                    holder.failed.setVisibility(View.INVISIBLE);
                    if(msg.getMsgType()==BmobConfig.TYPE_VOICE){
                       // tv_send_status.setVisibility(View.GONE);
                        holder.voiceLong.setVisibility(View.VISIBLE);
                    }else{
                        //tv_send_status.setVisibility(View.VISIBLE);
                        //tv_send_status.setText("已发送");
                    }
                }else if(msg.getStatus()==BmobConfig.STATUS_SEND_FAIL){//服务器无响应或者查询失败等原因造成的发送失败，均需要重发
                    holder.load.setVisibility(View.INVISIBLE);
                    holder.failed.setVisibility(View.VISIBLE);
                    //tv_send_status.setVisibility(View.INVISIBLE);
                    if(msg.getMsgType()==BmobConfig.TYPE_VOICE){
                        holder.voiceLong.setVisibility(View.GONE);
                    }
                }else if(msg.getStatus()==BmobConfig.STATUS_SEND_RECEIVERED){//对方已接收到
                    holder.load.setVisibility(View.INVISIBLE);
                    holder.failed.setVisibility(View.INVISIBLE);
                    if(msg.getMsgType()==BmobConfig.TYPE_VOICE){
                        //tv_send_status.setVisibility(View.GONE);
                        holder.voiceLong.setVisibility(View.VISIBLE);
                    }else{
                       // tv_send_status.setVisibility(View.VISIBLE);
                       // tv_send_status.setText("已阅读");
                    }
                }else if(msg.getStatus()==BmobConfig.STATUS_SEND_START){//开始上传
                    holder.load.setVisibility(View.VISIBLE);
                    holder.failed.setVisibility(View.INVISIBLE);
                    //tv_send_status.setVisibility(View.INVISIBLE);
                    if(msg.getMsgType()==BmobConfig.TYPE_VOICE){
                        holder.voiceLong.setVisibility(View.GONE);
                    }
                }
            }

            final String text = msg.getContent();
            switch (type){
                case BmobConfig.TYPE_TEXT://收到文本消息
                    if(getItemViewType(position)==TYPE_RECEIVER_TXT){
                        holder.message.setText(msg.getContent());
                    }else{
                        holder.message.setText(msg.getContent());
                        if(msg.getStatus()==BmobConfig.STATUS_SEND_SUCCESS){
                            holder.load.setVisibility(View.INVISIBLE);
                        }
                    }
                    break;
                case BmobConfig.TYPE_VOICE://收到语音消息
                    try {
                        if (text != null && !text.equals("")) {
                            holder.voiceLong.setVisibility(View.VISIBLE);
                            String content = msg.getContent();
                            if (msg.getBelongId().equals(currentObjectId)) {//发送的消息
                                if(msg.getStatus()==BmobConfig.STATUS_SEND_RECEIVERED
                                        ||msg.getStatus()==BmobConfig.STATUS_SEND_SUCCESS){//当发送成功或者发送已阅读的时候，则显示语音长度
                                    holder.voiceLong.setVisibility(View.VISIBLE);
                                    String length = content.split("&")[2];
                                    holder.voiceLong.setText(length + "\''");
                                }else{
                                    holder.voiceLong.setVisibility(View.INVISIBLE);
                                }
                            } else {//收到的消息
                                boolean isExists = BmobDownloadManager.checkTargetPathExist(currentObjectId, msg);
                                if(!isExists){//若指定格式的录音文件不存在，则需要下载，因为其文件比较小，故放在此下载
                                    String netUrl = content.split("&")[0];
                                    final String length = content.split("&")[1];
                                    BmobDownloadManager downloadTask = new BmobDownloadManager(ChatActivity.this,msg,new DownloadListener() {

                                        @Override
                                        public void onStart() {
                                            // TODO Auto-generated method stub
                                            holder.load.setVisibility(View.VISIBLE);
                                            holder.voiceLong.setVisibility(View.GONE);
                                            holder.voice.setVisibility(View.INVISIBLE);//只有下载完成才显示播放的按钮
                                        }

                                        @Override
                                        public void onSuccess() {
                                            // TODO Auto-generated method stub
                                            holder.load.setVisibility(View.GONE);
                                            holder.voiceLong.setVisibility(View.VISIBLE);
                                            holder.voiceLong.setText(length + "\''");
                                            holder.voice.setVisibility(View.VISIBLE);
                                        }
                                        @Override
                                        public void onError(String error) {
                                            // TODO Auto-generated method stub
                                            holder.load.setVisibility(View.GONE);
                                            holder.voiceLong.setVisibility(View.GONE);
                                            holder.voice.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                    downloadTask.execute(netUrl);
                                }else{
                                    String length = content.split("&")[2];
                                    holder.voiceLong.setText(length + "\''");
                                }
                            }
                        }
                        //播放语音文件
                        holder.voice.setOnClickListener(new NewRecordPlayClickListener(ChatActivity.this, msg, holder.voice));
                    } catch (Exception e) {

                    }
                    break;
                case BmobConfig.TYPE_IMAGE://发出语音消息
                    holder.message.setText(msg.getContent());
                    break;
            }
            x.image().bind(holder.Avatar,msg.getBelongAvatar());
            //点击头像跳转到用户主页
            holder.Avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(ChatActivity.this,UserActivity.class);
                    intent.putExtra("username",msg.getBelongUsername());
                    startActivity(intent);
                }
            });
            //时间的显示
            long currentTime=Long.parseLong(msg.getMsgTime());
            holder.time.setVisibility(View.VISIBLE);
            if(position==0){
                holder.time.setText(TimeUtil.getChatTime(currentTime));
            }else{
                long lastTime=Long.parseLong(mMsgs.get(position-1).getMsgTime());
                //超过五分钟显示时间
                Log.e("BB","上一条时间:"+lastTime+"本条时间:"+currentTime+"间隔:"+(currentTime-lastTime));
                if(currentTime-lastTime>150){
                    holder.time.setText(TimeUtil.getChatTime(currentTime));
                }else{
                    holder.time.setVisibility(View.GONE);
                }
            }

        }


        @Override
        public int getItemViewType(int position) {
            BmobMsg msg = mMsgs.get(position);
            if(msg.getMsgType()== BmobConfig.TYPE_IMAGE){
                return msg.getBelongId().equals(currentObjectId) ? TYPE_SEND_IMAGE: TYPE_RECEIVER_IMAGE;
            }else if(msg.getMsgType()==BmobConfig.TYPE_VOICE){
                return msg.getBelongId().equals(currentObjectId) ? TYPE_SEND_VOICE: TYPE_RECEIVER_VOICE;
            }else{
                return msg.getBelongId().equals(currentObjectId) ? TYPE_SEND_TXT: TYPE_RECEIVER_TXT;
            }
        }

        private View getViewByType(int msgType, int position) {
            View v=null;
            if(msgType==BmobConfig.TYPE_IMAGE){//图片类型
                return getItemViewType(position) == TYPE_RECEIVER_IMAGE ?
                        null
                        :
                        null;
            }else if(msgType==BmobConfig.TYPE_VOICE){//语音类型
                if(getItemViewType(position) == TYPE_RECEIVER_VOICE){
                    v=View.inflate(ChatActivity.this,R.layout.item_received_voice, null);
                    v.setTag(R.id.type_of_convert,TYPE_RECEIVER_VOICE);
                }else{
                    v=View.inflate(ChatActivity.this,R.layout.item_sent_voice, null);
                    v.setTag(R.id.type_of_convert,TYPE_SEND_VOICE);
                }
                return v;
            }else{//剩下默认的都是文本
                if(getItemViewType(position) == TYPE_RECEIVER_TXT){
                    v=View.inflate(ChatActivity.this,R.layout.item_received_message, null);
                    v.setTag(R.id.type_of_convert,TYPE_RECEIVER_TXT);
                }else{
                    v=View.inflate(ChatActivity.this,R.layout.item_sent_message, null);
                    v.setTag(R.id.type_of_convert,TYPE_SEND_TXT);
                }
                return v;
            }
        }
    }
    class ViewHolder{
        CircularImageView Avatar;
        TextView message;
        TextView time;
        ProgressBar load;//发送时的圈圈
        ImageView failed;//发送失败
        //语音
        ImageView voice;//聊天框中的符号
        TextView voiceLong;//语音长度
        TextView voiceStatus;//发送情况
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // 新消息到达，重新刷新界面
        refresh();
        MyMessageReceiver.ehList.add(this);// 监听推送的消息
        // 有可能锁屏期间，在聊天界面出现通知栏，这时候需要清除通知和清空未读消息数
        BmobNotifyManager.getInstance(this).cancelNotify();
        BmobDB.create(this).resetUnread(mTargetId);
        //清空消息未读数-这个要在刷新之后
        MyMessageReceiver.mNewNum=0;
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        MyMessageReceiver.ehList.remove(this);// 监听推送的消息
        // 停止录音
        if (recordManager.isRecording()) {
            recordManager.cancelRecording();
            layout_record.setVisibility(View.GONE);
        }
        // 停止播放录音
        if (NewRecordPlayClickListener.isPlaying
                && NewRecordPlayClickListener.currentPlayListener != null) {
            NewRecordPlayClickListener.currentPlayListener.stopPlayRecord();
        }
    }
    private void refresh() {
        if (mAdapter != null) {
            if (MyMessageReceiver.mNewNum != 0) {// 用于更新当在聊天界面锁屏期间来了消息，这时再回到聊天页面的时候需要显示新来的消息
                int news=  MyMessageReceiver.mNewNum;//有可能锁屏期间，来了N条消息,因此需要倒叙显示在界面上
                int size = initMsgData().size();
                for(int i=(news-1);i>=0;i--){
                    mMsgs.add(initMsgData().get(size-(i+1)));// 添加最后一条消息到界面显示
                }
                mAdapter.notifyDataSetChanged();
                mListView.setSelection(mAdapter.getCount() - 1);
            } else {
                mAdapter.notifyDataSetChanged();
            }
        } else {
            mAdapter = new MsgAdapter();
            mMsgs=initMsgData();
            mListView.setAdapter(mAdapter);
        }
    }

    private List<BmobMsg> initMsgData() {
        return BmobDB.create(this).queryMessages(mTargetId,mCurrentPage);
    }

    private void initNewMessageBroadCast(){
        // 注册接收消息广播
        receiver = new NewMessageReceiver();
        IntentFilter intentFilter = new IntentFilter(BmobConfig.BROADCAST_NEW_MESSAGE);
        //设置广播的优先级别大于Mainacitivity,这样如果消息来的时候正好在chat页面，直接显示消息，而不是提示消息未读
        intentFilter.setPriority(5);
        registerReceiver(receiver, intentFilter);
    }
    private class NewMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("fromId");
            String msgId = intent.getStringExtra("msgId");
            String msgTime = intent.getStringExtra("msgTime");
            // 收到这个广播的时候，message已经在消息表中，可直接获取
            if(TextUtils.isEmpty(from)&&TextUtils.isEmpty(msgId)&&TextUtils.isEmpty(msgTime)){
                BmobMsg msg = BmobChatManager.getInstance(ChatActivity.this).getMessage(msgId, msgTime);
                if (!from.equals(mTargetId))// 如果不是当前正在聊天对象的消息，不处理
                    return;
                //添加到当前页面
                mMsgs.add(msg);
                mAdapter.notifyDataSetChanged();
                // 定位
                mListView.setSelection(mAdapter.getCount() - 1);
                //取消当前聊天对象的未读标示
                BmobDB.create(ChatActivity.this).resetUnread(mTargetId);
            }
            // 记得把广播给终结掉
            abortBroadcast();
        }
    }

    public void sendTextMessage(View v){
        final String msg = mInput.getText().toString();
        if (msg.equals("")) {
            ToastUtils.make(this,"请输入发送消息!");
            return;
        }
        boolean isNetConnected = NetUtils.isInternetAvilable(this);
        if (!isNetConnected) {
            ToastUtils.make(this,"请检查网络");
            // return;
        }
        // 组装BmobMessage对象
        BmobMsg message = BmobMsg.createTextSendMsg(this, mTargetId, msg);
        message.setExtra("Bmob");

        // 默认发送完成，将数据保存到本地消息表和最近会话表中
        manager.sendTextMessage(mTargetUser, message);
        // 刷新界面
        refreshMessage(message);
    }
    private void refreshMessage(BmobMsg msg) {
        Log.e("BB","消息类型"+msg.getBelongId()+"Chat");
        // 更新界面
        mMsgs.add(msg);
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(mAdapter.getCount() - 1);
        mInput.setText("");
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
        }
    }
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == NEW_MESSAGE) {
                BmobMsg message = (BmobMsg) msg.obj;
                String uid = message.getBelongId();
                BmobMsg m = BmobChatManager.getInstance(ChatActivity.this).getMessage(message.getConversationId(), message.getMsgTime());
                if (!uid.equals(mTargetId))// 如果不是当前正在聊天对象的消息，不处理
                    return;
                mMsgs.add(m);
                // 定位
                mAdapter.notifyDataSetChanged();
                mListView.setSelection(mAdapter.getCount() - 1);
                //取消当前聊天对象的未读标示
                BmobDB.create(ChatActivity.this).resetUnread(mTargetId);
            }
        }
    };

    public static final int NEW_MESSAGE = 0x001;// 收到消息
    @Override
    public void onMessage(BmobMsg message) {
        // TODO Auto-generated method stub
        Log.e("BB","OnMessage:"+message.getContent()+"-msgReceiver");
        Message handlerMsg = handler.obtainMessage(NEW_MESSAGE);
        handlerMsg.obj = message;
        handler.sendMessage(handlerMsg);
    }

    @Override
    public void onNetChange(boolean isNetConnected) {
        // TODO Auto-generated method stub
        if (!isNetConnected) {
        }
    }

    @Override
    public void onReaded(String conversionId, String msgTime) {
        // TODO Auto-generated method stub
        // 此处应该过滤掉不是和当前用户的聊天的回执消息界面的刷新
        if (conversionId.split("&")[1].equals(mTargetId)) {
            // 修改界面上指定消息的阅读状态
            for (BmobMsg msg : mMsgs) {
                if (msg.getConversationId().equals(conversionId)
                        && msg.getMsgTime().equals(msgTime)) {
                    msg.setStatus(BmobConfig.STATUS_SEND_RECEIVERED);
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onAddUser(BmobInvitation bmobInvitation) {

    }

    @Override
    public void onOffline() {

    }

    private boolean isSpeak=false;
    //切换当前状态
    public void changeInPut(View v){
        changeInputType();
    }

    private void changeInputType() {
        if(isSpeak){
            isSpeak=false;
            //切换到键盘输入
            mVoice.setBackgroundResource(R.drawable.btn_chat_voice_selector);
            mInput.setVisibility(View.VISIBLE);
            mSpeak.setVisibility(View.GONE);
            mSend.setVisibility(View.VISIBLE);
        }else{
            isSpeak=true;
            //切换到语音输入
            mVoice.setBackgroundResource(R.drawable.btn_chat_keyboard_selector);
            mInput.setVisibility(View.GONE);
            mSpeak.setVisibility(View.VISIBLE);
            mSend.setVisibility(View.GONE);
        }
    }
    // 语音有关
    RelativeLayout layout_record;
    TextView tv_voice_tips;
    ImageView iv_record;
    BmobRecordManager recordManager;
    //录音相关
    //初始化语音布局
    private void initVoiceView() {
        layout_record = (RelativeLayout) findViewById(R.id.layout_record);
        tv_voice_tips = (TextView) findViewById(R.id.tv_voice_tips);
        iv_record = (ImageView) findViewById(R.id.iv_record);
        mSpeak.setOnTouchListener(new VoiceTouchListen());
        initVoiceAnimRes();
        initRecordManager();
    }
    private void sendVoiceMessage(String local, int length) {
        manager.sendVoiceMessage(mTargetUser, local, length,
                new UploadListener() {

                    @Override
                    public void onStart(BmobMsg msg) {
                        // TODO Auto-generated method stub
                        refreshMessage(msg);
                    }

                    @Override
                    public void onSuccess() {
                        // TODO Auto-generated method stub
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(int error, String arg1) {
                        // TODO Auto-generated method stub
                        Log.e("BB","上传语音失败 -->arg1：" + arg1);
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }
    private void initRecordManager(){
        // 语音相关管理器
        recordManager = BmobRecordManager.getInstance(this);
        // 设置音量大小监听--在这里开发者可以自己实现：当剩余10秒情况下的给用户的提示，类似微信的语音那样
        recordManager.setOnRecordChangeListener(new OnRecordChangeListener() {

            @Override
            public void onVolumnChanged(int value) {
                // TODO Auto-generated method stub
                iv_record.setImageDrawable(drawable_Anims[value]);
            }

            @Override
            public void onTimeChanged(int recordTime, String localPath) {
                // TODO Auto-generated method stub
                Log.e("BB", "已录音长度:" + recordTime);
                if (recordTime >= BmobRecordManager.MAX_RECORD_TIME) {// 1分钟结束，发送消息
                    // 需要重置按钮
                    mSpeak.setPressed(false);
                    mSpeak.setClickable(false);
                    // 取消录音框
                    layout_record.setVisibility(View.INVISIBLE);
                    // 发送语音消息
                    sendVoiceMessage(localPath, recordTime);
                    //是为了防止过了录音时间后，会多发一条语音出去的情况。
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            mSpeak.setClickable(true);
                        }
                    }, 1000);
                } else {

                }
            }
        });
    }
    //录音话筒动画初始化
    private void initVoiceAnimRes() {
        drawable_Anims = new Drawable[] {
                getResources().getDrawable(R.drawable.chat_icon_voice2),
                getResources().getDrawable(R.drawable.chat_icon_voice3),
                getResources().getDrawable(R.drawable.chat_icon_voice4),
                getResources().getDrawable(R.drawable.chat_icon_voice5),
                getResources().getDrawable(R.drawable.chat_icon_voice6) };
    }

    class VoiceTouchListen implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!CommonUtils.checkSdCard()) {
                        ToastUtils.make(ChatActivity.this,"发送语音需要sdcard支持！");
                        return false;
                    }
                    try {
                        v.setPressed(true);
                        layout_record.setVisibility(View.VISIBLE);
                        tv_voice_tips.setText("松开手指，取消发送");
                        // 开始录音
                        recordManager.startRecording(mTargetId);
                    } catch (Exception e) {
                    }
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (event.getY() < 0) {
                        tv_voice_tips
                                .setText("松开手指，取消发送");
                        tv_voice_tips.setTextColor(Color.RED);
                    } else {
                        tv_voice_tips.setText("手指上滑,取消发送");
                        tv_voice_tips.setTextColor(Color.WHITE);
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    v.setPressed(false);
                    layout_record.setVisibility(View.INVISIBLE);
                    try {
                        if (event.getY() < 0) {// 放弃录音
                            recordManager.cancelRecording();
                            Log.e("BB", "放弃发送语音");
                        } else {
                            int recordTime = recordManager.stopRecording();
                            if (recordTime > 1) {
                                // 发送语音文件
                                Log.e("BB", "发送语音");
                                sendVoiceMessage(
                                        recordManager.getRecordFilePath(mTargetId),
                                        recordTime);
                            } else {// 录音时间过短，则提示录音过短的提示
                                layout_record.setVisibility(View.GONE);
                                showShortToast().show();
                            }
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    return true;
                default:
                    return false;
            }
        }
    }
    Toast toast;
    private Toast showShortToast() {
        if (toast == null) {
            toast = new Toast(this);
        }
        View view = LayoutInflater.from(this).inflate(
                R.layout.include_chat_voice_short, null);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        return toast;
    }
}
