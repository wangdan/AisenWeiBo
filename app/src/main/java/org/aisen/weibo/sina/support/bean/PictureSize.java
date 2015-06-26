package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

import org.aisen.orm.annotation.PrimaryKey;

/**
 * Created by wangdan on 15/5/2.
 */
public class PictureSize implements Serializable {

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
