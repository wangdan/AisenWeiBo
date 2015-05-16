package org.aisen.weibo.sina.ui.fragment.friendship;

import android.os.Bundle;

import com.m.common.context.GlobalContext;
import com.m.network.biz.ABizLogic;
import com.m.network.task.TaskException;
import com.m.network.task.WorkTask;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.ARefreshFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.sina.android.SinaSDK;
import org.sina.android.bean.Friendship;
import org.sina.android.bean.Token;
import org.sina.android.bean.WeiBoUser;

/**
 * 用户关注
 * 
 * @author wangdan
 *
 */
public class FriendsFragment extends AFriendshipFragment {

	public static ABaseFragment newInstance(WeiBoUser user) {
		ABaseFragment fragment = new FriendsFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("user", user);
		args.putBoolean("launch", false);
		fragment.setArguments(args);
		
		return fragment;
	}

    @Override
    protected void configRefresh(RefreshConfig config) {
        super.configRefresh(config);

        config.emptyLabel = getString(R.string.empty_friends);
    }

	@Override
    Friendship getFriendship(@SuppressWarnings("rawtypes") WorkTask task, ARefreshFragment.RefreshMode mode,
								String previousPage, String nextPage, Token token, Void... params)
			throws TaskException {
		if (getUser() != null) {
			ABizLogic.CacheMode cacheMode = getUser().getIdstr().equals(AppContext.getUser().getIdstr()) ? getTaskCacheMode(task) : ABizLogic.CacheMode.disable;
			
			return SinaSDK.getInstance(token, cacheMode).friendshipsFriends(getUser().getIdstr(), null, nextPage, 0);
		}
		
		throw new TaskException(GlobalContext.getInstance().getResources().getString(R.string.comm_error_timeout));
	}

	@Override
	String acTitle() {
		return getString(R.string.friendship_my_friends);
	}

}
