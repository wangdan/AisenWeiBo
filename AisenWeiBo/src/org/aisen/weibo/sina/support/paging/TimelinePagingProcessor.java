package org.aisen.weibo.sina.support.paging;

import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.StatusContents;

import android.text.TextUtils;

import com.m.support.paging.IPaging;

/**
 * 微博分页
 * 
 * @author wangdan
 * 
 */
public class TimelinePagingProcessor implements IPaging<StatusContent, StatusContents> {

	private static final long serialVersionUID = -1563104012290641720L;

	private String firstId;

	private String lastId;

	@Override
	public IPaging<StatusContent, StatusContents> newInstance() {
		return new TimelinePagingProcessor();
	}

	@Override
	public void processData(StatusContents newDatas, StatusContent firstData, StatusContent lastData) {
		if (firstData != null)
			firstId = AisenUtil.getId(firstData);
		if (lastData != null)
			lastId = AisenUtil.getId(lastData);
	}

	@Override
	public String getPreviousPage() {
		return firstId;
	}

	@Override
	public String getNextPage() {
		if (TextUtils.isEmpty(lastId))
			return null;

		return (Long.parseLong(lastId) - 1) + "";
	}

	@Override
	public boolean canRefresh() {
		return true;
	}

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void setPage(String previousPage, String nextPage) {
		this.firstId = previousPage;
		this.lastId = nextPage;
	}

}