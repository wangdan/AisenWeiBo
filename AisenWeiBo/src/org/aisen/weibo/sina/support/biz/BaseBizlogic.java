package org.aisen.weibo.sina.support.biz;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.m.common.context.GlobalContext;
import com.m.common.utils.Logger;
import com.m.network.biz.ABizLogic;
import com.m.network.http.HttpConfig;
import com.m.network.http.IHttpUtility;
import com.m.network.http.Params;
import com.m.network.task.TaskException;

import org.aisen.weibo.sina.support.bean.LikeResultBean;
import org.aisen.weibo.sina.support.bean.PictureSize;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.HashMap;
import java.util.Map;

public class BaseBizlogic extends ABizLogic {

	@Override
	protected HttpConfig configHttpConfig() {
		HttpConfig httpConfig = new HttpConfig();
//		httpConfig.baseUrl = getSetting("meizt_base_url").getValue();
		httpConfig.contentType = "application/x-www-form-urlencoded";
		httpConfig.cookie = String.format("pck=%s;", GlobalContext.getInstance().getPackageName().replace(".", "_"));
		return httpConfig;
	}

    @Override
    protected IHttpUtility configHttpUtility() {
        return super.configHttpUtility();
    }

    private BaseBizlogic() {

	}
	
	public BaseBizlogic(CacheMode cacheMode) {
		super(cacheMode);
	}

	public static BaseBizlogic newInstance() {
		return new BaseBizlogic();
	}
	
	public static BaseBizlogic newInstance(CacheMode cacheMode) {
		return new BaseBizlogic(cacheMode);
	}
	
	/**
	 * Github资源下载
	 * 
	 * @param fileName 下载的资源名称
	 * @param saveDir 保存的文件路径
	 * @return
	 * @throws TaskException
	 */
	public Boolean githubResDownload(String fileName, String saveDir) throws TaskException {
		Params params = new Params();
		params.addParameter("fileName", fileName);
		params.addParameter("dir", saveDir);
		
		return doGet(getSetting("githubResDownload"), params, Boolean.class);
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

    public PictureSize getPictureSize(String url) throws TaskException {
        Params params = new Params();
        params.addParameter("path", url);

        return doGet(getSetting("getPictureSize"), params, PictureSize.class);
    }

}
