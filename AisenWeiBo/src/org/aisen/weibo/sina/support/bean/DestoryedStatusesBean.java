package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

import com.m.support.sqlite.annotation.Id;

public class DestoryedStatusesBean implements Serializable {

	private static final long serialVersionUID = -2982307076088936016L;

	@Id(column = "statusId")
	private String statusId;
	
	public DestoryedStatusesBean() {
		
	}
	
	public DestoryedStatusesBean(String statusId) {
		this.statusId = statusId;
	}

	public String getStatusId() {
		return statusId;
	}

	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}
	
}
