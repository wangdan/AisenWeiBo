package org.aisen.weibo.sina.ui.component;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.m.ui.fragment.ATabTitlePagerFragment;
import com.m.ui.fragment.ATabTitlePagerFragment.TabTitlePagerBean;

public abstract class AMainTabsFragment<T extends TabTitlePagerBean> extends ATabTitlePagerFragment<T> {

//	@Override
//	protected int inflateContentView() {
//		return R.layout.ui_main_tabs;
//	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (AppSettings.isLaunchWallpaper() || AppContext.getWallpaper() != null) {
			getTabStrip().setBackgroundColor(Color.parseColor("#58000000"));
			getTabStrip().setTextColor(Color.parseColor("#FFFFFFFF"));
			getTabStrip().setIndicatorColor(Color.parseColor("#FFFFFFFF"));
			getTabStrip().setDividerColor(getResources().getColor(R.color.wallpaper_divider));
			getTabStrip().setUnderlineColor(getResources().getColor(R.color.wallpaper_divider));
		}
		else {
			getTabStrip().setBackgroundColor(Color.parseColor("#00ffffff"));
			getTabStrip().setTextColor(Color.parseColor("#FF666666"));
			getTabStrip().setIndicatorColor(Color.parseColor(AppSettings.getThemeColor()));
			getTabStrip().setDividerColor(Color.parseColor(AppSettings.getThemeColor().toLowerCase().replace("#ff", "#98")));
			getTabStrip().setUnderlineColor(Color.parseColor("#1A000000"));
		}
	}
	
}
