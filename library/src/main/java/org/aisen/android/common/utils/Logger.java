package org.aisen.android.common.utils;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

public class Logger {

	public final static String TAG = "Logger";

	public static boolean DEBUG = true;

	public static void v(Object o) {
		if (DEBUG) {
			String log = toJson(o);

			Log.v(TAG, log);

			Logger2File.log2File(TAG, log);
		}

	}

	public static void v(String tag, Object o) {
		if (DEBUG) {
			String log = toJson(o);

			Log.v(tag, log);

			Logger2File.log2File(tag, log);
		}
	}

	public static void v(String tag, String msg, Throwable tr) {
		if (DEBUG) {
			String log = msg + '\n' + getStackTraceString(tr);

			Log.v(tag, log);

			Logger2File.log2File(tag, log);
		}
	}

	public static void v(String tag, String format, Object... args) {
		if (DEBUG) {
			String log = String.format(format, args);

			Log.v(tag, log);

			Logger2File.log2File(tag, log);
		}
	}

	public static void d(Object o) {
		if (DEBUG) {
			String log = toJson(o);

			Log.d(TAG, log);

			Logger2File.log2File(TAG, log);
		}
	}

	public static void d(String tag, Object o) {
		if (DEBUG) {
			String log = toJson(o);

			Log.d(tag, log);

			Logger2File.log2File(tag, log);
		}
	}

	public static void d(String tag, String msg, Throwable tr) {
		if (DEBUG) {
			String log = msg + '\n' + getStackTraceString(tr);

			Log.d(tag, log);

			Logger2File.log2File(tag, log);
		}
	}

	public static void d(String tag, String format, Object... args) {
		if (DEBUG) {
			String log = String.format(format, args);

			Log.d(tag, log);

			Logger2File.log2File(tag, log);
		}
	}
	
	public static void i(Object o) {
		if (DEBUG) {
			String log = toJson(o);

			Log.i(TAG, log);

			Logger2File.log2File(TAG, log);
		}
	}

	public static void i(String tag, Object o) {
		if (DEBUG) {
			String log = toJson(o);

			Log.i(tag, log);

			Logger2File.log2File(tag, log);
		}
	}

	public static void i(String tag, String msg, Throwable tr) {
		if (DEBUG) {
			String log = msg + '\n' + getStackTraceString(tr);

			Log.i(tag, log);

			Logger2File.log2File(tag, log);
		}
	}
	
	public static void i(String tag, String format, Object... args) {
		if (DEBUG) {
			String log = String.format(format, args);

			Log.i(tag, log);

			Logger2File.log2File(tag, log);
		}
	}

	public static void w(Object o) {
		if (DEBUG) {
			String log = toJson(o);

			Log.w(TAG, log);

			Logger2File.log2File(TAG, log);
		}
	}

	public static void w(String tag, Object o) {
		if (DEBUG) {
			String log = toJson(o);

			Log.w(tag, log);

			Logger2File.log2File(tag, log);
		}
	}

	public static void w(String tag, String msg, Throwable tr) {
		if (DEBUG) {
			String log = msg + '\n' + getStackTraceString(tr);

			Log.w(tag, log);

			Logger2File.log2File(tag, log);
		}
	}

	public static void w(String tag, String format, Object... args) {
		if (DEBUG) {
			String log = String.format(format, args);

			Log.w(tag, log);

			Logger2File.log2File(tag, log);
		}
	}

	public static void e(Object o) {
		if (DEBUG) {
			String log = toJson(o);

			Log.e(TAG, log);

			Logger2File.log2File(TAG, log);
		}
	}

	public static void e(String tag, Object o) {
		if (DEBUG) {
			String log = toJson(o);

			Log.e(tag, log);

			Logger2File.log2File(tag, log);
		}
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (DEBUG) {
			String log = msg + '\n' + getStackTraceString(tr);

			Log.e(tag, log);

			Logger2File.log2File(tag, log);
		}
	}
	
	public static void e(String tag, String format, Object... args) {
		if (DEBUG) {
			String log = String.format(format, args);

			Log.e(tag, log);

			Logger2File.log2File(tag, log);
		}
	}

	// 这个日志会打印，不会因为release版本屏蔽
	public static void sysout(String msg) {
		try {
			Log.v(TAG, msg);

			Logger2File.log2File(TAG, msg);
		} catch (Throwable e) {
		}
	}

	public static void printExc(Class<?> clazz, Throwable e) {
		try {
			if (DEBUG) {
				e.printStackTrace();

				Logger2File.log2File(TAG, e);
			}
			else {
				String clazzName = clazz == null ? "Unknow" : clazz.getSimpleName();

				Log.v(TAG, String.format("class[%s], %s", clazzName, e + ""));
			}
		} catch (Throwable ee) {
			ee.printStackTrace();
		}
	}

	public static String toJson(Object msg) {
		if (msg instanceof String)
			return msg.toString();
		
		String json = JSON.toJSONString(msg);
		if (json.length() > 500)
			json = json.substring(0, 500);

		return json;
	}

	static String getStackTraceString(Throwable tr) {
		if (tr == null) {
			return "";
		}

		// This is to reduce the amount of log spew that apps do in the non-error
		// condition of the network being unavailable.
		Throwable t = tr;
		while (t != null) {
			if (t instanceof UnknownHostException) {
				return "";
			}
			t = t.getCause();
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		tr.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}

}
