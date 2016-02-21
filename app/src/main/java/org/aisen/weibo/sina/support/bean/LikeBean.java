package org.aisen.weibo.sina.support.bean;


import org.aisen.android.component.orm.annotation.PrimaryKey;

/**
 * Created by wangdan on 15-3-6.
 */
public class LikeBean {

    @PrimaryKey(column = "statusId")
    private String statusId;

    private boolean liked;

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
