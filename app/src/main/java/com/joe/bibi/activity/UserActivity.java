package com.joe.bibi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.joe.bibi.R;
import com.joe.bibi.application.BBApplication;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.Comment;
import com.joe.bibi.utils.PrefUtils;
import com.joe.bibi.utils.ToastUtils;
import com.joe.bibi.view.PullUpListView;

import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.PushListener;

public class UserActivity extends AppCompatActivity {

    private BBUser mUser;
    private String mUserName;
    private CircularImageView mAvatar;
    private TextView mNick;
    private TextView mDesc;
    private PullUpListView mListComments;
    private TextView mNoCom;
    private int limit=20;
    private List<Comment> mComments;
    private BmobQuery<Comment> comQuery;
    private int mCurrentPage=0;
    private myAdapter mAdapter;
    private ImageView mAddFriend;
    private ImageView mSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mUser = (BBUser) getIntent().getSerializableExtra("user");
        mUserName = getIntent().getStringExtra("username");
        initView();
        if(TextUtils.isEmpty(mUserName)) mUserName=mUser.getUsername();
        if(mUserName.equals(BBUser.getCurrentUser(this,BBUser.class).getUsername())){
            mAddFriend.setVisibility(View.GONE);
            mSend.setVisibility(View.GONE);
        }else{
            for(BmobChatUser user: BBApplication.getInstance().getContactList()){
                if(!TextUtils.isEmpty(mUserName)&&mUserName.equals(user.getUsername())){
                    mAddFriend.setVisibility(View.GONE);
                }
            }
        }
    }

    private void initView() {
        mAvatar = (CircularImageView) findViewById(R.id.iv_avatar_user);
        mNick = (TextView) findViewById(R.id.tv_nick_user);
        mDesc = (TextView) findViewById(R.id.tv_desc_user);
        mListComments = (PullUpListView) findViewById(R.id.list_comment_user);
        mAddFriend = (ImageView) findViewById(R.id.iv_add_user);
        mSend = (ImageView) findViewById(R.id.iv_send_user);
        mNoCom = (TextView) findViewById(R.id.tv_nocom_user);
        if(mUser==null){
            queryUser();
        }else{
            if(mUser.getAvatarUrl().equals("default")){
                mAvatar.setImageResource(PrefUtils.getInt(UserActivity.this, "defaultAvatar", R.drawable.ic_1_default));
            }else{
                x.image().bind(mAvatar, mUser.getAvatarUrl());
            }
            mNick.setText(mUser.getNick());
            if(TextUtils.isEmpty(mUser.getDesc())){
                mDesc.setText("");
            }else{
                mDesc.setText(mUser.getDesc());
            }
            initData();
        }
    }


    private void initData() {
        mComments = new ArrayList<Comment>();
        comQuery = new BmobQuery<Comment>();
        if(mUser==null){
            comQuery.addWhereEqualTo("UserName", mUserName);
        }else{
            comQuery.addWhereEqualTo("UserName", mUser.getUsername());
        }
        comQuery.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
        comQuery.setLimit(limit);
        comQuery.order("-Like");
        comQuery.findObjects(this, new FindListener<Comment>() {
            @Override
            public void onSuccess(List<Comment> list) {
                mComments = list;
                initComments();
            }

            @Override
            public void onError(int i, String s) {
                initComments();
            }
        });

    }

    private void initComments() {
        mListComments.setVisibility(View.VISIBLE);
        mNoCom.setVisibility(View.GONE);
        if(mComments.size()>0){
            initAdapter();
        }else{
            mListComments.setVisibility(View.GONE);
            mNoCom.setVisibility(View.VISIBLE);
        }
    }

    private void initAdapter() {
        mAdapter = new myAdapter();
        mListComments.setAdapter(mAdapter);
        initListener();
    }

    private void initListener() {
        mListComments.setOnRefreshListener(new PullUpListView.OnRefreshListener() {
            @Override
            public void onLoadMore() {
                if (mComments.size() < limit) {
                    mListComments.LoadingDone();
                    return;
                }
                loadMore();

            }
        });
        mListComments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(UserActivity.this,CommentDetailActivity.class);
                Comment comment= (Comment) mListComments.getAdapter().getItem(position);
                intent.putExtra("comment",comment);
                startActivity(intent);
            }
        });
    }

    private void loadMore() {
        mCurrentPage++;
        comQuery.setSkip(mCurrentPage * limit);
        comQuery.findObjects(this, new FindListener<Comment>() {
            @Override
            public void onSuccess(List<Comment> list) {
                for (Comment c:list) {
                    mComments.add(c);
                }
                mListComments.LoadingDone();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(int i, String s) {
                mListComments.LoadingDone();
                mCurrentPage--;
            }
        });
    }

    class myAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mComments.size();
        }

        @Override
        public Object getItem(int position) {
            return mComments.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;
            Comment comment=mComments.get(position);
            if(convertView==null){
                holder=new ViewHolder();
                convertView=View.inflate(UserActivity.this,R.layout.item_comment_user,null);
                holder.title= (TextView) convertView.findViewById(R.id.tv_title_item_user);
                holder.comment= (TextView) convertView.findViewById(R.id.tv_comment_item_user);
                holder.hint= (TextView) convertView.findViewById(R.id.tv_hint_user);
                holder.like= (TextView) convertView.findViewById(R.id.tv_like_user);
                convertView.setTag(holder);
            }else{
                holder= (ViewHolder) convertView.getTag();
            }
            holder.title.setText(comment.getTitle());
            holder.comment.setText(comment.getContent());
            holder.hint.setText(comment.getNick()+" 参与了话题");
            int change=comment.getLike()/100;
            double total=change;
            if(total>9.0){
                total=total/10.0;
                holder.like.setText(total+"K");
            }else{
                holder.like.setText(comment.getLike()+"");
            }
            //设置支持的立场

            switch (comment.getPoint()){
                case Comment.POSITIVE_COMMENT:
                    //holder.like.setBackground(getDrawable(R.drawable.shape_text_red));
                    holder.like.setBackgroundResource(R.drawable.shape_text_red);
                    break;
                case Comment.NEUTRAL_COMMENT:
                    //holder.like.setBackground(getDrawable(R.drawable.shape_text_gray));
                    holder.like.setBackgroundResource(R.drawable.shape_text_gray);
                    break;
                case Comment.NEGATIVE_COMMENT:
                    // holder.like.setBackground(getDrawable(R.drawable.shape_text_blue));
                    holder.like.setBackgroundResource(R.drawable.shape_text_blue);
                    break;

            }
            return convertView;
        }
    }
    class ViewHolder{
        TextView title;
        TextView comment;
        TextView hint;
        TextView like;
    }
    private void queryUser() {
        BmobQuery<BBUser> query=new BmobQuery<BBUser>();
        query.addWhereEqualTo("username",mUserName);
        query.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.findObjects(this, new FindListener<BBUser>() {
            @Override
            public void onSuccess(List<BBUser> list) {
                if(list.size()>0){
                    mUser=list.get(0);
                    if(list.get(0).getAvatarUrl().equals("default")){
                        mAvatar.setImageResource(PrefUtils.getInt(UserActivity.this, "defaultAvatar", R.drawable.ic_1_default));
                    }else{
                        x.image().bind(mAvatar, list.get(0).getAvatarUrl());
                    }

                    mNick.setText(list.get(0).getNick());
                    if(TextUtils.isEmpty(list.get(0).getDesc())){
                        mDesc.setText("");
                    }else{
                        mDesc.setText(list.get(0).getDesc());
                    }
                    initData();
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }
    public void sendMessage(View v){
        Intent intent=new Intent(this,ChatActivity.class);
        intent.putExtra("user",mUser);
        startActivity(intent);
    }
    public void addFriend(View v){
        mAddFriend.setClickable(false);
        Log.e("BB", "接收人ID" + mUser.getObjectId());
        BmobChatManager.getInstance(this).sendTagMessage(BmobConfig.TAG_ADD_CONTACT, mUser.getObjectId(), new PushListener() {
            @Override
            public void onSuccess() {
                ToastUtils.make(UserActivity.this, "发送请求成功，等待对方回应");
                mAddFriend.setClickable(true);
            }

            @Override
            public void onFailure(int i, String s) {
                mAddFriend.setClickable(true);
                ToastUtils.make(UserActivity.this, "发送请求失败，请重试");
            }
        });
    }
}
