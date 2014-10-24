package org.aisen.weibo.sina.ui.fragment.base;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.ui.widget.WallpaperViewGroup;

import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.m.ui.activity.AActivityHelper;

public class ActivityHelper extends AActivityHelper {

	public boolean insertBottom = false;
	
	public boolean blur = true;
	
	public WallpaperViewGroup wallpaper;
	
	@Override
	public ViewGroup setContentView(int layoutResID) {
		View view = View.inflate(getActivity(), layoutResID, null);
		
		wallpaper = (WallpaperViewGroup) View.inflate(getActivity(), R.layout.ui_settings_wallpaper, null);
		wallpaper.insertBottom = insertBottom;
		wallpaper.blur = blur;
		
		wallpaper.addView(new View(getActivity()), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
		params.weight = 1.0f;
		wallpaper.addView(view, params);
		
		wallpaper.addView(new View(getActivity()), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
		
		// 初始化，如果有背景图片，设置背景图片，清理颜色条
		if (WallpaperViewGroup.wallpaperBitmap != null) {
			wallpaper.setBackground(new BitmapDrawable(getActivity().getResources(), WallpaperViewGroup.wallpaperBitmap));
			
			wallpaper.clearTranslucentColor();
		}
		// 判断是否需要设置背景
		else {
			wallpaper.setWallpaper();
		}
		
		return wallpaper;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (wallpaper != null) {
			wallpaper.setWallpaper();
		}
	}
	
}
