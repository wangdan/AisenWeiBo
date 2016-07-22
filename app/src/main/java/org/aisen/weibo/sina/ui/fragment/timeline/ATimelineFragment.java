package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.ARecycleViewSwipeRefreshFragment;
import org.aisen.android.ui.fragment.adapter.BasicRecycleViewAdapter;
import org.aisen.android.ui.fragment.adapter.IPagingAdapter;
import org.aisen.android.ui.fragment.itemview.AHeaderItemViewCreator;
import org.aisen.android.ui.fragment.itemview.BasicFooterView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.support.paging.TimelinePaging;
import org.aisen.weibo.sina.ui.activity.base.MainActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
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

    private String feature = "0";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            BizFragment.createBizFragment(getActivity()).getFabAnimator().attachToRecyclerView(getRefreshView(), null, null);
        }

        if (getArguments() == null) {
            feature = savedInstanceState == null ? feature
                                                 : savedInstanceState.getString("feature", "0");
        }
        else {
            feature = savedInstanceState == null ? getArguments().getString("feature", "0")
                                                 : savedInstanceState.getString("feature", "0");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("feature", feature);
    }

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
    protected AHeaderItemViewCreator<StatusContent> configHeaderViewCreator() {
        if (this instanceof TimelineDefFragment || this instanceof TimelineGroupsFragment) {
            return new AHeaderItemViewCreator<StatusContent>() {

                @Override
                public int[][] setHeaders() {
                    return new int[][]{ { ATimelineHeaderView.LAYOUT_RES, 100 } };
                }

                @Override
                public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                    return new ATimelineHeaderView(ATimelineFragment.this, convertView) {

                        @Override
                        protected int getTitleArrRes() {
                            return R.array.timeline_headers;
                        }

                        @Override
                        protected String[] getTitleFeature() {
                            return ATimelineHeaderView.timelineFeatureArr;
                        }

                    };
                }

            };
        }

        return super.configHeaderViewCreator();
    }

    @Override
    public IItemViewCreator<StatusContent> configItemViewCreator() {
        return new IItemViewCreator<StatusContent>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                if (5 == viewType) {
                    return inflater.inflate(TimelinePhotosItemView.LAYOUT_RES, parent, false);
                }

                return inflater.inflate(TimelineItemView.LAYOUT_RES, parent, false);
            }

            @Override
            public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                if (5 == viewType) {
                    return new TimelinePhotosItemView(convertView, ATimelineFragment.this);
                }

                return new TimelineItemView(convertView, ATimelineFragment.this);
            }

        };
    }

    @Override
    protected IPagingAdapter<StatusContent> newAdapter(ArrayList<StatusContent> datas) {
        return new TimelineAdapter(this, configItemViewCreator(), datas);
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
        return new IItemViewCreator<StatusContent>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(BasicFooterView.LAYOUT_RES, parent, false);
            }

            @Override
            public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                return new BasicFooterView<StatusContent>(getActivity(), convertView, ATimelineFragment.this) {

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

    class TimelineAdapter extends BasicRecycleViewAdapter<StatusContent> {

        public TimelineAdapter(APagingFragment holderFragment, IItemViewCreator<StatusContent> itemViewCreator, ArrayList<StatusContent> datas) {
            super(holderFragment, itemViewCreator, datas);
        }

        @Override
        public int getItemViewType(int position) {
            int itemType = super.getItemViewType(position);

            // 如果不是HeaderView和FooterView
            if (itemType == IPagingAdapter.TYPE_NORMAL) {
                return Integer.parseInt(getFeature());
            }

            return itemType;
        }

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

            // 是否是原创
            if (!TextUtils.isEmpty(getFeature())) {
                if ("5".equals(getFeature())) {
                    params.addParameter("feature", "2");
                }
                else {
                    params.addParameter("feature", getFeature());
                }
            }

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

    @Override
    public void onResume() {
        super.onResume();

        // 刷新点赞数据
        if (getRefreshView() != null && getRefreshView().getChildCount() > 0) {
            for (int i = 0; i < getRefreshView().getChildCount(); i++) {
                View view = getRefreshView().getChildAt(i);
                if (view.getTag(R.id.itemview) != null && view.getTag(R.id.itemview) instanceof TimelineItemView) {
                    ((TimelineItemView) view.getTag(R.id.itemview)).setLikeView();
                }
            }
        }
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

}
