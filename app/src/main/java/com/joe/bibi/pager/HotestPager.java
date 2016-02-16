package com.joe.bibi.pager;

import android.app.Activity;
import android.util.Log;

/**
 * Created by Joe on 2016/2/2.
 */
public class HotestPager extends BasePager {
    public HotestPager(Activity mActivity) {
        super(mActivity);
        mOrder="-total";
    }
    @Override
    public void initTitle() {
        mTitle="热门讨论";
    }
}
