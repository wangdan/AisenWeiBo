package org.aisen.weibo.sina.ui.fragment.timeline;

import android.content.Intent;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.utils.AisenUtils;

/**
 * Created by wangdan on 16/1/27.
 */
public abstract class TimelineMainFragment extends ATimelineFragment {

    public static void sendBroadcast() {
        GlobalContext.getInstance().sendBroadcast(new Intent("org.aisen.weibo.sina.OFFLINE_END"));
    }

    public static void clearLastRead(Group group, WeiBoUser user) {
        String key = AisenUtils.getUserKey(group.getIdstr(), user);
        ActivityHelper.putIntShareData(GlobalContext.getInstance(), key + "Position", 0);
        ActivityHelper.putIntShareData(GlobalContext.getInstance(), key + "Top", 0);
    }

}
