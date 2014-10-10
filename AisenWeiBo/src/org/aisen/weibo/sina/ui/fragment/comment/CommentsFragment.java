package org.aisen.weibo.sina.ui.fragment.comment;

import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.TimelineGroupBean;
import org.aisen.weibo.sina.support.paging.CommentsPagingProcessor;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.ui.activity.main.MainActivity;
import org.aisen.weibo.sina.ui.component.CommentItemView;
import org.aisen.weibo.sina.ui.fragment.base.ARefreshProxyFragment;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment.RemindType;
import org.sina.android.SinaSDK;
import org.sina.android.bean.StatusComment;
import org.sina.android.bean.StatusComments;
import org.sina.android.bean.WeiBoUser;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.paging.IPaging;
import com.m.support.task.TaskException;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.ATabTitlePagerFragment;

/**
 * 我收到的、发出的所有评论
 * 
 * @author wangdan
 *
 */
public class CommentsFragment extends ARefreshProxyFragment<StatusComment, StatusComments> 
										implements OnItemClickListener {

	public static ABaseFragment newInstance(TimelineGroupBean bean) {
		ABaseFragment fragment = new CommentsFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("bean", bean);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	private TimelineGroupBean mBean;
	private WeiBoUser loggedIn;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_mention_comments;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		loggedIn = AppContext.getUser();
		mBean = savedInstanceSate == null ? (TimelineGroupBean) getArguments().getSerializable("bean")
										  : (TimelineGroupBean) savedInstanceSate.getSerializable("bean");
		
		getRefreshView().setOnItemClickListener(this);
		
		setHasOptionsMenu(true);
		
		((TextView) findViewById(R.id.layoutEmpty).findViewById(R.id.txtLoadFailed)).setText(R.string.empty_cmts);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("bean", mBean);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ListView listView = (ListView) getRefreshView();
		position -= listView.getHeaderViewsCount();
		BizFragment.getBizFragment(this).replyComment(null, getAdapter().getDatas().get(position));
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
	protected void requestData(RefreshMode mode) {
		if ("200".equals(mBean.getType()))
			new CommentToMe(mode).execute();
		else if ("201".equals(mBean.getType()))
			new CommentByMe(mode).execute();
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
			
			return SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(this))
									.commentsToMe(sinceId, maxId, filterByAuthor, String.valueOf(AppSettings.getCommentCount()));
		}
		
		@Override
		protected void onSuccess(StatusComments result) {
			super.onSuccess(result);
			
			if (AppContext.getUnreadCount() != null && AppContext.getUnreadCount().getCmt() > 0) {
				requestDataDelay(1000);
				
				// fuck sina
				AppContext.getUnreadCount().setCmt(0);
				
				BizFragment.getBizFragment(CommentsFragment.this).remindSetCount(RemindType.cmt);
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
			
			return SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(this))
									.commentsByMe(sinceId, maxId, String.valueOf(AppSettings.getCommentCount()));
		}
		
	};
	
	abstract class CommentsTask extends PagingTask<Void, Void, StatusComments> {

		public CommentsTask(RefreshMode mode) {
			super("CommentsTask", mode);
		}
		
		@Override
		protected List<StatusComment> parseResult(StatusComments result) {
			return result.getComments();
		}
		
		@Override
		protected boolean handleResult(RefreshMode mode, List<StatusComment> datas) {
			// 如果是重置或者刷新数据，加载数据大于分页大小，则清空之前的数据
			if (mode == RefreshMode.refresh)
				// 目前微博加载分页大小是默认大小
				if (datas.size() >= AppSettings.getCommentCount()) {
					getAdapter().setDatas(new ArrayList<StatusComment>());
					return true;
				}

			return super.handleResult(mode, datas);
		}
		
		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			
			showMessage(exception.getMessage());
		}
		
	}
	
	
	@Override
	public String getLastReadKey() {
		return AisenUtil.getUserKey(mBean.getType(), loggedIn);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.removeGroup(R.id.timeline);
		if (getActivity() instanceof MainActivity) {
			inflater.inflate(R.menu.refresh_comments, menu);
		}
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		if (getActivity() instanceof MainActivity) {
			MainActivity mainActivity = (MainActivity) getActivity();
			
			menu.findItem(R.id.refresh).setVisible(!mainActivity.isDrawerOpened());
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.refresh) 
			requestDataDelay(100);
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onAcUnusedDoubleClicked() {
		ABaseFragment aFragment = (ABaseFragment) getActivity().getFragmentManager().findFragmentByTag("MainFragment");
		if (aFragment instanceof ATabTitlePagerFragment) {
			@SuppressWarnings("rawtypes")
			ATabTitlePagerFragment tabTitlePagerFragment = (ATabTitlePagerFragment) aFragment;
			if (tabTitlePagerFragment.getCurrentFragment() == this)
				return super.onAcUnusedDoubleClicked();
			else 
				return false;
		}

		return super.onAcUnusedDoubleClicked();
	}
	
}
