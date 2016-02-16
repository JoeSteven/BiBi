package com.joe.bibi.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.joe.bibi.R;
import com.joe.bibi.pager.BasePager;
import com.joe.bibi.pager.FollowPager;
import com.joe.bibi.pager.HotestPager;
import com.joe.bibi.pager.LatestPager;
import com.joe.bibi.pager.MinePager;

import java.util.ArrayList;

/**
 * Created by Joe on 2016/2/2.
 */
public class HomeFragment extends Fragment {
    private Activity mActivity;
    private ArrayList<BasePager> mPagerList;
    private ViewPager viewPager;
    private PagerSlidingTabStrip tab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivity=getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return initView();
    }

    private View initView() {
        View v=View.inflate(mActivity, R.layout.fragment_home,null);
        viewPager = (ViewPager) v.findViewById(R.id.viewpager_fragment_home);
        tab = (PagerSlidingTabStrip) v.findViewById(R.id.tab_fagment_home);
        return v;
    }

    private void initData() {
        mPagerList=new ArrayList<BasePager>();
        mPagerList.add(new LatestPager(mActivity));
        mPagerList.add(new HotestPager(mActivity));
        mPagerList.add(new FollowPager(mActivity));
        mPagerList.add(new MinePager(mActivity));

        initAdapter();
        initListener();
        tab.setViewPager(viewPager);
    }

    private void initListener() {
        tab.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPagerList.get(position).initData();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mPagerList.get(1).initData();
    }

    private void initAdapter() {
        MyPagerAdapter adapter=new MyPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
    }

    class MyPagerAdapter extends PagerAdapter{
        @Override
        public CharSequence getPageTitle(int position) {
            return mPagerList.get(position).mTitle;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v =mPagerList.get(position).mRootView;
            container.addView(v);

            return v;
        }

        @Override
        public int getCount() {
            return mPagerList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
