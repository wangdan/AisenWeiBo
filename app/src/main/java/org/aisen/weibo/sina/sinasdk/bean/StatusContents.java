package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.network.biz.IResult;
import org.aisen.weibo.sina.support.bean.PhotosBean;
import org.aisen.weibo.sina.sys.service.OfflineService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StatusContents extends PhotosBean implements Serializable, IResult, OfflineService.OfflineLength {

	private static final long serialVersionUID = 2115103214814709009L;

	private List<StatusContent> statuses;

	private Long selectedGroupId;

	private int total_number;
	
	private boolean cache;// 是否是缓存数据
	
	private boolean _expired;
	
	private boolean _noMore;

    private long length;

	public StatusContents() {
		statuses = new ArrayList<StatusContent>();
	}

	public StatusContents(List<StatusContent> statuses) {
		this.statuses = statuses;
	}

	public List<StatusContent> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<StatusContent> statuses) {
		this.statuses = statuses;
	}

	public Long getSelectedGroupId() {
		return selectedGroupId;
	}

	public void setSelectedGroupId(Long selectedGroupId) {
		this.selectedGroupId = selectedGroupId;
	}

	public int getTotal_number() {
		return total_number;
	}

	public void setTotal_number(int total_number) {
		this.total_number = total_number;
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

    public long getLength() {
        return length;
    }

    @Override
    public void setLength(long length) {
        this.length = length;
    }
}
