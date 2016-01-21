package org.aisen.weibo.sina.support.paging;

import org.aisen.android.support.paging.IPaging;
import org.aisen.weibo.sina.sinasdk.bean.Friendship;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

/**
 * 好友关系分页
 * 
 * @author wangdan
 * 
 */
public class FriendshipPaging implements IPaging<WeiBoUser, Friendship> {

	private static final long serialVersionUID = 512475769503397868L;

	private int nextCursor = 0;

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

}
