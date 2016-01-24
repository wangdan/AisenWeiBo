package org.aisen.weibo.sina.ui.fragment.mention;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.aisen.android.support.bean.TabItem;
import org.aisen.android.ui.fragment.ATabsTabLayoutFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;

import java.util.ArrayList;

/**
 * Created by wangdan on 16/1/24.
 */
public class MentionPagerFragment extends ATabsTabLayoutFragment<TabItem> {

    public static MentionPagerFragment newInstance() {
        MentionPagerFragment fragment = new MentionPagerFragment();

        // 如果没有提及微博，就显示提及评论
        if (AppContext.getAccount().getUnreadCount() != null &&
                AppContext.getAccount().getUnreadCount().getMention_status() == 0 &&
                AppContext.getAccount().getUnreadCount().getMention_cmt() > 0) {
            Bundle args = new Bundle();
            args.putInt(SET_INDEX, 1);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public int inflateContentView() {
        return org.aisen.android.R.layout.comm_ui_tabs;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setTabLayout((TabLayout) getActivity().findViewById(R.id.tabLayout));

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void setupTabLayout(Bundle savedInstanceSate, TabLayout tabLayout) {
        super.setupTabLayout(savedInstanceSate, tabLayout);

        tabLayout.setTabMode(TabLayout.MODE_FIXED);
    }

    @Override
    protected ArrayList<TabItem> generateTabs() {
        ArrayList<TabItem> items = new ArrayList<>();

        items.add(new TabItem("1", getString(R.string.mention_status)));
        items.add(new TabItem("2", getString(R.string.mention_cmt)));

        return items;
    }

    @Override
    protected Fragment newFragment(TabItem bean) {
        // 提及的微博
        if ("1".equals(bean.getType())) {
            return MentionTimelineFragment.newInstance();
        }
        // 提及的评论
        else if ("2".equals(bean.getType())) {
            return MentionCmtsFragment.newInstance();
        }

        return null;
    }

}
