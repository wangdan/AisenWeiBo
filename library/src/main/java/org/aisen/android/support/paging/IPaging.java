package org.aisen.android.support.paging;

import java.io.Serializable;

public interface IPaging<T extends Serializable, Ts extends Serializable> extends Serializable {

	/**
	 * 重设参数
	 * 
	 * @return
	 */
    public IPaging<T, Ts> newInstance();

	/**
	 * 处理数据
	 * 
	 * @param newDatas
	 *            新获取的数据集合
	 * @param firstData
	 *            adapter数据集中的第一条数据
	 * @param lastData
	 *            adapter数据集中的最后一条数据
	 */
    public void processData(Ts newDatas, T firstData, T lastData);

    public String getPreviousPage();

    public String getNextPage();

    public void setPage(String previousPage, String nextPage);

	/**
	 * 是否还能刷新最新
	 * 
	 * @return
	 */
    public boolean canRefresh();

	/**
	 * 是否还能拉取更多
	 * 
	 * @return
	 */
    public boolean canUpdate();

}
