package org.aisen.weibo.sina.base;

import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.utils.AccountUtils;

/**
 * Created by wangdan on 15/12/13.
 */
public class AppContext {

    private static AccountBean mAccount;

    public static boolean isLoggedIn() {
        return mAccount != null;
    }

    public static void logout() {
        mAccount = null;
    }

    public static void setAccount(AccountBean account) {
        mAccount = account;
    }

    public static AccountBean getAccount() {
        return mAccount;
    }

    public static void clearCookie() {
        mAccount.setCookie(null);

        AccountUtils.updateAccount(mAccount);
        AccountUtils.setLogedinAccount(mAccount);
    }

}
