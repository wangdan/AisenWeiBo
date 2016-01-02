package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;

import org.aisen.weibo.sina.sinasdk.bean.Group;

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

}
