package org.aisen.android.support.bean;

import java.io.Serializable;

/**
 * ViewPager的Tab页标签
 *
 * Created by wangdan on 15/12/22.
 */
public class TabItem implements Serializable {

    private static final long serialVersionUID = -1162756298239591517L;

    private String type;

    private String title;

    private Serializable tag;

    public TabItem() {

    }

    public TabItem(String type, String title) {
        this.type = type;
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Serializable getTag() {
        return tag;
    }

    public void setTag(Serializable tag) {
        this.tag = tag;
    }

}
