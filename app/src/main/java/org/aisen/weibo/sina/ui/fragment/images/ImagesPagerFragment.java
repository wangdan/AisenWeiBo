package org.aisen.weibo.sina.ui.fragment.images;

import android.app.Activity;
import android.app.Fragment;
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
 * Created by wangdan on 16/7/19.
 */
public class ImagesPagerFragment extends ATabsTabLayoutFragment<TabItem> {

    public static void launch(Activity from) {
        SinaCommonActivity.launch(from, ImagesPagerFragment.class, null);
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_settings_tabs;
    }

    @Override
    protected void setupTabLayout(Bundle savedInstanceSate) {
        super.setupTabLayout(savedInstanceSate);

        getTablayout().setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        activity.getSupportActionBar().setTitle(R.string.photos_title);
    }

    @Override
    protected ArrayList<TabItem> generateTabs() {
        ArrayList<TabItem> items = new ArrayList<>();

        items.add(new TabItem("1", getString(R.string.photos_pic)));
        items.add(new TabItem("2", getString(R.string.photos_video)));

        return items;
    }

    @Override
    protected Fragment newFragment(TabItem bean) {
        return SavedImagesFragment.newInstance();
    }

    @Override
    protected String configLastPositionKey() {
        return "AisenPhotos";
    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(getActivity(), "微博相册");
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), "微博相册");
    }
}
