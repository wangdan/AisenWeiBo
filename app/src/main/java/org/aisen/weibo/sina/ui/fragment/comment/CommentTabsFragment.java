package org.aisen.weibo.sina.ui.fragment.comment;

import android.app.Fragment;

import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.AStripTabsFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.ui.fragment.basic.AMainStripTabsFragment;

import java.util.ArrayList;

/**
 * 评论标签页
 *
 * Created by wangdan on 15/4/23.
 */
public class CommentTabsFragment extends AMainStripTabsFragment {

    public static ABaseFragment newInstance() {
        return new CommentTabsFragment();
    }

    @Override
    protected ArrayList<AStripTabsFragment.StripTabItem> generateTabs() {
        ArrayList<AStripTabsFragment.StripTabItem> beans = new ArrayList<AStripTabsFragment.StripTabItem>();

        beans.add(new AStripTabsFragment.StripTabItem("200", getString(R.string.cmts_to_me)));
        beans.add(new AStripTabsFragment.StripTabItem("201", getString(R.string.cmts_by_me)));

        return beans;
    }

    @Override
    protected Fragment newFragment(AStripTabsFragment.StripTabItem bean) {
        return CommentsFragment.newInstance(bean);
    }

}
