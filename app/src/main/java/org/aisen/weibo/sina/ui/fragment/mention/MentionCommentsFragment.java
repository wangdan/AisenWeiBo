package org.aisen.weibo.sina.ui.fragment.mention;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.adapter.ABaseAdapter;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.ARefreshFragment;
import org.aisen.android.ui.fragment.AStripTabsFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.paging.CommentsPagingProcessor;
import org.aisen.weibo.sina.ui.fragment.basic.AWeiboRefreshListFragment;
import org.aisen.weibo.sina.ui.fragment.basic.BizFragment;
import org.aisen.weibo.sina.ui.fragment.comment.CommentItemView;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusComments;

import java.util.ArrayList;
import java.util.List;

/**
 * 提及的评论
 *
 * Created by wangdan on 15/4/22.
 */
public class MentionCommentsFragment extends AWeiboRefreshListFragment<StatusComment, StatusComments>
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static ABaseFragment newInstance(AStripTabsFragment.StripTabItem bean) {
        ABaseFragment fragment = new MentionCommentsFragment();

        Bundle args = new Bundle();
        args.putSerializable("bean", bean);
        fragment.setArguments(args);

        return fragment;
    }

    static int count = 0;

    public MentionCommentsFragment() {
        Logger.e("MentionComments, " + ++count);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        Logger.e("MentionComments, " + --count);
    }

    private AStripTabsFragment.StripTabItem mGroupBean;

    @Override
    protected void configRefresh(RefreshConfig config) {
        super.configRefresh(config);

        config.expiredAutoRefresh = true;
        config.emptyLabel = getString(R.string.empty_cmts);
    }

    @Override
    protected String loadDisabledLabel() {
        return getString(R.string.disable_comments);
    }

    @Override
    protected String loadingLabel() {
        return String.format(getString(R.string.loading_cmts), AppSettings.getCommentCount());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGroupBean = savedInstanceState == null ? (AStripTabsFragment.StripTabItem) getArguments().getSerializable("bean")
                                                : (AStripTabsFragment.StripTabItem) savedInstanceState.getSerializable("bean");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("bean", mGroupBean);
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        getRefreshView().setOnItemClickListener(this);
        getRefreshView().setOnItemLongClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListView listView = (ListView) getRefreshView();
        position -= listView.getHeaderViewsCount();
        if (position >= 0 && position < getAdapter().getCount())
            BizFragment.getBizFragment(this).replyComment(null, getAdapterItems().get(position));
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        view.findViewById(R.id.btnMenus).performClick();
        return true;
    }

    @Override
    protected IPaging<StatusComment, StatusComments> configPaging() {
        return new CommentsPagingProcessor();
    }

    @Override
    protected ABaseAdapter.AbstractItemView<StatusComment> newItemView() {
        return new CommentItemView(this);
    }

    @Override
    protected void requestData(ARefreshFragment.RefreshMode mode) {
        new MentionCommentTask(mode).execute();
    }

    class MentionCommentTask extends PagingTask<Void, Void, StatusComments> {

        public MentionCommentTask(ARefreshFragment.RefreshMode mode) {
            super("MentionCommentTask", mode);
        }

        @Override
        protected List<StatusComment> parseResult(StatusComments result) {
            return result.getComments();
        }

        @Override
        protected StatusComments workInBackground(ARefreshFragment.RefreshMode mode, String previousPage, String nextPage,
                                                  Void... params) throws TaskException {
            String sinceId = null;
            String maxId = null;
            String filterByAuthor = null;
            if (mode == ARefreshFragment.RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
                sinceId = previousPage;
            if (mode == ARefreshFragment.RefreshMode.update && !TextUtils.isEmpty(nextPage))
                maxId = nextPage;
            if ("103".equals(mGroupBean.getType()))
                filterByAuthor = "0";
            else
                filterByAuthor = "1";

            return SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(this))
                                .commentsMentions(sinceId, maxId, filterByAuthor, String.valueOf(AppSettings.getCommentCount()));
        }

        @Override
        protected boolean handleResult(RefreshMode mode, List datas) {
            // 如果是重置或者刷新数据，加载数据大于分页大小，则清空之前的数据
            if (mode == ARefreshFragment.RefreshMode.refresh)
                // 目前微博加载分页大小是默认大小
                if (datas.size() >= AppSettings.getCommentCount()) {
                    setAdapterItems(new ArrayList<StatusComment>());
                }


            return super.handleResult(mode, datas);
        }

        @Override
        protected void onSuccess(StatusComments result) {
            super.onSuccess(result);

            if (result == null)
                return;

            if (result.isCache() &&
                    AppContext.getUnreadCount() != null && AppContext.getUnreadCount().getMention_cmt() > 0) {
                requestDataDelay(AppSettings.REQUEST_DATA_DELAY);

                // fuck sina
                AppContext.getUnreadCount().setMention_cmt(0);

                BizFragment.getBizFragment(MentionCommentsFragment.this).remindSetCount(BizFragment.RemindType.mention_cmt);
            }
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            if (!isContentEmpty())
                showMessage(exception.getMessage());
        }

    }

    @Override
    public boolean onToolbarDoubleClick() {
        ABaseFragment aFragment = (ABaseFragment) getActivity().getFragmentManager().findFragmentByTag("MainFragment");
        if (aFragment instanceof AStripTabsFragment) {
            AStripTabsFragment tabTitlePagerFragment = (AStripTabsFragment) aFragment;
            if (tabTitlePagerFragment.getCurrentFragment() == this) {
                ListView listView = (ListView) getRefreshView();
                listView.setSelectionFromTop(0, 0);

                requestDataDelay(200);
                return true;
            }
            else
                return false;
        }

        return super.onToolbarDoubleClick();
    }

}
