package org.aisen.android.support.paging;

import java.io.Serializable;

public interface IPaging<T extends Serializable, Ts extends Serializable> extends Serializable {

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

}
