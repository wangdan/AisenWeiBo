package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.support.bean.ResultBean;

import java.io.Serializable;
import java.util.List;

public class Friendship extends ResultBean implements Serializable {

	private static final long serialVersionUID = 1116859231214821370L;

	/**
	 * 用户群
	 */
	private List<WeiBoUser> users;

	/**
	 * 结果下一页的游标
	 */
	private Integer next_cursor = -1;

	/**
	 * 结果上一页的游标
	 */
	private Integer previous_cursor;

	/**
	 * 用户总数
	 */
	private Integer total_number;

	public List<WeiBoUser> getUsers() {
		return users;
	}

	public void setUsers(List<WeiBoUser> users) {
		this.users = users;
	}

	public Integer getNext_cursor() {
		return next_cursor;
	}

	public void setNext_cursor(Integer next_cursor) {
		this.next_cursor = next_cursor;
	}

	public Integer getPrevious_cursor() {
		return previous_cursor;
	}

	public void setPrevious_cursor(Integer previous_cursor) {
		this.previous_cursor = previous_cursor;
	}

	public Integer getTotal_number() {
		return total_number;
	}

	public void setTotal_number(Integer total_number) {
		this.total_number = total_number;
	}

}
