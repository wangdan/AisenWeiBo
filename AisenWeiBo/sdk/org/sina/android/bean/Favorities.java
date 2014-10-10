package org.sina.android.bean;

import java.io.Serializable;
import java.util.List;

import com.m.support.iclass.IResult;

public class Favorities implements Serializable, IResult {

	private static final long serialVersionUID = 7517616720409609120L;

	private List<Favority> favorites;

	private int total_number;
	
	private boolean cache;
	
	private boolean _expired;
	
	private boolean _noMore;
	
	private String[] pagingIndex;

	public List<Favority> getFavorites() {
		return favorites;
	}

	public void setFavorites(List<Favority> favorites) {
		this.favorites = favorites;
	}

	public int getTotal_number() {
		return total_number;
	}

	public void setTotal_number(int total_number) {
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
