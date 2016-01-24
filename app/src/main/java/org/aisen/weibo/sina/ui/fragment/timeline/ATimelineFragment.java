package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.ARecycleViewSwipeRefreshFragment;
import org.aisen.android.ui.fragment.itemview.BasicFooterView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.android.ui.fragment.itemview.NormalItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.support.paging.TimelinePaging;
import org.aisen.weibo.sina.ui.fragment.comment.TimelineDetailPagerFragment;
import org.aisen.weibo.sina.ui.fragment.mention.MentionTimelineFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 微博列表基类
 *
 * Created by wangdan on 16/1/2.
 */
public abstract class ATimelineFragment extends ARecycleViewSwipeRefreshFragment<StatusContent, StatusContents> {

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        if (this instanceof TimelineDefFragment || this instanceof TimelineGroupsFragment ||
                this instanceof MentionTimelineFragment) {
            setViewPadding(getEmptyLayout());
            setViewPadding(getLoadFailureLayout());
            setViewPadding(getLoadingLayout());
        }
    }
    
    private void setViewPadding(View viewGroup) {
        viewGroup.setPadding(viewGroup.getPaddingLeft(), viewGroup.getPaddingTop(),
                viewGroup.getPaddingRight(), SystemUtils.getNavigationBarHeight(getActivity()));
    }

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

        TimelineDetailPagerFragment.launch(getActivity(), getAdapterItems().get(position));
    }

    @Override
    protected IPaging<StatusContent, StatusContents> newPaging() {
        return new TimelinePaging();
    }

    @Override
    protected IItemViewCreator<StatusContent> configFooterViewCreator() {
        return new NormalItemViewCreator<StatusContent>(BasicFooterView.LAYOUT_RES) {

            @Override
            public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                return new BasicFooterView<StatusContent>(convertView, ATimelineFragment.this) {

                    @Override
                    protected String endpagingText() {
                        return getString(R.string.disable_status);
                    }

                    @Override
                    protected String loadingText() {
                        return String.format(getString(R.string.loading_status), AppSettings.getCommentCount());
                    }

                };
            }

        };
    }

    abstract public class ATimelineTask extends APagingTask<Void, Void, StatusContents> {

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

        @Override
        protected boolean handleResult(RefreshMode mode, List<StatusContent> datas) {
            // 如果是重置或者刷新数据，加载数据大于分页大小，则清空之前的数据
            if (mode == RefreshMode.refresh) {
                // 目前微博加载分页大小是默认大小
                if (datas.size() >= AppSettings.getTimelineCount()) {
                    setAdapterItems(new ArrayList<StatusContent>());
                    return true;
                }
            }

            return super.handleResult(mode, datas);
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            if (!isContentEmpty())
                showMessage(exception.getMessage());
        }

        public abstract StatusContents getStatusContents(Params params) throws TaskException;

    }

}
