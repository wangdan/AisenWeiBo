package org.aisen.weibo.sina.support.sqlit;

import android.util.Log;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.orm.SqliteUtility;
import org.aisen.android.component.orm.SqliteUtilityBuilder;

/**
 * Created by wangdan on 16/1/2.
 */
public class SinaDB {

    static final String DB_NAME = "sinadb";

    static final int DB_VERSION = 1;

    public static void setInitDB() {
        try {
            Log.w("LScreenDB", "初始化 db versionCode = " + DB_VERSION);

            new SqliteUtilityBuilder().configVersion(DB_VERSION).configDBName(DB_NAME).build(GlobalContext.getInstance());
            new SqliteUtilityBuilder().configVersion(DB_VERSION).configDBName("sina_timeline_db").build(GlobalContext.getInstance());
            new SqliteUtilityBuilder().configVersion(DB_VERSION).configDBName("sina_timeline_offline_db").build(GlobalContext.getInstance());
        } catch (Throwable e) {
            Logger.printExc(SinaDB.class, e);
        }
    }

    public static SqliteUtility getDB() {
        return SqliteUtility.getInstance(DB_NAME);
    }

    public static SqliteUtility getTimelineDB() {
        return SqliteUtility.getInstance("sina_timeline_db");
    }

    public static SqliteUtility getOfflineSqlite() {
        return SqliteUtility.getInstance("sina_timeline_offline_db");
    }

}
