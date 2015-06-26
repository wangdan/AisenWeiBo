package org.aisen.weibo.sina.sinasdk.bean;

import java.io.Serializable;

public class Ids implements Serializable {

	private static final long serialVersionUID = 3451387509438963609L;

	private String[] ids;

	private String total_number;

	public String[] getIds() {
		return ids;
	}

	public void setIds(String[] ids) {
		this.ids = ids;
	}

	public String getTotal_number() {
		return total_number;
	}

	public void setTotal_number(String total_number) {
		this.total_number = total_number;
	}

}
