package org.aisen.weibo.sina.support.bean;

import org.aisen.android.network.biz.IResult;

import java.io.Serializable;
import java.util.List;

public class PhotosBean implements Serializable, IResult {

	private static final long serialVersionUID = -5782864823784522733L;

	private boolean cache;// 是否是缓存数据
	
	private boolean _expired;
	
	private boolean _noMore;
	
	private List<PhotoBean> list;
	
	public List<PhotoBean> getList() {
		return list;
	}

	public void setList(List<PhotoBean> list) {
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
