package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;

import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;

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
    protected void requestData(RefreshMode mode) {
        new GroupsTimelineTask(mode).execute();
    }

    class GroupsTimelineTask extends ATimelineTask {

        public GroupsTimelineTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        StatusContents getStatusContents(Params params) throws TaskException {
            params.addParameter("list_id", group.getIdstr());

            return SinaSDK.getInstance(AppContext.getAccount().getAccessToken(), getTaskCacheMode(this)).friendshipGroupsTimeline(params);
        }

    }

}
