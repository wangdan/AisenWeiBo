package org.aisen.weibo.sina.ui.fragment.mention;

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
 * 提及的评论
 * 
 * @author wangdan
 *
 */
public class MentionCommentsFragment extends ARefreshProxyFragment<StatusComment, StatusComments>
										implements OnItemClickListener {

	public static ABaseFragment newInstance(TimelineGroupBean bean) {
		ABaseFragment fragment = new MentionCommentsFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("bean", bean);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	private TimelineGroupBean mGroupBean;
	private WeiBoUser loggedIn;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_mention_comments;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		loggedIn = AppContext.getUser();
		mGroupBean = savedInstanceSate == null ? (TimelineGroupBean) getArguments().getSerializable("bean")
											   : (TimelineGroupBean) savedInstanceSate.getSerializable("bean");
		
		getRefreshView().setOnItemClickListener(this);
		
		setHasOptionsMenu(true);
		
		((TextView) findViewById(R.id.layoutEmpty).findViewById(R.id.txtLoadFailed)).setText(R.string.empty_cmts);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("bean", mGroupBean);
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
		new MentionCommentTask(mode).execute();
	}
	
	class MentionCommentTask extends PagingTask<Void, Void, StatusComments> {

		public MentionCommentTask(RefreshMode mode) {
			super("MentionCommentTask", mode);
		}

		@Override
		protected List<StatusComment> parseResult(StatusComments result) {
			return result.getComments();
		}

		@Override
		protected StatusComments workInBackground(RefreshMode mode, String previousPage, String nextPage,
				Void... params) throws TaskException {
			String sinceId = null;
			String maxId = null;
			String filterByAuthor = null;
			if (mode == RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
				sinceId = previousPage;
			if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
				maxId = nextPage;
			if ("103".equals(mGroupBean.getGroup()))
				filterByAuthor = "0";
			else 
				filterByAuthor = "1";

			return SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(this))
								.commentsMentions(sinceId, maxId, filterByAuthor, String.valueOf(AppSettings.getCommentCount()));
		}
		
		@Override
		protected boolean handleResult(com.m.ui.fragment.ARefreshFragment.RefreshMode mode, List<StatusComment> datas) {
			// 如果是重置或者刷新数据，加载数据大于分页大小，则清空之前的数据
			if (mode == RefreshMode.refresh)
				// 目前微博加载分页大小是默认大小
				if (datas.size() >= AppSettings.getCommentCount()) {
					getAdapter().setDatas(new ArrayList<StatusComment>());
				}

			return super.handleResult(mode, datas);
		}
		
		@Override
		protected void onSuccess(StatusComments result) {
			super.onSuccess(result);
			
			if (result == null)
				return;
			
			if (AppContext.getUnreadCount() != null && AppContext.getUnreadCount().getMention_cmt() > 0) {
				requestDataDelay(1000);
				
				// fuck sina
				AppContext.getUnreadCount().setMention_cmt(0);
				
				BizFragment.getBizFragment(MentionCommentsFragment.this).remindSetCount(RemindType.mention_cmt);
			}
		}
		
		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			
			showMessage(exception.getMessage());
		}
		
	}

	@Override
	public String getLastReadKey() {
		return AisenUtil.getUserKey(mGroupBean.getGroup(), loggedIn);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.removeGroup(R.id.timeline);
		if (getActivity() instanceof MainActivity) 
			inflater.inflate(R.menu.refresh_comments, menu);
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
			requestData(RefreshMode.reset);
		
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
