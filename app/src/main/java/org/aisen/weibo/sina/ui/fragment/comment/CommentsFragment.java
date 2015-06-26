package org.aisen.weibo.sina.ui.fragment.comment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.adapter.ABaseAdapter.AbstractItemView;
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
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusComments;

import java.util.ArrayList;
import java.util.List;

/**
 * 我收到的、发出的所有评论
 * 
 * @author wangdan
 *
 */
public class CommentsFragment extends AWeiboRefreshListFragment<StatusComment, StatusComments>
										implements OnItemClickListener, OnItemLongClickListener {

	public static ABaseFragment newInstance(AStripTabsFragment.StripTabItem bean) {
		ABaseFragment fragment = new CommentsFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("bean", bean);
		fragment.setArguments(args);
		
		return fragment;
	}

    static int count = 0;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        Logger.e("CommentsFragment, " + --count);
    }
	
	private AStripTabsFragment.StripTabItem mBean;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.e("CommentsFragment, " + ++count);

        mBean = savedInstanceState == null ? (AStripTabsFragment.StripTabItem) getArguments().getSerializable("bean")
                                           : (AStripTabsFragment.StripTabItem) savedInstanceState.getSerializable("bean");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("bean", mBean);
    }

    @Override
    protected void configRefresh(RefreshConfig config) {
        super.configRefresh(config);

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
	protected AbstractItemView<StatusComment> newItemView() {
		return new CommentItemView(this);
	}

	@Override
	protected void requestData(ARefreshFragment.RefreshMode mode) {
		if (mBean != null) {
			if ("200".equals(mBean.getType()))
				new CommentToMe(mode).execute();
			else if ("201".equals(mBean.getType()))
				new CommentByMe(mode).execute();
		}
	}
	
	// 发给我的评论
	class CommentToMe extends CommentsTask {

		public CommentToMe(ARefreshFragment.RefreshMode mode) {
			super(mode);
		}

		@Override
		protected StatusComments workInBackground(ARefreshFragment.RefreshMode mode, String previousPage, String nextPage, Void... params) throws TaskException {
			String sinceId = null;
			String maxId = null;
			String filterByAuthor = null;
			if (mode == ARefreshFragment.RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
				sinceId = previousPage;
			if (mode == ARefreshFragment.RefreshMode.update && !TextUtils.isEmpty(nextPage))
				maxId = nextPage;
			filterByAuthor = "0";
			
			return SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(this))
									.commentsToMe(sinceId, maxId, filterByAuthor, String.valueOf(AppSettings.getCommentCount()));
		}
		
		@Override
		protected void onSuccess(StatusComments result) {
			super.onSuccess(result);
			
			if (AppContext.getUnreadCount() != null && AppContext.getUnreadCount().getCmt() > 0) {
				if (getActivity() == null)
					return;

                if (result.isCache())
				    requestDataDelay(AppSettings.REQUEST_DATA_DELAY);
				
				BizFragment.getBizFragment(CommentsFragment.this).remindSetCount(BizFragment.RemindType.cmt);
			}
		}

	};

	// 我发出的评论
	class CommentByMe extends CommentsTask {

		public CommentByMe(ARefreshFragment.RefreshMode mode) {
			super(mode);
		}

		@Override
		protected StatusComments workInBackground(ARefreshFragment.RefreshMode mode, String previousPage, String nextPage, Void... params) throws TaskException {
			String sinceId = null;
			String maxId = null;
			if (mode == ARefreshFragment.RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
				sinceId = previousPage;
			if (mode == ARefreshFragment.RefreshMode.update && !TextUtils.isEmpty(nextPage))
				maxId = nextPage;
			
			return SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(this))
									.commentsByMe(sinceId, maxId, String.valueOf(AppSettings.getCommentCount()));
		}
		
	};
	
    abstract class CommentsTask extends PagingTask<Void, Void, StatusComments> {

        public CommentsTask(ARefreshFragment.RefreshMode mode) {
            super("CommentsTask", mode);
        }

        @Override
        protected List<StatusComment> parseResult(StatusComments result) {
            return result.getComments();
        }

        @Override
        protected boolean handleResult(RefreshMode mode, List datas) {
            // 如果是重置或者刷新数据，加载数据大于分页大小，则清空之前的数据
            if (mode == ARefreshFragment.RefreshMode.refresh)
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
        ABaseFragment aFragment = (ABaseFragment) getActivity().getFragmentManager().findFragmentByTag("MainFragment");
        if (aFragment instanceof AStripTabsFragment) {
            @SuppressWarnings("rawtypes")
            AStripTabsFragment tabTitlePagerFragment = (AStripTabsFragment) aFragment;
            if (tabTitlePagerFragment.getCurrentFragment() == this) {
                ListView listView = (ListView) getRefreshView();
                listView.setSelectionFromTop(0, 0);

                requestDataDelay(200);
                return true;
            }
        }

        return super.onToolbarDoubleClick();
    }

}
