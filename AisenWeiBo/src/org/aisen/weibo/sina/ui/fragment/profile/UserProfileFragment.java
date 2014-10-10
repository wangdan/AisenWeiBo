package org.aisen.weibo.sina.ui.fragment.profile;

import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment.OnCreateFriendshipCallback;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment.OnDestoryFollowerCallback;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment.OnDestoryFriendshipCallback;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment.OnModifyUserRemarkCallback;
import org.aisen.weibo.sina.ui.fragment.friendship.FriendshipPagerFragment;
import org.aisen.weibo.sina.ui.fragment.group.GroupSortFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;
import org.android.loader.BitmapLoader;
import org.sina.android.SinaSDK;
import org.sina.android.bean.FriendshipShow;
import org.sina.android.bean.Group;
import org.sina.android.bean.GroupListed;
import org.sina.android.bean.GroupMemberListed;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.StatusContents;
import org.sina.android.bean.WeiBoUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.m.common.params.Params;
import com.m.common.utils.ActivityHelper;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.activity.BaseActivity;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.utils.FragmentArgs;
import com.m.ui.utils.ViewUtils;

/**
 * 用户信息，用户微博列表
 * 
 * @author wangdan
 *
 */
public class UserProfileFragment extends ATimelineFragment 
									implements OnClickListener, OnModifyUserRemarkCallback,
												OnCreateFriendshipCallback, OnDestoryFriendshipCallback,
												OnDestoryFollowerCallback {

	public static void launch(Activity from, WeiBoUser user) {
		FragmentArgs args = new FragmentArgs();
		args.add("user", user);
		
		FragmentContainerActivity.launch(from, UserProfileFragment.class, args);
	}
	
	public static ABaseFragment newInstance() {
		UserProfileFragment fragment = new UserProfileFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("user", AppContext.getUser());
		fragment.setArguments(args);
		
		return fragment;
	}
	
	public static ABaseFragment newInstance(WeiBoUser searchResult) {
		UserProfileFragment fragment = new UserProfileFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("newUser", searchResult);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	private WeiBoUser mUser;
	private WeiBoUser mNewUser;

	private FriendshipShow mFriendship;
	private GroupMemberListed mGroupMemberListed;
	
	private View viewProfile;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_user_timeline;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		ListView listView = (ListView) getRefreshView();
		viewProfile = View.inflate(getActivity(), R.layout.lay_profile, null);
		listView.addHeaderView(viewProfile);
		
		super.layoutInit(inflater, savedInstanceSate);
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setTitle(R.string.title_user_profile);
		
		mNewUser = savedInstanceSate == null ? (WeiBoUser) getArguments().getSerializable("newUser")
											 : (WeiBoUser) savedInstanceSate.getSerializable("mNewUser");
		if (mNewUser != null) {
			mUser = mNewUser;
		}
		else {
			mUser = savedInstanceSate == null ? (WeiBoUser) getArguments().getSerializable("user")
					: (WeiBoUser) savedInstanceSate.getSerializable("user");
		}
		mFriendship = savedInstanceSate == null ? (FriendshipShow) getArguments().getSerializable("friendship")
				  								: (FriendshipShow) savedInstanceSate.getSerializable("friendship");
		mGroupMemberListed = savedInstanceSate == null ? (GroupMemberListed) getArguments().getSerializable("groupListed")
												 : (GroupMemberListed) savedInstanceSate.getSerializable("groupListed");
		
		viewProfile = getRootView();
		
		if (savedInstanceSate == null) {
			loadFriendship();
		}
		
		setHasOptionsMenu(true);
	}
	
	public int refreshLayoutInfo() {
		return R.id.layContent;
	}
	
	private void loadFriendship() {
		// 加载用户关系
		if (mUser != null && !mUser.getIdstr().equals(AppContext.getUser().getIdstr())) {
			new FriendshipTask().execute();
		}
	}
	
	@Override
	protected void config(RefreshConfig config) {
		super.config(config);
		
		config.savePosition = false;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("mNewUser", mNewUser);
		outState.putSerializable("user", mUser);
		outState.putSerializable("friendship", mFriendship);
		outState.putSerializable("groupListed", mGroupMemberListed);
	}
	
	private void setProfileView() {
		ImageView imgPhoto = (ImageView) viewProfile.findViewById(R.id.imgPhoto);
		BitmapLoader.getInstance().display(this, AisenUtil.getUserPhoto(mUser), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
		
		TextView txtName = (TextView) viewProfile.findViewById(R.id.txtName);
		txtName.setText(mUser.getName());
		// fuck 2014-09-04 当名字过长大于6个字时，截取部分文字
		int maxLength = AisenUtil.getStrLength("当名字过长大于个");
		if (AisenUtil.getStrLength(mUser.getName()) > maxLength) {
			StringBuffer sb = new StringBuffer();
			int index = 0;
			while (AisenUtil.getStrLength(sb.toString()) < maxLength) {
				if (index >= mUser.getName().length())
					break;
				
				sb.append(mUser.getName().charAt(index));
				index++;
			}
			sb.append("...");
			txtName.setText(sb.toString());
		}
		
		// 性别
		ImageView imgGender = (ImageView) viewProfile.findViewById(R.id.imgGender);
		imgGender.setVisibility(View.VISIBLE);
		if ("m".equals(mUser.getGender()))
			imgGender.setImageResource(R.drawable.list_male);
		else if ("f".equals(mUser.getGender()))
			imgGender.setImageResource(R.drawable.list_female);
		else
			imgGender.setVisibility(View.GONE);
		
		// 认证
		ImageView imgVerified = (ImageView) viewProfile.findViewById(R.id.imgVerified);
		AisenUtil.setImageVerified(imgVerified, mUser);
		
		// 简介
		TextView txtDesc = (TextView) viewProfile.findViewById(R.id.txtDesc);
		if (!TextUtils.isEmpty(mUser.getDescription()))
			txtDesc.setText(String.format(getString(R.string.profile_des_hint), mUser.getDescription()));
		else 
			txtDesc.setText(String.format(String.format(getString(R.string.profile_des_hint), getString(R.string.profile_des_none))));
		
		// 认证原因
		TextView txtVerifiedReason = (TextView) viewProfile.findViewById(R.id.txtVerifiedReason);
		txtVerifiedReason.setVisibility(!TextUtils.isEmpty(mUser.getVerified_reason()) ? View.VISIBLE : View.GONE);
		txtVerifiedReason.setText(String.format(getString(R.string.profile_ver_reason), mUser.getVerified_reason() + ""));
		
		// 备注
		TextView txtRemark = (TextView) viewProfile.findViewById(R.id.txtRemark);
		txtRemark.setVisibility(mFriendship == null || !mFriendship.getSource().getFollowing() ? View.GONE : View.VISIBLE);
		if (mFriendship != null) {
			try {
				BizFragment.getBizFragment(this).modifyUserMark(txtRemark, mUser, this);
			} catch (Exception e) {
			}
			if (!TextUtils.isEmpty(mUser.getRemark()))
				txtRemark.setText(String.format(getString(R.string.profile_remark_hint), mUser.getRemark()));
			else
				txtRemark.setText(R.string.profile_remark_none);
		}
		
		// 分组
		TextView txtGroups = (TextView) viewProfile.findViewById(R.id.txtGroups);
		txtGroups.setVisibility(mGroupMemberListed == null ? View.GONE : View.VISIBLE);
		txtGroups.setOnClickListener(this);
		if (mGroupMemberListed != null) {
			if (mGroupMemberListed.getLists().size() == 0) {
				txtGroups.setText(R.string.profile_group_none);
			}
			else {
				StringBuffer sb = new StringBuffer();
				for (GroupListed groupListed : mGroupMemberListed.getLists())
					sb.append(groupListed.getName()).append(" ");
				
				txtGroups.setText(String.format(getString(R.string.profile_group_hint), sb.toString().trim()));
			}
		}
		
		// 所在地
		TextView txtLocation = (TextView) viewProfile.findViewById(R.id.txtLocation);
		txtLocation.setText(mUser.getLocation());
		
		// 微博数
		TextView txtStatusCount = (TextView) viewProfile.findViewById(R.id.txtStatusCount);
		txtStatusCount.setText(AisenUtil.getCounter(mUser.getStatuses_count()));
		
		// 关注数
		viewProfile.findViewById(R.id.btnFriendCounter).setOnClickListener(this);
		TextView txtFriendCount = (TextView) viewProfile.findViewById(R.id.txtFriendCount);
		txtFriendCount.setText(AisenUtil.getCounter(mUser.getFriends_count()));
		
		// 粉丝数
		viewProfile.findViewById(R.id.btnFollowerCounter).setOnClickListener(this);
		TextView txtFollowerCount = (TextView) viewProfile.findViewById(R.id.txtFollowerCount);
		txtFollowerCount.setText(AisenUtil.getCounter(mUser.getFollowers_count()));
	}

	@Override
	protected void requestData(RefreshMode mode) {
		new UserTimelineTask(mode).execute();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		setProfileView();
		
		BaiduAnalyzeUtils.onPageStart("个人微博");
		
		// 分组发生改变
		if (ActivityHelper.getInstance().getBooleanShareData("ChanneSortHasChanged", false) || 
				ActivityHelper.getInstance().getBooleanShareData("offlineChanneChanged", false)) {
			setGroupList();
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.profile, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		MenuItem mentionItem = menu.findItem(R.id.mention);
		MenuItem createItem = menu.findItem(R.id.create);
		MenuItem destoryItem = menu.findItem(R.id.destory);
		MenuItem followerDestoryItem = menu.findItem(R.id.followDestory);
		
		if (mUser == null || AppContext.getUser().getIdstr().equals(mUser.getIdstr())) {
			mentionItem.setVisible(false);
			createItem.setVisible(false);
			destoryItem.setVisible(false);
			followerDestoryItem.setVisible(false);
		}
		else {
			mentionItem.setVisible(true);
			
			createItem.setVisible(mFriendship != null && !mFriendship.getSource().getFollowing());
			destoryItem.setVisible(mFriendship != null && mFriendship.getSource().getFollowing());
			
			followerDestoryItem.setVisible(mFriendship != null && mFriendship.getTarget().getFollowing());
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// @Ta
		if (item.getItemId() == R.id.mention) {
			BizFragment.getBizFragment(this).mentionUser(mUser);
		}
		// 关注
		else if (item.getItemId() == R.id.create) {
			BizFragment.getBizFragment(this).createFriendship(mUser, this);
		}
		// 取消关注
		else if (item.getItemId() == R.id.destory) {
			BizFragment.getBizFragment(this).destoryFriendship(mUser, this);
		}
		// 移除粉丝
		else if (item.getItemId() == R.id.followDestory) {
			BizFragment.getBizFragment(this).destoryFollower(mUser, this);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 设置分组信息
	 */
	AlertDialog groupDialog;
	private void setGroupList() {
		if (groupDialog != null && groupDialog.isShowing())
			groupDialog.dismiss();
		
		String[] items = new String[AppContext.getGroups().getLists().size()];
		final boolean[] checkedItems = new boolean[AppContext.getGroups().getLists().size()];
		final boolean[] editCheckedItems = new boolean[AppContext.getGroups().getLists().size()];
		 
		for (int i = 0; i < AppContext.getGroups().getLists().size(); i++) {
			Group group = AppContext.getGroups().getLists().get(i);
			
			items[i] = group.getName();
			checkedItems[i] = false;
			editCheckedItems[i] = false;
			for (GroupListed groupListed : mGroupMemberListed.getLists()) {
				if (groupListed.getIdstr().equals(group.getIdstr())) {
					checkedItems[i] = true;
					editCheckedItems[i] = true;
					break;
				}
			}
		}

		View customTitle = View.inflate(getActivity(), R.layout.lay_group_dialogtitle, null);
		customTitle.findViewById(R.id.btnSettings).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 设置分组
				GroupSortFragment.lanuch(getActivity());
			}
		});
		groupDialog = new AlertDialog.Builder(getActivity()).setCustomTitle(customTitle)
					.setMultiChoiceItems(items, editCheckedItems, new DialogInterface.OnMultiChoiceClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
							editCheckedItems[which] = isChecked;
						}
					})
					.setOnDismissListener(new DialogInterface.OnDismissListener() {
						
						@Override
						public void onDismiss(DialogInterface dialog) {
							groupDialog = null;
						}
					})
					.setNegativeButton(R.string.cancel, null)
					.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new SetGroupTask().execute(checkedItems, editCheckedItems);
						}
					})
					.show();
	}
	
	// 获取用户关系
	class FriendshipTask extends WorkTask<Void, Void, FriendshipShow> {

		@Override
		public FriendshipShow workInBackground(Void... params) throws TaskException {
			FriendshipShow friendshipShow = SinaSDK.getInstance(AppContext.getToken()).friendshipsShow(AppContext.getUser().getIdstr(), mUser.getIdstr());
			
			if (friendshipShow.getSource().getFollowing()) {
				GroupMemberListed[] result = SinaSDK.getInstance(AppContext.getToken()).friendshipGroupsListed(mUser.getIdstr());
				if (result != null && result.length > 0) {
					mGroupMemberListed = result[0];
				}
				else {
					mGroupMemberListed = new GroupMemberListed();
					mGroupMemberListed.setLists(new ArrayList<GroupListed>());
					mGroupMemberListed.setUid(mUser.getId());
				}
			}
			
			return friendshipShow;
		}
		
		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			
			// 如果界面没有被销毁，就加载
			if (getActivity() != null) {
				BaseActivity baseActivity = (BaseActivity) getActivity();
				if (!baseActivity.mIsDestoryed()) {
					new FriendshipTask().execute();
				}
			}
		}
		
		@Override
		protected void onSuccess(FriendshipShow result) {
			super.onSuccess(result);
			
			if (getActivity() != null) {
				mFriendship = result;
				
				getActivity().invalidateOptionsMenu();
				
				setProfileView();
			}
		}
		
	}
	
	// 设置分组
	class SetGroupTask extends WorkTask<boolean[], String, Boolean> {

		@Override
		protected void onPrepare() {
			super.onPrepare();
			
			ViewUtils.createNormalProgressDialog(getActivity(), getString(R.string.profile_group_update_loading)).show();
		}
		
		@Override
		public Boolean workInBackground(boolean[]... params) throws TaskException {
			final boolean[] checkedItems = params[0];
			final boolean[] editCheckedItems = params[1];
			mGroupMemberListed = new GroupMemberListed();
			mGroupMemberListed.setLists(new ArrayList<GroupListed>());
			for (int i = 0; i < editCheckedItems.length; i++) {
				Group group = AppContext.getGroups().getLists().get(i);
				GroupListed groupListed = new GroupListed();
				groupListed.setIdstr(group.getIdstr());
				groupListed.setName(group.getName());
				
				// 这个分组没有选中
				if (editCheckedItems[i]) {
					// 如果这个分组原来也是选中的，则不需要编辑
					if (checkedItems[i]) {
						mGroupMemberListed.getLists().add(groupListed);
					}
					// 如果这个分组原来没有选中，则添加
					else {
						// 添加好友到分组
						SinaSDK.getInstance(AppContext.getToken()).friendshipsGroupdMembersAdd(mUser.getIdstr(), group.getIdstr());
						publishProgress(String.format(getString(R.string.profile_add_to_group), group.getName()));
						
						mGroupMemberListed.getLists().add(groupListed);
					}
				}
				else {
					// 如果原来有这个分组，现在没有勾选，则删除
					if (checkedItems[i]) {
						// 从分组中删除好友
						SinaSDK.getInstance(AppContext.getToken()).friendshipsGroupdMembersDestory(mUser.getIdstr(), group.getIdstr());
						publishProgress(String.format(getString(R.string.profile_group_remvoe_friend), group.getName()));
					}
					// 原来没有勾选，现在也没有勾选，不做处理
				}
			}
			
			return true;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			
			if (getActivity() != null && values != null && values.length > 0)
				ViewUtils.updateNormalProgressDialog(values[0]);
		}
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			ViewUtils.dismissNormalProgressDialog();
			
			setProfileView();
		}
		
		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			
			showMessage(R.string.profile_edit_group_faild);
		}
		
		@Override
		protected void onSuccess(Boolean result) {
			super.onSuccess(result);
			
			showMessage(R.string.profile_edit_group_success);
		}
		
	}
	
	// 用户微博列表
	class UserTimelineTask extends TimelineTask {

		public UserTimelineTask(RefreshMode mode) {
			super(mode);
		}

		@Override
		protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage,
				Void... p) throws TaskException {
			Params params = new Params();

			if (mode == RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
				params.addParameter("since_id", previousPage);

			if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
				params.addParameter("max_id", nextPage);

//			params.addParameter("feature", feature);

			// 不管user_id字段传值什么，都返回登录用户的微博
			if (AppContext.getUser().getIdstr().equals(mUser.getIdstr())) {
				params.addParameter("user_id", mUser.getIdstr());
			}
			else {
				params.addParameter("screen_name", mUser.getScreen_name());
			}
			
			params.addParameter("count", String.valueOf(AppSettings.getTimelineCount()));
			
			StatusContents statusContents = SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(this)).statusesUserTimeLine(params);
			
			if (mNewUser == null && (mUser != null && !mUser.getIdstr().equals(AppContext.getUser().getIdstr()))) {
				mNewUser = SinaSDK.getInstance(AppContext.getToken()).userShow(null, mUser.getScreen_name());
				mUser = mNewUser;
				if (mNewUser.getStatus() != null)
					mUser.getStatus().setUser(mNewUser);
				if (mUser.getStatus() != null && statusContents != null 
						&& (statusContents.getStatuses() == null || statusContents.getStatuses().size() == 0)) {
					List<StatusContent> statusList = new ArrayList<StatusContent>();
					statusList.add(mNewUser.getStatus());
					statusContents.setStatuses(statusList);
				}
			}
			
			if (statusContents != null && statusContents.getStatuses() != null && statusContents.getStatuses().size() > 0) {
				for (StatusContent status : statusContents.getStatuses())
					status.setUser(mUser);
			}
			
			return statusContents;
		}
		
		@Override
		protected void onSuccess(StatusContents result) {
			super.onSuccess(result);

			if (result == null)
				return;
			
			setProfileView();
			getActivity().invalidateOptionsMenu();
			
			if (result != null && result.getStatuses().size() == 1 && getTaskCount(getTaskId()) == 1
					&& !ActivityHelper.getInstance().getBooleanShareData("donotRemindFuckSina", false)) {
				new AlertDialog.Builder(getActivity())
								.setMessage(R.string.profile_fuck_sina)
								.setPositiveButton(R.string.i_know, null)
								.setNegativeButton(R.string.donnot_remind, new DialogInterface.OnClickListener() {
									
																@Override
																public void onClick(DialogInterface dialog, int which) {
																	ActivityHelper.getInstance().putBooleanShareData("donotRemindFuckSina", true);
																}
								})
								.show();
			}
		}
		
	}
	
	@Override
	public void onClick(View v) {
		// 关注群
		if (v.getId() == R.id.btnFriendCounter) {
			FriendshipPagerFragment.launch(getActivity(), mUser, 0);
//			FriendsFragment.launch(getActivity(), mUser);
		}
		// 粉丝群
		else if (v.getId() == R.id.btnFollowerCounter) {
			FriendshipPagerFragment.launch(getActivity(), mUser, 1);
//			FollowersFragment.launch(getActivity(), mUser);
		}
		// 分组
		else if (v.getId() == R.id.txtGroups) {
			setGroupList();
		}
	}

	@Override
	public void onModifyUserRemark(String remark) {
		mUser.setRemark(remark);
			
		setProfileView();
		
		getAdapter().notifyDataSetChanged();
	}

	@Override
	public void onFriendshipCreated(WeiBoUser targetUser) {
		if (mFriendship != null)
			mFriendship.getSource().setFollowing(true);
		
		mUser.setRecentStatusId(targetUser.getRecentStatusId());
		if (mNewUser != null)
			mNewUser.setRecentStatusId(targetUser.getRecentStatusId());
		
		mGroupMemberListed = new GroupMemberListed();
		mGroupMemberListed.setLists(new ArrayList<GroupListed>());
		mGroupMemberListed.setUid(mUser.getIdstr());
		
		setProfileView();
		
		getActivity().invalidateOptionsMenu();
		
		getAdapter().notifyDataSetChanged();
	}

	@Override
	public void onFriendshipDestoryed(WeiBoUser targetUser) {
		if (mFriendship != null)
			mFriendship.getSource().setFollowing(false);
		
		mGroupMemberListed = null;
		
		mUser.setRemark("");
		if (mNewUser != null)
			mNewUser.setRemark("");
		
		setProfileView();
		
		getActivity().invalidateOptionsMenu();
		
		getAdapter().notifyDataSetChanged();
	}

	@Override
	public void onDestoryFollower(WeiBoUser user) {
		if (mFriendship != null)
			mFriendship.getTarget().setFollowing(false);
		
		getActivity().invalidateOptionsMenu();
	}
	
	@Override
	public String getLastReadKey() {
		if (AppContext.isLogedin())
			return String.format("UserProfile-%s", AppContext.getUser().getIdstr());
		
		return super.getLastReadKey();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("个人微博");
	}

}
