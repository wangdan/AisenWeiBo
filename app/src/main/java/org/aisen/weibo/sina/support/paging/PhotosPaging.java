package org.aisen.weibo.sina.support.paging;

import android.text.TextUtils;

import org.aisen.android.support.paging.IPaging;
import org.aisen.weibo.sina.support.bean.PhotoBean;
import org.aisen.weibo.sina.support.bean.PhotosBean;

/**
 * 微博分页
 * 
 * @author wangdan
 * 
 */
public class PhotosPaging implements IPaging<PhotoBean, PhotosBean> {

	private static final long serialVersionUID = -1563104012290641720L;

	private String firstId;

	private String lastId;

	@Override
	public void processData(PhotosBean newDatas, PhotoBean firstData, PhotoBean lastData) {
		if (firstData != null)
			firstId = String.valueOf(firstData.getStatus().getId());
		if (lastData != null)
			lastId = String.valueOf(lastData.getStatus().getId());
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

}