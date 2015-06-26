package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.network.biz.IResult;

import java.io.Serializable;
import java.util.List;

public class TrendsBean implements Serializable, IResult {

	private static final long serialVersionUID = -1880799989137644825L;

	private List<TrendBean> list;
	
	private boolean cache;// 是否是缓存数据
	
	private boolean _expired;
	
	private boolean _noMore;

	public List<TrendBean> getList() {
		return list;
	}

	public void setList(List<TrendBean> list) {
		this.list = list;
	}
	
	@Override
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
