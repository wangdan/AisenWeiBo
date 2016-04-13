package org.aisen.weibo.sina.ui.fragment.friendship;

import android.os.Bundle;

import org.aisen.android.network.biz.ABizLogic;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Friendship;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

/**
 * 用户粉丝
 * 
 * @author wangdan
 *
 */
public class FollowersFragment extends AFriendshipFragment {

	public static ABaseFragment newInstance(WeiBoUser user) {
		ABaseFragment fragment = new FollowersFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("user", user);
		args.putBoolean("launch", false);
		fragment.setArguments(args);
		
		return fragment;
	}

	@Override
	protected void setupRefreshConfig(RefreshConfig config) {
		super.setupRefreshConfig(config);

		config.emptyHint = getString(R.string.empty_followers);
	}

	@Override
	Friendship getFriendship(@SuppressWarnings("rawtypes") WorkTask task, RefreshMode mode,
									String previousPage, String nextPage, Token token, Void... params)
			throws TaskException {
		ABizLogic.CacheMode cacheMode = getUser().getIdstr().equals(AppContext.getAccount().getUser().getIdstr()) ? getTaskCacheMode(task) : ABizLogic.CacheMode.disable;
		
		return SinaSDK.getInstance(token, cacheMode)
											.friendshipsFollowers(null, getUser().getScreen_name(), nextPage);
	}
	
	@Override
	String acTitle() {
		return getString(R.string.friendship_my_followers);
	}

}
