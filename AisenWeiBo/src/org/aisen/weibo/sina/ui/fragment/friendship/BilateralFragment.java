package org.aisen.weibo.sina.ui.fragment.friendship;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.sina.android.SinaSDK;
import org.sina.android.bean.Friendship;
import org.sina.android.bean.WeiBoUser;

import android.os.Bundle;

import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.fragment.ABaseFragment;

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
	protected void config(RefreshConfig config) {
		super.config(config);
		
		config.emptyLabel = getString(R.string.empty_bilateral);
	}
	
	@Override
	Friendship getFriendship(@SuppressWarnings("rawtypes") WorkTask task, RefreshMode mode, String previousPage, String nextPage, Void... params)
			throws TaskException {
		return SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(task))
										.friendshipsBilateral(getUser().getIdstr(), nextPage);
	}

	@Override
	String acTitle() {
		return getString(R.string.friendship_my_bilateral);
	}

}
