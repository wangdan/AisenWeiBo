package org.aisen.weibo.sina.ui.fragment.hot;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;

import org.aisen.android.support.bean.TabItem;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ATabsTabLayoutFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;

import java.util.ArrayList;

/**
 * Created by wangdan on 16/8/14.
 */
public class TopicsHotPagerFragment extends ATabsTabLayoutFragment<TabItem> {

    public static void launch(Activity from) {
        SinaCommonActivity.launch(from, TopicsHotPagerFragment.class, null);
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_settings_tabs;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceState) {
        super.layoutInit(inflater, savedInstanceState);

        getContentView().setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void setupTabLayout(Bundle savedInstanceSate) {
        super.setupTabLayout(savedInstanceSate);

        getTablayout().setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        activity.getSupportActionBar().setTitle("热门话题");
    }

    @Override
    protected ArrayList<TabItem> generateTabs() {
        ArrayList<TabItem> items = new ArrayList<>();

        items.add(new TabItem("100803", "推荐"));
        items.add(new TabItem("100803_-_page_hot_list", "榜单"));
        items.add(new TabItem("100803_ctg1_140_-_page_topics_ctg1__140", "笑话"));
        items.add(new TabItem("100803_ctg1_100_-_page_topics_ctg1__100", "电影"));
        items.add(new TabItem("100803_ctg1_98_-_page_topics_ctg1__98", "体育"));
        items.add(new TabItem("100803_ctg1_133_-_page_topics_ctg1__133", "校园"));
        items.add(new TabItem("100803_ctg1_128_-_page_topics_ctg1__128", "萌宠"));
        items.add(new TabItem("100803_ctg1_93_-_page_topics_ctg1__93", "旅游"));
        items.add(new TabItem("100803_ctg1_144_-_page_topics_ctg1__144", "军事"));

        return items;
    }

    @Override
    protected Fragment newFragment(TabItem bean) {
        return TopicsHotFragment.newInstance(bean);
    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(getActivity(), "热门话题页");
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), "热门话题页");
    }

}
