package org.aisen.weibo.sina.support.utils;

import com.alibaba.fastjson.JSON;

import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.bean.LogedinAccountBean;
import org.aisen.weibo.sina.support.sqlit.SinaDB;

import java.util.List;

/**
 * Created by wangdan on 16/1/2.
 */
public class AccountUtils {

    public static void newAccount(AccountBean account) {
        SinaDB.getDB().insertOrReplace(null, account);
    }

    public static void updateAccount(AccountBean account) {
        SinaDB.getDB().update(null, account);
    }

    public static List<AccountBean> queryAccount() {
        return SinaDB.getDB().select(null, AccountBean.class);
    }

    public static void remove(String id) {
        SinaDB.getDB().deleteById(null, AccountBean.class, id);
    }

    public static void setLogedinAccount(AccountBean account) {
        SinaDB.getDB().deleteAll(null, LogedinAccountBean.class);

        SinaDB.getDB().insert(null, JSON.parseObject(JSON.toJSONString(account), LogedinAccountBean.class));
    }

    public static AccountBean getLogedinAccount() {
        List<LogedinAccountBean> accounts =  SinaDB.getDB().select(null, LogedinAccountBean.class);
        if (accounts.size() > 0)
            return accounts.get(0);

        return null;
    }

}
