package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;

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
    protected void requestData(RefreshMode mode) {
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

}
