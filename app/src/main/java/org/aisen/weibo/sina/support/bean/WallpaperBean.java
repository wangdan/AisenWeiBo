package org.aisen.weibo.sina.support.bean;

import org.aisen.android.component.orm.annotation.PrimaryKey;

import java.io.Serializable;

/**
 * Created by wangdan on 16/3/23.
 */
public class WallpaperBean implements Serializable {

    private static final long serialVersionUID = -6886432578977226567L;

    @PrimaryKey(column = "id")
    private long id;

    private String indexThumbnailUrl;

    private String detailUrl;

    private String normalUrl;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIndexThumbnailUrl() {
        return indexThumbnailUrl;
    }

    public void setIndexThumbnailUrl(String indexThumbnailUrl) {
        this.indexThumbnailUrl = indexThumbnailUrl;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public String getNormalUrl() {
        return normalUrl;
    }

    public void setNormalUrl(String normalUrl) {
        this.normalUrl = normalUrl;
    }
}
