package org.aisen.android.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.aisen.android.R;
import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.BitmapOwner;
import org.aisen.android.network.biz.ABizLogic.CacheMode;
import org.aisen.android.network.biz.IResult;
import org.aisen.android.network.task.ITaskManager;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.TaskManager;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.InjectUtility;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;

import java.text.SimpleDateFormat;

/**
 * 基于ABaseFragment，维护与Activity之间的生命周期绑定，管理WorkTask线程，支持四种个基本视图之间的自动切换<br/>
 *
 * 1、处理缓存数据过期后，自动刷新页面<br/>
 * 2、处理页面离开设定时间后，自动刷新页面<br/>
 *
 */
public abstract class ABaseFragment extends Fragment implements ITaskManager, BitmapOwner {

    static final String TAG = "AFragment-Base";

    public enum ABaseTaskState {
        none, prepare, falid, success, finished, canceled
    }

    private TaskManager taskManager;// 管理线程

    ViewGroup rootView;// 根视图
    @ViewInject(idStr = "layoutLoading")
    View loadingLayout;// 加载中视图
    @ViewInject(idStr = "layoutLoadFailed")
    View loadFailureLayout;// 加载失败视图
    @ViewInject(idStr = "layoutContent")
    View contentLayout;// 内容视图
    @ViewInject(idStr = "layoutEmpty")
    View emptyLayout;// 空视图

    // 标志是否ContentView是否为空
    private boolean contentEmpty = true;

    protected long lastResultGetTime = 0;// 最后一次非缓存数据获取时间

    private boolean destory = false;

    // UI线程的Handler
    Handler mHandler = new Handler(Looper.getMainLooper()) {

    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof BaseActivity)
            ((BaseActivity) activity).addFragment(toString(), this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        taskManager = new TaskManager();

        if (savedInstanceState != null)
            taskManager.restore(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (inflateContentView() > 0) {
            ViewGroup contentView = (ViewGroup) inflater.inflate(inflateContentView(), null);
            contentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            setupContentView(inflater, contentView, savedInstanceState);

            return getContentView();
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * 根据ContentView初始化视图
     *
     * @param inflater
     * @param contentView
     * @param savedInstanceState
     */
    protected void setupContentView(LayoutInflater inflater, ViewGroup contentView, Bundle savedInstanceState) {
        setContentView(contentView);

        _layoutInit(inflater, savedInstanceState);

        layoutInit(inflater, savedInstanceState);
    }

    public void setContentView(ViewGroup view) {
        this.rootView = view;
    }

    /**
     * 根视图
     *
     * @return
     */
    public ViewGroup getContentView() {
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null)
            requestData();
    }

    /**
     * Action的home被点击了
     *
     * @return
     */
    public boolean onHomeClick() {
        return onBackClick();
    }

    /**
     * 返回按键被点击了
     *
     * @return
     */
    public boolean onBackClick() {
        return false;
    }

    /**
     * 初次创建时默认会调用一次
     */
    public void requestData() {

    }

    /**
     * 延迟时间刷新
     *
     * @param delay
     */
    public void requestDataDelay(long delay) {
        Runnable requestDelayRunnable = new Runnable() {

            @Override
            public void run() {
                Logger.d(TAG, "延迟刷新，开始刷新, " + toString());

                requestData();
            }

        };

        runUIRunnable(requestDelayRunnable, delay);
    }

    public void requestDataOutofdate() {
        requestData();
    }

    /**
     * A*Fragment重写这个方法
     *
     * @param inflater
     * @param savedInstanceSate
     */
    void _layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        InjectUtility.initInjectedView(getActivity(), this, getContentView());

        if (emptyLayout != null) {
            View reloadView = emptyLayout.findViewById(R.id.layoutReload);
            if (reloadView != null)
                setViewOnClick(reloadView);
        }

        if (loadFailureLayout != null) {
            View reloadView = loadFailureLayout.findViewById(R.id.layoutReload);
            if (reloadView != null)
                setViewOnClick(reloadView);
        }

        setViewVisiable(loadingLayout, View.GONE);
        setViewVisiable(loadFailureLayout, View.GONE);
        setViewVisiable(emptyLayout, View.GONE);

        if (isContentEmpty()) {
            // 如果视图为空，就开始加载数据
            if (savedInstanceSate != null) {
                requestData();
            }
            else {
                setViewVisiable(emptyLayout, View.VISIBLE);
                setViewVisiable(contentLayout, View.GONE);
            }
        }
        else {
            setViewVisiable(contentLayout, View.VISIBLE);
        }
    }

    public View findViewById(int viewId) {
        if (getContentView() == null)
            return null;

        return getContentView().findViewById(viewId);
    }

    public void setContentEmpty(boolean empty) {
        this.contentEmpty = empty;
    }

    public boolean isContentEmpty() {
        return contentEmpty;
    }

    /**
     * 视图点击回调，子类重写
     *
     * @param view
     */
    public void onViewClicked(View view) {
        if (view.getId() == R.id.layoutReload)
            requestData();
        else if (view.getId() == R.id.layoutRefresh)
            requestData();
    }

    protected void setViewVisiable(View v, int visibility) {
        if (v != null && v.getVisibility() != visibility)
            v.setVisibility(visibility);
    }

    /**
     * 根据{@link ABaseTask} 的加载状态，刷新视图
     *
     * @param state
     *
     * @param exception
     */
    protected void onTaskStateChanged(ABaseTaskState state, TaskException exception) {
        // 开始Task
        if (state == ABaseTaskState.prepare) {
            if (isContentEmpty()) {
                setViewVisiable(loadingLayout, View.VISIBLE);

                setViewVisiable(contentLayout, View.GONE);
            }
            else {
                setViewVisiable(loadingLayout, View.GONE);

                setViewVisiable(contentLayout, View.VISIBLE);
            }

            setViewVisiable(emptyLayout, View.GONE);
            if (isContentEmpty() && loadingLayout == null) {
                setViewVisiable(contentLayout, View.VISIBLE);
            }

            setViewVisiable(loadFailureLayout, View.GONE);
        }
        // Task成功
        else if (state == ABaseTaskState.success) {
            setViewVisiable(loadingLayout, View.GONE);

            if (isContentEmpty()) {
                setViewVisiable(emptyLayout, View.VISIBLE);
                setViewVisiable(contentLayout, View.GONE);
            }
            else {
                setViewVisiable(contentLayout, View.VISIBLE);
                setViewVisiable(emptyLayout, View.GONE);
            }
        }
        // 取消Task
        else if (state == ABaseTaskState.canceled) {
            if (isContentEmpty()) {
                setViewVisiable(loadingLayout, View.GONE);
                setViewVisiable(emptyLayout, View.VISIBLE);
            }
        }
        // Task失败
        else if (state == ABaseTaskState.falid) {
            if (isContentEmpty()) {
                if (loadFailureLayout != null) {
                    setViewVisiable(loadFailureLayout, View.VISIBLE);

                    if (exception != null) {
                        TextView txtLoadFailed = (TextView) loadFailureLayout.findViewById(R.id.txtLoadFailed);
                        if (txtLoadFailed != null)
                            txtLoadFailed.setText(exception.getMessage());
                    }

                    setViewVisiable(emptyLayout, View.GONE);
                } else {
                    setViewVisiable(emptyLayout, View.VISIBLE);
                }
                setViewVisiable(loadingLayout, View.GONE);
            }
        }
        // Task结束
        else if (state == ABaseTaskState.finished) {

        }
    }

    public void showMessage(CharSequence msg) {
        if (!TextUtils.isEmpty(msg) && getActivity() != null)
            ViewUtils.showMessage(getActivity(), msg.toString());
    }

    public void showMessage(int msgId) {
        if (getActivity() != null)
            showMessage(getString(msgId));
    }

    /**
     * Fragment主要的刷新任务线程，定义任务加载流程，耦合Fragment各个状态下的视图刷新方法
     *
     * @param <Params>
     * @param <Progress>
     * @param <Result>
     */
    protected abstract class ABaseTask<Params, Progress, Result> extends WorkTask<Params, Progress, Result> {

        public ABaseTask(String taskId) {
            super(taskId, ABaseFragment.this);
        }

        @Override
        protected void onPrepare() {
            super.onPrepare();

            onTaskStateChanged(ABaseTaskState.prepare, null);
        }

        @Override
        protected void onSuccess(Result result) {
            super.onSuccess(result);

            // 默认加载数据成功，且ContentView有数据展示
            ABaseFragment.this.setContentEmpty(resultIsEmpty(result));

            onTaskStateChanged(ABaseTaskState.success, null);

            if (Logger.DEBUG)
                Logger.d(TAG, "Result获取时间：%s", new SimpleDateFormat("HH:mm:ss").format(lastResultGetTime));

            if (result instanceof IResult) {
                IResult iResult = (IResult) result;

                // 数据是缓存数据
                if (iResult.fromCache()) {
                    // 缓存过期刷新数据
                    if (iResult.outofdate()) {
                        runUIRunnable(new Runnable() {

                                                @Override
                                                public void run() {
                                                    Logger.d(TAG, "数据过期，开始刷新, " + toString());

                                                    requestDataOutofdate();
                                                }

                                            }, configRequestDelay());
                    }
                } else {
                    lastResultGetTime = System.currentTimeMillis();
                }
            } else {
                lastResultGetTime = System.currentTimeMillis();
            }
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            onTaskStateChanged(ABaseTaskState.falid, exception);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            onTaskStateChanged(ABaseTaskState.canceled, null);
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            onTaskStateChanged(ABaseTaskState.finished, null);
        }

        /**
         * 返回数据是否空
         *
         * @param result
         * @return
         */
        protected boolean resultIsEmpty(Result result) {
            return result == null ? true : false;
        }

    }

    @Override
    public void onDestroy() {
        destory = true;

        try {
            // 4.1.1必报错，不知道为什么
            super.onDestroy();
        } catch (Exception e) {
            Logger.printExc(getClass(), e);
        }

        removeAllTask(true);

        if (BitmapLoader.getInstance() != null)
        	BitmapLoader.getInstance().cancelPotentialTask(this);
    }

    public boolean isDestory() {
        return destory;
    }

    public boolean isActivityRunning() {
        return getActivity() != null;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (getActivity() != null && getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).removeFragment(this.toString());
    }

    @Override
    final public void addTask(@SuppressWarnings("rawtypes") WorkTask task) {
        taskManager.addTask(task);
    }

    @Override
    final public void removeTask(String taskId, boolean cancelIfRunning) {
        taskManager.removeTask(taskId, cancelIfRunning);
    }

    @Override
    final public void removeAllTask(boolean cancelIfRunning) {
        taskManager.removeAllTask(cancelIfRunning);
    }

    @Override
    final public int getTaskCount(String taskId) {
        return taskManager.getTaskCount(taskId);
    }

    /**
     * 初步定义，当Task执行BizLogic方法时，第一次创建时拉取缓存，其他都只拉取网络
     *
     * @param task
     * @return
     */
    final protected CacheMode getTaskCacheMode(WorkTask task) {
        if (task == null || !TextUtils.isEmpty(task.getTaskId()))
            return getTaskCount(task.getTaskId()) == 1 ? CacheMode.auto : CacheMode.disable;

        return CacheMode.disable;
    }

    public void cleatTaskCount(String taskId) {
        taskManager.cleatTaskCount(taskId);
    }

    protected void setViewOnClick(View v) {
        if (v == null)
            return;

        v.setOnClickListener(innerOnClickListener);
    }

    View.OnClickListener innerOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            onViewClicked(v);
        }

    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (taskManager != null)
            taskManager.save(outState);
    }

    protected ITaskManager getTaskManager() {
        return taskManager;
    }

    public void runUIRunnable(Runnable runnable) {
        runUIRunnable(runnable, 0);
    }

    public void runUIRunnable(Runnable runnable, long delay) {
        if (delay > 0) {
            mHandler.removeCallbacks(runnable);
            mHandler.postDelayed(runnable, delay);
        }
        else {
            mHandler.post(runnable);
        }
    }

    /**
     * 子类重写这个方法，初始化视图
     *
     * @param inflater
     * @param savedInstanceSate
     */
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {

    }

    /**
     * 是否显示图片接口实现
     */
    @Override
    public boolean canDisplay() {
        return true;
    }

    /**
     * 指定Fragment的LayoutID
     *
     * @return
     */
    abstract public int inflateContentView();

    /**
     * 指定Activity的ContentViewID
     *
     * @return
     */
    public int inflateActivityContentView() {
        return -1;
    }

    /**
     * 设置Activity的Theme
     *
     * @return
     */
    public int setActivityTheme() {
        return -1;
    }

    public int configRequestDelay() {
        return 500;
    }

    public View getLoadingLayout() {
        return loadingLayout;
    }

    public View getLoadFailureLayout() {
        return loadFailureLayout;
    }

    public View getContentLayout() {
        return contentLayout;
    }

    public View getEmptyLayout() {
        return emptyLayout;
    }
}
