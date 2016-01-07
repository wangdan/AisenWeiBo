package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

/**
 * Created by wangdan on 15-3-3.
 */
public class PicSize implements Serializable {

    private static final long serialVersionUID = -2046887085192115167L;

    private int width;

    private int height;

    private String key;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
