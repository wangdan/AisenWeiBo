package org.aisen.weibo.sina.support.bean;

import org.aisen.android.component.orm.annotation.PrimaryKey;
import org.aisen.android.ui.fragment.adapter.BasicRecycleViewAdapter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangdan on 16/3/14.
 */
public class JokeBean implements Serializable, BasicRecycleViewAdapter.ItemTypeData {

    private static final long serialVersionUID = -2772854043143937178L;

    @PrimaryKey(column = "id")
    private long id;

    private String excerpt;

    private String imgUrl;

    private String source;

    private int imgWidth;

    private int imgHeight;

    private int itemType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getImgWidth() {
        return imgWidth;
    }

    public void setImgWidth(int imgWidth) {
        this.imgWidth = imgWidth;
    }

    public int getImgHeight() {
        return imgHeight;
    }

    public void setImgHeight(int imgHeight) {
        this.imgHeight = imgHeight;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    @Override
    public int itemType() {
        return getItemType();
    }

}
