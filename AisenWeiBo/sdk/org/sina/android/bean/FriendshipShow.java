package org.sina.android.bean;

import java.io.Serializable;


/**
 * 两个用户之间的相互关系
 * 
 * @author wangd
 * 
 */
public class FriendshipShow implements Serializable{

	private static final long serialVersionUID = 175084750315441537L;

	private WeiBoUser source;

	/**
	 * 正在加载
	 */
	private boolean isLoading;

	private WeiBoUser target;

	public WeiBoUser getSource() {
		return source;
	}

	public void setSource(WeiBoUser source) {
		this.source = source;
	}

	public WeiBoUser getTarget() {
		return target;
	}

	public void setTarget(WeiBoUser target) {
		this.target = target;
	}

	public boolean isLoading() {
		return isLoading;
	}

	public void setLoading(boolean isLoading) {
		this.isLoading = isLoading;
	}
}
