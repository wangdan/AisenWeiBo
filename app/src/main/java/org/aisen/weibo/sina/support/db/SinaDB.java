package org.aisen.weibo.sina.support.db;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.orm.SqliteUtility;
import org.aisen.orm.SqliteUtilityBuilder;

public class SinaDB {

	public static SqliteUtility getSqlite() {
        if (SqliteUtility.getInstance("sina_db") == null)
            new SqliteUtilityBuilder().configDBName("sina_db").configVersion(2).build(GlobalContext.getInstance());

		return SqliteUtility.getInstance("sina_db");
	}
	
	public static SqliteUtility getTimelineSqlite() {
        if (SqliteUtility.getInstance("sina_timeline_db") == null)
            new SqliteUtilityBuilder().configDBName("sina_timeline_db").build(GlobalContext.getInstance());

		return SqliteUtility.getInstance("sina_timeline_db");
	}

    public static SqliteUtility getOfflineSqlite() {
        if (SqliteUtility.getInstance("sina_offline_db") == null)
            new SqliteUtilityBuilder().configDBName("sina_offline_db").configVersion(43).build(GlobalContext.getInstance());

        return SqliteUtility.getInstance("sina_offline_db");
    }

}
