package org.aisen.weibo.sina.support.db;

import com.m.common.context.GlobalContext;
import com.m.component.sqlite.SqliteUtility;
import com.m.component.sqlite.SqliteUtilityBuilder;

public class SinaDB {

	public static SqliteUtility getSqlite() {
        if (SqliteUtility.getInstance("sina_db") == null)
            new SqliteUtilityBuilder().configDBName("sina_db").build(GlobalContext.getInstance());

		return SqliteUtility.getInstance("sina_db");
	}
	
	public static SqliteUtility getTimelineSqlite() {
        if (SqliteUtility.getInstance("sina_timeline_db") == null)
            new SqliteUtilityBuilder().configDBName("sina_timeline_db").build(GlobalContext.getInstance());

		return SqliteUtility.getInstance("sina_timeline_db");
	}
	
}
