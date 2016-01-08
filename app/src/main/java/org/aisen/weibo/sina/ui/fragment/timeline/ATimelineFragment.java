package org.aisen.weibo.sina.ui.fragment.timeline;

import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.ARecycleViewSwipeRefreshFragment;
import org.aisen.android.ui.fragment.adapter.NormalItemViewCreator;
import org.aisen.android.ui.fragment.adapter.IITemView;
import org.aisen.android.ui.fragment.adapter.IItemViewCreator;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.support.paging.TimelinePaging;
import org.aisen.weibo.sina.ui.fragment.comment.TimelineCommentFragment;

import java.util.List;

/**
 * 微博列表基类
 *
 * Created by wangdan on 16/1/2.
 */
public abstract class ATimelineFragment extends ARecycleViewSwipeRefreshFragment<StatusContent, StatusContents> {

    @Override
    public IItemViewCreator<StatusContent> configItemViewCreator() {
        return new NormalItemViewCreator<StatusContent>(TimelineItemView.LAYOUT_RES) {

            @Override
            public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                return new TimelineItemView(convertView, ATimelineFragment.this);
            }

        };
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);

        TimelineCommentFragment.launch(getActivity(), getAdapterItems().get(position));
    }

    @Override
    protected IPaging<StatusContent, StatusContents> newPaging() {
        return new TimelinePaging();
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

            return getStatusContents(params);
        }

        abstract StatusContents getStatusContents(Params params) throws TaskException;

    }

}
