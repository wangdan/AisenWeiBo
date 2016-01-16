package org.aisen.weibo.sina.ui.fragment.profile;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.aisen.android.support.bean.TabItem;
import org.aisen.android.support.inject.InjectUtility;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.android.ui.fragment.ATabsTabLayoutFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineDefFragment;

import java.util.ArrayList;

/**
 * Created by wangdan on 16/1/12.
 */
public class ProfilePagerFragment extends ATabsTabLayoutFragment<TabItem> {

    public static void launch(Activity from, WeiBoUser user) {
        FragmentArgs args = new FragmentArgs();
        args.add("mUser", user);

        SinaCommonActivity.launch(from, ProfilePagerFragment.class, args);
    }

    private WeiBoUser mUser;

    @Override
    protected int inflateContentView() {
        return -1;
    }

    public int setActivityContentView() {
        return R.layout.ui_profile_pager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);

        InjectUtility.initInjectedView(this, ((BaseActivity) getActivity()).getRootView());
        layoutInit(inflater, savedInstanceState);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = savedInstanceState == null ? (WeiBoUser) getArguments().getSerializable("mUser")
                                           : (WeiBoUser) savedInstanceState.getSerializable("mUser");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("mUser", mUser);
    }

    @Override
    protected ArrayList<TabItem> generateTabs() {
        ArrayList<TabItem> tabItems = new ArrayList<>();

        tabItems.add(new TabItem("1", "关于"));
        tabItems.add(new TabItem("2", "微博"));

        return tabItems;
    }

    @Override
    protected Fragment newFragment(TabItem bean) {
        int type = Integer.parseInt(bean.getType());

        if (type == 1) {
            return Profile01Fragment.newInstance();
        }
        else if (type == 2) {
            return TimelineDefFragment.newInstance("statusesFriendsTimeLine");
        }

        return null;
    }

}
