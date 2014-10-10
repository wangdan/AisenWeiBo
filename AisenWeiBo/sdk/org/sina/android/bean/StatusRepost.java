package org.sina.android.bean;

import java.io.Serializable;
import java.util.List;

import com.m.support.iclass.IResult;

public class StatusRepost implements Serializable, IResult {

	private static final long serialVersionUID = 1388689503057498611L;

	private List<StatusContent> reposts;
	
	private boolean cache;
	
	private boolean _expired;
	
	private boolean _noMore;

	public StatusRepost() {
		
	}
	
	public StatusRepost(List<StatusContent> reposts) {
		this.reposts = reposts;
	}

	public List<StatusContent> getReposts() {
		return reposts;
	}

	public void setReposts(List<StatusContent> reposts) {
		this.reposts = reposts;
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
		return null;
	}

}
