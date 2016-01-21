package org.aisen.weibo.sina.support.bean;

import org.aisen.android.component.orm.annotation.PrimaryKey;

import java.io.Serializable;

/**
 * Created by wangdan on 15/5/2.
 */
public class PictureSize implements Serializable {

    private static final long serialVersionUID = -5213202786229561729L;

    @PrimaryKey(column = "url")
    private String url;

    private long size;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
