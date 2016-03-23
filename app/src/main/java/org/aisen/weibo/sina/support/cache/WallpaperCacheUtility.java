package org.aisen.weibo.sina.support.cache;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.network.biz.IResult;
import org.aisen.android.network.cache.ICacheUtility;
import org.aisen.android.network.http.Params;
import org.aisen.weibo.sina.support.bean.WallpaperBean;
import org.aisen.weibo.sina.support.bean.WallpaperBeans;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.CacheTimeUtils;

import java.util.List;

/**
 * Created by wangdan on 16/3/23.
 */
public class WallpaperCacheUtility implements ICacheUtility {

    @Override
    public IResult findCacheData(Setting action, Params params) {
        int page = Integer.parseInt(params.getParameter("page"));

        // 只有第一次加载数据的时候才加载缓存
        if (page == 1) {
            // 根据id倒序
            List<WallpaperBean> beanList = SinaDB.getDB().select(null, WallpaperBean.class);

            if (beanList.size() > 0) {
                WallpaperBeans beans = new WallpaperBeans();
                beans.setItem(new WallpaperBeans.Data());
                beans.getItem().setWallpaperList(beanList);

                beans.setFromCache(true);
                beans.setEndPaging(beanList.size() == 0);
                beans.setOutofdate(CacheTimeUtils.isOutofdate("Wallpaper", null));
                return beans;
            }
        }

        return null;
    }

    @Override
    public void addCacheData(Setting action, Params params, IResult result) {
        int page = Integer.parseInt(params.getParameter("page"));

        // 简单点，只存第一份缓存
        if (page == 1) {
            WallpaperBeans beans = (WallpaperBeans) result;

            SinaDB.getDB().deleteAll(null, WallpaperBean.class);

            SinaDB.getDB().insert(null, beans.getItem().getWallpaperList());

            CacheTimeUtils.saveTime("Wallpaper", null);
        }
    }

}
