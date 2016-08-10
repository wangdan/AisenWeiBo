package org.aisen.weibo.sina.sinasdk.bean;

import java.io.Serializable;

/**
 * Created by wangdan on 16/8/10.
 */
public class StatusHotCard implements Serializable {

    private static final long serialVersionUID = -4772497779347234809L;

    private StatusMoblog mblog;

    public StatusMoblog getMblog() {
        return mblog;
    }

    public void setMblog(StatusMoblog mblog) {
        this.mblog = mblog;
    }

}
