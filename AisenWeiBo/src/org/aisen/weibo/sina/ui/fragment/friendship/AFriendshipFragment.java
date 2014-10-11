package org.aisen.weibo.sina.ui.fragment.friendship;

import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.paging.FriendshipPagingProcessor;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.fragment.base.ARefreshProxyFragment;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment.RemindType;
import org.aisen.weibo.sina.ui.fragment.profile.UserProfileFragment;
import org.android.loader.BitmapLoader;
import org.sina.android.bean.Friendship;
import org.sina.android.bean.WeiBoUser;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.m.common.utils.Logger;
import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.paging.IPaging;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.ATabTitlePagerFragment;

/**
 * 朋友圈基类
 * 
 * @author wangdan
 * 
 */
public abstract class AFriendshipFragment extends ARefreshProxyFragment<WeiBoUser, Friendship>
												implements OnItemClickListener {

	private WeiBoUser mUser;
	
	private boolean launch;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_friendship;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);

		mUser = savedInstanceSate == null ? (WeiBoUser) getArguments().getSerializable("user")
										  : (WeiBoUser) savedInstanceSate.getSerializable("user");
		launch = savedInstanceSate != null ? savedInstanceSate.getBoolean("launch") 
										   : getArguments().getBoolean("launch", true);
		
		getRefreshView().setOnItemClickListener(this);
		
		if (launch) {
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
			getActivity().getActionBar().setTitle(acTitle());
			getActivity().getActionBar().setSubtitle(AisenUtil.getUserScreenName(getUser()));
		}
	}
	
	@Override
	protected void config(RefreshConfig config) {
		super.config(config);
		
		config.soundPlay = true;
		config.savePosition = false;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("user", mUser);
		outState.putBoolean("launch", launch);
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		int header = ((ListView) getRefreshView()).getHeaderViewsCount();
		UserProfileFragment.launch(getActivity(), getAdapter().getDatas().get(position - header));
	}

	@Override
	protected AbstractItemView<WeiBoUser> newItemView() {
		return new FriendshipItemView();
	}
	
	@Override
	protected IPaging<WeiBoUser, Friendship> configPaging() {
		return new FriendshipPagingProcessor();
	}

	@Override
	protected void requestData(RefreshMode mode) {
		new FriendshipTask(mode == RefreshMode.refresh ? RefreshMode.reset : mode).execute();
	}
	
	class FriendshipItemView extends AbstractItemView<WeiBoUser> {

		@ViewInject(id = R.id.imgPhoto)
		ImageView imgPhoto;
		@ViewInject(id = R.id.txtName)
		TextView txtName;
		@ViewInject(id = R.id.txtRemark)
		TextView txtRemark;
		
		@Override
		public int inflateViewId() {
			return R.layout.item_friendship;
		}

		@Override
		public void bindingData(View convertView, WeiBoUser data) {
			BitmapLoader.getInstance().display(AFriendshipFragment.this, 
							AisenUtil.getUserPhoto(data), imgPhoto, ImageConfigUtils.getPhotoConfig());
			txtName.setText(AisenUtil.getUserScreenName(data));
			if (!TextUtils.isEmpty(data.getRemark())) {
				txtRemark.setVisibility(View.VISIBLE);
				
				txtRemark.setText(data.getRemark());
			}
			else {
				txtRemark.setVisibility(View.GONE);
			}
		}
		
	}
	
	class FriendshipTask extends PagingTask<Void, Void, Friendship> {

		public FriendshipTask(RefreshMode mode) {
			super("FriendshipTask", mode);
		}

		@Override
		protected List<WeiBoUser> parseResult(Friendship result) {
			return result.getUsers();
		}
		
		@Override
		protected Friendship workInBackground(RefreshMode mode, String previousPage, String nextPage,
				Void... params) throws TaskException {
			if (mode != RefreshMode.update)
				nextPage = "0";
			
			Friendship resut = getFriendship(this, mode, previousPage, nextPage, params);
			if (resut.getNext_cursor() <= 0)
				resut.setNoMore(true);
			
			return resut;
		}
		
		@Override
		protected void onSuccess(Friendship result) {
			super.onSuccess(result);
			
			if (result == null)
				return;
			
			// 刷新actionbar
			if (getActivity() != null && result.getUsers() != null && result.getUsers().size() > 0) {
				Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(FriendshipPagerFragment.class.getName());
				if (fragment == null) 
					fragment = getActivity().getFragmentManager().findFragmentByTag("MainFragment");
				if (fragment != null && fragment instanceof FriendshipPagerFragment) {
					((FriendshipPagerFragment) fragment).setSubtitle();
				}
			}
			
			if (AFriendshipFragment.this instanceof FollowersFragment && 
					AppContext.getUnreadCount() != null && AppContext.getUnreadCount().getFollower() > 0) {
				Logger.w(String.format("有新的粉丝，刷新列表, group = %s", getLastReadKey() + ""));
				
				requestDataDelay(1000);
				
				// fuck sina，貌似这个接口也被封了
				AppContext.getUnreadCount().setFollower(0);
				
				BizFragment.getBizFragment(AFriendshipFragment.this).remindSetCount(RemindType.follower);
			}
		}
		
		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			
			showMessage(exception.getMessage());
		}
		
	}
	
	WeiBoUser getUser() {
		return mUser;
	}
	

	@Override
	public void onMovedToScrapHeap(View view) {
	}

	/**
	 * Pager页面，用来设置actionbar的subtitle的提示信息
	 * 
	 * @return
	 */
	int getSize() {
		return getAdapter().getDatas().size();
	}
	
	@Override
	public String getLastReadKey() {
		return acTitle();
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

		if (getActivity() instanceof FragmentContainerActivity) {
			@SuppressWarnings("rawtypes")
			ATabTitlePagerFragment aTabTitlePagerFragment = (ATabTitlePagerFragment) getActivity().getFragmentManager().findFragmentByTag(FriendshipPagerFragment.class.getName());
			if (aTabTitlePagerFragment.getCurrentFragment() == this)
				return super.onAcUnusedDoubleClicked();
			else 
				return false;
		}
			
		return super.onAcUnusedDoubleClicked();
	}
	
	abstract Friendship getFriendship(@SuppressWarnings("rawtypes") WorkTask task, RefreshMode mode, String previousPage, String nextPage,
			Void... params) throws TaskException;
	
	abstract String acTitle();
	
}
