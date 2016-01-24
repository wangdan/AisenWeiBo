package org.aisen.weibo.sina.ui.fragment.friendship;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.AListSwipeRefreshFragment;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.android.ui.fragment.itemview.NormalItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.Friendship;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.paging.FriendshipPaging;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
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
												implements OnItemClickListener {

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
		return new NormalItemViewCreator<WeiBoUser>(R.layout.item_friendship) {

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
	protected void requestData(RefreshMode mode) {
		new FriendshipTask(mode == RefreshMode.refresh ? RefreshMode.reset : mode).execute();
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
			super(itemView);
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
			
			if (result == null)
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
        ABaseFragment aFragment = (ABaseFragment) getActivity().getFragmentManager().findFragmentByTag("MainFragment");
        if (aFragment instanceof ATabsFragment) {
            @SuppressWarnings("rawtypes")
			ATabsFragment tabTitlePagerFragment = (ATabsFragment) aFragment;
            if (tabTitlePagerFragment.getCurrentFragment() == this) {
                requestDataDelay(200);

                ((ListView) getRefreshView()).setSelectionFromTop(0, 0);

                return true;
            }
            else
                return false;
        }

        if (getActivity() instanceof SinaCommonActivity) {
            @SuppressWarnings("rawtypes")
			ATabsFragment aTabTitlePagerFragment = (ATabsFragment) getActivity().getFragmentManager().findFragmentByTag(SinaCommonActivity.FRAGMENT_TAG);
            if (aTabTitlePagerFragment.getCurrentFragment() == this) {
                requestDataDelay(200);

                ((ListView) getRefreshView()).setSelectionFromTop(0, 0);

                return true;
            }
            else
                return false;
        }

        return super.onToolbarDoubleClick();
    }

	abstract Friendship getFriendship(@SuppressWarnings("rawtypes") WorkTask task, RefreshMode mode,
											String previousPage, String nextPage,
											Token extraToken, Void... params) throws TaskException;
	
	abstract String acTitle();

}
