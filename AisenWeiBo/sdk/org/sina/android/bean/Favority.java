package org.sina.android.bean;

import java.io.Serializable;

public class Favority implements Serializable {

	private static final long serialVersionUID = -8036008298041521000L;

	private StatusContent status;

	private String favorited_time;

	public StatusContent getStatus() {
		return status;
	}

	public void setStatus(StatusContent status) {
		this.status = status;
	}

	public String getFavorited_time() {
		return favorited_time;
	}

	public void setFavorited_time(String favorited_time) {
		this.favorited_time = favorited_time;
	}

}
