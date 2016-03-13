package com.joe.bibi.pager;

import android.app.Activity;

/**
 * Created by Joe on 2016/2/2.
 */
public class MinePager extends BasePager {
    public MinePager(Activity mActivity) {
        super(mActivity);
        mOrder="-comment,-total";
    }
    @Override
    public void initTitle() {
        mTitle="我的辩题";
    }
}
