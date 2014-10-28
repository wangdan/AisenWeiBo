package org.aisen.weibo.sina.ui.fragment.base;

import java.io.Serializable;

import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.activity.main.MainActivity;
import org.aisen.weibo.sina.ui.fragment.friendship.AFriendshipFragment;
import org.aisen.weibo.sina.ui.fragment.profile.UserProfileFragment;
import org.aisen.weibo.sina.ui.fragment.publish.AddFriendMentionFragment;
import org.aisen.weibo.sina.ui.fragment.search.SearchTopicsFragment;
import org.aisen.weibo.sina.ui.fragment.topics.TopicsFragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.TextView;

import com.m.R;
import com.m.ui.activity.BaseActivity;
import com.m.ui.fragment.ACombinationRefreshListFragment;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;

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
			int bottom = activityHelper.wallpaper.systemBarConfig.getPixelInsetBottom() + listView.getBottom();
			listView.setPadding(listView.getPaddingLeft(), 
									listView.getPaddingTop(), 
									listView.getPaddingRight(), 
									bottom);
		}
	}
	
	@Override
	public void resetRefreshView(com.m.ui.fragment.ARefreshFragment.RefreshConfig config) {
		super.resetRefreshView(config);
		
		// ActionBarPullToRefresh的背景颜色
		// 如果是设定主题，就保持颜色一致，其他情况就是透明色
		if (configListType() == RefreshListType.actionbarPulltorefresh) {
			if (AisenUtil.isTranslucent())
				getPullToRefreshLayout().getHeaderView().findViewById(R.id.ptr_text).setBackgroundColor(getResources().getColor(R.color.transparent));
			else
				getPullToRefreshLayout().getHeaderView().findViewById(R.id.ptr_text).setBackgroundColor(Color.parseColor(AppSettings.getThemeColor()));
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
		
		if (AppSettings.isLaunchWallpaper() || AppContext.getWallpaper() != null)
			AnimationAdapter.alpha = 0.75f;
		else 
			AnimationAdapter.alpha = 1.0f;
		
		getConfig().animEnable = AppSettings.isListAnim();
	}
	
}
