package com.joe.bibi.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.joe.bibi.R;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.Debate;
import com.joe.bibi.utils.ToastUtils;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.SaveListener;

public class PublishActivity extends BaseActivity {

    private EditText mTitle;
    private EditText mDesc;
    private EditText mPositive;
    private EditText mNegative;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_publish);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mTitle = (EditText) findViewById(R.id.et_title_publish);
        mDesc = (EditText) findViewById(R.id.et_desc_publish);
        mPositive = (EditText) findViewById(R.id.et_positive_publish);
        mNegative = (EditText) findViewById(R.id.et_negative_publish);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pub, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_done_pub){
            Log.d("BB",checkDebate()+"-Publish");
            if(checkDebate()){
                //如果符合要求，发布辩题
                Log.d("BB","发布话题-Publish");
                publishDebate();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void publishDebate() {
        Debate debate=new Debate();
        BBUser publisher= BmobUser.getCurrentUser(this,BBUser.class);
        debate.setTitle(mTitle.getText().toString().replace(" ", ""));
        debate.setDesc(mDesc.getText().toString());
        debate.setPositiveop(mPositive.getText().toString().replace(" ", ""));
        debate.setNegativeop(mNegative.getText().toString().replace(" ", ""));
        debate.setPositive(0);
        debate.setNegative(0);
        debate.setTotal(0);
        debate.setComment(0);
        debate.setPublisher(publisher.getUsername());
        debate.setAvatar(publisher.getAvatarUrl());
        debate.save(this, new SaveListener() {
            @Override
            public void onSuccess() {
                ToastUtils.make(PublishActivity.this,"发布辩题成功");
                finish();
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtils.make(PublishActivity.this,"发布失败"+s);
                finish();
            }
        });
    }

    private Boolean checkDebate() {
        Log.d("BB",(mTitle==null)+"-Publish");
        String title=mTitle.getText().toString();
        String positive=mPositive.getText().toString();
        String negative=mNegative.getText().toString();
        if(TextUtils.isEmpty(title)){
            ToastUtils.make(this,"辩题不能为空");
            return false;
        }else if(TextUtils.isEmpty(positive)||TextUtils.isEmpty(negative)){
            ToastUtils.make(this,"正反双方观点不能为空");
            return false;
        }
        return true;
    }
}
