package org.aisen.android.common.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import org.aisen.android.common.context.GlobalContext;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

@SuppressLint("SdCardPath") public class SystemUtils {

	private static int screenWidth;

	private static int screenHeight;

	private static float density;

	private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
	private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
	private static final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";
	private static final String SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar";
	private static final String NAV_BAR_WIDTH_RES_NAME = "navigation_bar_width";

	private static String sNavBarOverride;

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

	public enum NetWorkType {
		none, mobile, wifi
	}

	private static void setScreenInfo(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
		density = dm.density;
	}

	public static int getScreenWidth(Context context) {
		if (screenWidth == 0)
			setScreenInfo(context);
		return screenWidth;
	}

	public static int getScreenHeight(Context context) {
		if (screenHeight == 0)
			setScreenInfo(context);
		return screenHeight;
	}

	public static int getTitleBarHeight(Activity activity) {
		int contentTop = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
		//statusBarHeight是上面所求的状态栏的高度  
		int titleBarHeight = contentTop - getStatusBarHeight(activity);

		return titleBarHeight;
	}

	public static float getDensity(Context context) {
		if (density == 0.0f)
			setScreenInfo(context);
		return density;
	}

	public static boolean hasSDCard() {
		boolean mHasSDcard = false;
		if (Environment.MEDIA_MOUNTED.endsWith(Environment.getExternalStorageState())) {
			mHasSDcard = true;
		} else {
			mHasSDcard = false;
		}

		return mHasSDcard;
	}

	public static String getSdcardPath() {

		if (hasSDCard())
			return Environment.getExternalStorageDirectory().getAbsolutePath();

		return "/sdcard/";
	}

	private static boolean sdcardCanWrite() {
		return Environment.getExternalStorageDirectory().canWrite();
	}

	public static boolean hasSdcardAndCanWrite() {
		return hasSDCard() && sdcardCanWrite();
	}

	/**
	 * 获取SDCARD的可用大小,单位字节
	 *
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public long getSdcardtAvailableStore() {

		if (hasSdcardAndCanWrite()) {
			String path = getSdcardPath();
			if (path != null) {
				StatFs statFs = new StatFs(path);

				long blocSize = statFs.getBlockSize();

				long availaBlock = statFs.getAvailableBlocks();

				return availaBlock * blocSize;
			}
		}

		return 0;
	}

	public static NetWorkType getNetworkType(Context context) {

		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null) {
			switch (networkInfo.getType()) {
				case ConnectivityManager.TYPE_MOBILE:
					return NetWorkType.mobile;
				case ConnectivityManager.TYPE_WIFI:
					return NetWorkType.wifi;
			}
		}

		return NetWorkType.none;
	}

	/**
	 * mac地址
	 *
	 * @return
	 */
	public static String getMacAddress(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo != null && wifiInfo.getMacAddress() != null)
			return wifiInfo.getMacAddress().replace(":", "");
		else
			return "0022f420d03f";// 00117f29d23a
	}

	public static String getUDPIP(Context context) {

		WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcpInfo = wifi.getDhcpInfo();
		int IpAddress =dhcpInfo.ipAddress;
		int subMask = dhcpInfo.netmask;
		return transformIp((~subMask) | IpAddress);
	}

	private static String transformIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);
	}

	public static String getIP(Context context) {
		WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		return transformIp(wifi.getConnectionInfo().getIpAddress());
	}

	public static String getVersionName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (Exception e) {
		}
		return "";
	}

	public static int getVersionCode(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (Exception e) {
		}
		return 0;
	}

	public static String getPackage(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.packageName;
		} catch (Exception e) {
		}
		return "";
	}

	public static void scanPhoto(Context context, File file) {
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri uri = Uri.fromFile(file);
		intent.setData(uri);
		context.sendBroadcast(intent);
	}

	public static void hideSoftInput(Context context, View paramEditText) {
		((InputMethodManager) context.getSystemService("input_method"))
				.hideSoftInputFromWindow(paramEditText.getWindowToken(), 0);
	}

	public static void showKeyBoard(final Context context, final View paramEditText) {
		paramEditText.requestFocus();
		paramEditText.post(new Runnable() {
			@Override
			public void run() {
				((InputMethodManager) context.getSystemService("input_method")).showSoftInput(paramEditText, 0);
			}
		});
	}

	public static int getScreenHeight(Activity paramActivity) {
		Display display = paramActivity.getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		return metrics.heightPixels;
	}

	public static int getKeyboardHeight(Activity paramActivity) {

		int height = SystemUtils.getScreenHeight(paramActivity) - SystemUtils.getStatusBarHeight(paramActivity)
				- SystemUtils.getAppHeight(paramActivity);
		if (height == 0) {
			height = ActivityHelper.getIntShareData(GlobalContext.getInstance(), "KeyboardHeight", 400);
		}
		else {
			ActivityHelper.putIntShareData(GlobalContext.getInstance(), "KeyboardHeight", height);
		}


		return height;
	}

	public static boolean isKeyBoardShow(Activity paramActivity) {
		int height = SystemUtils.getScreenHeight(paramActivity) - SystemUtils.getStatusBarHeight(paramActivity)
				- SystemUtils.getAppHeight(paramActivity);
		return height != 0;
	}

	@TargetApi(14)
	public static int getActionBarHeight(Context context) {
		int result = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			TypedValue tv = new TypedValue();
			context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
			result = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
		}
		return result;
	}

	public static boolean inPortarit(Resources res) {
		return (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
	}

	@TargetApi(14)
	public static int getNavigationBarHeight(Context context) {
		Resources res = context.getResources();
		int result = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (hasNavigationBar(context)) {
				String key;
				if (inPortarit(res)) {
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
	public static int getNavigationBarWidth(Context context) {
		Resources res = context.getResources();
		int result = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (hasNavigationBar(context)) {
				return getInternalDimensionSize(res, NAV_BAR_WIDTH_RES_NAME);
			}
		}
		return result;
	}

	@TargetApi(14)
	public static boolean hasNavigationBar(Context context) {
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

	public static int getStatusBarHeight(Context context) {
		return getInternalDimensionSize(context.getResources(), STATUS_BAR_HEIGHT_RES_NAME);
	}

	// below actionbar, above softkeyboard
	public static int getAppContentHeight(Activity paramActivity) {
		return SystemUtils.getScreenHeight(paramActivity) - SystemUtils.getStatusBarHeight(paramActivity)
				- getActionBarHeight(paramActivity) - SystemUtils.getKeyboardHeight(paramActivity);
	}

	// below status bar,include actionbar, above softkeyboard
	public static int getAppHeight(Activity paramActivity) {
		Rect localRect = new Rect();
		paramActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
		return localRect.height();
	}

	private static int getInternalDimensionSize(Resources res, String key) {
		int result = 0;
		int resourceId = res.getIdentifier(key, "dimen", "android");
		if (resourceId > 0) {
			result = res.getDimensionPixelSize(resourceId);
		}
		return result;
	}

	public static void startActivity(Activity context, String packageName) {
		try {
			Log.e("startActivity", packageName);
			// 获取目标应用安装包的Intent
			Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
			if (intent == null) {
				startActivityByApplication(context, packageName);
				return;
			}
			context.startActivity(intent);
		} catch (Exception e) {
			// e.printStackTrace();
			Logger.printExc(SystemUtils.class, e);
		}
	}

	private static void startActivityByApplication(Context context, String packageNameStr) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(packageNameStr, 0);

			Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(pi.packageName);

			List<ResolveInfo> apps = pm.queryIntentActivities(resolveIntent, 0);

			ResolveInfo ri = apps.iterator().next();
			if (ri != null) {
				String packageName = ri.activityInfo.packageName;
				String className = ri.activityInfo.name;

				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);

				ComponentName cn = new ComponentName(packageName, className);

				intent.setComponent(cn);
				context.startActivity(intent);
				return;
			}
		} catch (Exception e) {
		}
	}

}
