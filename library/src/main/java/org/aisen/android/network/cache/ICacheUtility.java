package org.aisen.android.network.cache;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.network.http.Params;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 缓存接口
 * 
 * @author wangdan
 * 
 */
public interface ICacheUtility {

    static final int CORE_POOL_SIZE = 5;
    static final int MAXIMUM_POOL_SIZE = 128;
    static final int KEEP_ALIVE = 1;

    static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };

    static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(10);

    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
            sPoolWorkQueue, sThreadFactory);

	public <T> Cache<T> findCacheData(Setting action, Params params, Class<T> responseCls);

	public void addCacheData(Setting action, Params params, Object responseObj);

	public static class Cache<T> {
		
		private T t;

		// true-缓存到期
		private boolean expired;

		public Cache() {

		}

		public Cache(T t, boolean expired) {
			this.t = t;
			this.expired = expired;
		}

		public T getT() {
			return t;
		}

		public void setT(T t) {
			this.t = t;
		}

		public boolean expired() {
			return expired;
		}

		public void setExpired(boolean expired) {
			this.expired = expired;
		}

	}
	
}
