package org.aisen.weibo.sina.support.paging;

import android.text.TextUtils;

import org.aisen.android.support.paging.IPaging;

import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusComments;

/**
 * 评论分页
 * 
 * @author wangdan
 * 
 */
public class CommentsPagingProcessor implements IPaging<StatusComment, StatusComments> {

	private static final long serialVersionUID = 6968903478998151211L;

	private String firstId;

	private String lastId;

	@Override
	public IPaging<StatusComment, StatusComments> newInstance() {
		return new CommentsPagingProcessor();
	}

	@Override
	public void processData(StatusComments newDatas, StatusComment firstData, StatusComment lastData) {
		if (firstData != null)
			firstId = AisenUtils.getId(firstData);
		if (lastData != null)
			lastId = AisenUtils.getId(lastData);
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
		
	}

}