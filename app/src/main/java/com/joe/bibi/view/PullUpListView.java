package com.joe.bibi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.joe.bibi.R;

/**
 * Created by Joe on 2016/2/16.
 */
public class PullUpListView extends ListView implements AbsListView.OnScrollListener{

    private View mFooter;
    private int mFooterViewHeight;

    public PullUpListView(Context context) {
        super(context);
        initFootView();
    }

    public PullUpListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFootView();
    }

    public PullUpListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFootView();
    }

    private void initFootView() {
        mFooter = View.inflate(getContext(), R.layout.footer_more_listview, null);
        mFooter.measure(0, 0);
        mFooterViewHeight = mFooter.getMeasuredHeight();

        mFooter.setPadding(0, -mFooterViewHeight, 0, 0);// 隐藏
        this.addFooterView(mFooter);
        this.setOnScrollListener(this);
    }
    public boolean isLoadingMore;
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE
                || scrollState == SCROLL_STATE_FLING) {
            if (getLastVisiblePosition() == getCount() - 1 && !isLoadingMore) {
                mFooter.setPadding(0,0,0,0);
                setSelection(getCount() - 1);// 改变listview显示位置

                isLoadingMore = true;
                if (mListener != null) {
                    mListener.onLoadMore();
                }
            }

        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    OnRefreshListener mListener;
    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    public interface OnRefreshListener {
        public void onLoadMore();// 加载下一页数据
    }

    public void LoadingDone(){
        if (isLoadingMore) {// 正在加载更多...
            mFooter.setPadding(0, -mFooterViewHeight, 0, 0);// 隐藏脚布局
            isLoadingMore = false;
        }
    }
}
