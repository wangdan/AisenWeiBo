package org.aisen.android.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

	// yyyy-MM-dd hh:mm:ss 12小时制
	// yyyy-MM-dd HH:mm:ss 24小时制

	public static final String TYPE_01 = "yyyy-MM-dd HH:mm:ss";

	public static final String TYPE_02 = "yyyy-MM-dd";

	public static final String TYPE_03 = "HH:mm:ss";

	public static final String TYPE_04 = "yyyy年MM月dd日";

	public static String formatDate(long time, String format) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return new SimpleDateFormat(format).format(cal.getTime());
	}

	public static String formatDate(String longStr, String format) {
		try {
			return formatDate(Long.parseLong(longStr), format);
		} catch (Exception e) {
		}
		return "";
	}
	
	public static long formatStr(String timeStr, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		try {
			return sdf.parse(timeStr).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

}
