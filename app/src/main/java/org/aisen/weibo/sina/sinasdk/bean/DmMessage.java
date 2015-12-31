package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.component.orm.annotation.PrimaryKey;

import java.io.Serializable;

public class DmMessage implements Serializable {

	private static final long serialVersionUID = 8398707338476241682L;

	@PrimaryKey(column = "beanId")
	private String id;
	
	private WeiBoUser user;
	
	private DirectMessage direct_message;
	
	private int unread_count;

	public WeiBoUser getUser() {
		return user;
	}

	public void setUser(WeiBoUser user) {
		this.user = user;
	}

	public DirectMessage getDirect_message() {
		return direct_message;
	}

	public void setDirect_message(DirectMessage direct_message) {
		this.direct_message = direct_message;
	}

	public int getUnread_count() {
		return unread_count;
	}

	public void setUnread_count(int unread_count) {
		this.unread_count = unread_count;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
