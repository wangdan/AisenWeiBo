package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;

import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.support.utils.UMengUtil;

/**
 * 分组微博列表
 *
 * Created by wangdan on 16/1/2.
 */
public class TimelineGroupsFragment extends ATimelineFragment {

    public static TimelineGroupsFragment newInstance(Group group) {
        TimelineGroupsFragment fragment = new TimelineGroupsFragment();

        Bundle args = new Bundle();
        args.putSerializable("group", group);
        fragment.setArguments(args);

        return fragment;
    }

    private Group group;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        group = savedInstanceState == null ? (Group) getArguments().getSerializable("group")
                                           : (Group) savedInstanceState.getSerializable("group");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("group", group);
    }

    @Override
    public void requestData(RefreshMode mode) {
        new GroupsTimelineTask(mode).execute();
    }

    class GroupsTimelineTask extends ATimelineTask {

        public GroupsTimelineTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        public StatusContents getStatusContents(Params params) throws TaskException {
            params.addParameter("list_id", group.getIdstr());

            return SinaSDK.getInstance(AppContext.getAccount().getAccessToken(), getTaskCacheMode(this)).friendshipGroupsTimeline(params);
        }

    }

    @Override
    public boolean onToolbarDoubleClick() {
        requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);
        getRefreshView().scrollToPosition(0);

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(getActivity(), "好友分组微博页");
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), "好友分组微博页");
    }

}
