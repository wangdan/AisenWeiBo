package org.aisen.weibo.sina.ui.fragment.search;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.activity.profile.UserProfileActivity;
import org.sina.android.SinaSDK;
import org.sina.android.bean.SuggestionsUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.m.common.context.GlobalContext;
import com.m.common.utils.ActivityHelper;
import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.task.TaskException;
import com.m.ui.fragment.AListFragment;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * 搜索建议用户<br/>
 * 联想搜索的数据范围是：v用户、粉丝500以上的达人、粉丝600以上的普通用户。
 * 
 * @author wangdan
 *
 */
public class SearchUsersFragment extends AListFragment<SuggestionsUser, SuggestionsUser[]>
									implements OnItemClickListener, OnQueryTextListener, OnActionExpandListener {

	public static void launch(Activity from) {
		FragmentContainerActivity.launch(from, SearchUsersFragment.class, null);
	}
	
	@ViewInject(id = R.id.progress)
	SmoothProgressBar mSmoothProgressBar;
	
	private SearchUsersTask mTask;
	
	private MenuItem searchMenu;
	private SearchView searchView;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_search_users;
	}
	
	public int setTheme() {
		return R.style.BaseTheme_DarkActionBar;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		getRefreshView().setOnItemClickListener(this);
		
		setHasOptionsMenu(true);
		
		getActivity().getActionBar().setTitle(R.string.title_search_user);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mSmoothProgressBar.setIndeterminate(true);
		
		if (ActivityHelper.getInstance().getBooleanShareData("showSearchHint", true)) {
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					if (getActivity() != null) {
						new AlertDialog.Builder(getActivity()).setTitle(R.string.search_search_remind)
											.setCancelable(false)
											.setMessage(R.string.search_search_fuck)
											.setNegativeButton(R.string.donnot_remind, new DialogInterface.OnClickListener() {

												@Override
												public void onClick(DialogInterface dialog, int which) {
													ActivityHelper.getInstance().putBooleanShareData("showSearchHint", false);
												}
												
											})
											.setPositiveButton(R.string.i_know, null)
											.show();
					}
				}
			}, 1000);
		}
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.search_friend, menu);
		
		searchMenu = menu.findItem(R.id.search);
		searchMenu.expandActionView();
		searchMenu.setOnActionExpandListener(this);
		searchView = (SearchView) searchMenu.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint(getString(R.string.hint_user_name));
        try {
        	Field textField = searchView.getClass().getDeclaredField("mQueryTextView");
        	textField.setAccessible(true);
        	TextView txt = (TextView) textField.get(searchView);
        	txt.setHintTextColor(Color.parseColor("#88ffffff"));
		} catch (Exception e) {
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.search) {
			if (searchMenu.isActionViewExpanded())
				searchMenu.collapseActionView();
			else 
				searchMenu.expandActionView();
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		query(query);
		
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		query(newText);
		
		return true;
	}

	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		return true;
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {
		getActivity().finish();
		
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
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
			
			if (TextUtils.isEmpty(params[0]))
				return new SuggestionsUser[0];
			
			return SinaSDK.getInstance(AppContext.getToken()).searchSuggestionsUsers(params[0], 100);
		}
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			mTask = null;
			mSmoothProgressBar.setVisibility(View.GONE);
		}
		
	}
	
}
