package org.sina.android.bean;

import java.io.Serializable;
import java.util.List;

public class WeiboUsers implements Serializable {

	private static final long serialVersionUID = 6598510583769514324L;
	private List<WeiBoUser> users;

	public List<WeiBoUser> getUsers() {
		return users;
	}

	public void setUsers(List<WeiBoUser> users) {
		this.users = users;
	}

}
