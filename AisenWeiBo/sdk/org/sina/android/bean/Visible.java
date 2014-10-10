package org.sina.android.bean;

import java.io.Serializable;

public class Visible implements Serializable {

	private static final long serialVersionUID = 7285113172667412284L;

	private String type;

	private String list_id;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getList_id() {
		return list_id;
	}

	public void setList_id(String list_id) {
		this.list_id = list_id;
	}

}
