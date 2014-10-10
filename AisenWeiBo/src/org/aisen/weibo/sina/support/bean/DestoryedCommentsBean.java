package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

import com.m.support.sqlite.annotation.Id;

public class DestoryedCommentsBean implements Serializable {

	private static final long serialVersionUID = -2982307076088936016L;

	@Id(column = "statusId")
	private String cmtId;
	
	public DestoryedCommentsBean() {
		
	}
	
	public DestoryedCommentsBean(String cmtId) {
		this.cmtId = cmtId;
	}

	public String getCmtId() {
		return cmtId;
	}

	public void setCmtId(String cmtId) {
		this.cmtId = cmtId;
	}
	
}
