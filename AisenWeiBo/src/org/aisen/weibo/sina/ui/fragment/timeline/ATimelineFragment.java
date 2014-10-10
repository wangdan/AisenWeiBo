package org.aisen.weibo.sina.ui.fragment.timeline;

import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.TimelineGroupBean;
import org.aisen.weibo.sina.support.paging.TimelinePagingProcessor;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.ui.activity.guide.MainGuideActivity;
import org.aisen.weibo.sina.ui.activity.main.MainActivity;
import org.aisen.weibo.sina.ui.component.TimelineItemView;
import org.aisen.weibo.sina.ui.fragment.base.ARefreshProxyFragment;
import org.aisen.weibo.sina.ui.fragment.comment.TimelineCommentsFragment;
import org.aisen.weibo.sina.ui.widget.TimelinePicsView;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.StatusContents;
import org.sina.android.bean.WeiBoUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.m.common.utils.Logger;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.paging.IPaging;
import com.m.support.task.TaskException;
import com.m.ui.activity.AViewpagerActivity;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.ATabTitlePagerFragment;

/**
 * 微博列表基类
 * 
 * @author wangdan
 * 
 */
public abstract class ATimelineFragment extends ARefreshProxyFragment<StatusContent, StatusContents> implements OnItemClickListener {

	private TimelineGroupBean mGroupBean;

	private WeiBoUser loggedIn;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_timeline;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceSate) {
		mGroupBean = savedInstanceSate == null ? (TimelineGroupBean) getArguments().getSerializable("bean") 
				   : (TimelineGroupBean) savedInstanceSate.getSerializable("bean");
		
		super.onCreate(savedInstanceSate);
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		loggedIn = AppContext.getUser();

		getRefreshView().setOnItemClickListener(this);

		setHasOptionsMenu(true);
	}
	
	@Override
	protected void config(RefreshConfig config) {
		super.config(config);
		
		config.savePosition = true;
		config.emptyLabel = getString(R.string.empty_status);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable("bean", mGroupBean);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ListView listView = (ListView) getRefreshView();
		int index = listView.getHeaderViewsCount();

		if (getAdapter().getDatas().size() > 0 && position >= index) {
			StatusContent status = getAdapter().getDatas().get(position - index);

			TimelineCommentsFragment.launch(this, status);
		}
	}

	@Override
	protected IPaging<StatusContent, StatusContents> configPaging() {
		return new TimelinePagingProcessor();
	}

	@Override
	protected AbstractItemView<StatusContent> newItemView() {
		return new TimelineItemView(this, true);
	}

	public abstract class TimelineTask extends PagingTask<Void, Void, StatusContents> {

		public TimelineTask(RefreshMode mode) {
			super("TimelineTask", mode);
		}

		@Override
		protected List<StatusContent> parseResult(StatusContents result) {
			return result.getStatuses();
		}
		
		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			
			showMessage(exception.getMessage());
		}
		
		@Override
		protected boolean handleResult(RefreshMode mode, List<StatusContent> datas) {
			// 如果是重置或者刷新数据，加载数据大于分页大小，则清空之前的数据
			if (mode == RefreshMode.refresh) {
				// 目前微博加载分页大小是默认大小
				if (datas.size() >= AppSettings.getTimelineCount()) {
					getAdapter().setDatas(new ArrayList<StatusContent>());
					return true;
				}
			}
			
			return super.handleResult(mode, datas);
		}

		@Override
		protected void onSuccess(StatusContents result) {
			if (result == null)
				return;
			super.onSuccess(result);

			ListView listView = (ListView) getRefreshView();
			// 2014-08-27 当刷新列表时，返回最上面
			if (mode == RefreshMode.reset && getTaskCount(getTaskId()) > 1)
				listView.setSelectionFromTop(0, 0);
			
			// 2014-09-11 显示首页引导
			if (getActivity() != null && getActivity() instanceof MainActivity) {
				MainActivity mainActivity = (MainActivity) getActivity();
				if (!mainActivity.isDrawerOpened() && MainGuideActivity.canGuide()) {
					MainGuideActivity.launch(mainActivity);
				}
			}
		}

	}

	public TimelineGroupBean getGroup() {
		return mGroupBean;
	}

	static final int[] imageResArr = new int[] { R.id.img01, R.id.img02, R.id.img03, R.id.img04, R.id.img05, R.id.img06, R.id.img07, R.id.img08,
			R.id.img09, R.id.imgPhoto, R.id.imgRePhoto };

	@Override
	protected int[] recyleImageViewRes() {
		return imageResArr;
	}

	/**
	 * 如果当前的视图是Pager显示的视图，释放后会造成视图闪烁的情况出现
	 */
	@Override
	public void onMovedToScrapHeap(View view) {
		CharSequence current = getTabShowingTitle();
		if (!TextUtils.isEmpty(current) && mGroupBean.getTitle().equals(current.toString())) {
			Logger.v("当前展示的是" + current + "，不释放视图");
			return;
		}

		super.onMovedToScrapHeap(view);
	}

	/**
	 * 如果Pager显示的不是当前视图，则不刷新视图
	 */
	@Override
	public void refreshUI() {
		CharSequence current = getTabShowingTitle();
		if (!TextUtils.isEmpty(current) && !mGroupBean.getTitle().equals(current.toString())) {
			Logger.v("展示的是" + current + ", 当前是" + mGroupBean.getTitle() + "，不刷新视图");
			return;
		}

		super.refreshUI();
	}

	private String getTabShowingTitle() {
		ABaseFragment aFragment = (ABaseFragment) getActivity().getFragmentManager().findFragmentByTag("MainFragment");
		if (aFragment instanceof ATabTitlePagerFragment) {
			@SuppressWarnings("rawtypes")
			ATabTitlePagerFragment fragment = (ATabTitlePagerFragment) aFragment;
			if (fragment != null && fragment.getViewPagerAdapter() != null && fragment.getViewPager() != null) {
				CharSequence current = fragment.getViewPagerAdapter().getPageTitle(fragment.getViewPager().getCurrentItem());
				return current.toString();
			}
		}

		return null;
	}

	@Override
	protected boolean releaseView(View view) {
		TimelinePicsView picsView = (TimelinePicsView) view.findViewById(R.id.layPicturs);
		if (picsView != null)
			picsView.release();

		return super.releaseView(view);
	}

	@Override
	public String getLastReadKey() {
		if (getGroup() != null)
			return AisenUtil.getUserKey(getGroup().getType(), loggedIn);

		return null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.removeGroup(R.id.timeline);
		if (getActivity() instanceof MainActivity) {
			inflater.inflate(R.menu.refresh_timeline, menu);
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
		// 刷新微博列表
		if (item.getItemId() == R.id.refresh) {
			if (!setRefreshing())
				requestData(RefreshMode.reset);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// 微博被删除了
		if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
			String statusId = data.getStringExtra("status");
			if (!TextUtils.isEmpty(statusId)) {
				for (int i = 0; i < getAdapter().getDatas().size(); i++) {
					if (statusId.equals(getAdapter().getDatas().get(i).getId())) {
						getAdapter().removeItem(i);

						getAdapter().notifyDataSetChanged();
						break;
					}
				}
			}
		}
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
