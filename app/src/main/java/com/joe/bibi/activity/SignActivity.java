package com.joe.bibi.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bmob.BTPFileResponse;
import com.bmob.BmobProFile;
import com.bmob.btp.callback.UploadListener;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.joe.bibi.R;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.utils.AvatarUtils;
import com.joe.bibi.utils.ConsUtils;
import com.joe.bibi.utils.PrefUtils;
import com.joe.bibi.utils.ToastUtils;

import cn.bmob.im.BmobUserManager;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.SaveListener;

public class SignActivity extends AppCompatActivity {

    private static final int CHOOSE_PHOTO =1 ;
    private static final int CROP_PHOTO = 2;
    private EditText mNick;
    private EditText mMail;
    private EditText mPass;
    private CircularImageView circularImageView;
    private String mAvatar;
    private boolean isUpdateAvatar;
    private AvatarUtils avatarUtils;
    private String AvatarName;
    private String AvatarUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        ActionBar ab = getSupportActionBar();
        // 设置返回开启
        ab.setDisplayHomeAsUpEnabled(true);
        initView();
    }

    protected void initView() {
        circularImageView = (CircularImageView) findViewById(R.id.iv_avatar_sign);
        mNick = (EditText) findViewById(R.id.et_nick_sign);
        mMail = (EditText) findViewById(R.id.et_mail_sign);
        mPass = (EditText) findViewById(R.id.et_pass_sign);
        mAvatar = "";
        isUpdateAvatar=false;
        AvatarName="default";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sign, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_done){
            if(isUpdateAvatar){
                saveAvatar();
            }else{
                signUp();
            }
            Toast.makeText(this,"注册成功",Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signUp() {
        String mail=mMail.getText().toString();
        String nick=mNick.getText().toString();
        //加密用户密码
        final String pass= mPass.getText().toString();
        if(mail.equals("")||nick.equals("")||pass.equals("")){
            ToastUtils.make(this, "信息不能为空");
            return;
        }
        if(nick.length()<2||nick.length()>12){
            ToastUtils.make(this, "昵称长度不符合要求");
            return;
        }
        final BBUser bbUser=new BBUser();
        if(mAvatar.equals("")){
            bbUser.setAvatar(ConsUtils.DEFAULT_AVATAR);
            bbUser.setAvatarUrl("");
        }else{
            bbUser.setAvatar(mAvatar);
            bbUser.setAvatarUrl(AvatarUrl);
        }
        //邮箱地址作为用户名
        bbUser.setUsername(mail);
        bbUser.setPassword(pass);
        bbUser.setEmail(mail);
        bbUser.setNick(nick);
        bbUser.setAvatarName(AvatarName);
        bbUser.setDeviceType("android");
        bbUser.setInstallId(BmobInstallation.getInstallationId(this));
        bbUser.signUp(this, new SaveListener() {
            @Override
            public void onSuccess() {
                //将用户名密码保存在本地
                PrefUtils.putString(SignActivity.this,ConsUtils.USER_NAME,bbUser.getUsername());
                PrefUtils.putString(SignActivity.this,ConsUtils.NICK_NAME,bbUser.getNick());
                // 将设备与username进行绑定
                BmobUserManager userManager=BmobUserManager.getInstance(SignActivity.this);
                userManager.bindInstallationForRegister(bbUser.getUsername());
                //注册成功直接到主页面并结束LoginActivity
                Intent intent=new Intent(SignActivity.this,HomeActivity.class);
                intent.setAction(ConsUtils.SIGN_UP_SUCCESS);
                startActivity(intent);
                sendBroadcast(intent);
                finish();
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtils.make(SignActivity.this,"注册失败:"+s);
            }
        });
    }

    public void upLoadAvatar(View v){
        avatarUtils = new AvatarUtils(this);
        avatarUtils.upLoadAvatar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CHOOSE_PHOTO:
                if(resultCode==RESULT_OK) {
                   avatarUtils.choosePhoto(data);
                }
                break;
            case CROP_PHOTO:
                if(resultCode==RESULT_OK){
                    showAvatar();
                }
                break;
        }
    }


    private void showAvatar(){
        circularImageView.setImageBitmap(BitmapFactory.decodeFile(PrefUtils.getString(this, ConsUtils.AVATAR, "")));
        isUpdateAvatar=true;
    }

    private void saveAvatar() {
        //将图片上传到服务器
        BTPFileResponse response = BmobProFile.getInstance(this).upload(avatarUtils.getmAvatarPath(), new UploadListener() {

            @Override
            public void onSuccess(String fileName,String url,BmobFile file) {
                Log.i("bmob","文件上传成功："+fileName+",可访问的文件地址："+file.getUrl());
                // TODO Auto-generated method stub
                // fileName ：文件名（带后缀），这个文件名是唯一的，开发者需要记录下该文件名，方便后续下载或者进行缩略图的处理
                // url        ：文件地址
                // file        :BmobFile文件类型，`V3.4.1版本`开始提供，用于兼容新旧文件服务。file.getUrl()
                // 注：若上传的是图片，url地址并不能直接在浏览器查看（会出现404错误），
                // 需要经过`URL签名`得到真正的可访问的URL地址,
                // 当然，`V3.4.1`的版本可直接从'file.getUrl()'中获得可访问的文件地址。
                PrefUtils.putString(SignActivity.this,ConsUtils.AVATAR_NAME,fileName);
                AvatarName=fileName;
                mAvatar =url;
                AvatarUrl=file.getUrl();
                signUp();
            }

            @Override
            public void onProgress(int progress) {
            }

            @Override
            public void onError(int statuscode, String errormsg) {
            }
        });
    }

}
