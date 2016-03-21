package org.aisen.weibo.sina.ui.fragment.secondgroups;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.ARecycleViewSwipeRefreshFragment;
import org.aisen.android.ui.fragment.AWaterfallSwipeRefreshFragment;
import org.aisen.weibo.sina.support.bean.JokeBean;
import org.aisen.weibo.sina.support.bean.JokeBeans;
import org.aisen.weibo.sina.support.sdk.SDK;
import org.aisen.weibo.sina.ui.fragment.mention.MentionTimelineFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineDefFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineGroupsFragment;

import java.util.List;

/**
 * Created by wangdan on 16/3/14.
 */
public abstract class JokeBaseFragment extends AWaterfallSwipeRefreshFragment<JokeBean, JokeBeans> {

    abstract protected int setType();

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        setViewPadding(getEmptyLayout());
        setViewPadding(getLoadFailureLayout());
        setViewPadding(getLoadingLayout());
    }

    private void setViewPadding(View viewGroup) {
        viewGroup.setPadding(viewGroup.getPaddingLeft(), viewGroup.getPaddingTop(),
                viewGroup.getPaddingRight(), SystemUtils.getNavigationBarHeight(getActivity()));
    }

    @Override
    public void requestData(RefreshMode mode) {
        if (mode == RefreshMode.refresh) {
            mode = RefreshMode.reset;
        }

        new JokeTask(mode).execute();
    }

    class JokeTask extends APagingTask<Void, Void, JokeBeans> {

        public JokeTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected List<JokeBean> parseResult(JokeBeans jokeBeans) {
            return jokeBeans.getData().getContents();
        }

        @Override
        protected JokeBeans workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... params) throws TaskException {
            long newsid = 0;
            String direction = "up";

            // 下拉刷新
            if (mode == RefreshMode.refresh) {
                direction = "down";
                if (!TextUtils.isEmpty(previousPage)) newsid = Long.parseLong(previousPage);
            }
            // 上拉刷新
            else if (mode == RefreshMode.update) {
                direction = "up";
                if (!TextUtils.isEmpty(nextPage)) newsid = Long.parseLong(nextPage);
            }

            JokeBeans beans = SDK.newInstance(getTaskCacheMode(this)).getJokes(newsid, direction, 20, setType());

            if (mode == RefreshMode.update &&
                    beans.getData().getContents().size() == 0) {
                beans.setEndPaging(true);
            }

            return beans;
        }

    }
}
