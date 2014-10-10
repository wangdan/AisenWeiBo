package org.aisen.weibo.sina.support.paging;

import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.sina.android.bean.StatusComment;
import org.sina.android.bean.StatusComments;

import android.text.TextUtils;

import com.m.support.paging.IPaging;

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
		
	}

}