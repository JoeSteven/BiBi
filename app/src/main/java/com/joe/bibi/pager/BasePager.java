package com.joe.bibi.pager;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.joe.bibi.R;
import com.joe.bibi.activity.DebateActivity;
import com.joe.bibi.domain.Debate;
import com.joe.bibi.utils.NetUtils;
import com.joe.bibi.utils.ToastUtils;

import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Joe on 2016/2/2.
 */
public abstract class BasePager {
    public View mRootView;
    public String mTitle;
    public String mOrder;
    public Activity mActivity;
    public ListView mListView;
    private ArrayList<Debate> debates;
    private PullRefreshLayout mRefresh;
    private ProgressBar mLoading;

    public BasePager(Activity mActivity) {
        super();
        this.mActivity = mActivity;
        initTitle();
        initView();
    }

    public abstract void initTitle();

    public void initView() {
        mRootView=View.inflate(mActivity, R.layout.pager_base,null);
        mListView = (ListView) mRootView.findViewById(R.id.list_viewpager_home);
        mRefresh = (PullRefreshLayout)mRootView.findViewById(R.id.swipeRefreshLayout);
        mRefresh.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        mLoading = (ProgressBar) mRootView.findViewById(R.id.loading_pager);
    }

    public void initData(){
        BmobQuery<Debate> query = new BmobQuery<Debate>();
        query.setCachePolicy(BmobQuery.CachePolicy.CACHE_THEN_NETWORK);
        final ArrayList<Debate> list = new ArrayList<Debate>();
        query.order(mOrder);

        query.findObjects(mActivity, new FindListener<Debate>() {
            @Override
            public void onSuccess(List<Debate> list) {
                Message msg = Message.obtain();
                msg.obj = list;
                handler.sendMessage(msg);
            }

            @Override
            public void onError(int i, String s) {
                handler.sendEmptyMessage(1);

            }
        });


    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==1){
                CoordinatorLayout container= (CoordinatorLayout) mActivity.findViewById(R.id.coorl_home);
                Snackbar.make(container,"加载数据失败，请检查下网络",Snackbar.LENGTH_SHORT).show();
            }else{
                debates= (ArrayList<Debate>) msg.obj;
                myAdapter adapter=new myAdapter();
                mListView.setAdapter(adapter);
                initListener();
            }
            mRefresh.setRefreshing(false);
            mLoading.setVisibility(View.GONE);
        }
    };

    protected void initListener(){
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mActivity, DebateActivity.class);
                Debate debate = (Debate) mListView.getAdapter().getItem(position);
                intent.putExtra("debateId", debate.getObjectId());
                mActivity.startActivity(intent);
            }
        });
        mRefresh.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(){
                    @Override
                    public void run() {
                        SystemClock.sleep(2000);
                        refreshData();
                    }
                }.start();
            }
        });
    }

    protected  void refreshData(){
        initData();
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
            int positive=debate.getPositive()*100/debate.getTotal();
            holder.positive.setText(debate.getPositiveop()+""+positive+"%");
            holder.negative.setText(debate.getNegativeop()+""+(100-positive)+"%");
            holder.desc.setText(debate.getDesc());
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
