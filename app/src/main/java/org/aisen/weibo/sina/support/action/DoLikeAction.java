package org.aisen.weibo.sina.support.action;

import java.util.List;

import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.bean.LikeBean;
import org.aisen.weibo.sina.support.bean.LikeResultBean;
import org.aisen.weibo.sina.support.biz.BaseBizlogic;
import org.aisen.weibo.sina.support.db.LikeDB;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.ui.fragment.basic.BizFragment;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;

import android.app.Activity;
import android.view.View;

import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.component.bitmaploader.core.LruMemoryCache;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.action.IAction;
import org.aisen.orm.extra.Extra;
import org.aisen.orm.utils.FieldUtils;

/**
 * Created by wangdan on 15/5/1.
 */
public class DoLikeAction extends IAction {

    public static LruMemoryCache<String, LikeBean> likeCache = new LruMemoryCache<String, LikeBean>(30);

    BizFragment bizFragment;
    boolean like;
    StatusContent data;
    OnLikeCallback callback;
    View likeView;

    public DoLikeAction(Activity context, BizFragment bizFragment, View likeView,
                                StatusContent data, boolean like, OnLikeCallback callback) {
        super(context, new WebLoginAction(context, bizFragment));

        this.bizFragment = bizFragment;
        this.data = data;
        this.like = like;
        this.likeView = likeView;
        this.callback = callback;
    }

    @Override
    public void doAction() {
        final String key = String.valueOf(data.getId());

        LikeBean likeBean = likeCache.get(key);

        // 如果还没有点赞过，首先添加一个点赞
        if (likeBean == null) {
            likeBean = new LikeBean();
            likeBean.setLiked(like);
            likeBean.setStatusId(key);

            LikeDB.insert(likeBean);
            likeCache.put(key, likeBean);
        }
        // 点赞过了，刷新内存，刷新DB
        else {
            likeBean.setLiked(like);

            LikeDB.insert(likeBean);
        }

        // 开始处理点赞
        new LikeTask().execute();
    }

    public static void refreshLikeCache() {
        likeCache.evictAll();

        new WorkTask<Void, Void, Void>() {

            @Override
            public Void workInBackground(Void... params) throws TaskException {
                String uid = AppContext.getUser().getIdstr();

                String selection = String.format(" %s = ? ", FieldUtils.OWNER);
                String[] args = new String[]{ uid };
                List<LikeBean> likeBeans = SinaDB.getSqlite().select(LikeBean.class, selection, args);
                SinaDB.getSqlite().deleteAll(new Extra(uid, null), LikeBean.class);
                SinaDB.getSqlite().insert(new Extra(uid, null), likeBeans);

                for (LikeBean likeBean : likeBeans)
                    likeCache.put(likeBean.getStatusId(), likeBean);
                return null;
            }

        }.execute();
    }

    public interface OnLikeCallback {

        // 点赞失败或者成功后，有必要时会回调这个方法刷新UI
        public void onLikeRefreshUI();

        public void onLikeRefreshView(StatusContent data, View likeView);

    }

    public boolean isRunning() {
        return mTask != null;
    }

    LikeTask mTask = null;
    class LikeTask extends WorkTask<Void, Void, LikeResultBean> {

        LikeTask() {
            mTask = this;
        }

        @Override
        public LikeResultBean workInBackground(Void... params) throws TaskException {
            return BaseBizlogic.newInstance().doLike(data.getId() + "", like, AppContext.getAccount().getCookie());
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            final String key = String.valueOf(data.getId());

            LikeBean likeBean = likeCache.get(key);
            if (likeBean != null) {
                likeBean.setLiked(!like);
                LikeDB.insert(likeBean);
            }

            if (bizFragment.getActivity() == null)
                return;

            // 未登录，或者登录失效
            if ("-100".equalsIgnoreCase(exception.getCode())) {

                ((WebLoginAction) getParent()).doAction();
            }
            else {
                ViewUtils.showMessage(exception.getMessage());
            }

            callback.onLikeRefreshUI();
        }

        @Override
        protected void onSuccess(LikeResultBean likeResultBean) {
            super.onSuccess(likeResultBean);

            callback.onLikeRefreshView(data, likeView);
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            mTask = null;
        }
    }

}
