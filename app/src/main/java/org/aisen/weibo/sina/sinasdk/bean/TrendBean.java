package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.component.orm.annotation.PrimaryKey;

import java.io.Serializable;

public class TrendBean implements Serializable {

	private static final long serialVersionUID = -7535208511111741372L;

	private String name;

	@PrimaryKey(column = "query")
	private String query;
	
	private String amount;
	
	private String delta;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getDelta() {
		return delta;
	}

	public void setDelta(String delta) {
		this.delta = delta;
	}
	
}
