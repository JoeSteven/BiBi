package com.joe.bibi.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bmob.BmobProFile;
import com.bmob.btp.callback.DownloadListener;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.joe.bibi.R;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.fragment.HomeFragment;
import com.joe.bibi.utils.AvatarUtils;
import com.joe.bibi.utils.ConsUtils;
import com.joe.bibi.utils.PrefUtils;

import org.xutils.x;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.bmob.v3.BmobUser;

/*
* Rereshing 要分页加载，区分下拉刷新和上拉加载更多，用ListView实现pullUp效果
* query.setSkip()设置跳过数据条数 =currentPage*count+1;
* query.setLimit(count);
* */
public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int UPDATE_USER = 0;
    private CircularImageView mAvatar;
    private TextView mNickName;
    private BBUser mUser;
    private NavigationView navigationView;
    private NoUpdateReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();
        initData();
    }

    private void initView() {
        Log.d("BB","初始化View-Home");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Log.d("BB",(toolbar==null)+"toobar为空-Home");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        initFragment();
        initChildView();
    }

    private void initFragment() {
        HomeFragment homeFragment=new HomeFragment();
        FragmentManager fm=getFragmentManager();
        FragmentTransaction ft=fm.beginTransaction();
        ft.replace(R.id.fl_frag_home,homeFragment);
        ft.commit();
    }

    private void initChildView() {
        View menuHeader=View.inflate(this, R.layout.nav_header_home, navigationView);
        mAvatar = (CircularImageView) menuHeader.findViewById(R.id.iv_avatar_home);
        mNickName = (TextView) menuHeader.findViewById(R.id.tv_nick_home);
        mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(HomeActivity.this,UserActivity.class);
                intent.putExtra("user", BmobUser.getCurrentUser(HomeActivity.this,BBUser.class));
                startActivity(intent);
            }
        });
    }

    private void initData() {
        initReceiver();
        mUser = BBUser.getCurrentUser(this, BBUser.class);
        //设置用户名和头像
        showAvatarAndNick();
    }

    private void showAvatarAndNick() {
        //先检查本地缓存
        Log.d("BB","准备显示头像");
        mNickName.setText(mUser.getNick());
        x.image().bind(mAvatar,mUser.getAvatarUrl());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            startActivity(new Intent(this,SearchActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.pub_debate_drawer) {
            startActivity(new Intent(this,PublishActivity.class));
        } else if (id == R.id.my_friend_drawer) {

        } else if (id == R.id.msg_drawer) {

        } else if (id == R.id.setting_drawer) {

        } else if (id == R.id.count_set_drawer) {
            //个人中心
            openUserCount();
        } else if (id == R.id.exit_count_drawer) {
            //退出登陆
            Intent intent=new Intent(this,LoginActivity.class);
            intent.setAction(ConsUtils.LOGIN_OUT);
            startActivity(intent);
            mUser.logOut(this);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //打开个人中心
    private void openUserCount() {
        Intent intent=new Intent(this,UserCountActivity.class);
        startActivityForResult(intent, UPDATE_USER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            initData();
        }
    }

    private void initReceiver() {
        IntentFilter intentFilter=new IntentFilter("com.joe.NO_UPDATE");
        receiver = new NoUpdateReceiver();
        registerReceiver(receiver,intentFilter);
    }

    class NoUpdateReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.joe.NO_UPDATE")){
                showAvatarAndNick();
            }
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
