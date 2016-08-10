package org.aisen.weibo.sina.sinasdk.bean;

import java.io.Serializable;

/**
 * Created by wangdan on 16/8/10.
 */
public class HotPicLarge implements Serializable {

    private static final long serialVersionUID = 6338157546034243398L;

    private HotPic large;

    public HotPic getLarge() {
        return large;
    }

    public void setLarge(HotPic large) {
        this.large = large;
    }

}
