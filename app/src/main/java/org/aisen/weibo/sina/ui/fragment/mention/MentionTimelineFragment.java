package org.aisen.weibo.sina.ui.fragment.mention;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ListView;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.ARefreshFragment;
import org.aisen.android.ui.fragment.AStripTabsFragment;

import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.ui.fragment.basic.BizFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;

/**
 * 提及的微博
 * 100:全部, 101:关注人的 102:原创
 *
 * Created by wangdan on 15/4/15.
 */
public class MentionTimelineFragment extends ATimelineFragment {

    public static ABaseFragment newInstance(AStripTabsFragment.StripTabItem bean) {
        ABaseFragment fragment = new MentionTimelineFragment();

        Bundle args = new Bundle();
        args.putSerializable("bean", bean);
        fragment.setArguments(args);

        return fragment;
    }

    static int count = 0;

    public MentionTimelineFragment() {
        Logger.e("MentionTimeline, " + ++count);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        Logger.e("MentionTimeline, " + --count);
    }

    @Override
    protected void configRefresh(RefreshConfig config) {
        super.configRefresh(config);

        config.saveLastPositionKey = null;
    }

    @Override
    protected void requestData(RefreshMode mode) {
        new MentionTimelineTask(mode).execute();
    }

    class MentionTimelineTask extends TimelineTask {

        public MentionTimelineTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage,
                                                  Void... p) throws TaskException {
            Params params = new Params();
            if (mode == RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
                params.addParameter("since_id", previousPage);
            if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
                params.addParameter("max_id", nextPage);
            switch (Integer.parseInt(getGroup().getType())) {
                case 100:
                    params.addParameter("filter_by_author", "0");
                    break;
                case 101:
                    params.addParameter("filter_by_author", "1");
                    break;
                case 102:
                    params.addParameter("filter_by_type", "0");
                    break;
            }

            params.addParameter("count", String.valueOf(AppSettings.getTimelineCount()));

            return SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(this)).statusesMentions(params);
        }

        @Override
        protected void onSuccess(StatusContents result) {
            super.onSuccess(result);

            try {
                if (AppContext.getUnreadCount() != null && AppContext.getUnreadCount().getMention_status() > 0
                        && result.isCache()) {
                    requestDataDelay(AppSettings.REQUEST_DATA_DELAY);

                    // fuck sina
                    AppContext.getUnreadCount().setMention_status(0);

                    BizFragment.getBizFragment(MentionTimelineFragment.this).remindSetCount(BizFragment.RemindType.mention_status);
                }

            } catch (Exception e) {
            }
        }

    }

}
