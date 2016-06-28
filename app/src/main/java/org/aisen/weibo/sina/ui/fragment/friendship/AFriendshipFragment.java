package org.aisen.weibo.sina.ui.fragment.friendship;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.AListSwipeRefreshFragment;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.Friendship;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.paging.FriendshipPaging;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.profile.ProfilePagerFragment;

import java.util.List;

/**
 * 朋友圈基类
 * 
 * @author wangdan
 * 
 */
public abstract class AFriendshipFragment extends AListSwipeRefreshFragment<WeiBoUser, Friendship>
												implements OnItemClickListener, ATabsFragment.ITabInitData {

	private WeiBoUser mUser;
	
	private boolean launch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = savedInstanceState == null ? (WeiBoUser) getArguments().getSerializable("user")
                                           : (WeiBoUser) savedInstanceState.getSerializable("user");
        launch = savedInstanceState != null ? savedInstanceState.getBoolean("launch")
                                            : getArguments().getBoolean("launch", true);
    }

    @Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);

		if (launch) {
			BaseActivity activity = (BaseActivity) getActivity();
			activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			activity.getSupportActionBar().setTitle(acTitle());
			activity.getSupportActionBar().setSubtitle(AisenUtils.getUserScreenName(getUser()));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("user", mUser);
		outState.putBoolean("launch", launch);
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		int header = getRefreshView().getHeaderViewsCount();
		ProfilePagerFragment.launch(getActivity(), getAdapterItems().get(position - header));
	}

	@Override
	public IItemViewCreator<WeiBoUser> configItemViewCreator() {
		return new IItemViewCreator<WeiBoUser>() {

			@Override
			public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
				return inflater.inflate(R.layout.item_friendship, parent, false);
			}

			@Override
			public IITemView<WeiBoUser> newItemView(View convertView, int viewType) {
				return new FriendshipItemView(convertView);
			}

		};
	}

	@Override
	protected IPaging<WeiBoUser, Friendship> newPaging() {
		return new FriendshipPaging();
	}

	@Override
	public void requestData(RefreshMode mode) {
		boolean load = true;

		// 如果还没有加载过数据，切且显示的是当前的页面
		if (getTaskCount(PAGING_TASK_ID) == 0) {
			load = AisenUtils.checkTabsFragmentCanRequestData(this);
		}

		if (load) {
			new FriendshipTask(mode == RefreshMode.refresh ? RefreshMode.reset : mode).execute();
		}
	}

	@Override
	public void onTabRequestData() {
		// 如果还没有加载过数据，就开始加载
		if (getTaskCount(PAGING_TASK_ID) == 0) {
			requestData(RefreshMode.reset);
		}
	}

	class FriendshipItemView extends ARecycleViewItemView<WeiBoUser> {

		@ViewInject(id = R.id.imgPhoto)
		ImageView imgPhoto;
		@ViewInject(id = R.id.txtName)
		TextView txtName;
		@ViewInject(id = R.id.txtRemark)
		TextView txtRemark;
		@ViewInject(id = R.id.divider)
		View divider;

		public FriendshipItemView(View itemView) {
			super(getActivity(), itemView);
		}

		@Override
		public void onBindData(View convertView, WeiBoUser data, int position) {
			BitmapLoader.getInstance().display(AFriendshipFragment.this,
					AisenUtils.getUserPhoto(data), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
			String name = data.getScreen_name();
			if (!TextUtils.isEmpty(data.getRemark()))
				name = String.format("%s(%s)", name, data.getRemark());
			txtName.setText(name);
			txtRemark.setVisibility(View.VISIBLE);
			if (data.getStatus() != null)
				txtRemark.setText(data.getStatus().getText());
			else if (!TextUtils.isEmpty(data.getDescription()))
				txtRemark.setText(data.getDescription());
			else {
				txtRemark.setVisibility(View.GONE);
				txtRemark.setText("");
			}
			divider.setVisibility(itemPosition() == getSize() - 1 ? View.GONE : View.VISIBLE);
		}

	}
	
	class FriendshipTask extends APagingTask<Void, Void, Friendship> {

		public FriendshipTask(RefreshMode mode) {
			super(mode);
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
			
			Token token = AppContext.getAccount().getAdvancedToken();
			if (token == null) {
				token = AppContext.getAccount().getAccessToken();
			}
			Friendship resut = getFriendship(this, mode, previousPage, nextPage, token, params);
			if (resut.getNext_cursor() <= 0)
				resut.setEndPaging(true);
			
			return resut;
		}
		
		@Override
		protected void onSuccess(Friendship result) {
			super.onSuccess(result);
			
			if (result == null || isDestory())
				return;
			
			if (AFriendshipFragment.this instanceof FollowersFragment &&
					AppContext.getAccount().getUnreadCount() != null && AppContext.getAccount().getUnreadCount().getFollower() > 0) {
                if (result.fromCache())
				    requestDataDelay(AppSettings.REQUEST_DATA_DELAY);

				if (getActivity() != null)
					BizFragment.createBizFragment(AFriendshipFragment.this).remindSetCount(BizFragment.RemindType.follower);
			}
		}
		
		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			
			showMessage(exception.getMessage());
		}
		
		@Override
		protected void onFinished() {
			super.onFinished();
			if (mode != RefreshMode.update)
				getRefreshView().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						getRefreshView().setSelectionFromTop(0, 0);
					}
				}, 20);
		}
		
	}
	
	WeiBoUser getUser() {
		return mUser;
	}
	

	/**
	 * Pager页面，用来设置actionbar的subtitle的提示信息
	 * 
	 * @return
	 */
	int getSize() {
		return getAdapterItems().size();
	}
	
    @Override
    public boolean onToolbarDoubleClick() {
		if (AisenUtils.checkTabsFragmentCanRequestData(this)) {
			requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);
			getRefreshView().setSelectionFromTop(0, 0);

			return true;
		}

		return false;
    }

	abstract Friendship getFriendship(@SuppressWarnings("rawtypes") WorkTask task, RefreshMode mode,
											String previousPage, String nextPage,
											Token extraToken, Void... params) throws TaskException;
	
	abstract String acTitle();

}
