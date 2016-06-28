package org.aisen.weibo.sina.ui.fragment.comment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.fragment.ARecycleViewSwipeRefreshFragment;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.android.ui.fragment.adapter.BasicRecycleViewAdapter;
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
import org.aisen.weibo.sina.ui.fragment.mention.MentionCmtItemView;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论列表，我发出的和我收到的
 *
 * Created by wangdan on 16/1/24.
 */
public class CommentsFragment extends ARecycleViewSwipeRefreshFragment<StatusComment, StatusComments>
                                implements AdapterView.OnItemLongClickListener, ATabsFragment.ITabInitData {

    public static CommentsFragment newInstance(Type type) {
        CommentsFragment fragment = new CommentsFragment();

        Bundle args = new Bundle();
        args.putSerializable("type", type.toString());
        fragment.setArguments(args);

        return fragment;
    }

    public enum Type {
        toMe, byMe
    }

    private BizFragment bizFragment;
    private Type type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        type = savedInstanceState == null ? Type.valueOf(getArguments().getString("type"))
                                          : Type.valueOf(savedInstanceState.getString("type"));

        bizFragment = BizFragment.createBizFragment(this);
    }

    private void setViewPadding(View viewGroup) {
        viewGroup.setPadding(viewGroup.getPaddingLeft(), viewGroup.getPaddingTop(),
                viewGroup.getPaddingRight(), SystemUtils.getNavigationBarHeight(getActivity()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("type", type.toString());
    }

    @Override
    protected void setupRefreshConfig(RefreshConfig config) {
        super.setupRefreshConfig(config);

        config.emptyHint = getString(R.string.empty_cmts);
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
                return new BasicFooterView<StatusComment>(getActivity(), convertView, CommentsFragment.this) {

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
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        if (getAdapter() instanceof BasicRecycleViewAdapter) {
            ((BasicRecycleViewAdapter) getAdapter()).setOnItemLongClickListener(this);
        }

        setViewPadding(getEmptyLayout());
        setViewPadding(getLoadFailureLayout());
        setViewPadding(getLoadingLayout());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);

        bizFragment.replyComment(null, getAdapterItems().get(position));
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        view.findViewById(R.id.btnMenus).performClick();
        return true;
    }

    @Override
    protected IPaging<StatusComment, StatusComments> newPaging() {
        return new CommentPaging();
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
                return new MentionCmtItemView(convertView, CommentsFragment.this, bizFragment);
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
            if (type == Type.toMe)
                new CommentToMe(mode).execute();
            else if (type == Type.byMe)
                new CommentByMe(mode).execute();
        }
    }

    @Override
    public void onTabRequestData() {
        // 如果还没有加载过数据，就开始加载
        if (getTaskCount(PAGING_TASK_ID) == 0) {
            requestData(RefreshMode.reset);
        }
    }

    // 发给我的评论
    class CommentToMe extends CommentsTask {

        public CommentToMe(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected StatusComments workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... params) throws TaskException {
            String sinceId = null;
            String maxId = null;
            String filterByAuthor = null;
            if (mode == RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
                sinceId = previousPage;
            if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
                maxId = nextPage;
            filterByAuthor = "0";

            return SinaSDK.getInstance(AppContext.getAccount().getAccessToken(), getTaskCacheMode(this))
                    .commentsToMe(sinceId, maxId, filterByAuthor, String.valueOf(AppSettings.getCommentCount()));
        }

        @Override
        protected void onSuccess(StatusComments result) {
            super.onSuccess(result);

            if (AppContext.getAccount().getUnreadCount() != null && AppContext.getAccount().getUnreadCount().getCmt() > 0) {
                if (getActivity() == null)
                    return;

                if (result.fromCache())
                    requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);

                bizFragment.remindSetCount(BizFragment.RemindType.cmt);
            }
        }

    };

    // 我发出的评论
    class CommentByMe extends CommentsTask {

        public CommentByMe(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected StatusComments workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... params) throws TaskException {
            String sinceId = null;
            String maxId = null;
            if (mode == RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
                sinceId = previousPage;
            if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
                maxId = nextPage;

            return SinaSDK.getInstance(AppContext.getAccount().getAccessToken(), getTaskCacheMode(this))
                    .commentsByMe(sinceId, maxId, String.valueOf(AppSettings.getCommentCount()));
        }

    };

    abstract class CommentsTask extends APagingTask<Void, Void, StatusComments> {

        public CommentsTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected List<StatusComment> parseResult(StatusComments result) {
            return result.getComments();
        }

        @Override
        protected boolean handleResult(RefreshMode mode, List datas) {
            // 如果是重置或者刷新数据，加载数据大于分页大小，则清空之前的数据
            if (mode == RefreshMode.refresh)
                // 目前微博加载分页大小是默认大小
                if (datas.size() >= AppSettings.getCommentCount()) {
                    setAdapterItems(new ArrayList<StatusComment>());
                    return true;
                }

            return super.handleResult(mode, datas);
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            if (isContentEmpty())
                showMessage(exception.getMessage());
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
