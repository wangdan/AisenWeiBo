package org.aisen.android.network.biz;

import java.io.File;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import org.aisen.android.common.setting.Setting;
import org.aisen.android.common.setting.SettingUtil;
import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.Consts;
import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.cache.ICacheUtility;
import org.aisen.android.network.cache.ICacheUtility.Cache;
import org.aisen.android.network.http.HttpConfig;
import org.aisen.android.network.http.IHttpUtility;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.http.ParamsUtil;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;

public abstract class ABizLogic implements IHttpUtility {

	public static final String TAG = "ABizlogic";

	public enum CacheMode {
		/**
		 * 有缓存且有效，返回缓存<br/>
		 * 有缓存但无效，拉取服务数据，如果拉取失败，仍然返回无效缓存<br/>
		 * 没有缓存，拉取服务数据
		 */
		auto,
		/**
		 * 在{@link #auto}的基础上，不管存不存在缓存，都拉取服务数据更新缓存
		 */
		servicePriority,
		/**
		 * 每次拉取数据，都优先拉取缓存
		 */
		cachePriority,
        CacheMode, /**
		 * 只拉取服务数据
		 */
		disable
	}

	private IHttpUtility mHttpUtility;
	private ICacheUtility memoryCacheUtility;

	private CacheMode mCacheMode;

	public ABizLogic() {
		mHttpUtility = configHttpUtility();

		mCacheMode = SettingUtility.getBooleanSetting("debug") ? CacheMode.auto : CacheMode.disable;
	}

	public ABizLogic(CacheMode cacheMode) {
		this();
		this.mCacheMode = cacheMode;
	}
	
	protected Setting getSetting(String type) {
		return SettingUtility.getSetting(type);
	}

	@Override
	public <T> T doGet(HttpConfig config, Setting actionSetting, Params params, Class<T> responseCls) throws TaskException {
		throw new RuntimeException(
				"not support this method ---> doGet(HttpConfig config, Setting actionSetting, Params params, Class<T> responseCls), please call ---> doGet(Setting actionSetting, Params params, Class<T> responseCls)");
	}

	public <T> T doGet(final Setting actionSetting, final Params params, Class<T> responseCls) throws TaskException {
		HttpConfig mConfig = cloneHttpConfig(configHttpConfig(), actionSetting);

		String action = actionSetting.getValue();

		Logger.v(TAG, String.format("do get() method, url = %s, action = %s, desc = %s", mConfig.baseUrl, action, actionSetting.getDescription()));
		Logger.v(TAG, String.format("params ---> %s", toJson(params)));
		Logger.v(TAG, String.format("HttpConfig ---> %s", toJson(mConfig)));

		ICacheUtility cacheUtility = null;
		// 配置的缓存模式
		String cacheUtilityStr = SettingUtil.getSettingValue(actionSetting, Consts.CACHE_UTILITY);
		if (!TextUtils.isEmpty(cacheUtilityStr)) {
			try {
				cacheUtility = (ICacheUtility) Class.forName(cacheUtilityStr).newInstance();
			} catch (Exception e) {
				Logger.w(TAG, "CacheUtility 没有配置或者配置错误");
			}
		}
		// 内存缓存
		if (actionSetting.getExtras().containsKey(Consts.MEMORY_CACHE_UTILITY)) {
			String memoryCacheUtilityStr = actionSetting.getExtras().get(Consts.MEMORY_CACHE_UTILITY).getValue();
			if (!TextUtils.isEmpty(memoryCacheUtilityStr)) {
				try {
					memoryCacheUtility = (ICacheUtility) Class.forName(memoryCacheUtilityStr).newInstance();
				} catch (Exception e) {
					Logger.w(TAG, "MemoryCacheUtility 没有配置或者配置错误");
				}
			}
		}

		Cache<T> cache = null;

		// 缓存是否在action中配置打开
		boolean cacheEnable = actionSetting.getExtras().containsKey(Consts.CACHE_ENABLE) ? Boolean.parseBoolean(actionSetting.getExtras()
				.get(Consts.CACHE_ENABLE).getValue()) : false;

		if (cacheEnable && (mCacheMode == CacheMode.cachePriority || mCacheMode != CacheMode.disable)) {
			// 拉取内存
			// SettingUtility.getIntSetting(Consts.MEMORY_CACHE_VALIDITY) > 0
			if (memoryCacheUtility != null) {
				cache = memoryCacheUtility.findCacheData(actionSetting, params, responseCls);

				if (cache != null) {
					String state = cache.expired() ? "invalid" : "valid";
					Logger.d(TAG, String.format("%s 有效期： %s, action = %s", "MemoryCacheUtility", state,
							actionSetting.getValue()));
				}
			}

			// 拉取缓存数据
			if ((cache == null || cache.expired()) && cacheUtility != null) {
				cache = cacheUtility.findCacheData(actionSetting, params, responseCls);
				if (cache != null) {
					String state = cache.expired() ? "invalid" : "valid";
					Logger.d(TAG, String.format(" %s 有效期 %s, action = %s", cacheUtility.getClass().getSimpleName(), state,
							actionSetting.getValue()));
				}

				if (memoryCacheUtility != null && cache != null && !cache.expired()) {
					// 文件缓存 拉到数据后 刷新内存
					putToCache(actionSetting, params, cache.getT(), memoryCacheUtility);
					Logger.v(TAG, String.format("刷新内存缓存, action = %s", actionSetting.getValue()));
				}
			}
		}

		T result = null;
		// 缓存不存在、缓存无效、服务数据优先等情况下，拉取服务数据
		if ((cache == null || cache.expired()) || mCacheMode == CacheMode.servicePriority) {
			Exception serviceEx = null;
			try {
				result = getHttpUtility(actionSetting).doGet(mConfig, actionSetting, params, responseCls);

				if (result != null) {
					// 刷新内存缓存
					if (memoryCacheUtility != null)
						putToCache(actionSetting, params, result, memoryCacheUtility);
					// 刷新持久缓存
					if (cacheUtility != null) {
						// 如果数据来自缓存，则不刷新
						if (result instanceof IResult && ((IResult) result).isCache()) {
							Logger.w(ABizLogic.TAG, "数据来自缓存，不刷新");
						}
						else {
							putToCache(actionSetting, params, result, cacheUtility);
						}
					}

					Logger.d(TAG, String.format("加载服务端数据, action = %s --->%s", actionSetting.getValue(), Logger.toJson(result)));
				}
			} catch (Exception e) {
				serviceEx = e;
			}
			// 拉取网络失败，如果存在缓存，仍然返回缓存
			if (result == null && (cache != null && mCacheMode != CacheMode.disable)) {
				result = cache.getT();
				// 刷新内存缓存
				if (memoryCacheUtility != null && result != null)
					putToCache(actionSetting, params, result, memoryCacheUtility);
			} else {
				// 如果是拉取服务数据异常，抛出异常
				if (serviceEx != null) {
					TaskException taskException = null;
					if (serviceEx.getCause() instanceof TaskException) {
						
						taskException = (TaskException) serviceEx.getCause();
					}
					else if (serviceEx instanceof TaskException) {
						taskException = (TaskException) serviceEx;
					}
					if (taskException != null)
						throw taskException;

					throw new TaskException(TextUtils.isEmpty(serviceEx.getMessage()) ? "服务器错误" : serviceEx.getMessage());
				}
			}
		} else {
			// 缓存存在，且有效
			if (cache != null && !cache.expired())
				result = cache.getT();
		}
		// 返回 克隆对象 任意编辑不对这边的 数据产生影响
//		return ObjectUtil.cloneObject(result);
		return result;
	}

	@Override
	public <T> T doPost(HttpConfig config, Setting action, Params params, Class<T> responseCls, Object requestObj) throws TaskException {
		HttpConfig mConfig = cloneHttpConfig(config, action);

		try {
			Logger.d(TAG, String.format("doPost(config --->%s, \naction--->%s, \nparams --->%s, \nrequestObj --->%s)", toJson(mConfig),
					toJson(action), toJson(params), toJson(requestObj == null ? "requestObj is null" : requestObj)));
		} catch (Exception e) {
		}

		String key = generateMD5(action.getValue(), params);
		Logger.d(TAG, String.format("cacheKey = %s", key));

		T result = getHttpUtility(action).doPost(mConfig, action, params, responseCls, requestObj);

		if (result != null)
			Logger.d(TAG, String.format("%s Successfully loaded --->%s", action.getDescription(), toJson(result)));
		else
			Logger.d(TAG, String.format("%s load failed", action.getDescription()));

		return result;
	}

	@Override
	public <T> T uploadFile(HttpConfig config, Setting action, Params params, File file, Params headers, Class<T> responseClass) throws TaskException {
		HttpConfig mConfig = cloneHttpConfig(config, action);

		Logger.d(TAG, String.format("uploadFile(config --->%s, \naction--->%s, \nparams --->%s, \nfilePath --->%s)", toJson(mConfig), toJson(action),
				toJson(params), file.getAbsoluteFile()));

		String key = generateMD5(action.getValue(), params);
		Logger.d(TAG, String.format("cacheKey = %s", key));

		T result = getHttpUtility(action).uploadFile(mConfig, action, params, file, headers, responseClass);

		return result;
	}

	private IHttpUtility getHttpUtility(Setting action) {
		if (action.getExtras().get("http") != null && !TextUtils.isEmpty(action.getExtras().get("http").getValue())) {
			try {
				IHttpUtility httpUtility = (IHttpUtility) Class.forName(action.getExtras().get("http").getValue()).newInstance();
				return httpUtility;
			} catch (Exception e) {
				e.printStackTrace();
				Logger.w(TAG, "CacheUtility 没有配置或者配置错误");
			}
		}
		return mHttpUtility;
	}

	private String toJson(Object o) {
		if (o == null)
			return "null";

		return JSON.toJSONString(o);
//		return mGson.toJson(o);
	}

	/**
	 * 设置http相关参数
	 * 
	 * @return
	 */
	abstract protected HttpConfig configHttpConfig();

	/**
	 * 配置网络交互
	 * 
	 * @return
	 */
	protected IHttpUtility configHttpUtility() {
		// 取配置的http
		try {
			if(!TextUtils.isEmpty(SettingUtility.getStringSetting("http")))
				return (IHttpUtility) Class.forName(SettingUtility.getStringSetting("http")).newInstance();
		} catch (Exception e) {
		}
		
		throw new RuntimeException("没有配置默认的HttpUtility");
	}

	private HttpConfig cloneHttpConfig(HttpConfig config, Setting actionSetting) {
		try {
			HttpConfig mConfig = config;
			
			if (actionSetting != null && actionSetting.getExtras().containsKey(Consts.BASE_URL))
				mConfig.baseUrl = actionSetting.getExtras().get(Consts.BASE_URL).getValue().toString();

			return mConfig;
		} catch (Exception e) {
		}

		return config;
	}

	protected String getPageCount(Setting setting) {
		return SettingUtil.getSettingValue(setting, "page_count");
	}

	protected HttpConfig getHttpConfig() {
		return configHttpConfig();
	}

	public void putToCache(Setting setting, Params params, Object data, ICacheUtility cacheUtility) {
		if (data instanceof IResult) {
			IResult iResult = (IResult) data;
			if (!iResult.isCache())
				new PutCacheTask(setting, params, data, cacheUtility).executeOnExecutor(ICacheUtility.THREAD_POOL_EXECUTOR);
		}
		else {
			new PutCacheTask(setting, params, data, cacheUtility).executeOnExecutor(ICacheUtility.THREAD_POOL_EXECUTOR);
		}
	}
	
	protected CacheMode getCacheMode() {
		return mCacheMode;
	}
	
	protected void setCacheMode(CacheMode cacheMode) {
		this.mCacheMode = cacheMode;
	}

	class PutCacheTask extends WorkTask<Void, Void, Void> {

		private Setting setting;
		private Params params;
		private Object o;
		private ICacheUtility cacheUtility;

		PutCacheTask(Setting setting, Params params, Object o, ICacheUtility cacheUtility) {
			this.setting = setting;
			this.params = params;
			this.o = o;
			this.cacheUtility = cacheUtility;
		}

		@Override
		public Void workInBackground(Void... p) throws TaskException {
            long time = System.currentTimeMillis();
			cacheUtility.addCacheData(setting, params, o);
            Logger.d(TAG, "保存缓存耗时%sms", String.valueOf(System.currentTimeMillis() - time));
			return null;
		}

        @Override
        protected void onFinished() {
            super.onFinished();

            Logger.v(TAG, "CacheTask onFinished()");
        }
    }
	
	private String generateMD5(String action, Params params) {
		String key;
		if (params == null)
			key = action;
		else
			key = action + ParamsUtil.encodeToURLParams(params);
		
		return KeyGenerator.generateMD5(key);
	}

}
