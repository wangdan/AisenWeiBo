package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ListView;

import com.m.network.http.Params;
import com.m.network.task.TaskException;
import com.m.ui.fragment.ABaseFragment;

import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.bean.TimelineGroupBean;
import org.aisen.weibo.sina.ui.fragment.basic.BizFragment;
import org.sina.android.SinaSDK;
import org.sina.android.bean.StatusContents;

/**
 * 提及的微博
 * 100:全部, 101:关注人的 102:原创
 *
 * Created by wangdan on 15/4/15.
 */
public class TimelineMentionFragment extends ATimelineFragment {

    public static ABaseFragment newInstance(TimelineGroupBean bean) {
        ABaseFragment fragment = new TimelineMentionFragment();

        Bundle args = new Bundle();
        args.putSerializable("bean", bean);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected void config(com.m.ui.fragment.ARefreshFragment.RefreshConfig config) {
        super.config(config);
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
            switch (Integer.parseInt(getGroup().getGroup())) {
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
                if (AppContext.getUnreadCount() != null && AppContext.getUnreadCount().getMention_status() > 0) {
                    requestDataDelay(AppSettings.REQUEST_DATA_DELAY);

                    // fuck sina
                    AppContext.getUnreadCount().setMention_status(0);

                    BizFragment.getBizFragment(TimelineMentionFragment.this).remindSetCount(BizFragment.RemindType.mention_status);
                }

            } catch (Exception e) {
            }
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            if (mode != RefreshMode.update)
                getRefreshView().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        ((ListView) getRefreshView()).setSelectionFromTop(0, 0);
                    }

                }, 20);
        }

    }

}
