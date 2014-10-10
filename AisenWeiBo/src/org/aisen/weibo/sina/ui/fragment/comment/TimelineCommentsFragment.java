package org.aisen.weibo.sina.ui.fragment.comment;

import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.paging.CommentsPagingProcessor;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.ui.activity.comment.TimelineCommentsActivity;
import org.aisen.weibo.sina.ui.component.CommentItemView;
import org.aisen.weibo.sina.ui.component.TimelineItemView;
import org.aisen.weibo.sina.ui.fragment.base.ARefreshProxyFragment;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.sina.android.SinaSDK;
import org.sina.android.bean.StatusComment;
import org.sina.android.bean.StatusComments;
import org.sina.android.bean.StatusContent;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.m.common.params.Params;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.paging.IPaging;
import com.m.support.task.TaskException;
import com.m.ui.activity.AViewpagerActivity;
import com.m.ui.fragment.ABaseFragment;

/**
 * 微博评论界面<br/>
 * 普通模式展示
 * 
 * @author wangdan
 *
 */
public class TimelineCommentsFragment extends ARefreshProxyFragment<StatusComment, StatusComments>
											implements OnItemClickListener {

	public static void launch(ABaseFragment from, StatusContent status) {
		TimelineCommentsActivity.launch(from, status);
	}
	
	public static ABaseFragment newInstance(StatusContent status) {
		TimelineCommentsFragment fragment = new TimelineCommentsFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("bean", status);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	private StatusContent mStatusContent;
	
	private View headerView;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_timeline_comments;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceState) {
		super.layoutInit(inflater, savedInstanceState);
		
		ListView listView = (ListView) getRefreshView();
		TimelineItemView timelineItem = new TimelineItemView(this, true);
		View view = View.inflate(getActivity(), timelineItem.inflateViewId(), null);
		timelineItem.bindingView(view);
		view.setTag(timelineItem);
		listView.addHeaderView(view);
		
		headerView = view;
		
		mStatusContent = savedInstanceState == null ? (StatusContent) getArguments().getSerializable("bean")
													: (StatusContent) savedInstanceState.getSerializable("bean");
		
		timelineItem.bindingData(headerView, mStatusContent);
		
		listView.setOnItemClickListener(this);
		
		if (savedInstanceState == null) {
			listView.setSelectionFromTop(listView.getFooterViewsCount(), 0);
		}
	}
	
	public int refreshLayoutInfo() {
		return R.id.layList;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("bean", mStatusContent);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ListView listView = (ListView) getRefreshView();
		position -= listView.getHeaderViewsCount();
		BizFragment.getBizFragment(this).replyComment(mStatusContent, getAdapter().getDatas().get(position));
	}
	
	@Override
	protected IPaging<StatusComment, StatusComments> configPaging() {
		return new CommentsPagingProcessor();
	}

	@Override
	protected AbstractItemView<StatusComment> newItemView() {
		return new CommentItemView(this, mStatusContent);
	}

	@Override
	protected void requestData(RefreshMode mode) {
		new TimelineCommentTask(mode).execute();
	}
	
	class TimelineCommentTask extends PagingTask<Void, Void, StatusComments> {

		public TimelineCommentTask(RefreshMode mode) {
			super("TimelineCommentTask", mode);
		}
		
		@Override
		protected StatusComments workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... p) throws TaskException {
			Params params = new Params();

			if ((mode == RefreshMode.refresh || mode == RefreshMode.reset) && !TextUtils.isEmpty(previousPage))
				params.addParameter("since_id", previousPage);

			if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
				params.addParameter("max_id", nextPage);

			params.addParameter("id", mStatusContent.getId());
			
			params.addParameter("count", String.valueOf(AppSettings.getCommentCount()));
			
			return SinaSDK.getInstance(AppContext.getToken()).commentsShow(params);
		}

		@Override
		protected List<StatusComment> parseResult(StatusComments result) {
			return result.getComments();
		}

		@Override
		protected boolean handleResult(RefreshMode mode, List<StatusComment> datas) {
			// 如果是重置或者刷新数据，加载数据大于分页大小，则清空之前的数据
			if (mode == RefreshMode.reset || mode == RefreshMode.refresh)
				// 目前微博加载分页大小是默认大小
				if (datas.size() >= AppSettings.getCommentCount()) {
					getAdapter().setDatas(new ArrayList<StatusComment>());
					return true;
				}

			return super.handleResult(mode, datas);
		}
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			ListView listView = (ListView) getRefreshView();
			// 第一次加载完数据，将评论置顶
			if (mode == RefreshMode.reset)
				listView.setSelectionFromTop(listView.getFooterViewsCount(), 0);
		}
		
		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			
			showMessage(exception.getMessage());
		}
		
	}
	
	@Override
	public boolean onAcUnusedDoubleClicked() {
		if (getActivity() instanceof AViewpagerActivity) {
			AViewpagerActivity activity = (AViewpagerActivity) getActivity();
			if (activity.getCurrentFragment() == this)
				return super.onAcUnusedDoubleClicked();
			else 
				return false;
		}
			
		return super.onAcUnusedDoubleClicked();
	}

}
