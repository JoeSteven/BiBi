package com.joe.bibi.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.joe.bibi.R;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.Comment;
import com.joe.bibi.domain.Debate;
import com.joe.bibi.domain.Follow;
import com.joe.bibi.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class CommentActivity extends AppCompatActivity {

    private EditText mComment;
    private String mDebateID;
    private int mPoint;
    private String mTitle;
    private String mUserName;
    private Comment comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        ActionBar ab=getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        mDebateID = getIntent().getStringExtra("debateID");
        mTitle = getIntent().getStringExtra("title");
        mUserName = getIntent().getStringExtra("username");
        initView();
    }

    private void initView() {
        mComment = (EditText) findViewById(R.id.et_comment_comment);
        mPoint = Comment.NEUTRAL_COMMENT;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_comment,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_done){
            //发布评论
            choosePoint();
            return true;
        }
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void choosePoint() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        String[] item=new String[]{"正方","中立","反方"};
        builder.setTitle("请选择评论所支持的立场");
        builder.setSingleChoiceItems(item, 1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPoint = which;
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ToastUtils.make(CommentActivity.this, "正在发布，发布成功会自动跳转");
                publishComment();
                dialog.dismiss();
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

    private void publishComment() {
        BBUser currentUser= BmobUser.getCurrentUser(this,BBUser.class);
        Log.d("BB", "立场" + mPoint + "-Comment");
        comment = new Comment();
        comment.setBelongTo(mDebateID);
        comment.setUserName(currentUser.getUsername());
        comment.setAvatar(currentUser.getAvatarUrl());
        comment.setNick(currentUser.getNick());
        comment.setPoint(mPoint);
        comment.setLike(0);
        comment.setUnLike(0);
        comment.setTitle(mTitle);
        comment.setContent(mComment.getText().toString());
        Debate debate=new Debate();
        debate.increment("comment");
        debate.update(this, mDebateID, new UpdateListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i, String s) {
                Log.d("BB", "评论增加失败" + s + "-CommentActivity");
            }
        });
        comment.save(this, new SaveListener() {
            @Override
            public void onSuccess() {

                ToastUtils.make(CommentActivity.this, "发布成功！");
                sendBroadcast(new Intent("com.joe.bibi.COMMENT"));
                pushComment();
                finish();
            }

            @Override
            public void onFailure(int i, String s) {
                Log.d("BB", "评论失败" + s + "-CommentActivity");
                ToastUtils.make(CommentActivity.this, "发布失败，请检查网络");
            }
        });

    }

    private void pushComment() {
        BmobQuery<BBUser> idQuery=new BmobQuery<BBUser>();
        idQuery.addWhereEqualTo("username", mUserName);
        idQuery.findObjects(this, new FindListener<BBUser>() {
            @Override
            public void onSuccess(List<BBUser> list) {
                if (list.size() > 0) {
                    setReceiverId(list.get(0).getInstallId());
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });

    }

    private void setReceiverId(String installId) {
        //所有要接收通知的对象
        final List<String> receivers=new ArrayList<String>();
        String installationId =installId;
        receivers.add(installationId);
        BmobQuery<Follow> followBmobQuery=new BmobQuery<Follow>();
        followBmobQuery.addWhereEqualTo("debateId", comment.getBelongTo());
        followBmobQuery.setLimit(100000);
        followBmobQuery.findObjects(this, new FindListener<Follow>() {
            @Override
            public void onSuccess(List<Follow> list) {
                if (list.size() > 0) {
                    for (Follow f : list) {
                        receivers.add(f.getFollowerInstallationId());
                    }
                }
                pushCommit(receivers);
            }

            @Override
            public void onError(int i, String s) {
                pushCommit(receivers);
            }
        });
    }

    //确认推送
    private void pushCommit(List<String> receivers) {
        BmobPushManager bmobPush = new BmobPushManager(CommentActivity.this);
        BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
        query.addWhereContainedIn("installationId",receivers);
        bmobPush.setQuery(query);
        bmobPush.pushMessage(comment.getObjectId());
    }
}
