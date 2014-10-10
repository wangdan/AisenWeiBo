package org.aisen.weibo.sina.support.db;

import com.m.support.sqlite.SqliteUtility;

public class SinaDB {

	public static SqliteUtility getSqlite() {
		return SqliteUtility.getInstanceInApp("sina_db", "sina_db");
	}
	
	public static SqliteUtility getTimelineSqlite() {
		return SqliteUtility.getInstanceInApp("sina_timeline_db", "sina_timeline_db");
	}
	
}
