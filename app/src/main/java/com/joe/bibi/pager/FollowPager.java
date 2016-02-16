package com.joe.bibi.pager;

import android.app.Activity;

/**
 * Created by Joe on 2016/2/2.
 */
public class FollowPager extends BasePager {
    public FollowPager(Activity mActivity) {
        super(mActivity);
        mOrder="-createdAt";
    }
    @Override
    public void initTitle() {
        mTitle="关注辩题";
    }
}
