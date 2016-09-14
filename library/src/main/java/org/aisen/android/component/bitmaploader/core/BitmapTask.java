package org.aisen.android.component.bitmaploader.core;

import android.os.Handler;
import android.os.Message;

import org.aisen.android.common.utils.Logger;

import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BitmapTask<Params, Progress, Result> {
	static final String TAG = "BitmapTask";

	/**
	 * 加载图片默认是16个线程
	 */
	private static final int CORE_IMAGE_POOL_SIZE = 20;

	/**
	 * 默认核心线程是5个
	 */
	private static final int CORE_POOL_SIZE = 10;
	/**
	 * 默认执行最大线程是128个
	 */
	private static final int MAXIMUM_POOL_SIZE = 256;
	private static final int KEEP_ALIVE = 1;

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			return new Thread(r, "BitmapTask #" + mCount.getAndIncrement());
		}
	};

	/**
	 * 执行队列，默认是10个，超过10个后会开启新的线程，如果已运行线程大于 {@link #MAXIMUM_POOL_SIZE}，执行异常策略
	 */
	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(10);

	/**
	 * 默认线程池，最大执行{@link #CORE_POOL_SIZE}+{@link #MAXIMUM_POOL_SIZE}个线程
	 */
	public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
			sPoolWorkQueue, sThreadFactory);

	/**
	 * 固定大小为{@link #CORE_IMAGE_POOL_SIZE}的线程池<br/>
	 * 无界线程池，可以加载无限个线程
	 */
	public static final Executor IMAGE_POOL_EXECUTOR = Executors.newFixedThreadPool(CORE_IMAGE_POOL_SIZE, sThreadFactory);

	/**
	 * An {@link Executor} that executes tasks one at a time in serial order.
	 * This serialization is global to a particular process.
	 */
	public static final Executor SERIAL_EXECUTOR = new SerialExecutor();

	private static final int MESSAGE_POST_RESULT = 0x1;
	private static final int MESSAGE_POST_PROGRESS = 0x2;

	private static final InternalHandler sHandler = new InternalHandler();

	private static volatile Executor sDefaultExecutor = SERIAL_EXECUTOR;
	private final WorkerRunnable<Params, Result> mWorker;
	private final FutureTask<Result> mFuture;

	private volatile Status mStatus = Status.PENDING;

	private Exception exception;

	private final AtomicBoolean mTaskInvoked = new AtomicBoolean();

	private static class SerialExecutor implements Executor {
		final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
		Runnable mActive;

		public synchronized void execute(final Runnable r) {
			mTasks.offer(new Runnable() {
				public void run() {
					try {
						r.run();
					} finally {
						scheduleNext();
					}
				}
			});
			if (mActive == null) {
				scheduleNext();
			}
		}

		protected synchronized void scheduleNext() {
			if ((mActive = mTasks.poll()) != null) {
				THREAD_POOL_EXECUTOR.execute(mActive);
			}
		}
	}

	public enum Status {
		/**
		 * Indicates that the task has not been executed yet.
		 */
		PENDING,
		/**
		 * Indicates that the task is running.
		 */
		RUNNING,
		/**
		 * Indicates that {@link SyswealTask#onPostExecute} has finished.
		 */
		FINISHED,
	}

	/** @hide Used to force static handler to be created. */
	public static void init() {
		sHandler.getLooper();
	}

	private static void setDefaultExecutor(Executor exec) {
		sDefaultExecutor = exec;
	}

	static int count = 0;
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		count--;
	}
	
	public BitmapTask() {
		count++;
		Logger.d(TAG, count + "");
		mWorker = new WorkerRunnable<Params, Result>() {
			public Result call() throws Exception {
				mTaskInvoked.set(true);

				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				return postResult(doInBackground(mParams));
			}
		};

		mFuture = new FutureTask<Result>(mWorker) {
			@Override
			protected void done() {
				try {
					final Result result = get();

					postResultIfNotInvoked(result);
				} catch (InterruptedException e) {
					android.util.Log.w(TAG, e);
				} catch (ExecutionException e) {
					throw new RuntimeException("An error occured while executing doInBackground()", e.getCause());
				} catch (CancellationException e) {
					postResultIfNotInvoked(null);
				} catch (Throwable t) {
					throw new RuntimeException("An error occured while executing " + "doInBackground()", t);
				}
			}
		};
	}

	private void postResultIfNotInvoked(Result result) {
		final boolean wasTaskInvoked = mTaskInvoked.get();
		if (!wasTaskInvoked) {
			postResult(result);
		}
	}

	private Result postResult(Result result) {
		Message message = sHandler.obtainMessage(MESSAGE_POST_RESULT, new AsyncTaskResult<Result>(this, result));
		message.sendToTarget();
		return result;
	}

	public final Status getStatus() {
		return mStatus;
	}

	/**
	 * 线程开始执行
	 */
	protected void onTaskStarted() {

	}

	/**
	 * {@link #workInBackground(Object...)} 发生异常
	 */
	protected void onTaskFailed(Exception exception) {

	}

	/**
	 * 没有抛出异常，且<tt>Result</tt>不为<tt>Null</tt>
	 */
	protected void onTaskSuccess(Result result) {

	}

	/**
	 * 返回数据是<tt>Null</tt>，没有抛出异常
	 */
	protected void resultIsNull() {

	}

	/**
	 * 线程结束，不管线程结束是什么状态，都会执行这个方法
	 */
	protected void onTaskComplete() {

	}

	/**
	 * 异步执行方法
	 * 
	 * @param params
	 * @return
	 * @throws TaskException
	 */
	abstract public Result workInBackground(Params... params) throws Exception;

	private Result doInBackground(Params... params) {
		Logger.d(TAG, String.format("%s --->doInBackground()", "run "));

		try {
			return workInBackground(params);
		} catch (Exception e) {
			e.printStackTrace();
			exception = e;
		}

		return null;
	}

	final protected void onPreExecute() {
		onTaskStarted();
	}

	final protected void onPostExecute(Result result) {
		if (exception == null)
			if (result == null) {
				resultIsNull();
			} else {
				onTaskSuccess(result);
			}

		else if (exception != null) {
			onTaskFailed(exception);
		}

		onTaskComplete();
	}

	protected void onProgressUpdate(Progress... values) {
	}

	protected void onCancelled(Result result) {
		onCancelled();
		
		Logger.d(TAG, "onCanceled()");
	}

	protected void onCancelled() {
		onTaskComplete();
	}

	public final boolean isCancelled() {
		return mFuture.isCancelled();
	}

	public final boolean cancel(boolean mayInterruptIfRunning) {
		return mFuture.cancel(mayInterruptIfRunning);
	}

	public final Result get() throws InterruptedException, ExecutionException {
		return mFuture.get();
	}

	public final Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return mFuture.get(timeout, unit);
	}

	/**
	 * 连续执行线程，所有的线程都是按照队列一个一个执行下去的
	 * 
	 * @param params
	 * @return
	 */
	public final BitmapTask<Params, Progress, Result> executeOnSerialExecutor(Params... params) {
		return executeOnExecutor(SERIAL_EXECUTOR, params);
	}

	/**
	 * 加载图片的线程池
	 * 
	 * @param params
	 * @return
	 */
	public final BitmapTask<Params, Progress, Result> executrOnImageExecutor(Params... params) {
		return executeOnExecutor(IMAGE_POOL_EXECUTOR, params);
	}

	/**
	 * 默认线程池{@link #THREAD_POOL_EXECUTOR}
	 * 
	 * @param params
	 * @return
	 */
	public final BitmapTask<Params, Progress, Result> execute(Params... params) {
		return executeOnExecutor(THREAD_POOL_EXECUTOR, params);
	}

	private final BitmapTask<Params, Progress, Result> executeOnExecutor(Executor exec, Params... params) {
		if (mStatus != Status.PENDING) {
			switch (mStatus) {
			case RUNNING:
				throw new IllegalStateException("Cannot execute task:" + " the task is already running.");
			case FINISHED:
				throw new IllegalStateException("Cannot execute task:" + " the task has already been executed "
						+ "(a task can be executed only once)");
			}
		}

		mStatus = Status.RUNNING;

		onPreExecute();

		mWorker.mParams = params;
		exec.execute(mFuture);

		return this;
	}

	public static void execute(Runnable runnable) {
		sDefaultExecutor.execute(runnable);
	}

	protected final void publishProgress(Progress... values) {
		if (!isCancelled()) {
			sHandler.obtainMessage(MESSAGE_POST_PROGRESS, new AsyncTaskResult<Progress>(this, values)).sendToTarget();
		}
	}

	private void finish(Result result) {
		if (isCancelled()) {
			onCancelled(result);
		} else {
			onPostExecute(result);
		}
		mStatus = Status.FINISHED;
	}

	private static class InternalHandler extends Handler {
		@SuppressWarnings({ "unchecked" })
		@Override
		public void handleMessage(Message msg) {
			AsyncTaskResult result = (AsyncTaskResult) msg.obj;
			switch (msg.what) {
			case MESSAGE_POST_RESULT:
				// There is only one result
				result.mTask.finish(result.mData[0]);
				break;
			case MESSAGE_POST_PROGRESS:
				result.mTask.onProgressUpdate(result.mData);
				break;
			}
		}
	}

	private static abstract class WorkerRunnable<Params, Result> implements Callable<Result> {
		Params[] mParams;
	}

	private static class AsyncTaskResult<Data> {
		final BitmapTask mTask;
		final Data[] mData;

		AsyncTaskResult(BitmapTask task, Data... data) {
			mTask = task;
			mData = data;
		}
	}
}
