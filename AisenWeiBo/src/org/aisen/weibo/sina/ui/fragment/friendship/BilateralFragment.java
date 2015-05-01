package org.aisen.weibo.sina.ui.fragment.friendship;

import android.os.Bundle;

import com.m.common.context.GlobalContext;
import com.m.network.task.TaskException;
import com.m.network.task.WorkTask;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.ARefreshFragment;

import org.aisen.weibo.sina.R;
import org.sina.android.SinaSDK;
import org.sina.android.bean.Friendship;
import org.sina.android.bean.Token;
import org.sina.android.bean.WeiBoUser;

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
    protected void configRefresh(RefreshConfig config) {
        super.configRefresh(config);

        config.emptyLabel = getString(R.string.empty_bilateral);
    }

    @Override
    Friendship getFriendship(@SuppressWarnings("rawtypes") WorkTask task, ARefreshFragment.RefreshMode mode, String previousPage,
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
