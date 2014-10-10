package org.sina.android.bean;

import java.io.Serializable;
import java.util.List;

public class GroupMemberListed implements Serializable {

	private static final long serialVersionUID = -3904702302585001377L;

	private String uid;

	private List<GroupListed> lists;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public List<GroupListed> getLists() {
		return lists;
	}

	public void setLists(List<GroupListed> lists) {
		this.lists = lists;
	}

}
