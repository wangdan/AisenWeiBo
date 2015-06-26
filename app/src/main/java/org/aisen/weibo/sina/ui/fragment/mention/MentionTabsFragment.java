package org.aisen.weibo.sina.ui.fragment.mention;

import android.app.Fragment;
import android.os.Bundle;

import org.aisen.android.ui.fragment.ABaseFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.ui.fragment.basic.AMainStripTabsFragment;

import java.util.ArrayList;

/**
 * 提及标签页
 *
 * Created by wangdan on 15/4/22.
 */
public class MentionTabsFragment extends AMainStripTabsFragment {

    public static ABaseFragment newInstance() {
        MentionTabsFragment fragment = new MentionTabsFragment();

        if (AppContext.getUnreadCount() != null && AppContext.getUnreadCount().getMention_cmt() > 0) {
            Bundle args = new Bundle();
            args.putInt(SET_INDEX, 1);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    protected ArrayList<StripTabItem> generateTabs() {
        ArrayList<StripTabItem> beans = new ArrayList<StripTabItem>();

        // 提及的微博
        beans.add(new StripTabItem("100", getString(R.string.mention_status)));
        // 提及的评论
        beans.add(new StripTabItem("103", getString(R.string.mention_cmt)));

        return beans;
    }

    @Override
    protected Fragment newFragment(StripTabItem bean) {
        if ("100".equals(bean.getType()))
            return MentionTimelineFragment.newInstance(bean);
        else if ("103".equals(bean.getType()))
            return MentionCommentsFragment.newInstance(bean);

        return null;
    }

}
