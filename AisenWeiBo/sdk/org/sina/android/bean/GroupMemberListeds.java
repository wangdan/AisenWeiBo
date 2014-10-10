package org.sina.android.bean;

import java.io.Serializable;
import java.util.List;


public class GroupMemberListeds implements Serializable{

	private static final long serialVersionUID = -5571011756100278056L;

	private List<GroupMemberListed> lists;
	
	private String uid;

	public List<GroupMemberListed> getLists() {
		return lists;
	}

	public void setLists(List<GroupMemberListed> lists) {
		this.lists = lists;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
}
