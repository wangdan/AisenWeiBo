package org.aisen.android.ui.fragment;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.aisen.android.R;
import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.biz.IResult;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.adapter.ABaseAdapter;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.widget.AsToolbar;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于APagingFragment，可以扩展支持BaseAdapter的UI控件，并处理上、下拉的交互逻辑<br/>
 *
 * 1、维护Adapter<br/>
 * 2、维护分页线程<br/>
 * 3、处理UI界面的上、下拉<br/>
 * 4、新增FooterView，用于ListView等界面的自动加载更多<br/>
 * 5、当读取缓存数据并配置positionKey时，可以自动滑动到上次最后阅读位置<br/>
 *
 * @param <T>
 * @param <Ts>
 * @param <V>
 */
public abstract class APagingFragment<T extends Serializable, Ts extends Serializable, V extends ViewGroup> extends ABaseFragment
									implements AbsListView.RecyclerListener, AbsListView.OnScrollListener, AsToolbar.OnToolbarDoubleClick, AdapterView.OnItemClickListener {

	private static final String TAG = "AFragment-Paging";

	public static final String PAGING_TASK_ID = "org.aisen.android.PAGING_TASK";
	
	private static final String SAVED_DATAS = "org.aisen.android.ui.Datas";
	private static final String SAVED_PAGING = "org.aisen.android.ui.Paging";
    private static final String SAVED_CONFIG = "org.aisen.android.ui.Config";

	private IPaging mPaging;

	private ABaseAdapter<T> mAdapter;
	
	private PagingTask pagingTask;
	
	private RefreshConfig refreshConfig;
	private FooterViewConfig footerRefreshConfig;

	private View mFooterView;// FooterView，滑动到底部时，自动加载更多数据
	private int footviewHeight = GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.comm_footer_height);// 默认高度

	public enum RefreshMode {
		/**
		 * 重设数据
		 */
		reset,
		/**
		 * 上拉，加载更多
		 */
		update,
		/**
		 * 下拉，刷新最新
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

		footerRefreshConfig = initFooterViewConfig();
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // 将分页信息保存起来
        if (mPaging != null)
            outState.putSerializable(SAVED_PAGING, mPaging);

        onSaveDatas(outState);

//        outState.putSerializable(SAVED_CONFIG, refreshConfig);

        super.onSaveInstanceState(outState);
    }

    /**
     * 数据量比较大的时候，子类可以不保存，会阻塞
     *
     * @param outState
     */
    protected void onSaveDatas(Bundle outState) {
        // 将数据保存起来
        if (getAdapter() != null && getAdapter().getDatas().size() != 0)
            outState.putSerializable(SAVED_DATAS, getAdapter().getDatas());
    }
	
	@Override
	void _layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super._layoutInit(inflater, savedInstanceSate);
		
        if (savedInstanceSate == null) {
        }
        else {
            refreshConfig = (RefreshConfig) savedInstanceSate.getSerializable(SAVED_CONFIG);
        }
        if (refreshConfig == null) {
            refreshConfig = new RefreshConfig();
            configRefresh(refreshConfig);
        }

        setInitRefreshView(getRefreshView(), savedInstanceSate);

        onChangedByConfig(refreshConfig);
	}
	
	@Override
	final public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		handleScroll((V) view, firstVisibleItem, visibleItemCount, totalItemCount);
	}

	protected void handleScroll(V refreshView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

	}
	
	private boolean refreshViewScrolling = false;// 正在滚动
    @Override
	final public void onScrollStateChanged(AbsListView view, int scrollState) {
		handleScrollStateChanged((V) view, scrollState);
	}

	void handleScrollStateChanged(V refreshView, int scrollState) {
		// 滑动的时候，不加载图片
		if (!refreshConfig.displayWhenScrolling) {
			mHandler.removeCallbacks(refreshRunnable);

			if (scrollState == SCROLL_STATE_FLING) {
				refreshViewScrolling = true;
			}
			else if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
				refreshViewScrolling = true;
			}
			else if (scrollState == SCROLL_STATE_IDLE) {
				refreshViewScrolling = false;

				runUIRunnable(refreshRunnable, 200);
			}
		}

		if (scrollState == SCROLL_STATE_IDLE && // 停止滚动
				!refreshConfig.pagingEnd && // 分页未加载完
				refreshConfig.footerMoreEnable && // 自动加载更多
				mFooterView != null // 配置了FooterView
				) {
			int childCount = refreshView.getChildCount();
			if (childCount > 0 && refreshView.getChildAt(childCount - 1) == mFooterView) {
				if (setFooterRefreshing(mFooterView)) {
					onPullUpToRefresh();
				}
			}
		}

		// 保存最后浏览位置
		if (scrollState == SCROLL_STATE_IDLE) {
			if (!TextUtils.isEmpty(refreshConfig.positionKey) && refreshView != null) {
				runNUIRunnable(new Runnable() {
					@Override
					public void run() {
						putLastReadPosition(getFirstVisiblePosition());

						putLastReadTop(getRefreshView().getChildAt(0).getTop());
					}
				});
			}
		}
	}
	
	private Runnable refreshRunnable = new Runnable() {
		
		@Override
		public void run() {
			Logger.w(TAG, "刷新视图");

			getAdapter().notifyDataSetChanged();
		}
	};
	
	@Override
	public boolean canDisplay() {
		if (refreshConfig.displayWhenScrolling)
			return true;
		
		return !refreshViewScrolling;
	}

	@Override
    public void requestData() {
		// 如果没有Loading视图，且数据为空，就显示FootView加载状态
		RefreshMode mode = RefreshMode.reset;
		if (getAdapter().getCount() == 0 && loadingLayout == null)
			mode = RefreshMode.update;

		requestData(mode);
	}

	public ABaseAdapter getAdapter() {
		return mAdapter;
	}

	public ArrayList<T> getAdapterItems() {
		return mAdapter.getDatas();
	}
	
    public void onPullDownToRefresh() {
		requestData(RefreshMode.refresh);
	}

    public void onPullUpToRefresh() {
		requestData(RefreshMode.update);
	}
	
	@Override
    public boolean isContentEmpty() {
		return getAdapter() == null || getAdapter().getDatas().size() == 0;
	}

	// 子类不再允许重写这个类
	@Override
	final protected void taskStateChanged(ABaseTaskState state, TaskException exception) {
		// super.taskStateChanged(state, tag);
	}

	/**
	 * 子类需要根据不同的刷新模式，来刷新自己的UI
	 *
	 * @param state
	 * @param exception
	 * @param mode
	 */
	protected void taskStateChanged(ABaseTaskState state, TaskException exception, RefreshMode mode) {
		super.taskStateChanged(state, exception);

		setFooterViewAsTaskStateChanged(mFooterView, state, exception, mode);

		if (state == ABaseTaskState.success) {
			if (isContentEmpty()) {
				if (emptyLayout != null && !TextUtils.isEmpty(refreshConfig.emptyHint))
					ViewUtils.setTextViewValue(emptyLayout, R.id.txtLoadEmpty, refreshConfig.emptyHint);
			}
		}
		else if (state == ABaseTaskState.falid) {
			if (isContentEmpty()) {
				if (loadFailureLayout != null && !TextUtils.isEmpty(exception.getMessage()))
					ViewUtils.setTextViewValue(loadFailureLayout, R.id.txtLoadFailed, exception.getMessage());
			}
		}
		else if (state == ABaseTaskState.finished) {
			onRefreshViewComplete(mode);
		}
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

        public PagingTask(RefreshMode mode) {
			super(PAGING_TASK_ID);
			this.mode = mode;
			pagingTask = this;

			if (mode == RefreshMode.reset && mPaging != null)
                mPaging = configPaging();
		}

		@Override
		protected void onPrepare() {
			super.onPrepare();

			Logger.d(TAG, toString() + "-" + ABaseTaskState.prepare + " - " + mode);
			taskStateChanged(ABaseTaskState.prepare, null, mode);
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

            bindAdapter(getRefreshView(), getAdapter());
			
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
				getAdapter().addItemsAtFront(resultList);
			else if (mode == RefreshMode.update)
				getAdapter().addItems(resultList);
			
			// 处理分页数据
			if (mPaging != null) {
				if (getAdapter() != null && getAdapter().getDatas().size() != 0)
                    mPaging.processData(result, (T) getAdapter().getDatas().get(0),
							(T) getAdapter().getDatas().get(getAdapter().getDatas().size() - 1));
				else
                    mPaging.processData(result, null, null);
			}

            // 如果是重置数据，重置canLoadMore
            if (mode == RefreshMode.reset)
                refreshConfig.pagingEnd = false;
            // 如果数据少于这个值，默认加载完了
            if (mode == RefreshMode.update || mode == RefreshMode.reset)
                refreshConfig.pagingEnd = resultList.size() < refreshConfig.resultMin;

			// 如果是缓存数据，且已经过期
			if (result instanceof IResult) {
				// 这里增加一个自动刷新设置功能
                IResult iResult = (IResult) result;

				if (iResult.fromCache() && !iResult.outofdate())
					toLastReadPosition();

                if (iResult.endPaging())
                    refreshConfig.pagingEnd = true;
			}

			if (mode == RefreshMode.reset && getTaskCount(getTaskId()) > 1)
            	getAdapter().notifyDataSetChanged();

            onChangedByConfig(refreshConfig);

			Logger.d(TAG, toString() + "-" + ABaseTaskState.success + " - " + mode);
			taskStateChanged(ABaseTaskState.success, null, mode);

			super.onSuccess(result);
		}

		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);

			Logger.d(TAG, toString() + "-" + ABaseTaskState.falid + " - " + mode + "-" + exception.getMessage());
			taskStateChanged(ABaseTaskState.falid, exception, mode);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();

			Logger.d(TAG, toString() + "-" + ABaseTaskState.canceled + " - " + mode);
			taskStateChanged(ABaseTaskState.canceled, null, mode);
		}

		@Override
		protected void onFinished() {
			super.onFinished();

			Logger.d(TAG, toString() + "-" + ABaseTaskState.finished + " - " + mode);
			taskStateChanged(ABaseTaskState.finished, null, mode);

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

	@Override
	protected void requestDataOutofdate() {
		if (getRefreshView() instanceof ListView) {
			ListView listView = (ListView) getRefreshView();
			listView.setSelectionFromTop(0, 0);
		}

		putLastReadPosition(0);
		putLastReadTop(0);

		requestDataSetRefreshing();
	}

	/**
     * 设置刷新控件为刷新状态且刷新数据
     *
     */
    public void requestDataSetRefreshing() {
        // 如果没有正在刷新，设置刷新控件，且子类没有自动刷新
        if (!isRefreshing() && !setRefreshing())
            requestData(RefreshMode.reset);
    }
	
	class MyBaseAdapter extends ABaseAdapter<T> {

        public MyBaseAdapter(ArrayList<T> datas, Activity context) {
			super(datas, context);
		}

		@Override
		protected AItemView<T> newItemView() {
			return APagingFragment.this.newItemView();
		}

	}

    private Map<String, WeakReference<View>> viewCache = new HashMap<>();
	@Override
    public void onMovedToScrapHeap(View view) {
        if (!viewCache.containsKey(view.toString())) {
            viewCache.put(view.toString(), new WeakReference<View>(view));
        }
	}

	/**
	 * 
	 * @param container
	 * @return true:子类自行释放，父类不做处理
	 */
	protected boolean releaseImageView(View container) {
		if (refreshConfig.releaseItemIds != null) {
			for (int imgId : refreshConfig.releaseItemIds) {
				ImageView imgView = (ImageView) container.findViewById(imgId);
				Drawable drawable = loadingDrawableHolder(imgId);
				if (imgView != null && drawable != null) {
					imgView.setImageDrawable(drawable);
				
					Logger.v(APagingFragment.class.getSimpleName(), "释放ImageView");
				}
			}
		}
		
		return false;
	}

	private static Drawable defLoadingDrawable;
	protected Drawable loadingDrawableHolder(int viewId) {
		try {
			if (defLoadingDrawable == null)
				defLoadingDrawable = GlobalContext.getInstance().getResources().getDrawable(R.drawable.comm_loading);
		} catch (Throwable e) {
			Logger.printExc(getClass(), e);
		}

		return defLoadingDrawable;
	}

    @Override
    public void onPause() {
		super.onPause();

		// 当有手势返回时，不会调用onStop()，所以放到这里做释放了
		if (refreshConfig.releaseItemIds != null) {
			runUIRunnable(refreshRunnable, refreshConfig.releaseDelay);
		}
    }

    @Override
    public void onResume() {
		super.onResume();

		if (refreshConfig.releaseItemIds != null) {
			mHandler.removeCallbacks(releaseRunnable);

			refreshUI();
		}
	}

    public void refreshUI() {
        runUIRunnable(new Runnable() {

			@Override
			public void run() {
				getAdapter().notifyDataSetChanged();
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
		if (getRefreshView() == null || TextUtils.isEmpty(refreshConfig.positionKey))
			return;

		if (getRefreshView() instanceof ListView) {
			runUIRunnable(new Runnable() {

				@Override
				public void run() {
					ListView listView = (ListView) getRefreshView();
					listView.setSelectionFromTop(getLastReadPosition(), getLastReadTop() + listView.getPaddingTop());
				}
			});
		}
	}
	
	protected int getLastReadPosition() {
		return ActivityHelper.getIntShareData(refreshConfig.positionKey + "Position", 0);
	}
	
	protected void putLastReadPosition(int position) {
		if (!TextUtils.isEmpty(refreshConfig.positionKey))
			ActivityHelper.putIntShareData(refreshConfig.positionKey + "Position", position);
	}
	
	protected int getLastReadTop() {
		return ActivityHelper.getIntShareData(refreshConfig.positionKey + "Top", 0);
	}
	
	protected void putLastReadTop(int top) {
		if (!TextUtils.isEmpty(refreshConfig.positionKey))
			ActivityHelper.putIntShareData(refreshConfig.positionKey + "Top", top);
	}
	
	public void setAdapterItems(ArrayList<T> items) {
		mAdapter.setDatas(items);
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
	abstract protected ABaseAdapter.AItemView<T> newItemView();

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
	abstract public V getRefreshView();
	
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
	 * 设置列表控件状态为刷新结束
	 */
	abstract public void onRefreshViewComplete(RefreshMode mode);

    /**
     * 初始化RefreshView
     *
     * @param refreshView
     */
    protected void setInitRefreshView(V refreshView, Bundle savedInstanceSate) {
		if (refreshConfig.footerMoreEnable)
			mFooterView = configFooterView();

		// 设置事件
		if (getRefreshView() != null && getRefreshView() instanceof AbsListView) {
			AbsListView absListView = (AbsListView) getRefreshView();

			absListView.setOnScrollListener(this);
			absListView.setRecyclerListener(this);
			absListView.setOnItemClickListener(this);
		}

		bindFooterView(refreshView, mFooterView);

		bindAdapter(refreshView, getAdapter());
    }

	protected void bindFooterView(V refreshView, View footerView) {
		if (refreshView instanceof ListView && footerView != null) {
			ListView listView = (ListView) refreshView;

			listView.addFooterView(footerView);
		}
	}

	protected void bindAdapter(V refreshView, BaseAdapter adapter) {
		if (refreshView instanceof AbsListView) {
			AbsListView listView = (AbsListView) refreshView;
			if (listView.getAdapter() == null)
				listView.setAdapter(adapter);
		}
	}

	protected int getFirstVisiblePosition() {
		if (getRefreshView() instanceof AbsListView) {
			return ((AbsListView) getRefreshView()).getFirstVisiblePosition();
		}

		return 0;
	}

	protected View configFooterView() {
		View footerView = View.inflate(getActivity(), R.layout.comm_lay_footerview, null);

		footerView.findViewById(R.id.btnMore).setVisibility(View.VISIBLE);
		footerView.findViewById(R.id.layLoading).setVisibility(View.GONE);
		TextView btnMore = (TextView) footerView.findViewById(R.id.btnMore);
		btnMore.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!refreshConfig.pagingEnd) {
					onPullUpToRefresh();
				}
			}

		});

		return footerView;
	}

	/**
	 * 设置FooterView为加载状态，
	 *
	 * @param footerView
	 * @return false:当前正在加载数据
	 */
	protected boolean setFooterRefreshing(View footerView) {
		View layLoading = footerView.findViewById(R.id.layLoading);

		if (footviewHeight == 0 && footerView.getHeight() > 0)
			footviewHeight = footerView.getHeight();

		if (layLoading.getVisibility() == View.VISIBLE)
			return false;

		layLoading.setVisibility(View.VISIBLE);

		return true;
	}

	protected FooterViewConfig initFooterViewConfig() {
		FooterViewConfig config = new FooterViewConfig();

		config.hintLoading = getString(R.string.comm_footer_loading);
		config.btnFaild = getString(R.string.comm_footer_faild);
		config.btnMore = getString(R.string.comm_footer_more);
		config.btnPagingEnd = getString(R.string.comm_footer_pagingend);

		return config;
	}

	protected void setFooterViewAsTaskStateChanged(View footerView, ABaseTaskState state, TaskException exception, RefreshMode mode) {
		if (!refreshConfig.footerMoreEnable || footerView == null || mode != RefreshMode.update)
			return;

		if (state == ABaseTaskState.finished) {
			View layLoading = footerView.findViewById(R.id.layLoading);
			TextView btnLoadMore = (TextView) footerView.findViewById(R.id.btnMore);
			layLoading.setVisibility(View.GONE);
			btnLoadMore.setVisibility(View.VISIBLE);
		}
		else if (state == ABaseTaskState.prepare) {
			View layLoading = footerView.findViewById(R.id.layLoading);
			TextView txtLoadingHint = (TextView) footerView.findViewById(R.id.txtLoading);
			TextView btnLoadMore = (TextView) footerView.findViewById(R.id.btnMore);

			txtLoadingHint.setText(footerRefreshConfig.hintLoading);
			layLoading.setVisibility(View.VISIBLE);
			btnLoadMore.setVisibility(View.GONE);
			btnLoadMore.setText(footerRefreshConfig.btnMore);
		}
		else if (state == ABaseTaskState.success) {
			final TextView btnLoadMore = (TextView) footerView.findViewById(R.id.btnMore);
			if (refreshConfig.pagingEnd) {
				btnLoadMore.setText(footerRefreshConfig.btnPagingEnd);
			} else {
				btnLoadMore.setText(footerRefreshConfig.btnMore);
			}
		}
		else if (state == ABaseTaskState.falid) {
			// 正在加载
			View layLoading = footerView.findViewById(R.id.layLoading);
			if (layLoading.getVisibility() == View.VISIBLE) {
				TextView btnLoadMore = (TextView) footerView.findViewById(R.id.btnMore);
				btnLoadMore.setText(footerRefreshConfig.btnFaild);
			}
		}
	}

	/**
	 * 当前页面是否正在加载刷新
	 *
	 * @return
	 */
	public boolean isRefreshing() {
		return pagingTask != null;
	}
	
	private Runnable releaseRunnable = new Runnable() {
		
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

    public static class RefreshConfig {

        public boolean expiredAutoRefresh = true;// 缓存数据过期自动刷新列表

		public boolean pagingEnd = false;// 分页是否结束
		
		public String positionKey = null;// 最后阅读坐标的Key，null-不保存，针对缓存数据有效
		
		public int resultMin = 3;// 当加载的数据少于这个值时，默认没有更多加载
		
        public boolean displayWhenScrolling = true;// 滚动的时候加载图片

		public int releaseDelay = 5 * 1000;// 当配置了releaseItemIds参数时，离开页面后自动释放资源

		public int[] releaseItemIds = null;// 离开页面时，释放图片的控件，针对ItemView

		public String emptyHint = "数据为空";// 如果EmptyLayout中有R.id.txtLoadEmpty这个控件，将这个提示绑定显示

		public boolean footerMoreEnable = true;// FooterView加载更多
		
	}

	public static class FooterViewConfig {

		public String btnMore;

		public String btnFaild;

		public String btnPagingEnd;

		public String hintLoading;

	}

}
