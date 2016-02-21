package org.aisen.weibo.sina.support.sqlit;


import org.aisen.android.component.orm.extra.Extra;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.bean.LikeBean;

/**
 * Created by wangdan on 15-3-7.
 */
public class LikeDB {

    public static LikeBean get(String statusId) {
        return SinaDB.getDB().selectById(new Extra(AppContext.getAccount().getUser().getIdstr(), null), LikeBean.class, statusId);
    }

    public static void insert(LikeBean likeBean) {
        SinaDB.getDB().insertOrReplace(new Extra(AppContext.getAccount().getUser().getId(), null), likeBean);
    }

}
