package org.aisen.weibo.sina.support.sdk;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.biz.ABizLogic;
import org.aisen.android.network.http.HttpConfig;
import org.aisen.android.network.http.IHttpUtility;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.support.bean.JokeBeans;
import org.aisen.weibo.sina.support.bean.LikeResultBean;
import org.aisen.weibo.sina.support.bean.PictureSize;
import org.aisen.weibo.sina.support.bean.WallpaperBeans;
import org.aisen.weibo.sina.support.cache.JokesCacheUtility;
import org.aisen.weibo.sina.support.cache.WallpaperCacheUtility;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.HashMap;
import java.util.Map;

public class SDK extends ABizLogic {

    private SDK() {

    }

    private SDK(CacheMode mode) {
        super((mode));
    }

    public static SDK newInstance() {
        return new SDK();
    }

    public static SDK newInstance(CacheMode cacheMode) {
        return new SDK(cacheMode);
    }

	@Override
	protected HttpConfig configHttpConfig() {
		HttpConfig httpConfig = new HttpConfig();
		return httpConfig;
	}

    @Override
    protected IHttpUtility configHttpUtility() {
        return super.configHttpUtility();
    }

    /**
     * 获取图片大小
     *
     * @param url
     * @return
     * @throws TaskException
     */
    public PictureSize getPictureSize(String url) throws TaskException {
        Setting action = newSetting("getPictureSize", "", "读取图片的尺寸");

        action.getExtras().put(HTTP_UTILITY, newSettingExtra(HTTP_UTILITY, PictureSizeHttpUtility.class.getName(), "获取图片尺寸的HttpUtility"));

        Params params = new Params();
        params.addParameter("path", url);

        return doGet(action, params, PictureSize.class);
    }

    /**
     * 点赞
     *
     * @param statusId
     * @param like
     * @param cookie
     * @return
     * @throws TaskException
     */
    public LikeResultBean doLike(String statusId, boolean like, String cookie) throws TaskException {
        try {
            String url = like ? "http://m.weibo.cn/attitudesDeal/add" : "http://m.weibo.cn/attitudesDeal/delete";

            Map<String, String> cookieMap = new HashMap<String, String>();

            String[] cookieValues = cookie.split(";");
            for (String cookieValue : cookieValues) {
                String key = cookieValue.split("=")[0];
                String value = cookieValue.split("=")[1];

                cookieMap.put(key, value);
            }
//            Logger.d(WeiboClientActivity.TAG, cookieMap);

            Connection connection = Jsoup.connect(url);
            connection.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:33.0) Gecko/20100101 Firefox/33.0")
                    .referrer("http://m.weibo.cn/")
                    .cookies(cookieMap)
                    .data("id", statusId)
                    .method(Connection.Method.POST);
            if (like)
                connection.data("attitude", "heart");

            String body = connection.execute().body();
            if (!TextUtils.isEmpty(body)) {
                Logger.d(TAG, body);

                if (body.indexOf("http://passport.weibo.cn/sso/crossdomain") != -1)
                    throw new TaskException("-100", "未登录");
                else if (body.indexOf("<html") != -1)
                    throw new TaskException("-100", "未登录");

                LikeResultBean likeBean = JSON.parseObject(body, LikeResultBean.class);
                if (likeBean.getOk() == 1) {
                    return likeBean;
                }
                else if (likeBean.getOk() == -100) {
                    throw new TaskException("-100", "未登录");
                }
                else {
                    throw new TaskException("", likeBean.getMsg());
                }
            }
        } catch (Exception e) {
            if (e instanceof TaskException)
                throw (TaskException) e;

            e.printStackTrace();
        }

        throw new TaskException(TaskException.TaskError.timeout.toString());
    }

    /**
     * 获取笑料百科列表
     *
     * @param id
     * @param direction
     * @param limit
     * @return
     * @throws TaskException
     */
    public JokeBeans getJokes(long id, String direction, int limit, int type) throws TaskException {
        Setting action = newSetting("getJokes", "jokes", "获取笑话列表");
        action.getExtras().put(BASE_URL, newSettingExtra(BASE_URL, "http://stream-cn-api.tclclouds.com/api/", ""));

        Params params = new Params();
        if ("up".equalsIgnoreCase(direction) || "down".equalsIgnoreCase(direction)) {
        } else {
            direction = "up";
            id = 0;
        }
        params.addParameter("id", String.valueOf(id));
        params.addParameter("direction", String.valueOf(direction));
        params.addParameter("limit", String.valueOf(limit));
        params.addParameter("mode",String.valueOf(type));

        // 配置缓存器
        action.getExtras().put(CACHE_UTILITY, newSettingExtra(CACHE_UTILITY, JokesCacheUtility.class.getName(), ""));

        return doGet(action, params, JokeBeans.class);
    }

    /**
     * 获取壁纸列表
     *
     * @param page
     * @return
     * @throws TaskException
     */
    public WallpaperBeans getWallpaper(int page) throws TaskException {
        Setting action = newSetting("getWallpaper", "wallpaper/newestorhot/content", "获取最新壁纸列表");
        action.getExtras().put(BASE_URL, newSettingExtra(BASE_URL, "http://apps.tclclouds.com/api/", ""));

        Params params = new Params();
        params.addParameter("flag", "2");// 1：最新；2：最热）
        params.addParameter("page", String.valueOf(page));
        params.addParameter("per_page", "30");
        params.addParameter("encoder", "debug");

        // 配置缓存器
        action.getExtras().put(CACHE_UTILITY, newSettingExtra(CACHE_UTILITY, WallpaperCacheUtility.class.getName(), ""));

        WallpaperBeans beans = doGet(action, params, WallpaperBeans.class);
        if (beans.getItem() == null || beans.getItem().getWallpaperList() == null) {
            throw new TaskException(TaskException.TaskError.resultIllegal.toString());
        }
        return beans;
    }

}
