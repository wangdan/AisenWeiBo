package org.aisen.weibo.sina.base;

import org.aisen.weibo.sina.support.bean.AccountBean;

/**
 * Created by wangdan on 15/12/13.
 */
public class AppContext {

    private static AccountBean mAccount;

    public static boolean isLoggedIn() {
        return mAccount != null;
    }

    public static void setAccount(AccountBean account) {
        mAccount = account;
    }

    public static AccountBean getAccount() {
        return mAccount;
    }

}
