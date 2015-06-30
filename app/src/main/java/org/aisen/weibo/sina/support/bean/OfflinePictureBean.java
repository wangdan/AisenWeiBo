package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

import org.aisen.orm.annotation.PrimaryKey;

/**
 * Created by wangdan on 15/5/3.
 */
public class OfflinePictureBean implements Serializable {

    private String thumb;// 缩略图

    private long length;// 图片大小

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}