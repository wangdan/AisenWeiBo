package org.aisen.weibo.sina.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.UrlBean;
import org.aisen.weibo.sina.sinasdk.bean.UrlsBean;
import org.aisen.weibo.sina.support.bean.VideoBean;
import org.aisen.weibo.sina.support.sqlit.SinaDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

/**
 * 解析视频链接
 *
 * Created by wangdan on 16/7/20.
 */
public class VideoService extends Service {

    public static void start(Context context) {
        context.startService(new Intent(context, VideoService.class));
    }

    public static final int TYPE_VIDEO_WEIPAI = 0;

    public static final int TYPE_VIDEO_SINA = 1;

    public static final int TYPE_VIDEO_NONE = 2;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static final String TAG = VideoService.class.getSimpleName();

    public static void parseStatusURL(StatusContents statusContents) throws TaskException {
        // 不解析缓存
        if (statusContents.fromCache()) {
            return;
        }
//        SinaDB.getDB().deleteAll(null, VideoBean.class);

        List<VideoBean> videoList = new ArrayList<>();
        Map<String, VideoBean> videoMap = new HashMap<>();
        Map<String, StatusContent> statusMap = new HashMap<>();
        LinkedBlockingQueue<VideoBean> videoQueue = new LinkedBlockingQueue<>();

        // 把未解析的短连接拎出来
        for (StatusContent statusContent : statusContents.getStatuses()) {
            if (statusContent.getRetweeted_status() != null) {
                statusContent = statusContent.getRetweeted_status();
            }

            String content = statusContent.getText();
            if (!TextUtils.isEmpty(content)) {
                SpannableString spannableString = SpannableString.valueOf(content);
                Linkify.addLinks(spannableString, Pattern.compile("http://[a-zA-Z0-9+&@#/%?=~_\\-|!:,\\.;]*[a-zA-Z0-9+&@#/%=~_|]"), "http://");
                URLSpan[] urlSpans = spannableString.getSpans(0, spannableString.length(), URLSpan.class);
                for (URLSpan urlSpan : urlSpans) {
                    if (!urlSpan.getURL().startsWith("http://t.cn/"))
                        continue;

                    String id = KeyGenerator.generateMD5(urlSpan.getURL());

                    VideoBean videoBean = SinaDB.getDB().selectById(null, VideoBean.class, id);
                    if ((videoBean == null || TextUtils.isEmpty(videoBean.getLongUrl())) && !videoMap.containsKey(urlSpan.getURL())) {
                        videoBean = new VideoBean();
                        videoBean.setIdStr(id);
                        videoBean.setShortUrl(urlSpan.getURL());

                        Logger.v(TAG, "add url : " + urlSpan.getURL());
                        videoMap.put(urlSpan.getURL(), videoBean);
                        statusMap.put(urlSpan.getURL(), statusContent);
                        videoList.add(videoBean);
                        videoQueue.add(videoBean);
                    }
                }
            }
        }

        // 把短链解析成长链
        if (videoList.size() > 0) {
            Logger.d(TAG, "找到未解析的链接 %d 个", videoList.size());

            do {
                List<VideoBean> parseList = new ArrayList<>();

                while (videoQueue.size() > 0 && parseList.size() < 20) {
                    parseList.add(videoQueue.poll());
                }

                String[] urlArr = new String[parseList.size()];
                for (int i = 0; i < parseList.size(); i++) {
                    urlArr[i] = parseList.get(i).getShortUrl();
                }

                Logger.w(TAG, "开始解析 %d 条短链", parseList.size());
                UrlsBean urlsBean = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).shortUrlExpand(urlArr);

                for (UrlBean urlBean : urlsBean.getUrls()) {
                    VideoBean videoBean = videoMap.get(urlBean.getUrl_short());
                    if (videoBean != null) {
                        videoBean.setLongUrl(urlBean.getUrl_long());

                        StatusContent statusContent = statusMap.get(urlBean.getUrl_short());

                        statusContent.setVideoUrl(urlBean);

                        if (isSinaVideo(videoBean.getLongUrl())) {
                            videoBean.setType(VideoService.TYPE_VIDEO_SINA);

                            statusContent.setVideo(true);
                        }
                        else if (isWeipai(videoBean.getLongUrl())) {
                            videoBean.setType(VideoService.TYPE_VIDEO_WEIPAI);

                            statusContent.setVideo(true);
                        }
                        else {
                            videoBean.setType(VideoService.TYPE_VIDEO_NONE);
                        }

                        Logger.v(TAG, "Id[%s], Type[%d], 短链[%s], 长链[%s]", videoBean.getIdStr(), videoBean.getType(), urlBean.getUrl_short(), urlBean.getUrl_long());
                    }
                }
            } while (videoQueue.size() > 0);

            Logger.d(TAG, "存库 %d 条数据", videoList.size());
            SinaDB.getDB().insert(null, videoList);
        }

    }

    public static boolean isWeipai(String url) {
        if (url.startsWith("http://www.miaopai.com") ||
                url.startsWith("http://m.miaopai.com") ||
                url.startsWith("http://miaopai.com")) {
            return true;
        }

        return false;
    }

    public static boolean isSinaVideo(String url) {
        if (url.startsWith("http://video.weibo.com")) {
            return true;
        }

        return false;
    }


}
