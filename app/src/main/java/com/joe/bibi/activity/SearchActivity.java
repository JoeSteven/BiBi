package com.joe.bibi.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.joe.bibi.R;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.Comment;
import com.joe.bibi.domain.Debate;
import com.joe.bibi.utils.ToastUtils;
import com.joe.bibi.view.PullUpListView;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

public class SearchActivity extends AppCompatActivity {

    private SearchView mSearch;
    private TextView mDebateButton;
    private TextView mUserButton;
    private boolean isQueryDebate=true;//标记当前是查找辩题还是用户
    private int limit=20;
    private List<Debate> mDebates;
    private List<BBUser> mUsers;
    private DebateAdapter debateAdapter;
    private PullUpListView mListView;

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
        mListView = (PullUpListView) findViewById(R.id.lv_search);
        mDebates=new ArrayList<Debate>();
        mUsers=new ArrayList<BBUser>();
        initAdapter();
        initListener();
    }
    private void initListener() {
        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length()>1){
                    search(newText);
                }
                return false;
            }
        });
    }
    //查找
    private void search(String query) {
        if(isQueryDebate){
            mDebates.clear();
            BmobQuery<Debate> debateBmobQuery=new BmobQuery<Debate>();
            debateBmobQuery.addWhereContains("title",query);
            debateBmobQuery.setLimit(limit);
            debateBmobQuery.order("-comment,total");
            debateBmobQuery.findObjects(this, new FindListener<Debate>() {
                @Override
                public void onSuccess(List<Debate> list) {
                    if(list.size()>0){
                        mDebates=list;
                    }else{
                        ToastUtils.make(SearchActivity.this, "没有查询到相关辩题");
                    }
                    debateAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(int i, String s) {
                    ToastUtils.make(SearchActivity.this,"没有查询到相关辩题");
                    debateAdapter.notifyDataSetChanged();
                }
            });
        }else{
            mUsers.clear();
            BmobQuery<BBUser> bbUserBmobQuery=new BmobQuery<BBUser>();
            bbUserBmobQuery.addWhereContains("nick", query);
            bbUserBmobQuery.setLimit(limit);
            bbUserBmobQuery.findObjects(this, new FindListener<BBUser>() {
                @Override
                public void onSuccess(List<BBUser> list) {

                    if (list.size() > 0) {
                        mUsers = list;
                    } else {
                        ToastUtils.make(SearchActivity.this, "没有查询到相关用户");
                    }
                }

                @Override
                public void onError(int i, String s) {
                    ToastUtils.make(SearchActivity.this, "没有查询到相关用户");
                }
            });
        }
    }

    private void initAdapter() {
        debateAdapter = new DebateAdapter();
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
    }
    class UserAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    }
    public void showDebate(View view){
        mDebateButton.setTextColor(getResources().getColor(R.color.MainBlue));
        mUserButton.setTextColor(getResources().getColor(R.color.LightBlack));
        isQueryDebate=true;
        search(mSearch.getQuery().toString());
    }
    public void showUser(View view){
        mDebateButton.setTextColor(getResources().getColor(R.color.LightBlack));
        mUserButton.setTextColor(getResources().getColor(R.color.MainBlue));
        isQueryDebate=false;
        search(mSearch.getQuery().toString());
    }
}
