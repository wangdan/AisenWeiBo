package org.aisen.weibo.sina.ui.widget;

import java.lang.reflect.Method;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.WallpaperBean;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.android.loader.core.BitmapDecoder;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.m.common.context.GlobalContext;
import com.m.common.utils.Logger;
import com.m.common.utils.SystemUtility;
import com.m.common.utils.Utils;

/**
 * APP背景壁纸
 * 
 * @author Jeff.Wang
 *
 * @date 2014年10月19日
 */
public class WallpaperViewGroup extends LinearLayout {

	public static final String TAG = "Wallpaper";
	
	static {
		// Android allows a system property to override the presence of the
		// navigation bar.
		// Used by the emulator.
		// See
		// https://github.com/android/platform_frameworks_base/blob/master/policy/src/com/android/internal/policy/impl/PhoneWindowManager.java#L1076
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			try {
				@SuppressWarnings("rawtypes")
				Class c = Class.forName("android.os.SystemProperties");
				@SuppressWarnings("unchecked")
				Method m = c.getDeclaredMethod("get", String.class);
				m.setAccessible(true);
				sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
			} catch (Throwable e) {
				sNavBarOverride = null;
			}
		}
	}
	
	private static String sNavBarOverride;

	public SystemBarConfig systemBarConfig;
	private boolean mStatusBarAvailable;
	private boolean mNavBarAvailable;
	
	public static Bitmap wallpaperBitmap;// 壁纸图片
	
	public boolean blur = true;
	
	public WallpaperBean wallpaper;
	
	private Activity activity;
	
	public boolean insertBottom = false;
	
	public WallpaperViewGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(attrs);
	}

	public WallpaperViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(attrs);
	}

	public WallpaperViewGroup(Context context) {
		super(context);
		
		init(null);
	}
	
	private void init(AttributeSet attrs) {
		if (activity == null && getContext() instanceof Activity)
			activity = (Activity) getContext();
		
		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Wallpaper);
			
			blur = a.getBoolean(R.styleable.Wallpaper_blur, false);
		}
		
		initKitkat();
		
		if (!isKitKat())
			mStatusBarAvailable = false;
		
		systemBarConfig = new SystemBarConfig(activity, mStatusBarAvailable, mNavBarAvailable);
		
		if (!systemBarConfig.hasNavigtionBar()) {
			mNavBarAvailable = false;
		}
	}
	
	@TargetApi(19)
	private void initKitkat() {
		Window win = activity.getWindow();
		
		// check theme attrs
		int[] attrsSys = { android.R.attr.windowTranslucentStatus, android.R.attr.windowTranslucentNavigation };
		TypedArray a = activity.obtainStyledAttributes(attrsSys);
		try {
			mStatusBarAvailable = a.getBoolean(0, false);
			mNavBarAvailable = a.getBoolean(1, false);
		} finally {
			a.recycle();
		}

		// check window flags
		WindowManager.LayoutParams winParams = win.getAttributes();
		int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if ((winParams.flags & bits) != 0) {
			mStatusBarAvailable = true;
		}
		bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
		if ((winParams.flags & bits) != 0) {
			mNavBarAvailable = true;
		}
	}
	
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	
	public void setBlur(boolean blur) {
		this.blur = blur;
	}
	
	public void setWallpaper() {
		setTranslucent(0, 0);
		
		// 桌面壁纸
		if (AppSettings.isLaunchWallpaper()) {
			clearTranslucentColor();
			
			setBackground(new ColorDrawable(Color.parseColor("#00000000")));
		}
		else {
			WallpaperBean newWallpaper = AppContext.getWallpaper();
			
			// 删除壁纸
			if (newWallpaper == null) {
				Logger.d(TAG, "删除壁纸");
				
				wallpaper = null;
				wallpaperBitmap = null;
				
				setBackground(new ColorDrawable(getResources().getColor(R.color.bg_content)));
				
				setTranslucentColor();
			}
			// 设置壁纸
			else if (wallpaper == null) {
				Logger.d(TAG, "设置壁纸");
				
				wallpaper = newWallpaper;
				
				if (wallpaperBitmap == null) {
					wallpaperBitmap = getWallpaperBitmap();
				}
				setBackground(new BitmapDrawable(getResources(), wallpaperBitmap));
				
				clearTranslucentColor();
			}
			// 壁纸更新了
			else if (!wallpaper.getType().equals(newWallpaper.getType()) || !wallpaper.getPath().equals(newWallpaper.getPath())) {
				Logger.d(TAG, "壁纸更新了");
				
				wallpaper = newWallpaper;
				
//				wallpaperBitmap.recycle();
				wallpaperBitmap = getWallpaperBitmap();
				if (wallpaperBitmap != null)
					setBackground(new BitmapDrawable(getResources(), wallpaperBitmap));
			}
		}
		
		postInvalidate();
	}
	
	public void setTranslucent(int topOffset, int bottomOffset) {
//		SystemBarTintManager tintManager = SystemBarTintManager.getInstance(activity);
//		SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
		
		int height = systemBarConfig.getPixelInsetTop(true) + Utils.dip2px(topOffset);
		View topView = getChildAt(0);
		Logger.v(TAG, String.format("child 0, height = %d", height));
		topView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
//		if (height > 0)
//			topView.setBackground(new ColorDrawable(Color.parseColor(AppSettings.getThemeColor())));
		
		height = systemBarConfig.getPixelInsetBottom() + Utils.dip2px(bottomOffset);
		if (!insertBottom)
			height = 0;
		View bottomView = getChildAt(2);
		bottomView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
		Logger.v(TAG, String.format("child 2, height = %d", height));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (blur && (wallpaperBitmap != null || AppSettings.isLaunchWallpaper()))
			canvas.drawColor(Color.parseColor("#88000000"));
	}
	
	private Bitmap getWallpaperBitmap() {
		if (wallpaper == null)
			return null;
		
		if ("0".equals(wallpaper.getType())) {
			WallpaperManager wallpaperManager = WallpaperManager.getInstance(GlobalContext.getInstance());
			
			Bitmap bm = ((BitmapDrawable) wallpaperManager.getDrawable()).getBitmap();
			int reqWidth = Math.round(SystemUtility.getScreenHeight() * 1.0f / bm.getHeight() * bm.getWidth());
			bm = Bitmap.createBitmap(bm, 0, 0, reqWidth, SystemUtility.getScreenHeight());
			
			return bm;
		}
		// 默认壁纸
		else if ("1".equals(wallpaper.getType())) {
			try {
				return BitmapFactory.decodeStream(GlobalContext.getInstance().getAssets().open("8_8.jpg"));
			} catch (Exception e) {
				return BitmapDecoder.decodeSampledBitmapFromFile(wallpaper.getPath(), 800, 1280);
			}
		}
		// 网络默认壁纸
		else if ("100".equals(wallpaper.getType())) {
			return BitmapDecoder.decodeSampledBitmapFromFile(wallpaper.getPath(), 800, 1280);
		}
		// 自定义壁纸
		else if ("10".equals(wallpaper.getType())) {
			return BitmapDecoder.decodeSampledBitmapFromFile(wallpaper.getPath(), 800, 1280);
		}
		
		return null;
	}
	
	public void setTranslucentColor() {
		Drawable colorDrawable = new ColorDrawable(Color.parseColor(AppSettings.getThemeColor()));
		
		ActionBar actionBar = activity.getActionBar();
		// 设置ActionBar 
		if (actionBar != null) {
			actionBar.setBackgroundDrawable(colorDrawable);
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayShowTitleEnabled(true);
		}
		
		View topView = getChildAt(0);
		topView.setBackground(colorDrawable);
		View bottomView = getChildAt(2);
		bottomView.setBackground(colorDrawable);
	}
	
	public void clearTranslucentColor() {
		Drawable colorDrawable = new ColorDrawable(Color.parseColor("#00000000"));
		
		ActionBar actionBar = activity.getActionBar();
		// 清理ActionBar 
		if (actionBar != null) {
			actionBar.setBackgroundDrawable(colorDrawable);
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayShowTitleEnabled(true);
		}
		
		View topView = getChildAt(0);
		topView.setBackground(colorDrawable);
		View bottomView = getChildAt(2);
		bottomView.setBackground(colorDrawable);
	}
	
	/**
	 * Class which describes system bar sizing and other characteristics for the
	 * current device configuration.
	 * 
	 */
	public static class SystemBarConfig {

		private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
		private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
		private static final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";
		private static final String NAV_BAR_WIDTH_RES_NAME = "navigation_bar_width";
		private static final String SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar";

		private final boolean mTranslucentStatusBar;
		private final boolean mTranslucentNavBar;
		private final int mStatusBarHeight;
		private final int mActionBarHeight;
		private final boolean mHasNavigationBar;
		private final int mNavigationBarHeight;
		private final int mNavigationBarWidth;
		private final boolean mInPortrait;
		private final float mSmallestWidthDp;

		private SystemBarConfig(Activity activity, boolean translucentStatusBar, boolean traslucentNavBar) {
			Resources res = activity.getResources();
			mInPortrait = (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
			mSmallestWidthDp = getSmallestWidthDp(activity);
			mStatusBarHeight = getInternalDimensionSize(res, STATUS_BAR_HEIGHT_RES_NAME);
			mActionBarHeight = getActionBarHeight(activity);
			mNavigationBarHeight = getNavigationBarHeight(activity);
			mNavigationBarWidth = getNavigationBarWidth(activity);
			mHasNavigationBar = (mNavigationBarHeight > 0);
			mTranslucentStatusBar = translucentStatusBar;
			mTranslucentNavBar = traslucentNavBar;
		}

		@TargetApi(14)
		private int getActionBarHeight(Context context) {
			int result = 0;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				TypedValue tv = new TypedValue();
				context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
				result = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
			}
			return result;
		}

		@TargetApi(14)
		private int getNavigationBarHeight(Context context) {
			Resources res = context.getResources();
			int result = 0;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				if (hasNavBar(context)) {
					String key;
					if (mInPortrait) {
						key = NAV_BAR_HEIGHT_RES_NAME;
					} else {
						key = NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME;
					}
					return getInternalDimensionSize(res, key);
				}
			}
			return result;
		}

		@TargetApi(14)
		private int getNavigationBarWidth(Context context) {
			Resources res = context.getResources();
			int result = 0;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				if (hasNavBar(context)) {
					return getInternalDimensionSize(res, NAV_BAR_WIDTH_RES_NAME);
				}
			}
			return result;
		}

		@TargetApi(14)
		private boolean hasNavBar(Context context) {
			Resources res = context.getResources();
			int resourceId = res.getIdentifier(SHOW_NAV_BAR_RES_NAME, "bool", "android");
			if (resourceId != 0) {
				boolean hasNav = res.getBoolean(resourceId);
				// check override flag (see static block)
				if ("1".equals(sNavBarOverride)) {
					hasNav = false;
				} else if ("0".equals(sNavBarOverride)) {
					hasNav = true;
				}
				return hasNav;
			} else { // fallback
				return !ViewConfiguration.get(context).hasPermanentMenuKey();
			}
		}

		private int getInternalDimensionSize(Resources res, String key) {
			int result = 0;
			int resourceId = res.getIdentifier(key, "dimen", "android");
			if (resourceId > 0) {
				result = res.getDimensionPixelSize(resourceId);
			}
			return result;
		}

		@SuppressLint("NewApi")
		private float getSmallestWidthDp(Activity activity) {
			DisplayMetrics metrics = new DisplayMetrics();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
			} else {
				// TODO this is not correct, but we don't really care pre-kitkat
				activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			}
			float widthDp = metrics.widthPixels / metrics.density;
			float heightDp = metrics.heightPixels / metrics.density;
			return Math.min(widthDp, heightDp);
		}

		/**
		 * Should a navigation bar appear at the bottom of the screen in the
		 * current device configuration? A navigation bar may appear on the
		 * right side of the screen in certain configurations.
		 * 
		 * @return True if navigation should appear at the bottom of the screen,
		 *         False otherwise.
		 */
		public boolean isNavigationAtBottom() {
			return (mSmallestWidthDp >= 600 || mInPortrait);
		}

		/**
		 * Get the height of the system status bar.
		 * 
		 * @return The height of the status bar (in pixels).
		 */
		public int getStatusBarHeight() {
			return mStatusBarHeight;
		}

		/**
		 * Get the height of the action bar.
		 * 
		 * @return The height of the action bar (in pixels).
		 */
		public int getActionBarHeight() {
			return mActionBarHeight;
		}

		/**
		 * Does this device have a system navigation bar?
		 * 
		 * @return True if this device uses soft key navigation, False
		 *         otherwise.
		 */
		public boolean hasNavigtionBar() {
			return mHasNavigationBar;
		}

		/**
		 * Get the height of the system navigation bar.
		 * 
		 * @return The height of the navigation bar (in pixels). If the device
		 *         does not have soft navigation keys, this will always return
		 *         0.
		 */
		public int getNavigationBarHeight() {
			return mNavigationBarHeight;
		}

		/**
		 * Get the width of the system navigation bar when it is placed
		 * vertically on the screen.
		 * 
		 * @return The width of the navigation bar (in pixels). If the device
		 *         does not have soft navigation keys, this will always return
		 *         0.
		 */
		public int getNavigationBarWidth() {
			return mNavigationBarWidth;
		}

		/**
		 * Get the layout inset for any system UI that appears at the top of the
		 * screen.
		 * 
		 * @param withActionBar
		 *            True to include the height of the action bar, False
		 *            otherwise.
		 * @return The layout inset (in pixels).
		 */
		public int getPixelInsetTop(boolean withActionBar) {
			return (mTranslucentStatusBar ? mStatusBarHeight : 0) + (withActionBar ? mActionBarHeight : 0);
		}

		/**
		 * Get the layout inset for any system UI that appears at the bottom of
		 * the screen.
		 * 
		 * @return The layout inset (in pixels).
		 */
		public int getPixelInsetBottom() {
			if (mTranslucentNavBar && isNavigationAtBottom()) {
				return mNavigationBarHeight;
			} else {
				return 0;
			}
		}

		/**
		 * Get the layout inset for any system UI that appears at the right of
		 * the screen.
		 * 
		 * @return The layout inset (in pixels).
		 */
		public int getPixelInsetRight() {
			if (mTranslucentNavBar && !isNavigationAtBottom()) {
				return mNavigationBarWidth;
			} else {
				return 0;
			}
		}

	}

	public static boolean isKitKat() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}
	
}
