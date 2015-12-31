package org.aisen.android.network.biz;

public interface IResult {

	/**
	 * 缓存过期
	 * 
	 * @return
	 */
	public boolean outofdate();
	
	/**
	 * 是否是缓存数据
	 * 
	 * @return
	 */
	public boolean fromCache();
	
	/**
	 * 没有更多数据了
	 * 
	 * @return
	 */
	public boolean endPaging();

	/**
	 * 页码信息
	 *
	 * @return
	 */
	public String[] pagingIndex();

}
