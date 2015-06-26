package org.aisen.weibo.sina.support.db;


import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.bean.LikeBean;

import org.aisen.orm.extra.Extra;

/**
 * Created by wangdan on 15-3-7.
 */
public class LikeDB {

    public static LikeBean get(String statusId) {
        return SinaDB.getSqlite().selectById(new Extra(AppContext.getUser().getIdstr(), null), LikeBean.class, statusId);
    }

    public static void insert(LikeBean likeBean) {
        SinaDB.getSqlite().insert(new Extra(AppContext.getUser().getId(), null), likeBean);
    }

}
