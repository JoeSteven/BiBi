package com.joe.bibi.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bmob.BmobProFile;
import com.bmob.btp.callback.DownloadListener;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.joe.bibi.R;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.utils.AvatarUtils;
import com.joe.bibi.utils.ConsUtils;
import com.joe.bibi.utils.PrefUtils;
import com.joe.bibi.utils.ToastUtils;

import org.xutils.x;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import cn.bmob.im.BmobChat;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.ResetPasswordByEmailListener;
import cn.bmob.v3.listener.SaveListener;

public class LoginActivity extends AppCompatActivity {

    private static final int AUTO_LOGIN =1 ;
    private EditText mUserName;
    private EditText mPassWord;
    private LinearLayout loginContent;
    private TextView mLoading;
    private CircularImageView circularImageView;
    private BBUser bbUser;
    private SignUpReceiver receiver;
    private ImageView mLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initBmob();
        Log.d("BB","登陆页面-login");
        initView();
        initData();
        autoLogin();
        initReceiver();
    }

    private void initView() {
        loginContent = (LinearLayout) findViewById(R.id.content_login);
        mLoading = (TextView) findViewById(R.id.tv_loading_login);
        mUserName = (EditText) findViewById(R.id.et_user_log);
        mPassWord = (EditText) findViewById(R.id.et_pass_log);
        mLogo = (ImageView) findViewById(R.id.ic_logo_login);
        circularImageView = (CircularImageView) findViewById(R.id.iv_avatar_log);
        File allFile=new File(Environment.getExternalStorageDirectory(),"BiBi");
        if(!allFile.exists()){
            allFile.mkdir();
        }
        Log.d("BB", "初始化View完成-login");
        mUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("BB","注册on"+s.toString()+"-Sign");

            }

            @Override
            public void afterTextChanged(Editable s) {
            //动态查询用户的头像
                if(s.toString().contains(".com")) queryBBuser(s.toString());
            }
        });
    }

    private void queryBBuser(String s) {
        BmobQuery<BBUser> query=new BmobQuery<BBUser>();
        query.addWhereEqualTo("username",s);
        query.setCachePolicy(BmobQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.findObjects(this, new FindListener<BBUser>() {
            @Override
            public void onSuccess(List<BBUser> list) {
                if(list.size()>0){
                    x.image().bind(circularImageView,list.get(0).getAvatarUrl());
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    private void initData() {
        //看本地是否有头像缓存
        Log.d("BB","初始化Data-login");
        bbUser = BmobUser.getCurrentUser(this, BBUser.class);
        if(bbUser==null) return;
        x.image().bind(circularImageView,bbUser.getAvatarUrl());
        Log.d("BB","bbUser不为空-login");
       /* File avatar=new File(PrefUtils.getString(this, ConsUtils.AVATAR, ""));
        if(avatar.exists()){
            //从本地读取头像
            Bitmap bitmap= BitmapFactory.decodeFile(PrefUtils.getString(this,ConsUtils.AVATAR,""));
            circularImageView.setImageBitmap(bitmap);
        }else{
            getAvatarFromCloud();
        }*/
        if(!PrefUtils.getString(this,ConsUtils.USER_NAME,"").equals("")){
            mUserName.setText(PrefUtils.getString(this, ConsUtils.USER_NAME, ""));
        }
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==AUTO_LOGIN){
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                Log.d("BB", "启动Home-login");
                finish();
            }
        }
    };

    private void getAvatarFromCloud() {
        BmobProFile bmobProFile= BmobProFile.getInstance(this);
        bmobProFile.download(bbUser.getAvatarName(), new DownloadListener() {
            @Override
            public void onSuccess(String fullpath) {
                AvatarUtils utils=new AvatarUtils(LoginActivity.this);
                File file=new File(utils.getmAvatarPath());
                if (file.exists()){
                    file.delete();
                }
                FileInputStream fis=null;
                FileOutputStream fos=null;
                try {
                    file.createNewFile();
                    fis=new FileInputStream(fullpath);
                    fos=new FileOutputStream(file);
                    byte[] buffer=new byte[1024];
                    while (fis.read(buffer,0,buffer.length)!=-1){
                        fos.write(buffer,0,buffer.length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        fis.close();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Bitmap bitmap=BitmapFactory.decodeFile(utils.getmAvatarPath());
                Message msg=Message.obtain();
                msg.obj=bitmap;
                handler.sendMessage(msg);
            }

            @Override
            public void onProgress(String s, int i) {

            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    //自动登陆
    private void autoLogin() {
        //先判断是否要自动登陆
        Log.d("BB","自动登陆-login");
        Intent intent=getIntent();
        if(intent.getAction()!=null){
            if(intent.getAction().equals(ConsUtils.LOGIN_OUT)){
                //如果是退出登陆，不进行自动登陆
                return;
            }
        }

        if(bbUser!= null){
            loginContent.setVisibility(View.GONE);
            mLoading.setVisibility(View.VISIBLE);
            mLogo.setVisibility(View.VISIBLE);
            handler.sendEmptyMessageDelayed(AUTO_LOGIN, 2000);
        }
    }
    private void initBmob() {
        BmobChat.DEBUG_MODE = true;
        //BmobIM SDK初始化--只需要这一段代码即可完成初始化
        BmobChat.getInstance(this).init(ConsUtils.APPLICATION_ID);
        // 使用推送服务时的初始化操作
        BmobInstallation.getCurrentInstallation(this).save();
        // 启动推送服务
        BmobChat.DEBUG_MODE = true;
        //BmobIM SDK初始化--只需要这一段代码即可完成初始化
        BmobChat.getInstance(this).init(ConsUtils.APPLICATION_ID);
    }


    private void initReceiver() {
        //注册一个广播接收者
        receiver = new SignUpReceiver();
        IntentFilter intentFilter=new IntentFilter(ConsUtils.SIGN_UP_SUCCESS);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    //登陆
    public void login(View v){
        loginContent.setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);
        mLogo.setVisibility(View.VISIBLE);
        String UserName=mUserName.getText().toString();
        //加密用户密码
        String pass= mPassWord.getText().toString();
        if(UserName.equals("")||pass.equals("")){
            Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        BBUser bbUser=new BBUser();
        bbUser.setUsername(UserName);
        bbUser.setPassword(pass);
        loginBB(bbUser, true);

    }

    //登陆的具体实现
    private void loginBB(final BBUser bbUser, final boolean isHand) {
        //isHand判断是否手动登陆，手动登陆则修改信息
        bbUser.login(this, new SaveListener() {
            @Override
            public void onSuccess() {
                if(isHand){
                    PrefUtils.putString(LoginActivity.this,ConsUtils.USER_NAME,bbUser.getUsername());
                }
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtils.make(LoginActivity.this, "登陆失败" + s);
                mLoading.setVisibility(View.GONE);
                mLogo.setVisibility(View.GONE);
                loginContent.setVisibility(View.VISIBLE);
            }
        });
    }

    //注册
    public void signUp(View v){
        startActivity(new Intent(this,SignActivity.class));
    }

    class SignUpReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction()==ConsUtils.SIGN_UP_SUCCESS){
                finish();
            }
        }
    }

    public void resetPassword(View v){
        if(!mUserName.getText().toString().equals("")){
            final String email = mUserName.getText().toString();
            BmobUser.resetPasswordByEmail(this, email, new ResetPasswordByEmailListener() {
                @Override
                public void onSuccess() {
                    ToastUtils.make(LoginActivity.this,"重置密码邮件已发送到"+email+"，请前往修改");
                }

                @Override
                public void onFailure(int i, String s) {
                    ToastUtils.make(LoginActivity.this, "重置密码失败，请确认邮箱输入正确！");
                }
            });
        }
    }
}
