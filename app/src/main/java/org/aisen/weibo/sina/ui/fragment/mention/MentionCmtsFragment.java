package org.aisen.weibo.sina.ui.fragment.mention;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.fragment.ARecycleViewSwipeRefreshFragment;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.android.ui.fragment.itemview.BasicFooterView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusComments;
import org.aisen.weibo.sina.support.paging.CommentPaging;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 提及的评论
 *
 * Created by wangdan on 16/1/24.
 */
public class MentionCmtsFragment extends ARecycleViewSwipeRefreshFragment<StatusComment, StatusComments>
                                    implements ATabsFragment.ITabInitData {

    public static MentionCmtsFragment newInstance() {
        return new MentionCmtsFragment();
    }

    private BizFragment bizFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bizFragment = BizFragment.createBizFragment(this);
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        setViewPadding(getEmptyLayout());
        setViewPadding(getLoadFailureLayout());
        setViewPadding(getLoadingLayout());
    }

    private void setViewPadding(View viewGroup) {
        viewGroup.setPadding(viewGroup.getPaddingLeft(), viewGroup.getPaddingTop(),
                viewGroup.getPaddingRight(), SystemUtils.getNavigationBarHeight(getActivity()));
    }

    @Override
    protected IPaging<StatusComment, StatusComments> newPaging() {
        return new CommentPaging();
    }

    @Override
    protected IItemViewCreator<StatusComment> configFooterViewCreator() {
        return new IItemViewCreator<StatusComment>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(BasicFooterView.LAYOUT_RES, parent, false);
            }

            @Override
            public IITemView<StatusComment> newItemView(View convertView, int viewType) {
                return new BasicFooterView<StatusComment>(getActivity(), convertView, MentionCmtsFragment.this) {

                    @Override
                    protected String endpagingText() {
                        return getString(R.string.disable_comments);
                    }

                    @Override
                    protected String loadingText() {
                        return String.format(getString(R.string.loading_cmts), AppSettings.getCommentCount());
                    }

                };
            }

        };
    }

    @Override
    public IItemViewCreator<StatusComment> configItemViewCreator() {
        return new IItemViewCreator<StatusComment>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(MentionCmtItemView.RES_LAYOUT_ID, parent, false);
            }

            @Override
            public IITemView<StatusComment> newItemView(View convertView, int viewType) {
                return new MentionCmtItemView(convertView, MentionCmtsFragment.this, bizFragment);
            }

        };
    }

    @Override
    public void requestData(RefreshMode mode) {
        boolean load = true;

        // 如果还没有加载过数据，切且显示的是当前的页面
        if (getTaskCount(PAGING_TASK_ID) == 0) {
            load = AisenUtils.checkTabsFragmentCanRequestData(this);
        }

        if (load) {
            new MentionCmtTask(mode).execute();
        }
    }

    @Override
    public void onTabRequestData() {
        // 如果还没有加载过数据，就开始加载
        if (getTaskCount(PAGING_TASK_ID) == 0) {
            requestData(RefreshMode.reset);
        }
    }

    class MentionCmtTask extends APagingTask<Void, Void, StatusComments> {

        public MentionCmtTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected List<StatusComment> parseResult(StatusComments statusComments) {
            return statusComments.getComments();
        }

        @Override
        protected boolean handleResult(RefreshMode mode, List datas) {
            // 如果是重置或者刷新数据，加载数据大于分页大小，则清空之前的数据
            if (mode == RefreshMode.refresh)
                // 目前微博加载分页大小是默认大小
                if (datas.size() >= AppSettings.getCommentCount()) {
                    setAdapterItems(new ArrayList<StatusComment>());
                }


            return super.handleResult(mode, datas);
        }

        @Override
        protected StatusComments workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... params) throws TaskException {
            String sinceId = null;
            String maxId = null;
            String filterByAuthor = "0";

            if (mode == RefreshMode.refresh && !TextUtils.isEmpty(previousPage)) {
                sinceId = previousPage;
            }
            if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage)) {
                maxId = nextPage;
            }

            return SinaSDK.getInstance(AppContext.getAccount().getAccessToken(), getTaskCacheMode(this))
                                        .commentsMentions(sinceId, maxId, filterByAuthor, String.valueOf(AppSettings.getCommentCount()));
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            if (!isContentEmpty())
                showMessage(exception.getMessage());
        }

        @Override
        protected void onSuccess(StatusComments result) {
            super.onSuccess(result);

            if (result == null)
                return;

            if (result.fromCache() &&
                    AppContext.getAccount().getUnreadCount() != null && AppContext.getAccount().getUnreadCount().getMention_cmt() > 0) {
                requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);

                bizFragment.remindSetCount(BizFragment.RemindType.mention_cmt);
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
