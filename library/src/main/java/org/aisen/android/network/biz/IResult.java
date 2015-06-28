package org.aisen.android.network.biz;

public interface IResult {

	/**
	 * 缓存过期
	 * 
	 * @return
	 */
	public boolean expired();
	
	/**
	 * 是否是缓存数据
	 * 
	 * @return
	 */
	public boolean isCache();
	
	/**
	 * 没有更多数据了
	 * 
	 * @return
	 */
	public boolean noMore();
	
	/**
	 * 保存分页信息
	 * 
	 * @return index:0代表上一页页码，1代表上一页页码
	 */
	public String[] pagingIndex();
	
}
