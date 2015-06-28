package org.aisen.android.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import org.aisen.android.R;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.network.biz.IResult;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.adapter.ABaseAdapter;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.widget.AsToolbar;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nineoldandroids.animation.Animator;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ARefreshFragment<T extends Serializable, Ts extends Serializable, V extends View> extends ABaseFragment
									implements RecyclerListener, OnScrollListener, AsToolbar.OnToolbarDoubleClick, AdapterView.OnItemClickListener {

	private static final String TAG = "ARefresh";
	
	private static final String SAVED_DATAS = "org.aisen.android.ui.Datas";
	private static final String SAVED_PAGING = "org.aisen.android.ui.Paging";
    private static final String SAVED_CONFIG = "org.aisen.android.ui.Config";

	@SuppressWarnings("rawtypes")
    IPaging mPaging;

	private ABaseAdapter<T> mAdapter;
	
	private SwingBottomInAnimationAdapter swingAnimAdapter;
	
	@SuppressWarnings("rawtypes")
	private PagingTask pagingTask;
	
	private RefreshConfig refreshConfig;

	public enum RefreshMode {
		/**
		 * 重设数据
		 */
		reset,
		/**
		 * 拉取更多
		 */
		update,
		/**
		 * 刷新最新
		 */
		refresh
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ArrayList<T> datas = savedInstanceState == null ? new ArrayList<T>()
                                                        : (ArrayList<T>) savedInstanceState.getSerializable(SAVED_DATAS);
		mAdapter = new MyBaseAdapter(datas, getActivity());
		
		if (savedInstanceState != null && savedInstanceState.getSerializable(SAVED_PAGING) != null) {
            mPaging = (IPaging) savedInstanceState.getSerializable(SAVED_PAGING);
		} else {
            mPaging = configPaging();
		}
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // 将分页信息保存起来
        if (mPaging != null)
            outState.putSerializable(SAVED_PAGING, mPaging);

        onSaveDatas(outState);

        outState.putSerializable(SAVED_CONFIG, refreshConfig);

        super.onSaveInstanceState(outState);
    }

    /**
     * 数据量比较大的时候，子类可以不保存，会阻塞
     *
     * @param outState
     */
    protected void onSaveDatas(Bundle outState) {
        // 将数据保存起来
        if (getAdapterItems() != null && getAdapterItems().size() != 0)
            outState.putSerializable(SAVED_DATAS, getAdapterItems());
    }
	
	Handler mHandler = new Handler() {
		
	};
	
	@Override
	void _layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super._layoutInit(inflater, savedInstanceSate);
		
		if (getRefreshView() != null) {
			getRefreshView().setOnScrollListener(this);
			getRefreshView().setRecyclerListener(this);
            getRefreshView().setOnItemClickListener(this);
		}

        if (savedInstanceSate == null) {
        }
        else {
            refreshConfig = (RefreshConfig) savedInstanceSate.getSerializable(SAVED_CONFIG);
        }
        if (refreshConfig == null) {
            refreshConfig = new RefreshConfig();
            configRefresh(refreshConfig);
        }

        if (refreshConfig.animEnable) {
            swingAnimAdapter = new SwingBottomInAnimationAdapter(mAdapter) {

                @Override
                protected Animator getAnimator(ViewGroup parent, View view) {
                    return super.getAnimator(parent, view);
                }

            };
            swingAnimAdapter.setAbsListView(getRefreshView());
        }

        setInitRefreshView(getRefreshView(), savedInstanceSate);

        getRefreshView().setAdapter(getAdapter());
		
        onChangedByConfig(refreshConfig);
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		
	}
	
	private boolean isScrolling = false;// 正在滚动

    @Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// 滑动的时候，不加载图片
        if (!refreshConfig.ignoreScroll) {
			mHandler.removeCallbacks(refreshRunnable);
			
			if (scrollState == SCROLL_STATE_FLING) {
				isScrolling = true;
			}
			else if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
				isScrolling = true;
			}
			else if (scrollState == SCROLL_STATE_IDLE) {
				isScrolling = false;

				mHandler.postDelayed(refreshRunnable, 200);
//				notifyDataSetChanged();
			}
		}

        // 保存最后浏览位置
        if (scrollState == SCROLL_STATE_IDLE) {
            if (!TextUtils.isEmpty(refreshConfig.saveLastPositionKey) && getRefreshView() != null) {
                putLastReadPosition(getRefreshView().getFirstVisiblePosition());

                putLastReadTop(getRefreshView().getChildAt(0).getTop());
            }
        }
	}
	
	Runnable refreshRunnable = new Runnable() {
		
		@Override
		public void run() {
			Logger.w(TAG, "刷新视图");
			notifyDataSetChanged();
		}
	};
	
	@Override
	public boolean canDisplay() {
		if (refreshConfig.ignoreScroll)
			return true;
		
		return !isScrolling;
	}

	@Override
    public void requestData() {
		requestData(RefreshMode.reset);
	}

	public BaseAdapter getAdapter() {
		if (swingAnimAdapter != null)
			return swingAnimAdapter;
		
		return mAdapter;
	}
	
	ABaseAdapter<T> getABaseAdapter() {
		return mAdapter;
	}

    public void onPullDownToRefresh() {
		requestData(RefreshMode.refresh);
	}

    public void onPullUpToRefresh() {
		if (!isRefreshing())
			requestData(RefreshMode.update);
	}
	
	@Override
    public boolean isContentEmpty() {
		return getAdapterItems() == null || getAdapterItems().size() == 0;
	}

	/**
	 * 分页线程，根据{@link IPaging}构造的分页参数列表调用接口
	 * 
	 * @author wangdan
	 * 
	 * @param <Params>
	 * @param <Progress>
	 * @param <Result>
	 */
    public abstract class PagingTask<Params, Progress, Result extends Serializable> extends ABaseTask<Params, Progress, Result> {

		final protected RefreshMode mode;

        public PagingTask(String taskId, RefreshMode mode) {
			super(taskId);
			this.mode = mode;
			pagingTask = this;

			if (mode == RefreshMode.reset && mPaging != null)
                mPaging = configPaging();
		}

		@Override
		public Result workInBackground(Params... params) throws TaskException {
			String previousPage = null;
			String nextPage = null;

			if (mPaging != null) {
				previousPage = mPaging.getPreviousPage();
				nextPage = mPaging.getNextPage();
			}

			return workInBackground(mode, previousPage, nextPage, params);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onSuccess(Result result) {
			if (result == null || getActivity() == null) {
				super.onSuccess(result);
				return;
			}

            if (getRefreshView().getAdapter() == null)
                getRefreshView().setAdapter(getAdapter());
			
			List<T> resultList;
			if (result instanceof List)
				resultList = (List<T>) result;
			else {
				resultList = parseResult(result);
				if (resultList == null)
					resultList = new ArrayList<T>();
			}

			// 如果子类没有处理新获取的数据刷新UI，默认替换所有数据
			if (!handleResult(mode, resultList))
				if (mode == RefreshMode.reset)
					setAdapterItems(new ArrayList<T>());

			// append数据
			if (mode == RefreshMode.reset || mode == RefreshMode.refresh)
				addItemsAtFront(resultList);
			else if (mode == RefreshMode.update)
				addItems(resultList);
			
//			notifyDataSetChanged();

			// 处理分页数据
			if (mPaging != null) {
				if (getAdapterItems() != null && getAdapterItems().size() != 0)
                    mPaging.processData(result, getAdapterItems().get(0),
							getAdapterItems().get(getAdapterItems().size() - 1));
				else
                    mPaging.processData(result, null, null);
			}

            // 如果是重置数据，重置canLoadMore
            if (mode == RefreshMode.reset)
                refreshConfig.canLoadMore = true;
            // 如果数据少于这个值，默认加载完了
            if (mode == RefreshMode.update || mode == RefreshMode.reset)
                refreshConfig.canLoadMore = resultList.size() >= refreshConfig.minResultSize;

			// 如果是缓存数据，且已经过期
			if (result instanceof IResult) {
				// 这里增加一个自动刷新设置功能
                IResult iResult = (IResult) result;

                // 数据是缓存数据
                if (iResult.isCache()) {
                    // 缓存过期刷新数据
                    if (refreshConfig.expiredAutoRefresh && iResult.expired()) {
                        requestDataDelay(500);
                    }
                    // 滑动到最后浏览位置
                    else {
                        toLastReadPosition();
                    }
                }

                if (iResult.noMore())
                    refreshConfig.canLoadMore = false;
			}

            notifyDataSetChanged();

            onChangedByConfig(refreshConfig);

			super.onSuccess(result);
		}
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			if (isRefreshing())
                onRefreshViewComplete();
			
			pagingTask = null;
		}

		/**
		 * 每次调用接口，获取新的数据时调用这个方法
		 * 
		 * @param mode
		 *            当次拉取数据的类型
		 * @param datas
		 *            当次拉取的数据
		 * @return <tt>false</tt> 如果mode={@link RefreshMode#reset}
		 *         默认清空adapter中的数据
		 */
		protected boolean handleResult(RefreshMode mode, List<T> datas) {
			return false;
		}

		/**
		 * 将Ts转换成List(T)
		 * 
		 * @param result
		 *            List(T)
		 * @return
		 */
		abstract protected List<T> parseResult(Result result);

        /**
         * 异步执行方法
         *
         * @param mode 刷新模式
         * @param previousPage 上一页页码
         * @param nextPage 下一页页码
         * @param params task参数
         * @return
         * @throws TaskException
         */
		abstract protected Result workInBackground(RefreshMode mode, String previousPage, String nextPage, Params... params) throws TaskException;

	}

    public void requestDataDelay(int delay) {
		mHandler.removeCallbacks(refreshDelay);
		mHandler.postDelayed(refreshDelay, delay);
	}

    /**
     * 设置刷新控件为刷新状态且刷新数据
     *
     */
    public void setRefreshingRequestData() {
        // 如果没有正在刷新，设置刷新控件，且子类没有自动刷新
        if (!isRefreshing() && !setRefreshing())
            requestData(RefreshMode.reset);
    }
	
	Runnable refreshDelay = new Runnable() {
		
		@Override
		public void run() {
			if (getRefreshView() instanceof ListView) {
				ListView listView = (ListView) getRefreshView();
				listView.setSelectionFromTop(0, 0);
			}
			
			putLastReadPosition(0);
			putLastReadTop(0);

			setRefreshingRequestData();
		}
		
	};

	class MyBaseAdapter extends ABaseAdapter<T> {

        public MyBaseAdapter(ArrayList<T> datas, Activity context) {
			super(datas, context);
		}

		@Override
		protected AbstractItemView<T> newItemView() {
			return ARefreshFragment.this.newItemView();
		}

	}

    private Map<String, WeakReference<View>> viewCache = new HashMap<>();
	@Override
    public void onMovedToScrapHeap(View view) {
        if (!viewCache.containsKey(view.toString())) {
            Logger.d(TAG, "保存一个View到Cache");

            viewCache.put(view.toString(), new WeakReference<View>(view));
        }
	}

	/**
	 * 
	 * @param container
	 * @return true:子类自行释放，父类不做处理
	 */
	protected boolean releaseImageView(View container) {
		if (configCanReleaseIds() != null) {
			for (int imgId : configCanReleaseIds()) {
				ImageView imgView = (ImageView) container.findViewById(imgId);
				if (imgView != null) {
					imgView.setImageDrawable(BitmapLoader.getLoadingDrawable(imgView));
				
					Logger.v(ARefreshFragment.class.getSimpleName(), "释放ImageView");
				}
			}
		}
		
		return false;
	}

	protected int[] configCanReleaseIds() {
		return null;
	}

    protected int delayRlease() {
        return 5 * 1000;
    }

    @Override
    public void onPause() {
        super.onPause();

        mHandler.postDelayed(releaseRunnable, delayRlease());
    }

	@Override
    public void onStop() {
		super.onStop();

        // TODO 有手势返回的时候，不会调用onStop()
//		mHandler.removeCallbacks(releaseRunnable);
//
//        releaseImageViewByIds();
	}

    @Override
    public void onResume() {
		super.onResume();

		mHandler.removeCallbacks(releaseRunnable);
		
		refreshUI();
	}

    public void refreshUI() {
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                notifyDataSetChanged();
            }

        }, 200);

		_refreshUI();
	}

	void _refreshUI() {

	}

    public void releaseImageViewByIds() {
        Logger.v(TAG, "releaseImageViewByIds()");

		if (getRefreshView() != null) {
			int childSize = getRefreshView().getChildCount();
			for (int i = 0; i < childSize; i++) {
                View view = getRefreshView().getChildAt(i);

				releaseImageView(view);

                if (viewCache.containsKey(view.toString())) {
                    Logger.v(TAG, "已经释放了，从Cache中移除");

                    viewCache.remove(view.toString());
                }
            }

            if (viewCache.size() > 0) {
                Set<String> keySet = viewCache.keySet();
                for (String key : keySet) {
                    View view = viewCache.get(key).get();
                    if (view != null) {
                        Logger.v(TAG, "从Cache中释放一个View");

                        releaseImageView(view);
                    }
                }
            }
		}
	}
	
	protected void toLastReadPosition() {
		if (getRefreshView() == null)
			return;
		
		getRefreshView().post(new Runnable() {
			
			@Override
			public void run() {
				if (getRefreshView() instanceof ListView) {
					ListView listView = (ListView) getRefreshView();
//					listView.smoothScrollToPositionFromTop(getLastReadPosition(), getLastReadTop() + listView.getPaddingTop());
					listView.setSelectionFromTop(getLastReadPosition(), getLastReadTop() + listView.getPaddingTop());
				}
			}
		});
	}
	
	protected int getLastReadPosition() {
		return ActivityHelper.getIntShareData(refreshConfig.saveLastPositionKey + "Position", 0);
	}
	
	protected void putLastReadPosition(int position) {
		if (!TextUtils.isEmpty(refreshConfig.saveLastPositionKey))
			ActivityHelper.putIntShareData(refreshConfig.saveLastPositionKey + "Position", position);
	}
	
	private int getLastReadTop() {
		return ActivityHelper.getIntShareData(refreshConfig.saveLastPositionKey + "Top", 0);
	}
	
	protected void putLastReadTop(int top) {
		if (!TextUtils.isEmpty(refreshConfig.saveLastPositionKey))
			ActivityHelper.putIntShareData(refreshConfig.saveLastPositionKey + "Top", top);
	}
	
	public void notifyDataSetChanged() {
		if (swingAnimAdapter != null) {
			// 刷新的时候，不显示动画
			swingAnimAdapter.notifyDataSetChanged();
		}
		else {
			mAdapter.notifyDataSetChanged();
		}
	}
	
	public ArrayList<T> getAdapterItems() {
		return mAdapter.getDatas();
	}
	
	private int getAdapterCount() {
		if (swingAnimAdapter != null)
			return swingAnimAdapter.getCount();
		
		return mAdapter.getCount();
	}

	public void setAdapterSelected(int position) {
		mAdapter.setSelected(position);
	}
	
	public void setAdapterItems(ArrayList<T> items) {
		mAdapter.setDatas(items);
	}
	
	public void addItemsAtFront(List<T> items) {
		mAdapter.addItemsAtFront(items);
	}
	
	public void addItems(List<T> items) {
		mAdapter.addItems(items);
	}

    final protected RefreshConfig getRefreshConfig() {
        return refreshConfig;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    /**
     * 子类配置
     *
     * @param config
     */
    protected void configRefresh(RefreshConfig config) {

    }

    /**
     * 设置分页
     *
     * @return <tt>null</tt> 不分页
     */
    protected IPaging<T, Ts> configPaging() {
        return null;
    }

    /**
	 * Adapter的ItemView
	 * 
	 * @return
	 */
	abstract protected ABaseAdapter.AbstractItemView<T> newItemView();

	/**
	 * 根据RefreshMode拉取数据
	 * 
	 * @param mode
	 */
	abstract protected void requestData(RefreshMode mode);
	
	/**
	 * 列表控件
	 * 
	 * @return
	 */
	abstract public AbsListView getRefreshView();
	
	/**
	 * 设置列表控件状态为刷新状态
	 * 
	 * @return true:子类回调了刷新事件
	 */
	abstract public boolean setRefreshing();
	
	/**
	 * 根据Config刷新RefreshView
	 */
	abstract protected void onChangedByConfig(RefreshConfig config);

    /**
     * 初始化RefreshView
     *
     * @param refreshView
     */
    protected void setInitRefreshView(AbsListView refreshView, Bundle savedInstanceSate) {

    }

	/**
	 * 设置列表控件状态为刷新结束
	 */
	abstract public void onRefreshViewComplete();
	
	public boolean isRefreshing() {
		return pagingTask != null;
	}
	
	Runnable releaseRunnable = new Runnable() {
		
		@Override
		public void run() {
            releaseImageViewByIds();

			Logger.w("释放图片");
		}
		
	};

    @Override
    public boolean onToolbarDoubleClick() {
        return false;
    }

    @Override
    protected void taskStateChanged(ABaseTaskState state, Serializable tag) {
        super.taskStateChanged(state, tag);

        if (state == ABaseTaskState.success) {
            if (isContentEmpty()) {
                if (emptyLayout != null && !TextUtils.isEmpty(refreshConfig.emptyLabel))
                    ViewUtils.setTextViewValue(emptyLayout, R.id.txtLoadEmpty, refreshConfig.emptyLabel);
            }
        }
        else if (state == ABaseTaskState.falid) {
            if (isContentEmpty()) {
                if (loadFailureLayout != null && !TextUtils.isEmpty(tag + ""))
                    ViewUtils.setTextViewValue(loadFailureLayout, R.id.txtLoadFailed, tag + "");
            }
        }
    }

    public static class RefreshConfig implements Serializable {

        public static final long serialVersionUID = -963125420415611042L;

        public boolean expiredAutoRefresh = true;// 缓存数据过期自动刷新列表

		public boolean canLoadMore = true;// 是否可以加载更多
		
		public String saveLastPositionKey = null;// 最后阅读坐标的Key，null-不保存，针对缓存数据有效
		
		public int minResultSize = 10;// 当加载的数据少于这个值时，默认没有更多加载
		
		public boolean animEnable = false;// 是否启用加载动画
		
		public String emptyLabel;// 加载空显示文本

        public boolean ignoreScroll = true;// 置为false表示滑动列表时不加载图片
		
	}
	
}
