package org.aisen.weibo.sina.sinasdk.bean;

import java.io.Serializable;

public class StatusesIds implements Serializable {

	private static final long serialVersionUID = -6755072699630147474L;

	private String[] statuses;
	
	private long previous_cursor;
	
	private long next_cursor;
	
//	private int total_number;

	public String[] getStatuses() {
		return statuses;
	}

	public void setStatuses(String[] statuses) {
		this.statuses = statuses;
	}

	public long getPrevious_cursor() {
		return previous_cursor;
	}

	public void setPrevious_cursor(long previous_cursor) {
		this.previous_cursor = previous_cursor;
	}

	public long getNext_cursor() {
		return next_cursor;
	}

	public void setNext_cursor(long next_cursor) {
		this.next_cursor = next_cursor;
	}
	
}
