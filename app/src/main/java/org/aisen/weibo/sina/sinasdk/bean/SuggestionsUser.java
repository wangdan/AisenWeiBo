package org.aisen.weibo.sina.sinasdk.bean;

import java.io.Serializable;

public class SuggestionsUser implements Serializable {

	private static final long serialVersionUID = 5785614685694735554L;

	private String uid;
	
	private Integer followers_count;
	
	private String screen_name;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Integer getFollowers_count() {
		return followers_count;
	}

	public void setFollowers_count(Integer followers_count) {
		this.followers_count = followers_count;
	}

	public String getScreen_name() {
		return screen_name;
	}

	public void setScreen_name(String screen_name) {
		this.screen_name = screen_name;
	}
	
}
