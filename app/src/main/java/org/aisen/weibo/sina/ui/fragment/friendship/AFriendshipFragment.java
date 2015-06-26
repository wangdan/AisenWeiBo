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
import org.aisen.android.component.container.FragmentContainerActivity;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.adapter.ABaseAdapter.AbstractItemView;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.ARefreshFragment;
import org.aisen.android.ui.fragment.AStripTabsFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.paging.FriendshipPagingProcessor;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.fragment.basic.AWeiboRefreshListFragment;
import org.aisen.weibo.sina.ui.fragment.basic.BizFragment;
import org.aisen.weibo.sina.ui.fragment.profile.UserProfilePagerFragment;
import org.aisen.weibo.sina.sinasdk.bean.AccessToken;
import org.aisen.weibo.sina.sinasdk.bean.Friendship;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import java.util.List;

/**
 * 朋友圈基类
 * 
 * @author wangdan
 * 
 */
public abstract class AFriendshipFragment extends AWeiboRefreshListFragment<WeiBoUser, Friendship>
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
		int header = ((ListView) getRefreshView()).getHeaderViewsCount();
		UserProfilePagerFragment.launch(getActivity(), getAdapterItems().get(position - header));
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
	protected void requestData(ARefreshFragment.RefreshMode mode) {
		new FriendshipTask(mode == ARefreshFragment.RefreshMode.refresh ? ARefreshFragment.RefreshMode.reset : mode).execute();
	}
	
	class FriendshipItemView extends AbstractItemView<WeiBoUser> {

		@ViewInject(id = R.id.imgPhoto)
		ImageView imgPhoto;
		@ViewInject(id = R.id.txtName)
		TextView txtName;
		@ViewInject(id = R.id.txtRemark)
		TextView txtRemark;
        @ViewInject(id = R.id.divider)
        View divider;
		
		@Override
		public int inflateViewId() {
			return R.layout.as_item_friendship;
		}

		@Override
		public void bindingData(View convertView, WeiBoUser data) {
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
            divider.setVisibility(getPosition() == getSize() - 1 ? View.GONE : View.VISIBLE);
		}
		
	}
	
	class FriendshipTask extends PagingTask<Void, Void, Friendship> {

		public FriendshipTask(ARefreshFragment.RefreshMode mode) {
			super("FriendshipTask", mode);
		}

		@Override
		protected List<WeiBoUser> parseResult(Friendship result) {
			return result.getUsers();
		}
		
		@Override
		protected Friendship workInBackground(ARefreshFragment.RefreshMode mode, String previousPage, String nextPage,
				Void... params) throws TaskException {
			if (mode != ARefreshFragment.RefreshMode.update)
				nextPage = "0";
			
			Token token = AppContext.getToken();
			// 是当前登录用户
			if (mUser.getIdstr().equals(AppContext.getUser().getIdstr())) {
                if (AppContext.getAccount().getAdvancedToken() != null)
                    token = AppContext.getAccount().getAdvancedToken();
			}
			else if (mUser.getScreen_name().equals(AppContext.getUser().getScreen_name())) {
                if (AppContext.getAccount().getAdvancedToken() != null)
                    token = AppContext.getAccount().getAdvancedToken();
			}
			else {
				if (AppContext.getAdvancedToken() != null) {
					AccessToken accessToken = AppContext.getAdvancedToken();
					
					token = new Token();
					token.setToken(accessToken.getToken());
					token.setSecret(accessToken.getSecret());
				}
			}
			
			Friendship resut = getFriendship(this, mode, previousPage, nextPage, token, params);
			if (resut.getNext_cursor() <= 0)
				resut.setNoMore(true);
			
			return resut;
		}
		
		@Override
		protected void onSuccess(Friendship result) {
			super.onSuccess(result);
			
			if (result == null)
				return;
			
			if (AFriendshipFragment.this instanceof FollowersFragment &&
					AppContext.getUnreadCount() != null && AppContext.getUnreadCount().getFollower() > 0) {
                if (result.isCache())
				    requestDataDelay(AppSettings.REQUEST_DATA_DELAY);

				if (getActivity() != null)
					BizFragment.getBizFragment(AFriendshipFragment.this).remindSetCount(BizFragment.RemindType.follower);
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
			if (mode != ARefreshFragment.RefreshMode.update)
				getRefreshView().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						((ListView) getRefreshView()).setSelectionFromTop(0, 0);
					}
				}, 20);
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
		return getAdapterItems().size();
	}
	
    @Override
    public boolean onToolbarDoubleClick() {
        ABaseFragment aFragment = (ABaseFragment) getActivity().getFragmentManager().findFragmentByTag("MainFragment");
        if (aFragment instanceof AStripTabsFragment) {
            @SuppressWarnings("rawtypes")
            AStripTabsFragment tabTitlePagerFragment = (AStripTabsFragment) aFragment;
            if (tabTitlePagerFragment.getCurrentFragment() == this) {
                requestDataDelay(200);

                ((ListView) getRefreshView()).setSelectionFromTop(0, 0);

                return true;
            }
            else
                return false;
        }

        if (getActivity() instanceof FragmentContainerActivity) {
            @SuppressWarnings("rawtypes")
            AStripTabsFragment aTabTitlePagerFragment = (AStripTabsFragment) getActivity().getFragmentManager().findFragmentByTag(FragmentContainerActivity.FRAGMENT_TAG);
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

	abstract Friendship getFriendship(@SuppressWarnings("rawtypes") WorkTask task, ARefreshFragment.RefreshMode mode,
											String previousPage, String nextPage,
											Token extraToken, Void... params) throws TaskException;
	
	abstract String acTitle();

}
