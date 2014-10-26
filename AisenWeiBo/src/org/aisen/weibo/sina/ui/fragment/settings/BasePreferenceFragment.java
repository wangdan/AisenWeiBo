package org.aisen.weibo.sina.ui.fragment.settings;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.m.ui.activity.BaseActivity;

public class BasePreferenceFragment extends PreferenceFragment {

	public int setTheme() {
		if (AppSettings.isTranslucentModes() && AppSettings.isLaunchWallpaper()) {
			return R.style.BaseTheme_Dark_Wallpaper_Translucent;
		}
		else if (AppSettings.isLaunchWallpaper()) {
			return R.style.BaseTheme_Dark_Wallpaper;
		}
		else if (AppContext.getWallpaper() != null) {
			if (AppSettings.isTranslucentModes())
				return R.style.BaseTheme_Dark_Translucent;
			else
				return R.style.BaseTheme_Dark;
		}
		
		return 0;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		
		ListView listView = (ListView) rootView.findViewById(android.R.id.list);
		listView.setClipToPadding(false);
		BaseActivity baseActivity = (BaseActivity) getActivity();
		org.aisen.weibo.sina.ui.fragment.base.ActivityHelper activityHelper = (org.aisen.weibo.sina.ui.fragment.base.ActivityHelper) baseActivity.getActivityHelper();
		listView.setPadding(listView.getPaddingLeft(), 
										listView.getPaddingTop(), 
										listView.getPaddingRight(), 
										activityHelper.wallpaper.systemBarConfig.getPixelInsetBottom());
		
		return rootView;
	}
	
}
