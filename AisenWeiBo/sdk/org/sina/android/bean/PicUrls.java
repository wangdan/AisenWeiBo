package org.sina.android.bean;

import java.io.Serializable;

/**
 * Created by wangdan on 13-12-14.
 */
public class PicUrls implements Serializable{

	private static final long serialVersionUID = 2354439978931122615L;

	private String thumbnail_pic;

    public String getThumbnail_pic() {
        return thumbnail_pic;
    }

    public void setThumbnail_pic(String thumbnail_pic) {
        this.thumbnail_pic = thumbnail_pic;
    }
}
