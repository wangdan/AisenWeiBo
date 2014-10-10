package org.sina.android.bean;

import java.io.Serializable;
import java.util.List;

public class Groups implements Serializable {

	private static final long serialVersionUID = 8481611406822843100L;
	private List<Group> lists;
	
	private String total_number;

	public Groups() {

	}

	public Groups(List<Group> lists) {
		this.lists = lists;
	}

	public List<Group> getLists() {
		return lists;
	}

	public void setLists(List<Group> lists) {
		this.lists = lists;
	}

	public String getTotal_number() {
		return total_number;
	}

	public void setTotal_number(String total_number) {
		this.total_number = total_number;
	}

}
