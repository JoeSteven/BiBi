package com.joe.bibi.pager;

import android.app.Activity;
import android.util.Log;
import android.widget.ImageView;

import com.joe.bibi.R;

/**
 * Created by Joe on 2016/2/2.
 */
public class LatestPager extends BasePager {

    public LatestPager(Activity mActivity) {
        super(mActivity);
        mOrder="-createdAt";
    }

    @Override
    public void initTitle() {
        mTitle="最新讨论";
    }

}
