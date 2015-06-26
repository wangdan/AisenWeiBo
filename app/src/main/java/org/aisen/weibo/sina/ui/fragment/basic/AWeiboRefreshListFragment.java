package org.aisen.weibo.sina.ui.fragment.basic;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;

import org.aisen.android.common.utils.Utils;
import org.aisen.android.ui.fragment.ASwipeRefreshListFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.ui.activity.basic.MainActivity;
import org.aisen.weibo.sina.ui.fragment.comment.CommentsFragment;
import org.aisen.weibo.sina.ui.fragment.friendship.AFriendshipFragment;
import org.aisen.weibo.sina.ui.fragment.mention.MentionCommentsFragment;
import org.aisen.weibo.sina.ui.fragment.mention.MentionTimelineFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineDefaultFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineGroupsFragment;
import org.aisen.weibo.sina.ui.widget.MainListView;

import java.io.Serializable;

/**
 * 设置一个刷新列表中间层，更换刷新控件修改这里的父类即可
 *
 * Created by wangdan on 15/4/14.
 */
public abstract class AWeiboRefreshListFragment<T extends Serializable, Ts extends Serializable>
                            extends ASwipeRefreshListFragment<T, Ts> {

    @Override
    protected int inflateContentView() {
        if (this instanceof AFriendshipFragment) {
            return R.layout.as_ui_friendship;
        }
        else if (isInMain()) {
            return R.layout.as_ui_main_swipelist;
        }

        return super.inflateContentView();
    }

    @Override
    protected void setInitSwipeRefresh(ListView listView, SwipeRefreshLayout swipeRefreshLayout, Bundle savedInstanceState) {
        super.setInitSwipeRefresh(listView, swipeRefreshLayout, savedInstanceState);

        if (isInMain()) {
            int toolbarHeight = getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);

            int progressBarStartMargin = getResources().getDimensionPixelSize(
                    R.dimen.swipe_refresh_progress_bar_start_margin);
            int progressBarEndMargin = getResources().getDimensionPixelSize(
                    R.dimen.swipe_refresh_progress_bar_end_margin);
            swipeRefreshLayout.setProgressViewOffset(false,
                    Utils.dip2px(50) + toolbarHeight + progressBarStartMargin,
                    Utils.dip2px(50) + toolbarHeight + progressBarEndMargin);

            setPadding(listView);
            setPadding(findViewById(R.id.layoutEmpty));
            setPadding(findViewById(R.id.layoutLoadFailed));
            setPadding(findViewById(R.id.layoutLoading));

            ((MainListView) getRefreshView()).setFragment(this);
        }
    }

    private void setPadding(View view) {
        if (view == null)
            return;

        int toolbarHeight = getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);

        view.setPadding(view.getPaddingLeft(),
                Utils.dip2px(50) + toolbarHeight,
                view.getPaddingRight(),
                view.getPaddingBottom());
    }

    private boolean isInMain() {
        boolean isMainFragment = this instanceof TimelineDefaultFragment||
                this instanceof TimelineGroupsFragment ||
                this instanceof MentionCommentsFragment ||
                this instanceof MentionTimelineFragment ||
                this instanceof CommentsFragment ||
                this instanceof AFriendshipFragment;

        return isMainFragment && getActivity() instanceof MainActivity;
    }

}
