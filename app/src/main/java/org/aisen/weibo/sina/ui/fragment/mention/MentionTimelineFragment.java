package org.aisen.weibo.sina.ui.fragment.mention;

import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;

/**
 * 提及的微博
 *
 * Created by wangdan on 15/4/15.
 */
public class MentionTimelineFragment extends ATimelineFragment implements ATabsFragment.ITabInitData {

    public static MentionTimelineFragment newInstance() {
        MentionTimelineFragment fragment = new MentionTimelineFragment();

        return fragment;
    }

    @Override
    public void requestData(RefreshMode mode) {
        boolean load = true;

        // 如果还没有加载过数据，切且显示的是当前的页面
        if (getTaskCount(PAGING_TASK_ID) == 0) {
            load = AisenUtils.checkTabsFragmentCanRequestData(this);
        }

        if (load) {
            new MentionTimelineTask(mode).execute();
        }
    }

    @Override
    public void onTabRequestData() {
        // 如果还没有加载过数据，就开始加载
        if (getTaskCount(PAGING_TASK_ID) == 0) {
            requestData(RefreshMode.reset);
        }
    }

    class MentionTimelineTask extends ATimelineTask {

        public MentionTimelineTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        public StatusContents getStatusContents(Params params) throws TaskException {
            // 全部的提及微博
            params.addParameter("filter_by_author", "0");

            return SinaSDK.getInstance(AppContext.getAccount().getAccessToken(), getTaskCacheMode(this)).statusesMentions(params);
        }

        @Override
        protected void onSuccess(StatusContents result) {
            super.onSuccess(result);

            // 如果是缓存，发现有未读提及微博提醒，刷新列表
            try {
                if (AppContext.getAccount().getUnreadCount() != null && AppContext.getAccount().getUnreadCount().getMention_status() > 0
                        && result.fromCache()) {
                    requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);

                    BizFragment.createBizFragment(MentionTimelineFragment.this).remindSetCount(BizFragment.RemindType.mention_status);
                }

            } catch (Exception e) {
            }
        }

    }

    @Override
    public boolean onToolbarDoubleClick() {
        if (AisenUtils.checkTabsFragmentCanRequestData(this)) {
            requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);
            getRefreshView().scrollToPosition(0);

            return true;
        }

        return false;
    }

}
