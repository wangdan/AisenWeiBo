package org.aisen.android.network.cache;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.network.biz.IResult;
import org.aisen.android.network.http.Params;


/**
 * 缓存接口
 * 
 * @author wangdan
 * 
 */
public interface ICacheUtility {

	IResult findCacheData(Setting action, Params params);

	void addCacheData(Setting action, Params params, IResult result);

}
