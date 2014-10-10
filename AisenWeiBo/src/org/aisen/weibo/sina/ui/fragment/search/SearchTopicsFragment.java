package org.aisen.weibo.sina.ui.fragment.search;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.fragment.search.SearchHistoryFragment.OnSearchItemClicked;
import org.aisen.weibo.sina.ui.fragment.search.SearchHistoryFragment.Type;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;
import org.sina.android.SinaSDK;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.StatusContents;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.m.support.Inject.ViewInject;
import com.m.support.paging.IPaging;
import com.m.support.paging.PageIndexPaging;
import com.m.support.task.TaskException;
import com.m.ui.activity.AViewpagerActivity;
import com.m.ui.fragment.ABaseFragment;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * 搜索某话题下的微博
 * 
 * @author wangdan
 *
 */
public class SearchTopicsFragment extends ATimelineFragment
									implements OnQueryTextListener, OnSearchItemClicked{

	public static ABaseFragment newInstance() {
		SearchTopicsFragment fragment = new SearchTopicsFragment();
		
		fragment.setArguments(new Bundle());
		
		return fragment;
	}
	
	@ViewInject(id = R.id.topicsprogress)
	SmoothProgressBar mSmoothProgressBar;
	@ViewInject(id = R.id.searchView)
	SearchView searchView;
	@ViewInject(id = R.id.layTopicHistory)
	View layHistory;
	@ViewInject(id = R.id.layEmpty)
	View layEmpty;
	@ViewInject(id = R.id.layList)
	View layList;
	
	private SearchTopicsTask mTask;
	
	private String query;
	
	private SearchHistoryFragment mSearchHistoryFragment;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_search_topics;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		query = savedInstanceSate != null ? savedInstanceSate.getString("q") : "";
		
		mSmoothProgressBar.setIndeterminate(true);
		
		((TextView) layEmpty.findViewById(R.id.txtHint)).setText(R.string.empty_search_topics);
		
		searchView.onActionViewExpanded();
		searchView.setQueryHint(getString(R.string.hint_topics));
		searchView.setOnQueryTextListener(this);
		searchView.setOnCloseListener(new OnCloseListener() {
			
			@Override
			public boolean onClose() {
				if (TextUtils.isEmpty(searchView.getQuery().toString()))
					return true;
				
				return false;
			}
		});
		try {
			int left = getResources().getDimensionPixelSize(R.dimen.horizontal_gap);
			int right = getResources().getDimensionPixelSize(R.dimen.horizontal_margin);
        	Field textField = searchView.getClass().getDeclaredField("mQueryTextView");
        	textField.setAccessible(true);
        	TextView txt = (TextView) textField.get(searchView);
        	txt.setPadding(left, 0, right, 0);
		} catch (Exception e) {
		}
		
		if (savedInstanceSate == null) {
			mSearchHistoryFragment = SearchHistoryFragment.newInstance(Type.status);
			
			getFragmentManager().beginTransaction().add(R.id.layTopicHistory, mSearchHistoryFragment, "SearchTopicsHistoryFragment").commit();
		}
		else {
			mSearchHistoryFragment = (SearchHistoryFragment) getActivity().getFragmentManager().findFragmentByTag("SearchTopicsHistoryFragment");
		}
		mSearchHistoryFragment.setOnseaItemClicked(this);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString("q", query);
	}
	
	@Override
	public void onItemClicked(String query) {
		searchView.setQuery(query, true);
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		if (TextUtils.isEmpty(query.trim()))
			return true;
		
		query(query);
		
		SearchHistoryFragment.addQuery(Type.status, query);
		
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		layHistory.setVisibility(TextUtils.isEmpty(newText) ? View.VISIBLE : View.GONE);

		if (layHistory.getVisibility() == View.VISIBLE) {
			getAdapter().setDatasAndRefresh(new ArrayList<StatusContent>());
		}
		
		if (getAdapter().getDatas().size() == 0) {
			layList.setVisibility(View.GONE);
			layEmpty.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(newText))
			mSearchHistoryFragment.query();
		
		return true;
	}

	private void query(String q) {
		query = q;
		
		mSmoothProgressBar.setVisibility(View.VISIBLE);
		
		new SearchTopicsTask(RefreshMode.reset).execute();
	}
	
	@Override
	protected void requestData(RefreshMode mode) {
		if (getAdapter().getDatas().size() > 0)
			new SearchTopicsTask(mode == RefreshMode.refresh ? RefreshMode.reset : mode).execute();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected IPaging<StatusContent, StatusContents> configPaging() {
		return new PageIndexPaging("total_number");
	}
	
	@Override
	public boolean onAcUnusedDoubleClicked() {
		if (getActivity() instanceof AViewpagerActivity) {
			AViewpagerActivity activity = (AViewpagerActivity) getActivity();
			if (activity.getCurrentFragment() == this) {
				if (layHistory.getVisibility() == View.VISIBLE) {
					return mSearchHistoryFragment._onAcUnusedDoubleClicked();
				}
				
				return super.onAcUnusedDoubleClicked();
			}
			else 
				return false;
		}
		
		return super.onAcUnusedDoubleClicked();
	}
	
	class SearchTopicsTask extends TimelineTask {

		public SearchTopicsTask(RefreshMode mode) {
			super(mode);
			
			if (mTask != null)
				mTask.cancel(true);
			
			mTask = this;
		}
		
		@Override
		protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage,
				Void... params) throws TaskException {
			return SinaSDK.getInstance(AppContext.getToken()).searchTopics(nextPage, query, "30");
		}
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			BaiduAnalyzeUtils.onEvent("search_status", "搜索微博");
			
			mTask = null;
			mSmoothProgressBar.setVisibility(View.GONE);
			
			if (getAdapter().getDatas().size() == 0) {
				layEmpty.setVisibility(View.VISIBLE);
				
				layList.setVisibility(View.GONE);
			}
			else {
				layEmpty.setVisibility(View.GONE);
				layList.setVisibility(View.VISIBLE);
			}
		}
		
	}

}
