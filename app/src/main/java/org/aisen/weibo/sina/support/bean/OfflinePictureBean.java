package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

/**
 * Created by wangdan on 15/5/3.
 */
public class OfflinePictureBean implements Serializable {

    private static final long serialVersionUID = -8360317840805209421L;

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