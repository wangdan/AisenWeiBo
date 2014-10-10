package org.sina.android.bean;

import java.io.Serializable;
import java.util.List;

public class GroupMembers implements Serializable {

	private static final long serialVersionUID = 5349456647258503053L;

	private List<WeiBoUser> users;

	private String next_cursor;

	private String previous_cursor;

	private Integer total_number;

	public List<WeiBoUser> getUsers() {
		return users;
	}

	public void setUsers(List<WeiBoUser> users) {
		this.users = users;
	}

	public String getNext_cursor() {
		return next_cursor;
	}

	public void setNext_cursor(String next_cursor) {
		this.next_cursor = next_cursor;
	}

	public String getPrevious_cursor() {
		return previous_cursor;
	}

	public void setPrevious_cursor(String previous_cursor) {
		this.previous_cursor = previous_cursor;
	}

	public Integer getTotal_number() {
		return total_number;
	}

	public void setTotal_number(Integer total_number) {
		this.total_number = total_number;
	}

}
