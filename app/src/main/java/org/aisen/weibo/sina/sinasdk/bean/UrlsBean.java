package org.aisen.weibo.sina.sinasdk.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangdan on 16/7/20.
 */
public class UrlsBean implements Serializable {

    private static final long serialVersionUID = -363587778546952665L;

    private List<UrlBean> urls;

    public List<UrlBean> getUrls() {
        return urls;
    }

    public void setUrls(List<UrlBean> urls) {
        this.urls = urls;
    }

}
