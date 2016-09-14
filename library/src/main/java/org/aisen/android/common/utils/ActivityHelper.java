package org.aisen.android.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashSet;
import java.util.Set;

/**
 * 在Application中一定要先配置
 * 
 * @author wangdan
 *
 */
public class ActivityHelper {
	
	public static final String KEY = "org.aisen.android.activityhelp_key";
	
	private ActivityHelper() {
	}

	/**
	 * 获取string，默认值为""
	 * 
	 * @param key
	 * @return
	 */
	public static String getShareData(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return sp.getString(key, "");
	}

	/**
	 * 获取string
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static String getShareData(Context context, String key, String defValue) {
		SharedPreferences sp = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return sp.getString(key, defValue);
	}

	/**
	 * 获取int
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static int getIntShareData(Context context, String key, int defValue) {
		SharedPreferences sp = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return sp.getInt(key, defValue);
	}

	public static int getIntShareData(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return sp.getInt(key, 0);
	}

	public static boolean getBooleanShareData(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return sp.getBoolean(key, false);
	}

	public static boolean getBooleanShareData(Context context, String key, boolean defValue) {
		SharedPreferences sp = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return sp.getBoolean(key, defValue);
	}

	public static void putShareData(Context context, String key, String value) {
		SharedPreferences sp = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putString(key, value);
		et.commit();
	}

	public static void putIntShareData(Context context, String key, int value) {
		SharedPreferences sp = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putInt(key, value);
		et.commit();
	}

	public static void putBooleanShareData(Context context, String key, boolean value) {
		SharedPreferences sp = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean(key, value);
		et.commit();
	}

	public static void putSetShareData(Context context, String key, Set<String> value) {
		SharedPreferences sp = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putStringSet(key, value);
		et.commit();
	}

	public static Set<String> getSetShareData(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return sp.getStringSet(key, new HashSet<String>());
	}

}
