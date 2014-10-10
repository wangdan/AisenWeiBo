package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

import com.m.support.sqlite.annotation.Id;

public class SearchHisotyBean implements Serializable {

	private static final long serialVersionUID = -2947173260410243085L;

	@Id(column = "beanId")
	private String beanId;
	
	private String type;
	
	private String query;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getBeanId() {
		return beanId;
	}

	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}

}
