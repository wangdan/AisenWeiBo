package org.aisen.weibo.sina.support.bean;

import com.m.component.sqlite.annotation.AutoIncrementPrimaryKey;

/**
 * Created by wangdan on 15-3-6.
 */
public class LikeBean {

	@AutoIncrementPrimaryKey(column = "id")
	private int id;
	
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
    
}
