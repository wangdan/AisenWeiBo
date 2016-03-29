package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.support.utils.UMengUtil;

import java.lang.reflect.Method;

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

    private String method;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        method = savedInstanceState == null ? getArguments().getString("method")
                                            : savedInstanceState.getString("method");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("method", method);
    }

    @Override
    public void requestData(RefreshMode mode) {
        new DefTimelineTask(mode).execute();
    }

    class DefTimelineTask extends ATimelineTask {

        public DefTimelineTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        public StatusContents getStatusContents(Params params) throws TaskException {
            try {
                Method timelineMethod = SinaSDK.class.getMethod(method, new Class[] { Params.class });
                return (StatusContents) timelineMethod.invoke(SinaSDK.getInstance(AppContext.getAccount().getAccessToken(), getTaskCacheMode(this)), params);
            } catch (Throwable e) {
                if (e.getCause() instanceof TaskException) {
                    throw (TaskException) e.getCause();
                }
                else {
                    Logger.printExc(TimelineDefFragment.class, e);
                }
            }

            throw new TaskException(TaskException.TaskError.resultIllegal.toString());
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

        UMengUtil.onPageStart(getActivity(), getPageName());
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), getPageName());
    }

    private String getPageName() {
        if ("statusesFriendsTimeLine".equals(method)) {
            return getString(R.string.timeline_all);
        } else if ("statusesBilateralTimeLine".equals(method)) {
            return getString(R.string.timeline_bilateral);
        } else if ("statusesToMe".equals(method)) {
            return getString(R.string.timeline_tome);
        }

        return getString(R.string.timeline_all);
    }

}
