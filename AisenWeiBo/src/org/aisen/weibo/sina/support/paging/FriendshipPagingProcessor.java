package org.aisen.weibo.sina.support.paging;

import org.sina.android.bean.Friendship;
import org.sina.android.bean.WeiBoUser;

import com.m.support.paging.IPaging;

/**
 * 好友关系分页
 * 
 * @author wangdan
 * 
 */
public class FriendshipPagingProcessor implements IPaging<WeiBoUser, Friendship> {

	private static final long serialVersionUID = 512475769503397868L;

	private int nextCursor = 0;

	@Override
	public IPaging<WeiBoUser, Friendship> newInstance() {
		return new FriendshipPagingProcessor();
	}

	@Override
	public void processData(Friendship newDatas, WeiBoUser firstData, WeiBoUser lastData) {
		if (newDatas != null) {
			if (newDatas.getNext_cursor() == 0)
				nextCursor = -1;
			else
				nextCursor = newDatas.getNext_cursor();
		}
	}

	@Override
	public String getPreviousPage() {
		return "";
	}

	@Override
	public String getNextPage() {
		return Integer.toString(nextCursor);
	}

	@Override
	public boolean canRefresh() {
		return true;
	}

	@Override
	public boolean canUpdate() {
		return nextCursor != -1;
	}

	@Override
	public void setPage(String previousPage, String nextPage) {
		
	}

}
