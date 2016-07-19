package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

/**
 * Created by wangdan on 16/7/19.
 */
public class SavedImageBean implements Serializable {

    private static final long serialVersionUID = 118971088431402132L;

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
