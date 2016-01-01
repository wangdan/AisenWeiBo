package org.aisen.weibo.sina.base;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.AccountUtils;

/**
 * Created by wangdan on 15/12/13.
 */
public class MyApplication extends GlobalContext {

    @Override
    public void onCreate() {
        super.onCreate();

        SinaDB.setInitDB();

        AppContext.setAccount(AccountUtils.getLogedinAccount());
    }

}
