package org.aisen.weibo.sina.ui.fragment.hot;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.support.paging.HotPaging;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;

/**
 * 热门微博
 *
 * Created by wangdan on 16/8/10.
 */
public class TimelineHotFragment extends ATimelineFragment {

    public static void launch(Activity from) {
        SinaCommonActivity.launch(from , TimelineHotFragment.class, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle("热门微博");
    }

    @Override
    public void requestData(RefreshMode mode) {
        new HotTimelineTask(mode != RefreshMode.update ? RefreshMode.reset : mode).execute();
    }

    @Override
    protected IPaging<StatusContent, StatusContents> newPaging() {
        return new HotPaging();
    }

    @Override
    protected int timelineCount() {
        return 10;
    }

    class HotTimelineTask extends ATimelineTask {

        public HotTimelineTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... p) throws TaskException {
            int page = 0;

            if (!TextUtils.isEmpty(nextPage))
                page = Integer.parseInt(nextPage);

            StatusContents result = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).webGetHotStatuses(page);

            for (StatusContent content : result.getStatuses()) {
                AisenTextView.addText(content.getText());

                if (content.getRetweeted_status() != null) {
                    String reUserName = "";
                    if (content.getRetweeted_status().getUser() != null && !TextUtils.isEmpty(content.getRetweeted_status().getUser().getScreen_name()))
                        reUserName = String.format("@%s :", content.getRetweeted_status().getUser().getScreen_name());
                    AisenTextView.addText(reUserName + content.getRetweeted_status().getText());
                }
            }

            return result;
        }

        @Override
        public StatusContents getStatusContents(Params params) throws TaskException {
            throw new TaskException("", "不支持");
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

        UMengUtil.onPageStart(getActivity(), "热门微博页");
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), "热门微博页");
    }

}
