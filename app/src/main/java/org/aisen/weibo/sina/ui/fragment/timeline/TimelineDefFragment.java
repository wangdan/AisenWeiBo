package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;

import org.aisen.android.support.adapter.ABaseAdapter;
import org.aisen.android.ui.fragment.AListSwipeRefreshFragment;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;

/**
 * 默认微博列表
 *
 * Created by wangdan on 16/1/2.
 */
public class TimelineDefFragment extends ATimelineFragment {

    public static TimelineDefFragment newInstance(int position) {
        String[] methods = new String[]{ "statusesFriendsTimeLine", "statusesBilateralTimeLine", "statusesToMe" };

        TimelineDefFragment fragment = new TimelineDefFragment();

        Bundle args = new Bundle();
        args.putString("method", methods[position]);
        fragment.setArguments(args);

        return fragment;
    }
    
}
