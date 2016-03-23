package org.aisen.weibo.sina.support.cache;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.component.orm.extra.Extra;
import org.aisen.android.component.orm.utils.FieldUtils;
import org.aisen.android.network.biz.IResult;
import org.aisen.android.network.cache.ICacheUtility;
import org.aisen.android.network.http.Params;
import org.aisen.weibo.sina.support.bean.JokeBean;
import org.aisen.weibo.sina.support.bean.JokeBeans;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.CacheTimeUtils;

import java.util.List;

/**
 * Created by wangdan on 16/3/22.
 */
public class JokesCacheUtility implements ICacheUtility {

    @Override
    public IResult findCacheData(Setting action, Params params) {
        long id = Integer.parseInt(params.getParameter("id"));
        int type = Integer.parseInt(params.getParameter("mode"));

        // 只有第一次加载数据的时候才加载缓存
        if (id == 0) {
            // 根据id倒序
            String selection = String.format(" %s = ? ", FieldUtils.KEY);
            String[] selectionArgs = new String[] { String.valueOf(type) };
            List<JokeBean> beanList = SinaDB.getDB().select(JokeBean.class, selection, selectionArgs, null, null, " id desc ", null);

            if (beanList.size() > 0) {
                JokeBeans beans = new JokeBeans();
                JokeBeans.Data data = new JokeBeans.Data();
                data.setContents(beanList);

                beans.setData(data);
                beans.setFromCache(true);
                beans.setEndPaging(beanList.size() == 0);
                beans.setOutofdate(CacheTimeUtils.isOutofdate("Jokes" + type, null));
                return beans;
            }
        }

        return null;
    }

    @Override
    public void addCacheData(Setting action, Params params, IResult result) {
        long id = Long.parseLong(params.getParameter("id"));
        int type = Integer.parseInt(params.getParameter("mode"));
        String direction = params.getParameter("direction");

        JokeBeans beans = (JokeBeans) result;

        if (id == 0 || ("down".equalsIgnoreCase(direction) && beans.getData().getContents().size() >= 20)) {
            SinaDB.getDB().deleteAll(new Extra(null, String.valueOf(type)), JokeBean.class);
        }

        CacheTimeUtils.saveTime("Jokes" + type, null);

        SinaDB.getDB().insert(new Extra(null, String.valueOf(type)), beans.getData().getContents());
    }

}
