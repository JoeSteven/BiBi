package com.joe.bibi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.joe.bibi.R;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.Comment;
import com.joe.bibi.domain.Debate;
import com.joe.bibi.utils.PrefUtils;
import com.joe.bibi.utils.ToastUtils;

import org.xutils.x;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class CommentDetailActivity extends AppCompatActivity {

    private Comment mComment;
    private BBUser mComUser;
    private TextView mLiked;
    private boolean isLiked;//判断该评论是否被当前用户赞了
    private boolean isClickLiked;//判断用户当前是否有点赞动作
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_detail);
        ActionBar ab=getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        mComment = (Comment) getIntent().getSerializableExtra("comment");
        ab.setTitle(mComment.getTitle());
        initView();
        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_comment_detail,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        if(item.getItemId()==R.id.action_done){
            Debate debate=new Debate();
            debate.setObjectId(mComment.getBelongTo());
            Intent intent=new Intent(this,DebateActivity.class);
            intent.putExtra("debate",debate);
            startActivity(intent);
            return true;
        }
        if(item.getItemId()==R.id.action_reply){
            Intent intent=new Intent(this,CommentActivity.class);
            intent.putExtra("debateID",mComment.getBelongTo());
            intent.putExtra("title",mComment.getTitle());
            intent.putExtra("reply",true);
            intent.putExtra("nick",mComment.getNick());
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        isClickLiked=false;
        isLiked= PrefUtils.getBoolean(this,mComment.getObjectId(),false);

        CircularImageView mAvatar= (CircularImageView) findViewById(R.id.iv_avatar_detail_comment);
        TextView mNick= (TextView) findViewById(R.id.tv_nick_detail_comment);
        mLiked = (TextView) findViewById(R.id.tv_like_detail_comment);
        TextView mContent= (TextView) findViewById(R.id.tv_content_detail_comment);
        TextView mDate= (TextView) findViewById(R.id.tv_date_detail_comment);

        x.image().bind(mAvatar, mComment.getAvatar());
        mNick.setText(mComment.getNick());
        mLiked.setText(mComment.getLike() + "");
        mDate.setText("创建于 "+mComment.getCreatedAt().substring(0,10));
        if(isLiked) mLiked.setTextColor(getResources().getColor(R.color.HeartRed));
        String content=mComment.getContent();
        if(content.startsWith("@")) {
            int i = content.indexOf(" ");
            SpannableStringBuilder style=new SpannableStringBuilder(content);
            style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.MainBlue)),0,i, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            mContent.setText(style);
        }else{
            mContent.setText(content);
        }

    }


    private void initData() {
        final TextView mDesc= (TextView) findViewById(R.id.tv_desc_detail_comment);
        BmobQuery<BBUser> query=new BmobQuery<BBUser>();
        query.setCachePolicy(BmobQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.addWhereEqualTo("username", mComment.getUserName());
        query.findObjects(this, new FindListener<BBUser>() {
            @Override
            public void onSuccess(List<BBUser> list) {
                mDesc.setVisibility(View.VISIBLE);
                if (list.size() > 0) {
                    BBUser user = list.get(0);
                    getBBuserInfo(user);
                    if (TextUtils.isEmpty(user.getDesc())) {
                        mDesc.setVisibility(View.GONE);
                    } else {
                        mDesc.setText(user.getDesc());
                    }
                } else {
                    mDesc.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(int i, String s) {
                mDesc.setVisibility(View.GONE);
            }
        });
    }

    private void getBBuserInfo(BBUser user) {
        mComUser=user;
    }
    //进入该用户的主页
    public void goToUserPage(View v){
        Intent intent=new Intent(this,UserActivity.class);
        intent.putExtra("user",mComUser);
        intent.putExtra("username",mComment.getUserName());
        startActivity(intent);
    }
    //点赞
    public void likeIt(View v){
        mLiked.setClickable(false);
        if(isLiked){
            cancelLike();
            return;
        }
        mLiked.setTextColor(getResources().getColor(R.color.HeartRed));
        int liked=Integer.parseInt(mLiked.getText().toString());
        liked++;
        mLiked.setText(liked + "");
        ToastUtils.make(this, "点赞成功");
        Comment update=new Comment();
        update.increment("Like");
        update.update(this, mComment.getObjectId(), new UpdateListener() {
            @Override
            public void onSuccess() {
                PrefUtils.putBoolean(CommentDetailActivity.this,mComment.getObjectId(),true);
                isLiked=true;
                isClickLiked=true;
                mLiked.setClickable(true);
            }

            @Override
            public void onFailure(int i, String s) {
                mLiked.setClickable(true);
            }
        });
    }

    //取消点赞
    private void cancelLike() {
        mLiked.setClickable(false);
        mLiked.setTextColor(getResources().getColor(R.color.LightBlack));
        int liked=Integer.parseInt(mLiked.getText().toString());
        liked--;
        mLiked.setText(liked + "");
        ToastUtils.make(this, "取消点赞");
        Comment update=new Comment();
        update.increment("Like",-1);
        update.update(this, mComment.getObjectId(), new UpdateListener() {
            @Override
            public void onSuccess() {
                PrefUtils.putBoolean(CommentDetailActivity.this,mComment.getObjectId(),false);
                isLiked=false;
                isClickLiked=true;
                mLiked.setClickable(true);
            }

            @Override
            public void onFailure(int i, String s) {
                mLiked.setClickable(true);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isClickLiked){
            Intent intent=new Intent();
            intent.setAction("com.joe.bibi.CLICK_LIKED");
            sendBroadcast(intent);
        }
    }
}
