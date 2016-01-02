package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;

/**
 * 默认微博列表
 *
 * Created by wangdan on 16/1/2.
 */
public class TimelineDefFragment extends ATimelineFragment {

    public static TimelineDefFragment newInstance(String method) {
        TimelineDefFragment fragment = new TimelineDefFragment();

        Bundle args = new Bundle();
        args.putString("method", method);
        fragment.setArguments(args);

        return fragment;
    }
    
}
