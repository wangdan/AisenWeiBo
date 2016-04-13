package org.aisen.weibo.sina.ui.fragment.friendship;

import android.os.Bundle;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Friendship;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

/**
 * 用户的互粉
 * 
 * @author wangdan
 *
 */
public class BilateralFragment extends AFriendshipFragment {

	public static ABaseFragment newInstance(WeiBoUser user) {
		ABaseFragment fragment = new BilateralFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("user", user);
		args.putBoolean("launch", false);
		fragment.setArguments(args);
		
		return fragment;
	}

	@Override
	protected void setupRefreshConfig(RefreshConfig config) {
		super.setupRefreshConfig(config);

		config.emptyHint = getString(R.string.empty_bilateral);
	}

    @Override
	Friendship getFriendship(@SuppressWarnings("rawtypes") WorkTask task, RefreshMode mode, String previousPage,
								String nextPage, Token token, Void... params)
			throws TaskException {
		if (getUser() != null) {
			return SinaSDK.getInstance(token, getTaskCacheMode(task)).friendshipsBilateral(getUser().getIdstr(), nextPage);
		}

		throw new TaskException(GlobalContext.getInstance().getResources().getString(R.string.comm_error_timeout));
	}

	@Override
	String acTitle() {
		return getString(R.string.friendship_my_bilateral);
	}

}
