package com.joe.bibi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.joe.bibi.R;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.Debate;
import com.joe.bibi.utils.CommonUtils;
import com.joe.bibi.view.PullUpListView;

import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

public class SearchActivity extends AppCompatActivity {

    private SearchView mSearch;
    private TextView mDebateButton;
    private TextView mUserButton;
    private boolean isQueryDebate=true;//标记当前是查找辩题还是用户
    private int limit=10;
    private int mCurrentPage=0;
    private List<Debate> mDebates;
    private List<BBUser> mUsers;
    private DebateAdapter debateAdapter;
    private PullUpListView mListView;
    private TextView mNotFound;
    private UserAdapter userAdapter;
    private boolean noMoreData=false;//记录加载更多还有没有数据
    private ProgressBar mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initView();

    }

    private void initView() {
        mSearch = (SearchView) findViewById(R.id.search_home);
        mDebateButton = (TextView) findViewById(R.id.tv_debate_search);
        mUserButton = (TextView) findViewById(R.id.tv_user_search);
        mNotFound = (TextView) findViewById(R.id.tv_not_found_search);
        mListView = (PullUpListView) findViewById(R.id.lv_search);
        mLoading = (ProgressBar) findViewById(R.id.loading_search);
        mDebates=new ArrayList<Debate>();
        mUsers=new ArrayList<BBUser>();
        initAdapter();
        initListener();
    }
    private void initListener() {
        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mCurrentPage=0;
                noMoreData=false;
                search(query, false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 1) {
                    mCurrentPage=0;
                    noMoreData=false;
                    search(newText, false);
                }
                return false;
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                CommonUtils.hideKeyBoard(SearchActivity.this);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //判断当前查询的是辩题还是用户
                if (isQueryDebate) {
                    Debate debate = (Debate) mListView.getAdapter().getItem(position);
                    Intent intent = new Intent(SearchActivity.this, DebateActivity.class);
                    intent.putExtra("debate", debate);
                    startActivity(intent);
                } else {
                    BBUser user = (BBUser) mListView.getAdapter().getItem(position);
                    Intent intent = new Intent(SearchActivity.this, UserActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                }
            }
        });
        mListView.setOnRefreshListener(new PullUpListView.OnRefreshListener() {
            @Override
            public void onLoadMore() {
                if(isQueryDebate){
                    if(mDebates.size()<limit||noMoreData) {
                        mListView.LoadingDone();
                        return;
                    }
                }else{
                    if(mUsers.size()<limit||noMoreData){
                        mListView.LoadingDone();
                        return;
                    }
                }
                mCurrentPage++;
                search(mSearch.getQuery().toString(),true);
            }
        });
    }
    //查找
    private void search(String query, final boolean isLoadingMore) {
        if(isQueryDebate){
            BmobQuery<Debate> debateBmobQuery=new BmobQuery<Debate>();
            debateBmobQuery.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
            debateBmobQuery.addWhereContains("title", query);
            debateBmobQuery.setLimit(limit);
            //如果是加载更多就设置跳过
            if(isLoadingMore){
                debateBmobQuery.setSkip(mCurrentPage*limit);
            }else{
                mDebates.clear();
                showNotFoundView(false);
                mLoading.setVisibility(View.VISIBLE);
            }
            debateBmobQuery.order("-comment,total");
            debateBmobQuery.findObjects(this, new FindListener<Debate>() {
                @Override
                public void onSuccess(List<Debate> list) {
                    mLoading.setVisibility(View.GONE);
                    if(list.size()>0){
                        if(isLoadingMore){
                            mDebates.addAll(list);
                        }else{
                            mDebates=list;
                        }
                        showNotFoundView(false);
                    }else{
                        if(!isLoadingMore){
                            showNotFoundView(true);
                        }else{
                            //记录一下没有更多数据，不需要再刷新了
                            noMoreData=true;
                        }
                    }
                    debateAdapter.notifyDataSetChanged();
                    mListView.LoadingDone();
                }

                @Override
                public void onError(int i, String s) {
                    mLoading.setVisibility(View.GONE);
                    showNotFoundView(true);
                    debateAdapter.notifyDataSetChanged();
                }
            });
        }else{

            BmobQuery<BBUser> bbUserBmobQuery=new BmobQuery<BBUser>();
            bbUserBmobQuery.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
            if(isLoadingMore) {
                bbUserBmobQuery.setSkip(mCurrentPage*limit);
            }else{
                mUsers.clear();
                mLoading.setVisibility(View.VISIBLE);
                showNotFoundView(false);
            }
            bbUserBmobQuery.addWhereContains("nick", query);
            bbUserBmobQuery.setLimit(limit);
            bbUserBmobQuery.findObjects(this, new FindListener<BBUser>() {
                @Override
                public void onSuccess(List<BBUser> list) {
                    mLoading.setVisibility(View.GONE);
                    if (list.size() > 0) {
                        if (isLoadingMore) {
                            mUsers.addAll(list);
                        } else {
                            mUsers = list;
                        }
                        showNotFoundView(false);
                    } else {
                        if (!isLoadingMore) {
                            showNotFoundView(true);
                        }else{
                            //记录一下没有更多数据，不需要再刷新了
                            noMoreData=true;
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                    mListView.LoadingDone();
                }

                @Override
                public void onError(int i, String s) {
                    mLoading.setVisibility(View.GONE);
                    showNotFoundView(true);
                    userAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void showNotFoundView(boolean b) {
        if(isQueryDebate){
            mNotFound.setText("抱歉，没有找到相关辩题");
        }else{
            mNotFound.setText("抱歉，没有找到相关用户");
        }
        if(b){
            mNotFound.setVisibility(View.VISIBLE);
        }else{
            mNotFound.setVisibility(View.GONE);
        }
    }

    private void initAdapter() {
        debateAdapter = new DebateAdapter();
        userAdapter = new UserAdapter();
        mListView.setAdapter(debateAdapter);
    }
    class DebateAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mDebates.size();
        }

        @Override
        public Object getItem(int position) {
            return mDebates.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;
            Debate debate=mDebates.get(position);
            if(convertView==null){
                holder=new ViewHolder();
                convertView=View.inflate(SearchActivity.this,R.layout.item_debate_search,null);
                holder.title= (TextView) convertView.findViewById(R.id.tv_title_item_search);
                holder.comment= (TextView) convertView.findViewById(R.id.tv_comment_item_search);
                holder.descDebate= (TextView) convertView.findViewById(R.id.tv_desc_item_search);
                holder.vote= (TextView) convertView.findViewById(R.id.tv_vote_item_search);
                convertView.setTag(holder);
            }else{
                holder= (ViewHolder) convertView.getTag();
            }
            holder.title.setText(debate.getTitle());
            if(TextUtils.isEmpty(debate.getDesc())){
                holder.descDebate.setVisibility(View.GONE);
            }else{
                holder.descDebate.setText(debate.getDesc());
            }
            holder.comment.setText(debate.getComment()+" 评论");
            holder.vote.setText(debate.getTotal()+" 投票");
            return convertView;
        }
    }
    class ViewHolder{
        TextView title;
        TextView descDebate;
        TextView comment;
        TextView vote;
        TextView nick;
        TextView descUser;
        CircularImageView avatar;
    }
    class UserAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mUsers.size();
        }

        @Override
        public Object getItem(int position) {
            return mUsers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;
            BBUser user=mUsers.get(position);
            if(convertView==null){
                holder=new ViewHolder();
                convertView=View.inflate(SearchActivity.this,R.layout.item_user_search,null);
                holder.nick= (TextView) convertView.findViewById(R.id.tv_nick_search);
                holder.descUser= (TextView) convertView.findViewById(R.id.tv_desc_user_search);
                holder.avatar= (CircularImageView) convertView.findViewById(R.id.iv_avatar_search);
                convertView.setTag(holder);
            }else{
                holder= (ViewHolder) convertView.getTag();
            }
            x.image().bind(holder.avatar,user.getAvatarUrl());
            holder.nick.setText(user.getNick());
            if(TextUtils.isEmpty(user.getDesc())){
                holder.descUser.setText("ta很懒，什么都没留下");
            }else {
                holder.descUser.setText(user.getDesc());
            }
            return convertView;
        }
    }
    public void showDebate(View view){
        mDebateButton.setTextColor(getResources().getColor(R.color.MainBlue));
        mUserButton.setTextColor(getResources().getColor(R.color.LightBlack));
        isQueryDebate=true;
        mListView.setAdapter(debateAdapter);
        if(TextUtils.isEmpty(mSearch.getQuery().toString())) return;
        search(mSearch.getQuery().toString(), false);
    }
    public void showUser(View view){
        mDebateButton.setTextColor(getResources().getColor(R.color.LightBlack));
        mUserButton.setTextColor(getResources().getColor(R.color.MainBlue));
        isQueryDebate=false;
        mListView.setAdapter(userAdapter);
        if(TextUtils.isEmpty(mSearch.getQuery().toString())) return;
        search(mSearch.getQuery().toString(), false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDebates.clear();
        mUsers.clear();
    }
}
