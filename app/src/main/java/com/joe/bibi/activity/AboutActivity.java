package com.joe.bibi.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joe.bibi.R;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.FeedBack;
import com.joe.bibi.utils.ToastUtils;

import cn.bmob.v3.listener.SaveListener;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ActionBar ab=getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        initView();
    }

    private void initView() {
        TextView mVersion= (TextView) findViewById(R.id.tv_version_about);
        LinearLayout mIntroduce= (LinearLayout) findViewById(R.id.ll_introduce_about);
        LinearLayout mFeedBack= (LinearLayout) findViewById(R.id.ll_feedback_about);
        LinearLayout mWeiBo= (LinearLayout) findViewById(R.id.ll_weibo_about);
        mVersion.setText("版本号 " + getVersion());
        mIntroduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.make(AboutActivity.this,"还未开发，敬请期待");
            }
        });

        mFeedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(AboutActivity.this);
                final View feedView=View.inflate(AboutActivity.this,R.layout.dialog_feedback,null);
                builder.setTitle("问题反馈");
                builder.setView(feedView);
                builder.setPositiveButton("提交问题", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        EditText phone = (EditText) feedView.findViewById(R.id.et_phone_feedback);
                        EditText system = (EditText) feedView.findViewById(R.id.et_system_feedback);
                        EditText content = (EditText) feedView.findViewById(R.id.et_content_feedback);
                        if (TextUtils.isEmpty(content.getText().toString())) {
                            ToastUtils.make(AboutActivity.this,"请描述您的问题");
                            return;
                        }
                        ToastUtils.make(AboutActivity.this, "正在提交");
                        FeedBack feedBack=new FeedBack();
                        feedBack.setPhone(phone.getText().toString());
                        feedBack.setSystem(system.getText().toString());
                        feedBack.setFeedback(content.getText().toString());
                        feedBack.setUsername(BBUser.getCurrentUser(AboutActivity.this,BBUser.class).getUsername());
                        feedBack.save(AboutActivity.this, new SaveListener() {
                            @Override
                            public void onSuccess() {
                                ToastUtils.make(AboutActivity.this,"提交成功，感谢您的反馈");
                                dialog.dismiss();
                            }

                            @Override
                            public void onFailure(int i, String s) {
                                ToastUtils.make(AboutActivity.this,"提交失败，请重试");
                            }
                        });
                    }
                });
                builder.show();
            }
        });

        mWeiBo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(AboutActivity.this);
                builder.setTitle("开发者微博");
                builder.setMessage("@黑丫山上小旋风");
                builder.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    private String getVersion() {
        PackageManager packageManager=getPackageManager();
        try {
            PackageInfo info=packageManager.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "1.0.0";
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
