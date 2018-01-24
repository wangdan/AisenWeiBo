package org.aisen.weibo.sina.sinasdk.bean;

import java.io.Serializable;

/**
 * Created by wangdan on 16/8/10.
 */
public class HotPic implements Serializable {

    private static final long serialVersionUID = -2056862337785897636L;

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
