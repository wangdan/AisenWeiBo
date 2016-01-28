package org.aisen.android.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;

import org.aisen.android.R;
import org.aisen.android.support.bean.TabItem;
import org.aisen.android.support.inject.ViewInject;

/**
 * 对TabLayout的封装
 *
 * Created by wangdan on 15/12/22.
 */
public abstract class ATabsTabLayoutFragment<T extends TabItem> extends ATabsFragment<T> {

    @ViewInject(idStr = "tabLayout")
    TabLayout mTabLayout;

    @Override
    public int inflateContentView() {
        return R.layout.comm_ui_tabs_tablayout;
    }

    @Override
    final protected void setupViewPager(Bundle savedInstanceSate) {
        setupTabLayout(savedInstanceSate, mTabLayout);
    }

    protected void setupTabLayout(Bundle savedInstanceSate, final TabLayout tabLayout) {
        super.setupViewPager(savedInstanceSate);

        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setTabTextColors(Color.parseColor("#b3ffffff"), Color.WHITE);
        tabLayout.setupWithViewPager(getViewPager());
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                tabLayout.setScrollPosition(mCurrentPosition, 0, true);
            }

        }, 150);
    }

    public TabLayout getTablayout() {
        return mTabLayout;
    }

    protected void setTabLayout(TabLayout tabLayout) {
        mTabLayout = tabLayout;
    }

}
