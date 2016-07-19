package org.aisen.weibo.sina.support.bean;

import org.aisen.android.component.orm.annotation.PrimaryKey;

import java.io.Serializable;

/**
 * Created by wangdan on 16/7/19.
 */
public class WeipaiVideoBean implements Serializable {

    private static final long serialVersionUID = -4102724688858341090L;

    private String url;

    private String image;

    private String videoUrl;

    @PrimaryKey(column = "idstr")
    private String idStr;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
