package com.joe.bibi.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.joe.bibi.R;
import com.joe.bibi.application.BBApplication;
import com.joe.bibi.domain.BBUser;
import com.joe.bibi.domain.Comment;
import com.joe.bibi.domain.Debate;
import com.joe.bibi.domain.Follow;
import com.joe.bibi.utils.AnimUtils;
import com.joe.bibi.utils.ConsUtils;
import com.joe.bibi.utils.PrefUtils;
import com.joe.bibi.utils.ToastUtils;
import com.joe.bibi.view.PullUpListView;

import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class DebateActivity extends AppCompatActivity {
    private static final int SHOW_CURRENT_VS = 0;
    private static final int POS_PLUS = 1;
    private static final int NEG_PLUS = 2;
    private boolean isFabOn;
    private FloatingActionButton fabComment;
    private FloatingActionButton fabFollow;
    private Debate mDebate;
    private PullUpListView mListComment;
    private View mHeader;
    private TextView mNickPub;
    private List<Comment> mComments;
    private int mCurrentPage=0;
    private int limit=20;
    private BmobQuery<Comment> comQuery;
    private CommentAdapter mAdapter;
    private Button mPosBut;
    private Button mNegBut;
    private boolean isVoted;
    private BroadcastReceiver receiver;
    private CircularImageView mAvatarPub;
    private TextView mComNum;
    private ProgressBar mPosPro;
    private ProgressBar mNegPro;
    private TextView mPosNumber;
    private TextView mNegNumber;
    private ActionBar ab;
    private boolean mNoMoreComment;
    private boolean isFollowed;
    private Follow mFollow;
    private BmobQuery<Follow> followQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debate);
        mDebate = (Debate) getIntent().getSerializableExtra("debate");
        //获取最新的辩论信息
        mNoMoreComment=false;
        initDebate();
        IntentFilter filter=new IntentFilter("com.joe.bibi.CLICK_LIKED");
        filter.addAction("com.joe.bibi.COMMENT");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("com.joe.bibi.CLICK_LIKED")){
                    initData();
                }else if(intent.getAction().equals("com.joe.bibi.COMMENT")){
                    initData();
                    mComNum.setText((mDebate.getComment()+1)+ "条讨论");
                }

            }
        };
        registerReceiver(receiver, filter);
    }

    private void initDebate() {
        //FAB初始化
        final FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.fab_add_debate);
        fabComment = (FloatingActionButton) findViewById(R.id.fab_comment_debate);
        fabFollow = (FloatingActionButton) findViewById(R.id.fab_report_debate);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFabOn) {
                    fabAdd.startAnimation(AnimUtils.RoAnimSelf(-135f, 0f, 500, true));
                    showFAB(false);
                } else {
                    fabAdd.startAnimation(AnimUtils.RoAnimSelf(0f, -135f, 500, true));
                    showFAB(true);
                }

            }
        });
        BmobQuery<Debate> query=new BmobQuery<Debate>();
        query.addWhereEqualTo("objectId", mDebate.getObjectId());
        query.findObjects(this, new FindListener<Debate>() {
            @Override
            public void onSuccess(List<Debate> list) {
                if (list.size() > 0) {
                    mDebate = list.get(0);
                    initView();
                    initData();
                    initListener();
                }
            }

            @Override
            public void onError(int i, String s) {
                initView();
                initData();
                initListener();
            }
        });
        followQuery = new BmobQuery<Follow>();
        followQuery.addWhereEqualTo("debateId", mDebate.getObjectId());
        followQuery.addWhereEqualTo("followerId", BmobUser.getCurrentUser(this, BBUser.class).getObjectId());
        followQuery.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
        followQuery.findObjects(this, new FindListener<Follow>() {
            @Override
            public void onSuccess(List<Follow> list) {
                if (list.size() > 0) {
                    isFollowed = true;
                    fabFollow.setImageResource(R.drawable.ic_favorite_white_36dp);
                    mFollow = list.get(0);
                } else {
                    isFollowed = false;
                }
            }
            @Override
            public void onError(int i, String s) {
                if (s.equals("object not found for Follow.")) isFollowed = false;
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

    private void initView() {
        mComments = new ArrayList<Comment>();
        isFabOn=false;
        //标题栏初始化
        ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("有" + mDebate.getTotal() + "人参与话题投票");
        mListComment = (PullUpListView) findViewById(R.id.list_comment_debate);
        mHeader = View.inflate(this, R.layout.header_list_debate, null);
        initHeader();
        mListComment.addHeaderView(mHeader);
        mListComment.setHeaderDividersEnabled(false);
    }

    private void initHeader() {
        //拿到Header控件
        TextView mTitle= (TextView) mHeader.findViewById(R.id.tv_title_debate);
        TextView mDesc= (TextView) mHeader.findViewById(R.id.tv_desc_debate);
        TextView mPositive= (TextView) mHeader.findViewById(R.id.tv_positive_debate);
        TextView mNegative= (TextView) mHeader.findViewById(R.id.tv_negative_debate);
        mPosNumber = (TextView) mHeader.findViewById(R.id.tv_positivenum_debate);
        mNegNumber = (TextView) mHeader.findViewById(R.id.tv_negativenum_debate);
        mPosPro = (ProgressBar) mHeader.findViewById(R.id.pro_positive_debate);
        mNegPro = (ProgressBar) mHeader.findViewById(R.id.pro_negative_debate);
        mAvatarPub = (CircularImageView) mHeader.findViewById(R.id.iv_avatar_pub_debate);
        mNickPub = (TextView) mHeader.findViewById(R.id.tv_nick_pub_debate);
        TextView mDate= (TextView) mHeader.findViewById(R.id.tv_date_debate);
        mPosBut = (Button) mHeader.findViewById(R.id.bt_positive_debate);
        mNegBut = (Button) mHeader.findViewById(R.id.bt_negative_debate);
        mComNum = (TextView) mHeader.findViewById(R.id.tv_comnum_debate);
        //处理Header显示
        if(TextUtils.isEmpty(mDebate.getAvatar())){
            mAvatarPub.setImageResource(PrefUtils.getInt(DebateActivity.this, "defaultAvatar", R.drawable.ic_1_default));
        }else{
            x.image().bind(mAvatarPub, mDebate.getAvatar());
        }

        mDate.setText(mDebate.getCreatedAt().substring(0,10));
        mTitle.setText(mDebate.getTitle());
        //如果没有辩题描述就不显示控件
        if(TextUtils.isEmpty(mDebate.getDesc())){
            mDesc.setVisibility(View.GONE);
        }else{
            mDesc.setText(mDebate.getDesc());
        }
        mPositive.setText(mDebate.getPositiveop());
        mNegative.setText(mDebate.getNegativeop());
        mPosBut.setText(mDebate.getPositiveop());
        mNegBut.setText(mDebate.getNegativeop());
        mComNum.setText(mDebate.getComment() + "条讨论");
        setPosVsNeg(SHOW_CURRENT_VS);
    }

    //設置當前的支持比
    private void setPosVsNeg(int type){
        int positive=0;
        int negative=0;
        if(type==SHOW_CURRENT_VS){
            if(mDebate.getTotal()!=0){
                positive=mDebate.getPositive()*100/mDebate.getTotal();
                negative=100-positive;
            }
        }else if(type==POS_PLUS){
            positive=(mDebate.getPositive()+1)*100/(mDebate.getTotal()+1);
            negative=100-positive;

        }else if(type==NEG_PLUS){
            negative=(mDebate.getNegative()+1)*100/(mDebate.getTotal()+1);
            positive=100-negative;
        }
        mPosNumber.setText(positive + "%");
        mNegNumber.setText(negative + "%");
        mPosPro.setProgress(positive);
        mNegPro.setProgress(negative);
    }

    private void initData() {

        BmobQuery<BBUser> query = new BmobQuery<BBUser>();
        query.addWhereEqualTo("username", mDebate.getPublisher());
        query.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.findObjects(this, new FindListener<BBUser>() {
            @Override
            public void onSuccess(List<BBUser> list) {
                if (list.size() > 0) {
                    mNickPub.setText(list.get(0).getNick());

                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
        comQuery = new BmobQuery<Comment>();
        comQuery.addWhereEqualTo("BelongTo", mDebate.getObjectId());
        comQuery.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
        comQuery.setLimit(limit);
        comQuery.order("-Like");
        comQuery.findObjects(this, new FindListener<Comment>() {
            @Override
            public void onSuccess(List<Comment> list) {
                mComments = list;
                initAdapter();
            }

            @Override
            public void onError(int i, String s) {
                ToastUtils.make(DebateActivity.this, "加载评论失败");
                mComments = new ArrayList<Comment>();
                initAdapter();
            }
        });
    }
    //加载更多评论
    private void loadMoreComment() {
        comQuery.setSkip(mCurrentPage*limit);
        comQuery.findObjects(this, new FindListener<Comment>() {
            @Override
            public void onSuccess(List<Comment> list) {
                if(list.size()>0){
                    for (Comment com:list
                            ) {
                        mComments.add(com);
                    }
                    Log.d("BB", "加载成功" + list.size() + "-Debate");
                    mAdapter.notifyDataSetChanged();
                }else{
                    mNoMoreComment = true;
                }
                mListComment.LoadingDone();

            }

            @Override
            public void onError(int i, String s) {
                ToastUtils.make(DebateActivity.this,"没有更多评论咯");
                Log.d("BB","加载失败"+s+"Debate");
                mCurrentPage--;
                mListComment.LoadingDone();
            }
        });
    }
    private void initAdapter() {
        mAdapter = new CommentAdapter();
        if(mComments.size()==0) mListComment.setDivider(null);
        mListComment.setAdapter(mAdapter);
        mListComment.setOnRefreshListener(new PullUpListView.OnRefreshListener() {
            @Override
            public void onLoadMore() {
                //如果集合数目小于limit说明总评论数加载完毕，不需要分页加载
                if (mComments.size() < limit||mNoMoreComment) {
                    mListComment.LoadingDone();
                    return;
                }
                mCurrentPage++;
                Log.d("BB","刷新-Debate");
                loadMoreComment();
            }
        });
    }


    class CommentAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            if(mComments.size()==0) return 1;
            return mComments.size();
        }

        @Override
        public Object getItem(int position) {
            return mComments.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //如果没有评论 展示空视图
            if(mComments.size()==0) return View.inflate(DebateActivity.this,R.layout.item_null_comment_debate,null);
            ViewHolder holder=null;
            Comment comment=mComments.get(position);
            if(convertView==null){
                convertView=View.inflate(DebateActivity.this,R.layout.item_comment_debate,null);
                holder=new ViewHolder();
                holder.avatar= (CircularImageView) convertView.findViewById(R.id.iv_avatar_comment);
                holder.nick= (TextView) convertView.findViewById(R.id.tv_nick_comment);
                holder.like= (TextView) convertView.findViewById(R.id.tv_like_comment);
                holder.content= (TextView) convertView.findViewById(R.id.tv_content_comment);
                convertView.setTag(holder);
            }else{
                holder= (ViewHolder) convertView.getTag();
            }
            //头像显示
            if(TextUtils.isEmpty(comment.getAvatar())){
                holder.avatar.setImageResource(PrefUtils.getInt(DebateActivity.this, "defaultAvatar", R.drawable.ic_1_default));
            }else{
                x.image().bind(holder.avatar, comment.getAvatar());
            }
            holder.nick.setText(comment.getNick());
            //内容是否是回复
            String content=comment.getContent();
            if(content.startsWith("@")) {
                int i = content.indexOf(" ");
                SpannableStringBuilder style=new SpannableStringBuilder(content);
                style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.MainBlue)),0,i, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                holder.content.setText(style);
            }else{
                holder.content.setText(content);
            }
            int change=comment.getLike()/100;
            double total=change;
            if(total>9.0){
                total=total/10.0;
                holder.like.setText(total+"K");
            }else{
                holder.like.setText(comment.getLike()+"");
            }
            //设置支持的立场

            switch (comment.getPoint()){
                case Comment.POSITIVE_COMMENT:
                    //holder.like.setBackground(getDrawable(R.drawable.shape_text_red));
                    holder.like.setBackgroundResource(R.drawable.shape_text_red);
                    break;
                case Comment.NEUTRAL_COMMENT:
                    //holder.like.setBackground(getDrawable(R.drawable.shape_text_gray));
                    holder.like.setBackgroundResource(R.drawable.shape_text_gray);
                    break;
                case Comment.NEGATIVE_COMMENT:
                   // holder.like.setBackground(getDrawable(R.drawable.shape_text_blue));
                    holder.like.setBackgroundResource(R.drawable.shape_text_blue);
                    break;

            }
            return convertView;
        }
    }

    class ViewHolder{
        CircularImageView avatar;
        TextView nick;
        TextView like;
        TextView content;
    }

    private void initListener() {
        //进入出题人的主页
        mAvatarPub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DebateActivity.this,UserActivity.class);
                intent.putExtra("username",mDebate.getPublisher());
                startActivity(intent);
            }
        });
        isVoted= PrefUtils.getBoolean(this,mDebate.getObjectId(),false);
        //发表评论
        fabComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DebateActivity.this,CommentActivity.class);
                intent.putExtra("debateID",mDebate.getObjectId());
                intent.putExtra("title",mDebate.getTitle());
                intent.putExtra("username",mDebate.getPublisher());
                startActivity(intent);
            }
        });
        //关注辩题
        fabFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabFollow.setClickable(false);
                if(mFollow==null){
                    mFollow=new Follow();
                    mFollow.setDebateId(mDebate.getObjectId());
                    mFollow.setFollowerId(BmobUser.getCurrentUser(DebateActivity.this, BBUser.class).getObjectId());

                }
                if(isFollowed){
                    mFollow.delete(DebateActivity.this,mFollow.getObjectId(),new DeleteListener() {
                        @Override
                        public void onSuccess() {
                            isFollowed=false;
                            fabFollow.setImageResource(R.drawable.ic_favorite_border_white_36dp);
                            ToastUtils.make(DebateActivity.this, "取消关注");
                            fabFollow.setClickable(true);
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            ToastUtils.make(DebateActivity.this, "取消关注失败，请重试");
                            fabFollow.setClickable(true);
                        }
                    });
                }else{
                    Follow newFollow=new Follow();
                    newFollow.setDebateId(mDebate.getObjectId());
                    newFollow.setFollowerId(BmobUser.getCurrentUser(DebateActivity.this, BBUser.class).getObjectId());
                    newFollow.setFollowerInstallationId(BmobUser.getCurrentUser(DebateActivity.this, BBUser.class).getInstallId());
                    newFollow.setFollowerUserName(BmobUser.getCurrentUser(DebateActivity.this, BBUser.class).getUsername());
                    newFollow.save(DebateActivity.this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            fabFollow.setClickable(true);
                            isFollowed=true;
                            ToastUtils.make(DebateActivity.this,"关注成功");
                            fabFollow.setImageResource(R.drawable.ic_favorite_white_36dp);
                            followQuery.findObjects(DebateActivity.this, new FindListener<Follow>() {
                                @Override
                                public void onSuccess(List<Follow> list) {
                                    if(list.size()>0){
                                        mFollow.setObjectId(list.get(0).getObjectId());
                                    }
                                }

                                @Override
                                public void onError(int i, String s) {

                                }
                            });
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            ToastUtils.make(DebateActivity.this, "关注失败，请重试");
                            fabFollow.setClickable(true);
                        }
                    });
                }
            }
        });

        //投票监听
        mPosBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isVoted){
                    ToastUtils.make(DebateActivity.this,"您已经投过票啦");
                    return;
                }
                Debate update=new Debate();
                update.increment("positive");
                update.increment("total");
                update.update(DebateActivity.this, mDebate.getObjectId(), new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        //投票后记录下来，不能重复投票
                        ToastUtils.make(DebateActivity.this,"投票成功！");
                        PrefUtils.putBoolean(DebateActivity.this, mDebate.getObjectId(), true);
                        isVoted=true;
                        setPosVsNeg(POS_PLUS);
                        ab.setTitle("有" + (mDebate.getTotal()+1)+ "人参与话题讨论");
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        ToastUtils.make(DebateActivity.this,"投票失败，请重试");
                    }
                });

            }
        });
        mNegBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVoted) {
                    ToastUtils.make(DebateActivity.this, "您已经投过票啦");
                    return;
                }
                Debate update = new Debate();
                update.increment("negative");
                update.increment("total");
                update.update(DebateActivity.this, mDebate.getObjectId(), new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        //投票后记录下来，不能重复投票
                        ToastUtils.make(DebateActivity.this, "投票成功！");
                        PrefUtils.putBoolean(DebateActivity.this, mDebate.getObjectId(), true);
                        isVoted = true;
                        setPosVsNeg(NEG_PLUS);
                        ab.setTitle("有" + (mDebate.getTotal() + 1) + "人参与话题讨论");
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        ToastUtils.make(DebateActivity.this, "投票失败，请重试");
                    }
                });
            }
        });
        mListComment.setItemsCanFocus(true);
        mListComment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DebateActivity.this, CommentDetailActivity.class);
                intent.putExtra("title", mDebate.getTitle());
                intent.putExtra("comment", mComments.get(position - 1));
                startActivity(intent);
            }
        });
    }


    private void showFAB(boolean turnOn) {
        if(turnOn){
            fabComment.setVisibility(View.VISIBLE);
            fabFollow.setVisibility(View.VISIBLE);
            ObjectAnimator.ofFloat(fabComment,"translationY",0f,-400f).setDuration(500).start();
            ObjectAnimator.ofFloat(fabFollow,"translationY",0f,-200f).setDuration(500).start();
            isFabOn=true;
        }else{
            Log.d("BB", fabFollow.getHeight() + "-DebateActivity");
            ObjectAnimator oa= ObjectAnimator.ofFloat(fabComment,"translationY",-400f,0f);
            oa.setDuration(500).start();
            ObjectAnimator.ofFloat(fabFollow,"translationY",-200f,0f).setDuration(500).start();
            oa.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    fabFollow.setVisibility(View.GONE);
                    fabComment.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            isFabOn=false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
