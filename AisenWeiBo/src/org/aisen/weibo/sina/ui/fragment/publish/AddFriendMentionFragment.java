package org.aisen.weibo.sina.ui.fragment.publish;

import java.lang.reflect.Field;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.db.FriendMentionDB;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.fragment.base.ARefreshProxyFragment;
import org.android.loader.BitmapLoader;
import org.sina.android.SinaSDK;
import org.sina.android.bean.Friendship;
import org.sina.android.bean.WeiBoUser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.m.common.utils.Logger;
import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.task.TaskException;
import com.m.ui.fragment.ABaseFragment;

/**
 * 提及好友
 * 
 * @author wangdan
 *
 */
public class AddFriendMentionFragment extends ARefreshProxyFragment<WeiBoUser, Friendship> 
											implements OnItemClickListener, OnQueryTextListener, OnActionExpandListener {

	public static void launch(ABaseFragment from, int requestCode) {
		FragmentContainerActivity.launchForResult(from, AddFriendMentionFragment.class, null, requestCode);
	}
	
	@ViewInject(id = R.id.laySearchSuggest)
	View laySearchSuggest;
	@ViewInject(id = R.id.layoutContent)
	View layContent;
	
	private int recentSize;	
	private SearchView searchView;
	private MenuItem searchMenu;
	
	private MentionSuggestionFragment suggestionFragment;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_add_friend_mention;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setTitle(R.string.publish_mention);

		getRefreshView().setOnItemClickListener(this);
		
		setHasOptionsMenu(true);
		
		if (savedInstanceSate == null) {
			suggestionFragment = MentionSuggestionFragment.newInstance();
			getFragmentManager().beginTransaction().add(R.id.laySearchSuggest, suggestionFragment, "MentionSuggestionFragment").commit();
		}
		else {
			suggestionFragment = (MentionSuggestionFragment) getFragmentManager().findFragmentByTag("MentionSuggestionFragment");
		}
		
		((TextView) findViewById(R.id.layoutEmpty).findViewById(R.id.txtLoadFailed)).setText(R.string.empty_friends);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position = position - ((ListView) getRefreshView()).getHeaderViewsCount();
		
		FriendMentionDB.addFriend(getAdapter().getDatas().get(position));
		
		Intent data = new Intent();
		data.putExtra("bean", getAdapter().getDatas().get(position));
		getActivity().setResult(Activity.RESULT_OK, data);
		getActivity().finish();
	}
	
	@Override
	protected AbstractItemView<WeiBoUser> newItemView() {
		return new FriendItemView();
	}

	@Override
	protected void requestData(RefreshMode mode) {
		new FriendTask(mode == RefreshMode.refresh ? RefreshMode.reset : mode).execute();
	}
	
	class FriendItemView extends AbstractItemView<WeiBoUser> {

		@ViewInject(id = R.id.imgPhoto)
		ImageView imgPhoto;
		@ViewInject(id = R.id.txtName)
		TextView txtName;
		@ViewInject(id = R.id.txtRemark)
		TextView txtRemark;
		@ViewInject(id = R.id.txtDivider)
		TextView txtDivider;
		
		@Override
		public int inflateViewId() {
			return R.layout.item_friend;
		}

		@Override
		public void bindingData(View convertView, WeiBoUser data) {
			BitmapLoader.getInstance().display(AddFriendMentionFragment.this, 
											AisenUtil.getUserPhoto(data), 
											imgPhoto, ImageConfigUtils.getLargePhotoConfig());
			
			txtName.setText(data.getScreen_name());
			if (!TextUtils.isEmpty(data.getRemark()))
				txtRemark.setText(data.getRemark());
			else
				txtRemark.setText("");
			
			if (recentSize > 0) {
				txtDivider.setVisibility(getPosition() == 0 || getPosition() == recentSize ? View.VISIBLE : View.GONE);
				if (getPosition() == 0)
					txtDivider.setText(R.string.publish_recent);
				else if (getPosition() == recentSize)
					txtDivider.setText(R.string.publish_all);
			}
			else {
				txtDivider.setVisibility(View.GONE);
			}
		}
		
	}
	
	class FriendTask extends PagingTask<Void, Void, Friendship> {

		public FriendTask(RefreshMode mode) {
			super("PagingTask", mode);
		}

		@Override
		protected List<WeiBoUser> parseResult(Friendship result) {
			return result.getUsers();
		}

		@Override
		protected Friendship workInBackground(RefreshMode mode, String previousPage, String nextPage,
				Void... params) throws TaskException {
			List<WeiBoUser> recentUsers = FriendMentionDB.getRecentMention("5");
			recentSize = recentUsers.size();
			
			Friendship friendship = SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(this))
												.friendshipsFriends(AppContext.getUser().getIdstr(), null, "0");
			
			if (recentUsers.size() > 0 && friendship != null && friendship.getUsers() != null) {
				recentUsers.addAll(friendship.getUsers());
				friendship.setUsers(recentUsers);
				friendship.setNoMore(true);
			}
			
			return friendship;
		}
		
		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			
			showMessage(exception.getMessage());
		}
		
	}
	
	@Override
	public boolean onBackClick() {
		if (searchMenu.isActionViewExpanded()) {
			searchMenu.collapseActionView();
			return true;
		}
		
		return super.onBackClick();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.search_friend, menu);
		
		searchMenu = menu.findItem(R.id.search);
		searchMenu.setOnActionExpandListener(this);
		searchView = (SearchView) searchMenu.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint(getString(R.string.publish_mention_hint));
        try {
        	Field textField = searchView.getClass().getDeclaredField("mQueryTextView");
        	textField.setAccessible(true);
        	TextView txt = (TextView) textField.get(searchView);
        	txt.setHintTextColor(Color.parseColor("#88ffffff"));
		} catch (Exception e) {
		}
		
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onQueryTextChange(String newText) {
		Logger.v("suggestion query ---> " + newText);
		
		suggestionFragment.query(newText);
		
		return true;
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		Logger.v("suggestion query ---> " + query);
		
		suggestionFragment.query(query);
		
		return true;
	}
	
	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		layContent.setVisibility(View.GONE);
		laySearchSuggest.setVisibility(View.VISIBLE);
		
		return true;
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {
		layContent.setVisibility(View.VISIBLE);
		laySearchSuggest.setVisibility(View.GONE);
		
		return true;
	}

}
