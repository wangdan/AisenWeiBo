package org.aisen.android.common.utils;

import android.util.Log;

import com.alibaba.fastjson.JSON;

public class Logger {

	public final static String TAG = "Logger";

	public static boolean DEBUG = true;

	public static void v(Object o) {
		if (DEBUG)
			Log.v(TAG, toJson(o));
	}

	public static void v(String tag, Object msg) {
		if (DEBUG)
			Log.v(tag, toJson(msg));
	}

	public static void v(String tag, String format, Object... args) {
		if (DEBUG)
			Log.v(tag, String.format(format, args));
	}

	public static void d(Object o) {
		if (DEBUG)
			Log.d(TAG, toJson(o));
	}

	public static void d(String tag, Object msg) {
		if (DEBUG)
			Log.d(tag, toJson(msg));
	}

	public static void d(String tag, String format, Object... args) {
		if (DEBUG)
			Log.d(tag, String.format(format, args));
	}

	public static void i(Object o) {
		if (DEBUG)
			Log.i(TAG, toJson(o));
	}

	public static void i(String tag, Object msg) {
		if (DEBUG)
			Log.i(tag, toJson(msg));
	}

	public static void i(String tag, String format, Object... args) {
		if (DEBUG)
			Log.i(tag, String.format(format, args));
	}

	public static void w(Object o) {
		if (DEBUG)
			Log.w(TAG, toJson(o));
	}

	public static void w(String tag, Object msg) {
		if (DEBUG)
			Log.w(tag, toJson(msg));
	}

	public static void w(String tag, String format, Object... msg) {
		if (DEBUG)
			Log.w(tag, String.format(format, msg));
	}

	public static void e(Object o) {
		if (DEBUG)
			Log.e(TAG, toJson(o));
	}

	public static void e(String tag, Object msg) {
		if (DEBUG)
			Log.e(tag, toJson(msg));
	}

	public static void e(String tag, String format, Object... msg) {
		if (DEBUG)
			Log.e(tag, String.format(format, msg));
	}

	// 这个日志会打印，不会因为release版本屏蔽
	public static void sysout(String msg) {
		try {
			Log.v(TAG, msg);
		} catch (Throwable e) {
		}
	}

	public static void printExc(Class<?> clazz, Throwable e) {
		try {
			if (DEBUG) {
				e.printStackTrace();
			}
			else {
				String clazzName = clazz == null ? "Unknow" : clazz.getSimpleName();

				Log.v(TAG, String.format("class[%s], %s", clazzName, e + ""));
			}
		} catch (Throwable ee) {
			ee.printStackTrace();
		}
	}

	public static void logExc(Throwable e) {
		printExc(Logger.class, e);
	}

	public static String toJson(Object msg) {
		if (msg instanceof String)
			return msg.toString();

		String json = JSON.toJSONString(msg);
		if (json.length() > 500)
			json = json.substring(0, 500);

		return json;
	}

}
