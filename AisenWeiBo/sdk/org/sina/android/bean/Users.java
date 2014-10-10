package org.sina.android.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Users implements Serializable {

	private static final long serialVersionUID = 7267744057748550572L;
	private List<WeiBoUser> users;

	public Users() {
		users = new ArrayList<WeiBoUser>();
	}

	public Users(List<WeiBoUser> users) {
		this.users = users;
	}

	public List<WeiBoUser> getUsers() {
		return users;
	}

	public void setUsers(List<WeiBoUser> users) {
		this.users = users;
	}

}
