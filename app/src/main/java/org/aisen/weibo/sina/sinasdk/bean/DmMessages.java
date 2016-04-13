package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.network.biz.IResult;
import org.aisen.android.support.bean.ResultBean;

import java.io.Serializable;
import java.util.List;

public class DmMessages extends ResultBean implements Serializable {

	private static final long serialVersionUID = -6212279307812708266L;
	
	private List<DmMessage> user_list;

	private int next_cursor;

	private int previous_cursor;

	private String totalNumber;

	public List<DmMessage> getUser_list() {
		return user_list;
	}

	public void setUser_list(List<DmMessage> user_list) {
		this.user_list = user_list;
	}

	public int getNext_cursor() {
		return next_cursor;
	}

	public void setNext_cursor(int next_cursor) {
		this.next_cursor = next_cursor;
	}

	public int getPrevious_cursor() {
		return previous_cursor;
	}

	public void setPrevious_cursor(int previous_cursor) {
		this.previous_cursor = previous_cursor;
	}

	public String getTotalNumber() {
		return totalNumber;
	}

	public void setTotalNumber(String totalNumber) {
		this.totalNumber = totalNumber;
	}
	
}
