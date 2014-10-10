package org.aisen.weibo.sina.ui.fragment.search;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.profile.UserProfileActivity;
import org.aisen.weibo.sina.ui.fragment.search.SearchHistoryFragment.OnSearchItemClicked;
import org.aisen.weibo.sina.ui.fragment.search.SearchHistoryFragment.Type;
import org.sina.android.SinaSDK;
import org.sina.android.bean.SuggestionsUser;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.m.common.context.GlobalContext;
import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.task.TaskException;
import com.m.ui.activity.AViewpagerActivity;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.AListFragment;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * 搜索用户
 * 
 * @author wangdan
 *
 */
public class SearchUsers_v2Fragment extends AListFragment<SuggestionsUser, SuggestionsUser[]> 
										implements OnQueryTextListener, OnItemClickListener, OnSearchItemClicked {

	public static ABaseFragment newInstance() {
		return new SearchUsers_v2Fragment();
	}
	
	@ViewInject(id = R.id.progress)
	SmoothProgressBar mSmoothProgressBar;
	@ViewInject(id = R.id.searchView)
	SearchView searchView;
	@ViewInject(id = R.id.layUserHistory)
	View layHistory;
	@ViewInject(id = R.id.layEmpty)
	View layEmpty;
	@ViewInject(id = R.id.layList)
	View layList;
	
	private SearchUsersTask mTask;
	
	private SearchHistoryFragment mSearchHistoryFragment;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_search_users_v2;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		mSmoothProgressBar.setIndeterminate(true);
		
		searchView.onActionViewExpanded();
		searchView.setQueryHint(getString(R.string.hint_user_name));
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
		
		getRefreshView().setOnItemClickListener(this);
		
		if (savedInstanceSate == null) {
			mSearchHistoryFragment = SearchHistoryFragment.newInstance(Type.user);
			
			getFragmentManager().beginTransaction().add(R.id.layUserHistory, mSearchHistoryFragment, "SearchUserHistoryFragment").commit();
		}
		else {
			mSearchHistoryFragment = (SearchHistoryFragment) getActivity().getFragmentManager().findFragmentByTag("SearchUserHistoryFragment");
		}
		mSearchHistoryFragment.setOnseaItemClicked(this);
	}
	
	@Override
	public void onItemClicked(String query) {
		searchView.setQuery(query, true);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		InputMethodManager im = (InputMethodManager) GlobalContext.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
		im.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
		
		UserProfileActivity.launch(getActivity(), getAdapter().getDatas().get(position).getScreen_name());
	}
	
	@Override
	protected AbstractItemView<SuggestionsUser> newItemView() {
		return new SearchUsersItemView();
	}

	@Override
	protected void requestData(RefreshMode mode) {
		
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		if (TextUtils.isEmpty(query.trim()))
			return true;
		
		query(query);
		
		SearchHistoryFragment.addQuery(Type.user, query);
		
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		layHistory.setVisibility(TextUtils.isEmpty(newText) ? View.VISIBLE : View.GONE);

		if (layHistory.getVisibility() == View.VISIBLE) {
			setItems(new ArrayList<SuggestionsUser>());
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
		new SearchUsersTask().execute(q);
	}
	
	class SearchUsersItemView extends AbstractItemView<SuggestionsUser> {

		@ViewInject(id = R.id.txtName)
		TextView txtName;
		
		@Override
		public int inflateViewId() {
			return R.layout.item_search_users;
		}

		@Override
		public void bindingData(View convertView, SuggestionsUser data) {
			txtName.setText(data.getScreen_name());
		}
		
	}
	
	class SearchUsersTask extends PagingTask<String, Void, SuggestionsUser[]> {

		public SearchUsersTask() {
			super("SearchUsersTask", RefreshMode.reset);
			if (mTask != null)
				mTask.cancel(true);
			
			mTask = this;
		}
		
		@Override
		protected void onPrepare() {
			super.onPrepare();
			
			mSmoothProgressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected List<SuggestionsUser> parseResult(SuggestionsUser[] result) {
			List<SuggestionsUser> list = new ArrayList<SuggestionsUser>();
			
			for (SuggestionsUser user : result)
				list.add(user);
			
			return list;
		}

		@Override
		protected SuggestionsUser[] workInBackground(RefreshMode mode, String previousPage, String nextPage,
				String... params) throws TaskException {
			return SinaSDK.getInstance(AppContext.getToken()).searchSuggestionsUsers(params[0], 100);
		}
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			BaiduAnalyzeUtils.onEvent("search_user", "搜索用户");
			
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

}
