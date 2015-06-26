package org.aisen.weibo.sina.ui.fragment.profile;

import org.aisen.android.ui.fragment.ABaseFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineItemView;

/**
 * Created by wangdan on 15/5/2.
 */
public class UserTimelineItemView extends TimelineItemView {

    public UserTimelineItemView(ABaseFragment fragment, boolean showRetweeted) {
        super(fragment, showRetweeted);
    }

    @Override
    public int inflateViewId() {
        return R.layout.as_item_user_timeline;
    }

}
