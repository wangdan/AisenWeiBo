package org.aisen.weibo.sina.ui.fragment.comment;

import android.app.Fragment;

import com.m.ui.fragment.AAutoReleaseStripTabsFragment;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.AStripTabsFragment;

import org.aisen.weibo.sina.R;

import java.util.ArrayList;

/**
 * 评论标签页
 *
 * Created by wangdan on 15/4/23.
 */
public class CommentTabsFragment extends AAutoReleaseStripTabsFragment<AStripTabsFragment.StripTabItem> {

    public static ABaseFragment newInstance() {
        return new CommentTabsFragment();
    }

    @Override
    protected ArrayList<StripTabItem> generateTabs() {
        ArrayList<StripTabItem> beans = new ArrayList<StripTabItem>();

        beans.add(new StripTabItem("200", getString(R.string.cmts_to_me)));
        beans.add(new StripTabItem("201", getString(R.string.cmts_by_me)));

        return beans;
    }

    @Override
    protected Fragment newFragment(StripTabItem bean) {
        return CommentsFragment.newInstance(bean);
    }

}
