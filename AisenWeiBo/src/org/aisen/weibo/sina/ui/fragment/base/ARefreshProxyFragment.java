package org.aisen.weibo.sina.ui.fragment.base;

import java.io.Serializable;

import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.activity.main.MainActivity;
import org.aisen.weibo.sina.ui.fragment.friendship.AFriendshipFragment;
import org.aisen.weibo.sina.ui.fragment.profile.UserProfileFragment;
import org.aisen.weibo.sina.ui.fragment.publish.AddFriendMentionFragment;
import org.aisen.weibo.sina.ui.fragment.search.SearchTopicsFragment;
import org.aisen.weibo.sina.ui.fragment.topics.TopicsFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.TextView;

import com.m.R;
import com.m.ui.activity.BaseActivity;
import com.m.ui.fragment.ACombinationRefreshListFragment;

public abstract class ARefreshProxyFragment<T extends Serializable, Ts extends Serializable> 
							extends ACombinationRefreshListFragment<T, Ts> {

	private TextView txtLoadingHint;
	private TextView btnLoadMore;
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		txtLoadingHint = (TextView) getFooterView().findViewById(R.id.txtLoadingHint);
		btnLoadMore = (TextView) getFooterView().findViewById(R.id.btnLoadMore);
		
		BaseActivity baseActivity = (BaseActivity) getActivity();
		org.aisen.weibo.sina.ui.fragment.base.ActivityHelper activityHelper = (org.aisen.weibo.sina.ui.fragment.base.ActivityHelper) baseActivity.getActivityHelper();
		
		// 首页
		if (getActivity() instanceof MainActivity
				// 用户资料
				|| (getActivity() instanceof FragmentContainerActivity && this instanceof UserProfileFragment)
				// 用户朋友圈
				|| (getActivity() instanceof FragmentContainerActivity && this instanceof AFriendshipFragment)
				// 提及
				|| (getActivity() instanceof FragmentContainerActivity && this instanceof AddFriendMentionFragment)
				// 话题搜索
				|| (this instanceof TopicsFragment)
				// 微博搜索
				|| (this instanceof SearchTopicsFragment)
				) {
			ListView listView = (ListView) getRefreshView();
			listView.setClipToPadding(false);
			listView.setPadding(listView.getPaddingLeft(), 
									listView.getPaddingTop(), 
									listView.getPaddingRight(), 
									activityHelper.wallpaper.systemBarConfig.getPixelInsetBottom());
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (AisenUtil.isTranslucent()) {
			txtLoadingHint.setTextColor(getResources().getColor(R.color.white));
			btnLoadMore.setTextColor(getResources().getColor(R.color.white));
		}
		else {
			txtLoadingHint.setTextColor(getResources().getColor(R.color.black));
			btnLoadMore.setTextColor(getResources().getColor(R.color.black));
		}
	}
	
}
