package org.sina.android.bean;

import java.io.Serializable;
import java.util.List;

import com.m.support.iclass.IResult;

public class Friendship implements Serializable, IResult {

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
	
	private boolean cache;// 是否是缓存数据
	
	private boolean _expired;
	
	private boolean _noMore;
	
	private String[] pagingIndex;

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

	public boolean isCache() {
		return cache;
	}

	public void setCache(boolean cache) {
		this.cache = cache;
	}

	@Override
	public boolean expired() {
		return _expired;
	}
	
	public void setExpired(boolean expired) {
		this._expired = expired;
	}

	@Override
	public boolean noMore() {
		return _noMore;
	}
	
	public void setNoMore(boolean noMore) {
		this._noMore = noMore;
	}

	@Override
	public String[] pagingIndex() {
		return pagingIndex;
	}
	
	public void setPagingIndex(String[] pagingIndex) {
		this.pagingIndex = pagingIndex;
	}

}
