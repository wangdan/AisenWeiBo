package org.aisen.weibo.sina.ui.fragment.timeline;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import org.aisen.android.common.utils.Utils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.fragment.AListFragment;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.itemview.BasicFooterView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.StatusRepost;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.paging.TimelinePaging;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.comment.TimelineCommentItemView;
import org.aisen.weibo.sina.ui.fragment.comment.TimelineDetailPagerFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 某条原创微博的转发微博
 *
 * Created by wangdan on 16/1/22.
 */
public class TimelineRepostFragment extends AListFragment<StatusContent, StatusContents> implements ATabsFragment.ITabInitData {

    public static TimelineRepostFragment newInstance(StatusContent statusContent) {
        Bundle args = new Bundle();
        args.putSerializable("status", statusContent);

        TimelineRepostFragment fragment = new TimelineRepostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private StatusContent statusContent;

    private BizFragment bizFragment;

    @Override
    public int inflateContentView() {
        return R.layout.ui_timeline_comment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        statusContent = savedInstanceState == null ? (StatusContent) getArguments().getSerializable("status")
                                                   : (StatusContent) savedInstanceState.getSerializable("status");

        bizFragment = BizFragment.createBizFragment(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("status", statusContent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BizFragment.createBizFragment(getActivity()).getFabAnimator().attachToListView(getRefreshView(), null, this);
    }

    @Override
    public void requestData(RefreshMode mode) {
        boolean load = true;

        // 如果还没有加载过数据，切且显示的是当前的页面
        if (getTaskCount(PAGING_TASK_ID) == 0) {
            load = AisenUtils.checkTabsFragmentCanRequestData(this);
        }

        if (load)
            new RepostTask(mode).execute();
    }

    @Override
    public void onTabRequestData() {
        requestDataDelay(100);
    }

    class RepostTask extends ATimelineTask {

        public RepostTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        public StatusContents getStatusContents(Params params) throws TaskException {
            String statusId = statusContent.getId() + "";
            params.addParameter("id", statusId);

            StatusRepost statusRepost = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).statusRepostTimeline(params);
            if (statusRepost != null) {
                for (StatusContent status : statusRepost.getReposts()) {
                    status.setRetweeted_status(null);
                }

                return new StatusContents(statusRepost.getReposts());
            }

            throw new TaskException(TaskException.TaskError.resultIllegal.toString());
        }

    }

    class RepostItemView extends ARecycleViewItemView<StatusContent> {

        @ViewInject(id = R.id.imgPhoto)
        ImageView imgPhoto;
        @ViewInject(id = R.id.txtName)
        TextView txtName;
        @ViewInject(id = R.id.txtDesc)
        TextView txtDesc;
        @ViewInject(id = R.id.txtContent)
        AisenTextView txtContent;

        int firstTop;
        int normalTop;

        public RepostItemView(View convertView) {
            super(getActivity(), convertView);

            firstTop = Utils.dip2px(getContext(), 16);
            normalTop = Utils.dip2px(getContext(), 8);
        }


        @Override
        public void onBindData(View convertView, StatusContent data, int position) {
            WeiBoUser user = data.getUser();
            if (user != null) {
                BitmapLoader.getInstance().display(TimelineRepostFragment.this,
                        AisenUtils.getUserPhoto(user),
                        imgPhoto, ImageConfigUtils.getLargePhotoConfig());
                bizFragment.userShow(imgPhoto, user);
                txtName.setText(AisenUtils.getUserScreenName(user));
            }
            else {
                bizFragment.userShow(imgPhoto, null);
                txtName.setText(R.string.error_cmts);
                imgPhoto.setImageResource(R.drawable.user_placeholder);
            }

            txtContent.setContent(data.getText());
            AisenUtils.setTextSize(txtContent);

            String createAt = AisenUtils.convDate(data.getCreated_at());
            String from = String.format("%s", Html.fromHtml(data.getSource()));
            String desc = String.format("%s %s", createAt, from);
            txtDesc.setText(desc);

            int top = position == 0 ? firstTop : normalTop;
            convertView.setPadding(convertView.getPaddingLeft(), top, convertView.getPaddingRight(), convertView.getPaddingBottom());
        }

    }



    @Override
    public IItemViewCreator<StatusContent> configItemViewCreator() {
        return new IItemViewCreator<StatusContent>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(TimelineCommentItemView.LAYOUT_RES, parent, false);
            }

            @Override
            public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                return new RepostItemView(convertView);
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
        return new IItemViewCreator<StatusContent>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(BasicFooterView.LAYOUT_RES, parent, false);
            }

            @Override
            public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                return new BasicFooterView<StatusContent>(getActivity(), convertView, TimelineRepostFragment.this) {

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

        @Override
        protected void onFinished() {
            super.onFinished();

            if (getActivity() != null) {
                Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(SinaCommonActivity.FRAGMENT_TAG);
                if (fragment != null && fragment instanceof TimelineDetailPagerFragment) {
                    ((TimelineDetailPagerFragment) fragment).refreshEnd();
                }
            }
        }

        public abstract StatusContents getStatusContents(Params params) throws TaskException;

    }

    @Override
    public boolean onToolbarDoubleClick() {
        if (AisenUtils.checkTabsFragmentCanRequestData(this)) {
            Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(SinaCommonActivity.FRAGMENT_TAG);
            if (fragment != null && fragment instanceof TimelineDetailPagerFragment) {
                ((TimelineDetailPagerFragment) fragment).swipeRefreshLayout.setRefreshing(true);
            }

            requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);
            getRefreshView().setSelectionFromTop(0, 0);

            return true;
        }

        return false;
    }

}
