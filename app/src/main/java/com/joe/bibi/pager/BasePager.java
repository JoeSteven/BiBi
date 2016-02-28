package com.joe.bibi.pager;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.joe.bibi.R;
import com.joe.bibi.activity.DebateActivity;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.Debate;
import com.joe.bibi.view.PullUpListView;

import org.xutils.x;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Joe on 2016/2/2.
 */
public abstract class BasePager {
    protected static final int REFRESH_DONE =7 ;
    protected static final int NO_MORE_DATA =8 ;
    public View mRootView;
    public String mTitle;
    public String mOrder;
    public Activity mActivity;
    public PullUpListView mListView;
    protected List<Debate> debates;
    protected PullRefreshLayout mRefresh;
    protected ProgressBar mLoading;
    protected boolean isInitData;
    protected int mCurrentPage;
    protected boolean isLoadingMore;
    protected BmobQuery<Debate> query;
    protected myAdapter adapter;
    protected CoordinatorLayout container;
    protected boolean isRefreshing;
    protected int mLimit=20;
    protected boolean mNoMoreData;

    public BasePager(Activity mActivity) {
        super();
        this.mActivity = mActivity;
        mNoMoreData=false;
        initTitle();
        initView();
        isInitData=false;
        isRefreshing=false;
    }

    public abstract void initTitle();

    public void initView() {
        mRootView=View.inflate(mActivity, R.layout.pager_base,null);
        mListView = (PullUpListView) mRootView.findViewById(R.id.list_viewpager_home);
        mRefresh = (PullRefreshLayout)mRootView.findViewById(R.id.swipeRefreshLayout);
        mRefresh.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        mLoading = (ProgressBar) mRootView.findViewById(R.id.loading_pager);
        container = (CoordinatorLayout) mActivity.findViewById(R.id.coorl_home);
        mCurrentPage=0;
        isLoadingMore=false;
        query = new BmobQuery<Debate>();
    }

    public void initData(){
        if(isInitData==true) return;
        query.setLimit(mLimit);
        if(mTitle=="我的辩题") query.addWhereEqualTo("publisher", BmobUser.getCurrentUser(mActivity,BBUser.class).getUsername());
        if(isRefreshing){
            query.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
        }else{
            query.setCachePolicy(BmobQuery.CachePolicy.CACHE_THEN_NETWORK);
        }
        final ArrayList<Debate> list = new ArrayList<Debate>();
        query.order(mOrder);

        query.findObjects(mActivity, new FindListener<Debate>() {
            @Override
            public void onSuccess(List<Debate> list) {
                /*Message msg = Message.obtain();
                msg.obj = list;
                //handler.sendMessage(msg);*/
                isInitData = true;
                debates= list;
                adapter = new myAdapter();
                mListView.setAdapter(adapter);
                initListener();
                mRefresh.setRefreshing(false);
                mLoading.setVisibility(View.GONE);
                isLoadingMore=false;
                if(mListView.isLoadingMore){
                    mListView.LoadingDone();
                }
            }

            @Override
            public void onError(int i, String s) {
                if(i==9009) return;
                //handler.sendEmptyMessage(1);
                Snackbar.make(container,"加载数据失败，请检查下网络",Snackbar.LENGTH_SHORT).show();
                mRefresh.setRefreshing(false);
                mLoading.setVisibility(View.GONE);
                isLoadingMore=false;
                if(mListView.isLoadingMore){
                    mListView.LoadingDone();
                }
            }
        });


    }
   /* Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("BB", msg.what + "信息-BasePager");
            if(msg.what==1){
                mCurrentPage--;
                Snackbar.make(container,"加载数据失败，请检查下网络",Snackbar.LENGTH_SHORT).show();
            }else if(msg.what==REFRESH_DONE){
                Log.d("BB", "刷新数据-BasePager");
                adapter.notifyDataSetChanged();
                isLoadingMore=false;
            }else if(msg.what==NO_MORE_DATA){
                if(isLoadingMore){
                    //当前页面变为之前一样
                    mCurrentPage--;
                }
                Snackbar.make(container,"没有更多数据咯~",Snackbar.LENGTH_SHORT).show();
            }
            else{

                debates= (ArrayList<Debate>) msg.obj;
                adapter = new myAdapter();
                mListView.setAdapter(adapter);
                initListener();
            }
            mRefresh.setRefreshing(false);
            mLoading.setVisibility(View.GONE);
            isLoadingMore=false;
            if(mListView.isLoadingMore){
                mListView.LoadingDone();
            }
        }
    };*/

    protected void initListener(){
        mListView.setOnRefreshListener(new PullUpListView.OnRefreshListener() {
            @Override
            public void onLoadMore() {
                Log.d("BB","加载更多-BasePager");
                isLoadingMore=true;
                mCurrentPage++;
                refreshData();
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mActivity, DebateActivity.class);
                Debate debate = (Debate) mListView.getAdapter().getItem(position);
                //将该条目的Debate对象传递给显示页面
                intent.putExtra("debate",debate);
                mActivity.startActivity(intent);
            }
        });
        mRefresh.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(){
                    @Override
                    public void run() {
                        SystemClock.sleep(1000);
                        refreshData();
                    }
                }.start();
            }
        });
    }

    protected  void refreshData(){

        query.order(mOrder);
        if(isLoadingMore){
            if(debates.size()<mLimit||mNoMoreData){
                mListView.LoadingDone();
                isLoadingMore=false;
                return;
            }
            query.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
            query.setLimit(mLimit);
            Log.d("BB", "当前页面" + mCurrentPage + "-BasePager");
            query.setSkip(mCurrentPage * mLimit);

        }else{
                isInitData=false;
                isRefreshing=true;
                initData();
                mCurrentPage=0;
                return;
            }
        query.findObjects(mActivity, new FindListener<Debate>() {
            @Override
            public void onSuccess(List<Debate> list) {
                Log.d("BB","共查询到"+list.size()+"条数据-BasePager");
                if(list.size()==0){
                    //handler.sendEmptyMessageDelayed(NO_MORE_DATA, 2000);
                    if(isLoadingMore){
                        //当前页面变为之前一样
                        mCurrentPage--;
                    }
                    Snackbar.make(container,"没有更多数据咯~",Snackbar.LENGTH_SHORT).show();
                    mNoMoreData = true;
                    mRefresh.setRefreshing(false);
                    mLoading.setVisibility(View.GONE);
                    isLoadingMore=false;
                    if(mListView.isLoadingMore){
                        mListView.LoadingDone();
                    }
                    return;
                }
                if(isLoadingMore) {
                    for (int i = 0; i < list.size(); i++) {
                        debates.add(list.get(i));
                    }
                }
                Log.d("BB", "加载成功-BasePager");
                //handler.sendEmptyMessageDelayed(REFRESH_DONE, 1000);
                Log.d("BB", "刷新数据-BasePager");
                adapter.notifyDataSetChanged();
                isLoadingMore=false;
                mRefresh.setRefreshing(false);
                mLoading.setVisibility(View.GONE);
                if(mListView.isLoadingMore){
                    mListView.LoadingDone();
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.d("BB", i + s + "加载失败-BasePager");
                mCurrentPage--;
                Snackbar.make(container,"加载数据失败，请检查下网络",Snackbar.LENGTH_SHORT).show();
                mRefresh.setRefreshing(false);
                mLoading.setVisibility(View.GONE);
                isLoadingMore=false;
                if(mListView.isLoadingMore){
                    mListView.LoadingDone();
                }
                /*Message msg=Message.obtain();
                msg.arg1=i;
                handler.sendEmptyMessageDelayed(1,1000);*/
            }
        });
    };


    class myAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return debates.size();
        }

        @Override
        public Object getItem(int position) {
            return debates.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            Debate debate=debates.get(position);
            if(convertView==null){
                convertView=View.inflate(mActivity,R.layout.item_list_home,null);
                holder=new ViewHolder();
                holder.avatar= (CircularImageView) convertView.findViewById(R.id.iv_avatar_list);
                holder.title= (TextView) convertView.findViewById(R.id.tv_title_list);
                holder.desc= (TextView) convertView.findViewById(R.id.tv_desc_list);
                holder.positive= (TextView) convertView.findViewById(R.id.tv_positive_list);
                holder.negative= (TextView) convertView.findViewById(R.id.tv_negative_list);
                holder.total= (TextView) convertView.findViewById(R.id.tv_total_list);
                convertView.setTag(holder);
            }else{
                holder= (ViewHolder) convertView.getTag();
            }
            x.image().bind(holder.avatar, debate.getAvatar());
            int change=debate.getTotal()/100;
            double total=change;
            if(total>9.0){
                total=total/10.0;
                holder.total.setText("热度 "+total+"K");
            }else{
                holder.total.setText("热度 "+debate.getTotal());
            }
            holder.title.setText(debate.getTitle());

            holder.desc.setText(debate.getDesc());
            if(debate.getDesc()==null){
                holder.desc.setVisibility(View.GONE);
            }
            int positive=0;
            int negative=0;
            if(debate.getTotal()!=0){
                positive=debate.getPositive()*100/debate.getTotal();
                negative=100-positive;
            }

            holder.positive.setText(debate.getPositiveop()+" "+positive+"%");
            holder.negative.setText(debate.getNegativeop()+" "+negative+"%");
            if(TextUtils.isEmpty(debate.getDesc())){
                holder.desc.setVisibility(View.GONE);
            }else{
                holder.desc.setText(debate.getDesc());
            }
            return convertView;
        }
    }

    public class ViewHolder{
        CircularImageView avatar;
        TextView title;
        TextView desc;
        TextView positive;
        TextView negative;
        TextView total;
    }
}
