package com.joe.bibi.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.joe.bibi.R;
import com.joe.bibi.application.BBApplication;
import com.joe.bibi.utils.ConsUtils;
import com.joe.bibi.utils.PrefUtils;
import com.joe.bibi.view.SettingView;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ActionBar ab=getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        initView();
    }

    private void initView() {
        final BBApplication application=BBApplication.getInstance();
        SettingView mCommentSet= (SettingView) findViewById(R.id.sv_comment_setting);
        SettingView mVoiceSet= (SettingView) findViewById(R.id.sv_voice_setting);
        SettingView mVibrate= (SettingView) findViewById(R.id.sv_vibrate_setting);
        SettingView mMsg= (SettingView) findViewById(R.id.sv_msg_setting);
        SettingView mUpdate= (SettingView) findViewById(R.id.sv_update_setting);
        mCommentSet.setToggle(application.isCommentAllowed);
        mVoiceSet.setToggle(application.isVoiceAllowed);
        mVibrate.setToggle(application.isVibrateAllowed);
        mMsg.setToggle(application.isMessageAllowed);
        mUpdate.setToggle(PrefUtils.getBoolean(this,ConsUtils.IS_UPDATE,true));
        mCommentSet.setOnToggleChangeListener(new SettingView.OnToggleChangeListener() {
            @Override
            public void onChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.putBoolean(SettingActivity.this, ConsUtils.IS_COMMENT_ALLOWED, isChecked);
                application.isCommentAllowed = isChecked;
            }
        });
        mMsg.setOnToggleChangeListener(new SettingView.OnToggleChangeListener() {
            @Override
            public void onChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.putBoolean(SettingActivity.this, ConsUtils.IS_MESSAGE_ALLOWED,isChecked);
                application.isMessageAllowed=isChecked;
            }
        });
        mVoiceSet.setOnToggleChangeListener(new SettingView.OnToggleChangeListener() {
            @Override
            public void onChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.putBoolean(SettingActivity.this, ConsUtils.IS_VOICE_ALLOWED,isChecked);
                application.isVoiceAllowed=isChecked;
            }
        });
        mVibrate.setOnToggleChangeListener(new SettingView.OnToggleChangeListener() {
            @Override
            public void onChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.putBoolean(SettingActivity.this, ConsUtils.IS_VIBRATE_ALLOWED,isChecked);
                application.isVibrateAllowed=isChecked;
            }
        });
        mUpdate.setOnToggleChangeListener(new SettingView.OnToggleChangeListener() {
            @Override
            public void onChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.putBoolean(SettingActivity.this, ConsUtils.IS_UPDATE,isChecked);
            }
        });
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
