package org.aisen.weibo.sina.sinasdk.bean;

/**
 * Created by wangdan on 15/6/29.
 */
public class UploadPictureResultBean extends BaseSinaBean {

    private static final long serialVersionUID = -5742423620822198794L;

    private String pic_id;

    private String thumbnail_pic;

    public String getPic_id() {
        return pic_id;
    }

    public void setPic_id(String pic_id) {
        this.pic_id = pic_id;
    }

    public String getThumbnail_pic() {
        return thumbnail_pic;
    }

    public void setThumbnail_pic(String thumbnail_pic) {
        this.thumbnail_pic = thumbnail_pic;
    }
}
