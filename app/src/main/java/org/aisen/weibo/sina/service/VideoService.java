package org.aisen.weibo.sina.service;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.http.DefHttpUtility;
import org.aisen.android.network.http.HttpConfig;
import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.UrlBean;
import org.aisen.weibo.sina.sinasdk.bean.UrlsBean;
import org.aisen.weibo.sina.support.bean.VideoBean;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 解析视频链接
 *
 * Created by wangdan on 16/7/20.
 */
public class VideoService {

    public static void start(Context context) {
        context.startService(new Intent(context, VideoService.class));
    }

    public static final int TYPE_VIDEO_WEIPAI = 0;

    public static final int TYPE_VIDEO_SINA = 1;

    public static final int TYPE_VIDEO_NONE = 2;

    public static final int TYPE_PHOTO = 3;

    public static final int TYPE_VIDEO_MEIPAI = 4;

    static final String TAG = VideoService.class.getSimpleName();

    public static void parseCommentURL(List<StatusComment> comments) throws TaskException {
        if (comments.size() == 0)
            return;

        List<String> shortUrlList = new ArrayList<>();

        Map<String, List<StatusComment>> url2status = new HashMap<>();

        // 把未解析的短连接拎出来
        for (StatusComment comment : comments) {
            String content = comment.getText();
            if (!TextUtils.isEmpty(content)) {
                SpannableString spannableString = SpannableString.valueOf(content);
                Linkify.addLinks(spannableString, Pattern.compile("http://[a-zA-Z0-9+&@#/%?=~_\\-|!:,\\.;]*[a-zA-Z0-9+&@#/%=~_|]"), "http://");
                URLSpan[] urlSpans = spannableString.getSpans(0, spannableString.length(), URLSpan.class);
                for (URLSpan urlSpan : urlSpans) {
                    if (!urlSpan.getURL().startsWith("http://t.cn/"))
                        continue;

                    shortUrlList.add(urlSpan.getURL());

                    List<StatusComment> l = url2status.get(urlSpan.getURL());
                    if (l == null) {
                        l = new ArrayList<>();

                        url2status.put(urlSpan.getURL(), l);
                    }
                    if (!l.contains(comment))
                        l.add(comment);
                }
            }
        }

        if (shortUrlList.size() > 0) {
            do {
                String[] parseArr = new String[20];
                for (int i = 0; i < parseArr.length; i++) {
                    if (shortUrlList.size() > 0) {
                        parseArr[i] = shortUrlList.remove(0);
                    }
                }

                Logger.w(TAG, parseArr);
                UrlsBean urlsBean = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).urlShort2Long(parseArr);
                for (UrlBean urlBean : urlsBean.getUrls()) {
                    String id = KeyGenerator.generateMD5(urlBean.getUrl_short());

                    List<StatusComment> statusList = url2status.get(urlBean.getUrl_short());
                    for (StatusComment s : statusList) {
                        s.setVideoUrl(urlBean);

                        VideoBean videoBean = SinaDB.getDB().selectById(null, VideoBean.class, id);
                        boolean isNew = false;
                        if (videoBean == null) {
                            videoBean = new VideoBean();
                            isNew = true;
                        }
                        videoBean.setIdStr(id);
                        videoBean.setShortUrl(urlBean.getUrl_short());
                        videoBean.setLongUrl(urlBean.getUrl_long());

                        if (isSinaVideo(urlBean.getUrl_long())) {
                            videoBean.setType(VideoService.TYPE_VIDEO_SINA);

                            s.setPicture(true);
                        }
                        else if (isWeipai(urlBean.getUrl_long())) {
                            videoBean.setType(VideoService.TYPE_VIDEO_WEIPAI);

                            s.setPicture(true);
                        }
                        else if (isPhoto(urlBean.getUrl_long())) {
                            videoBean.setType(VideoService.TYPE_PHOTO);

                            s.setPicture(true);
                        }
                        else {
                            videoBean.setType(VideoService.TYPE_VIDEO_NONE);
                        }

                        if (isNew) {
                            SinaDB.getDB().insert(null, videoBean);
                        }
                        else {
                            SinaDB.getDB().update(null, videoBean);
                        }

                        Logger.v(TAG, "Id[%s], Type[%d], 短链[%s], 长链[%s]", videoBean.getIdStr(), videoBean.getType(), urlBean.getUrl_short(), urlBean.getUrl_long());
                    }
                }
            } while (shortUrlList.size() > 0);
        }
    }

    public static void parseStatusURL(List<StatusContent> statusContents) throws TaskException {
        if (statusContents.size() == 0)
            return;

        List<String> shortUrlList = new ArrayList<>();

        Map<String, List<StatusContent>> url2status = new HashMap<>();

        // 把未解析的短连接拎出来
        for (StatusContent statusContent : statusContents) {
            String content = statusContent.getText();
            if (!TextUtils.isEmpty(content)) {
                SpannableString spannableString = SpannableString.valueOf(content);
                Linkify.addLinks(spannableString, Pattern.compile("http://[a-zA-Z0-9+&@#/%?=~_\\-|!:,\\.;]*[a-zA-Z0-9+&@#/%=~_|]"), "http://");
                URLSpan[] urlSpans = spannableString.getSpans(0, spannableString.length(), URLSpan.class);
                for (URLSpan urlSpan : urlSpans) {
                    if (!urlSpan.getURL().startsWith("http://t.cn/"))
                        continue;

                    shortUrlList.add(urlSpan.getURL());

                    List<StatusContent> l = url2status.get(urlSpan.getURL());
                    if (l == null) {
                        l = new ArrayList<>();

                        url2status.put(urlSpan.getURL(), l);
                    }
                    if (!l.contains(statusContent))
                        l.add(statusContent);
                }
            }

            if (statusContent.getRetweeted_status() != null) {
                statusContent = statusContent.getRetweeted_status();

                content = statusContent.getText();
                if (!TextUtils.isEmpty(content)) {
                    SpannableString spannableString = SpannableString.valueOf(content);
                    Linkify.addLinks(spannableString, Pattern.compile("http://[a-zA-Z0-9+&@#/%?=~_\\-|!:,\\.;]*[a-zA-Z0-9+&@#/%=~_|]"), "http://");
                    URLSpan[] urlSpans = spannableString.getSpans(0, spannableString.length(), URLSpan.class);
                    for (URLSpan urlSpan : urlSpans) {
                        if (!urlSpan.getURL().startsWith("http://t.cn/"))
                            continue;

                        shortUrlList.add(urlSpan.getURL());

                        List<StatusContent> l = url2status.get(urlSpan.getURL());
                        if (l == null) {
                            l = new ArrayList<>();

                            url2status.put(urlSpan.getURL(), l);
                        }
                        if (!l.contains(statusContent))
                            l.add(statusContent);
                    }
                }
            }

        }

        if (shortUrlList.size() > 0) {
            do {
                String[] parseArr = new String[20];
                for (int i = 0; i < parseArr.length; i++) {
                    if (shortUrlList.size() > 0) {
                        parseArr[i] = shortUrlList.remove(0);
                    }
                }

                Logger.w(TAG, parseArr);
                UrlsBean urlsBean = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).urlShort2Long(parseArr);
                for (UrlBean urlBean : urlsBean.getUrls()) {
                    String id = KeyGenerator.generateMD5(urlBean.getUrl_short());

                    List<StatusContent> statusList = url2status.get(urlBean.getUrl_short());
                    for (StatusContent s : statusList) {
                        s.setVideoUrl(urlBean);

                        boolean isNew = false;
                        VideoBean videoBean = SinaDB.getDB().selectById(null, VideoBean.class, id);
                        if (videoBean == null) {
                            videoBean = new VideoBean();
                            isNew = true;
                        }
                        videoBean.setIdStr(id);
                        videoBean.setShortUrl(urlBean.getUrl_short());
                        videoBean.setLongUrl(urlBean.getUrl_long());

                        if (isSinaVideo(urlBean.getUrl_long())) {
                            videoBean.setType(VideoService.TYPE_VIDEO_SINA);

                            s.setVideo(true);
                        }
                        else if (isWeipai(urlBean.getUrl_long())) {
                            videoBean.setType(VideoService.TYPE_VIDEO_WEIPAI);

                            s.setVideo(true);
                        }
                        else if (isMeipai(urlBean.getUrl_long())) {
                            videoBean.setType(VideoService.TYPE_VIDEO_MEIPAI);

                            s.setVideo(true);
                        }
                        else if (isPhoto(urlBean.getUrl_long())) {
                            videoBean.setType(VideoService.TYPE_PHOTO);
                        }
                        else {
                            videoBean.setType(VideoService.TYPE_VIDEO_NONE);
                        }

                        if (isNew) {
                            SinaDB.getDB().insert(null, videoBean);
                        }
                        else {
                            SinaDB.getDB().update(null, videoBean);
                        }

                        Logger.v(TAG, "Id[%s], Type[%d], 短链[%s], 长链[%s]", videoBean.getIdStr(), videoBean.getType(), urlBean.getUrl_short(), urlBean.getUrl_long());
                    }
                }
            } while (shortUrlList.size() > 0);
        }
    }

    private static boolean isVideoValid(VideoBean bean) {
        if (bean != null && !TextUtils.isEmpty(bean.getLongUrl()))
            return true;

        return false;
    }

    public static VideoBean getVideoFromWeipai(VideoBean video) throws Exception {
        Document dom = Jsoup.connect(video.getLongUrl()).get();

        video.setIdStr(KeyGenerator.generateMD5(video.getShortUrl()));

        Elements divs = dom.select("div[class=video_img WscaleH]");
        if (divs != null && divs.size() > 0) {
            video.setImage(divs.get(0).attr("data-url"));
        }
        divs = dom.select("video#video");
        if (divs != null && divs.size() > 0) {
            video.setVideoUrl(divs.get(0).attr("src"));
        }

        return video;
    }

    public static void getPicture(VideoBean video) throws Exception {
        if (TextUtils.isEmpty(AppContext.getAccount().getCookie())) {
            throw new TaskException("123", "解析链接失败");
        }

        HttpConfig config = new HttpConfig();
        config.baseUrl = video.getShortUrl();
        config.cookie = AppContext.getAccount().getCookie();
        config.addHeader("Content-Type", "text/html;charset=utf-8");

        Setting action = new Setting();
        action.setType("");
        action.setValue("");
        action.setDescription("");

        String response = new DefHttpUtility().doGet(config, action, null, String.class);

        Document dom = Jsoup.parse(response);

        video.setIdStr(KeyGenerator.generateMD5(video.getShortUrl()));

        Elements divs = dom.select("img");
        if (divs != null && divs.size() > 0) {
            video.setImage(divs.get(0).attr("src"));
            video.setImage(video.getImage().replace("bmiddle", "small").replace("thumbnail", "small"));
        }

        if (TextUtils.isEmpty(video.getImage())) {
            String longUrl = video.getLongUrl();

            if (TextUtils.isEmpty(longUrl)) {
                UrlsBean urlsBean = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).urlShort2Long(video.getShortUrl());
                if (urlsBean.getUrls() != null && urlsBean.getUrls().size() > 0) {
                    longUrl = urlsBean.getUrls().get(0).getUrl_long();
                    longUrl.replace("bmiddle", "small").replace("thumbnail", "small");
                }
            }
            if (!TextUtils.isEmpty(longUrl)) {
                video.setImage(longUrl);
            }
        }
    }

    public static VideoBean getVideoFromMeipai(VideoBean video) throws Exception {
        Document dom = Jsoup.connect(video.getLongUrl()).get();

        Elements divs = dom.select("div#mediaPlayer");
        if (divs != null && divs.size() > 0) {
            Element div = divs.get(0);

            video.setVideoUrl(div.attr("data-video"));
            video.setImage(div.attr("data-poster"));
        }

        video.setIdStr(KeyGenerator.generateMD5(video.getShortUrl()));

        return video;
    }

    public static VideoBean getVideoFromSinaVideo(VideoBean video) throws Exception {
        Document dom = Jsoup.connect(video.getLongUrl()).get();

        video.setIdStr(KeyGenerator.generateMD5(video.getShortUrl()));

        Elements divs = dom.select("video.video");
        if (divs != null && divs.size() > 0) {
            String src = divs.get(0).attr("src");
            src = src.replace("amp;", "");

            video.setVideoUrl(src);
        }
        divs = dom.select("img.poster");
        if (divs != null && divs.size() > 0) {
            video.setImage(divs.get(0).attr("src"));
        }

        return video;
    }

    public static boolean isPhoto(String url) {
        if (url.startsWith("http://photo.weibo.com/h5") ||
                url.indexOf("sinaimg.cn") != -1) {
            return true;
        }

        return false;
    }

    public static boolean isWeipai(String url) {
        if (url.startsWith("http://www.miaopai.com") ||
                url.startsWith("http://m.miaopai.com") ||
                url.startsWith("http://miaopai.com")) {
            return true;
        }

        return false;
    }

    public static boolean isMeipai(String url) {
        if (url.indexOf("http://www.meipai.com") != -1) {
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

    public static boolean isVideo(int type) {
        switch (type) {
            case TYPE_VIDEO_MEIPAI:
            case TYPE_VIDEO_SINA:
            case TYPE_VIDEO_WEIPAI:
                return true;
            default:
                return false;
        }
    }

}
