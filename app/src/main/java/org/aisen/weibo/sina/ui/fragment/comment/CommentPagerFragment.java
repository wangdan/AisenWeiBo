package org.aisen.weibo.sina.ui.fragment.comment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;

import org.aisen.android.support.bean.TabItem;
import org.aisen.android.ui.fragment.ATabsTabLayoutFragment;
import org.aisen.weibo.sina.R;

import java.util.ArrayList;

/**
 * 首页的评论页面，包含我收到的、我发出的两个页面
 *
 * Created by wangdan on 16/1/24.
 */
public class CommentPagerFragment extends ATabsTabLayoutFragment<TabItem> {

    public static CommentPagerFragment newInstance() {
        CommentPagerFragment fragment = new CommentPagerFragment();

        return fragment;
    }

    @Override
    public int inflateContentView() {
        return org.aisen.android.R.layout.comm_ui_tabs;
    }

    @Override
    public TabLayout getTablayout() {
        return (TabLayout) getActivity().findViewById(R.id.tabLayout);
    }

    @Override
    protected void setupTabLayout(Bundle savedInstanceSate) {
        super.setupTabLayout(savedInstanceSate);

        getTablayout().setTabMode(TabLayout.MODE_FIXED);
    }

    @Override
    protected ArrayList<TabItem> generateTabs() {
        ArrayList<TabItem> items = new ArrayList<>();

        items.add(new TabItem("1", getString(R.string.cmts_to_me)));
        items.add(new TabItem("2", getString(R.string.cmts_by_me)));

        return items;
    }

    @Override
    protected Fragment newFragment(TabItem bean) {
        // 我收到的
        if ("1".equals(bean.getType())) {
            return CommentsFragment.newInstance(CommentsFragment.Type.toMe);
        }
        // 我发出的
        else if ("2".equals(bean.getType())) {
            return CommentsFragment.newInstance(CommentsFragment.Type.byMe);
        }

        return null;
    }

}
