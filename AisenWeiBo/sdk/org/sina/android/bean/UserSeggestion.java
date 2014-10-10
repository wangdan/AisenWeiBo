package org.sina.android.bean;

import java.io.Serializable;

public class UserSeggestion implements Serializable {

	private static final long serialVersionUID = -2562092744843812654L;

	private String uid;

	private String screen_name;

	private String followers_count;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getScreen_name() {
		return screen_name;
	}

	public void setScreen_name(String screen_name) {
		this.screen_name = screen_name;
	}

	public String getFollowers_count() {
		return followers_count;
	}

	public void setFollowers_count(String followers_count) {
		this.followers_count = followers_count;
	}

}
