package org.aisen.weibo.sina.ui.fragment.timeline;

import android.text.TextUtils;
import android.view.View;

import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.ARecycleViewFragment;
import org.aisen.android.ui.fragment.adapter.IITemView;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;

import java.util.List;

/**
 * 微博列表基类
 *
 * Created by wangdan on 16/1/2.
 */
public abstract class ATimelineFragment extends ARecycleViewFragment<StatusContent, StatusContents> {

    @Override
    public IITemView<StatusContent> newItemView(View convertView, int viewType) {
        return new TimelineItemView(convertView);
    }

    @Override
    public int[][] configItemViewAndType() {
        return getNormalItemViewAndType(TimelineItemView.LAYOUT_RES);
    }

    abstract class ATimelineTask extends APagingTask<Void, Void, StatusContents> {

        public ATimelineTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected List<StatusContent> parseResult(StatusContents statusContents) {
            return statusContents.getStatuses();
        }

        @Override
        protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... p) throws TaskException {
            Params params = new Params();

            if (mode == APagingFragment.RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
                params.addParameter("since_id", previousPage);

            if (mode == APagingFragment.RefreshMode.update && !TextUtils.isEmpty(nextPage))
                params.addParameter("max_id", nextPage);

            params.addParameter("count", String.valueOf(AppSettings.getTimelineCount()));
            params.addParameter("count", String.valueOf(5));

            return getStatusContents(params);
        }

        abstract StatusContents getStatusContents(Params params) throws TaskException;

    }

}
