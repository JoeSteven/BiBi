package com.joe.bibi.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bmob.BmobProFile;
import com.bmob.btp.callback.DeleteFileListener;
import com.bmob.btp.callback.UploadListener;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.joe.bibi.R;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.Comment;
import com.joe.bibi.domain.Debate;
import com.joe.bibi.service.UpdateService;
import com.joe.bibi.utils.AvatarUtils;
import com.joe.bibi.utils.ConsUtils;
import com.joe.bibi.utils.PrefUtils;
import com.joe.bibi.utils.ToastUtils;

import org.xutils.x;

import java.io.File;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class UserCountActivity extends AppCompatActivity {

    private CircularImageView mAvatar;
    private TextView mNick;
    private EditText mTag;
    private EditText mDesc;
    private BBUser bbUser;
    private AvatarUtils avatarUtils;
    private boolean isUpdateAvatar;
    private String mAvatarPath;
    private String mAvatarUrl;
    private String mAvatarName;
    private boolean isUpdateSure;//判断是否更新了用户信息
    private LinearLayout mLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_count);
        ActionBar ab = getSupportActionBar();
        // 设置返回开启
        ab.setDisplayHomeAsUpEnabled(true);
        initView();

    }

    protected void initView() {
        mAvatar = (CircularImageView) findViewById(R.id.iv_avatar_update);
        mNick = (TextView) findViewById(R.id.tv_nick_update);
        mTag = (EditText) findViewById(R.id.et_tag_update);
        mDesc = (EditText) findViewById(R.id.et_desc_update);
        mLoad = (LinearLayout) findViewById(R.id.loading_count);
        initData();
    }

    protected void initData() {
        isUpdateAvatar=false;
        isUpdateSure=false;
        mAvatarPath ="";
        bbUser = BBUser.getCurrentUser(this, BBUser.class);
        x.image().bind(mAvatar,bbUser.getAvatarUrl());
        mNick.setText(bbUser.getNick());
        if(bbUser.getTag()!=null){
            mTag.setText(bbUser.getTag());
        }
        if(bbUser.getDesc()!=null){
            mDesc.setText(bbUser.getDesc());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sign, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_done){
            updateDone();
            mLoad.setVisibility(View.VISIBLE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateAvatar(View v){
        avatarUtils = new AvatarUtils(this);
        //上传头像
        avatarUtils.upLoadAvatar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case ConsUtils.CHOOSE_PHOTO:
                if(resultCode==RESULT_OK){
                    avatarUtils.choosePhoto(data);
                }
            break;
            case ConsUtils.CROP_PHOTO:
                if(resultCode==RESULT_OK){
                    mAvatar.setImageBitmap(BitmapFactory.decodeFile(PrefUtils.getString(this, ConsUtils.AVATAR, "")));
                    isUpdateAvatar=true;
                }
                break;
        }
    }


    private void updateDone() {
        //上传图片到服务器
        if(isUpdateAvatar){
            updateAvatarToServer();
        }else{
            startUpdate();
        }
    }

    private void startUpdate() {
        final Intent intent=new Intent();
        isUpdateSure=true;
        Debate debate=new Debate();
        BBUser newUser = new BBUser();
        if(!mAvatarPath.equals("")){
            newUser.setAvatar(mAvatarUrl);
            newUser.setAvatarName(mAvatarName);
            newUser.setAvatarUrl(mAvatarUrl);
        }
        Intent intent1=new Intent(this,UpdateService.class);
        intent1.putExtra("Nick",bbUser.getNick());
        intent1.putExtra("publisher", bbUser.getUsername());
        intent1.putExtra("AvatarUrl", mAvatarUrl);
        startService(intent1);

        newUser.setTag(mTag.getText().toString());
        newUser.setDesc(mDesc.getText().toString());
        newUser.update(this, bbUser.getObjectId(), new UpdateListener() {
            @Override
            public void onSuccess() {
                setResult(RESULT_OK, intent);
                mLoad.setVisibility(View.GONE);
                ToastUtils.make(UserCountActivity.this, "修改成功");
                finish();
            }

            @Override
            public void onFailure(int code, String msg) {
                setResult(RESULT_CANCELED, intent);
                mLoad.setVisibility(View.GONE);
                ToastUtils.make(UserCountActivity.this, "修改失败");
                finish();
            }
        });
    }

    private void updateAvatarToServer() {
        //首先删除服务器的头像
        final BmobProFile bmobProFile=BmobProFile.getInstance(this);
        bmobProFile.deleteFile(PrefUtils.getString(this, ConsUtils.AVATAR_NAME, ""), new DeleteFileListener() {

            @Override
            public void onError(int errorcode, String errormsg) {
                savaToServer(bmobProFile);
            }

            @Override
            public void onSuccess() {
                savaToServer(bmobProFile);
            }
        });
    }

    private void savaToServer(BmobProFile bmobProFile) {
        bmobProFile.upload(avatarUtils.getmAvatarPath(), new UploadListener() {
            @Override
            public void onSuccess(String fileName, String url, BmobFile bmobFile) {
                PrefUtils.putString(UserCountActivity.this, ConsUtils.AVATAR_NAME, fileName);
                mAvatarPath = url;
                mAvatarName = fileName;
                mAvatarUrl=bmobFile.getUrl();
                startUpdate();
            }

            @Override
            public void onProgress(int i) {

            }

            @Override
            public void onError(int i, String s) {
                startUpdate();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isUpdateAvatar){
            if(!isUpdateSure){
                File file=new File(new AvatarUtils(this).getmAvatarPath());
                if(file.exists()){
                    file.delete();
                    sendBroadcast(new Intent("com.joe.NO_UPDATE"));
                    Log.d("BB","取消修改，删除图片");
                }
            }
        }
    }
}
