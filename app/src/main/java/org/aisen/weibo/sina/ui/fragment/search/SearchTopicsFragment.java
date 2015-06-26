package org.aisen.weibo.sina.ui.fragment.search;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.aisen.android.component.container.FragmentArgs;
import org.aisen.android.component.container.FragmentContainerActivity;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.support.paging.PageIndexPaging;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ARefreshFragment;
import org.aisen.android.ui.fragment.AStripTabsFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.w3c.dom.Text;

/**
 * Created by wangdan on 15/4/28.
 */
public class SearchTopicsFragment extends ATimelineFragment {

    public static void launch(Activity from) {
        FragmentArgs args = new FragmentArgs();
        AStripTabsFragment.StripTabItem group = new AStripTabsFragment.StripTabItem();
        group.setType("search");
        group.setTitle("search");
        args.add("bean", group);

        FragmentContainerActivity.launch(from, SearchTopicsFragment.class, args);
    }

    @ViewInject(id = R.id.searchView)
    SearchView searchView;
    @ViewInject(id = R.id.txtEmpty)
    TextView txtEmpty;

    @Override
    protected int inflateContentView() {
        return R.layout.as_ui_search_user;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        BaseActivity baseActivity = (BaseActivity) getActivity();
        baseActivity.getSupportActionBar().setTitle(R.string.title_search_status);
        baseActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtEmpty.setText("");
        searchView.setQueryHint(getString(R.string.hint_topics));
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                mHandler.removeCallbacks(searchRunnable);
                if (!TextUtils.isEmpty(searchView.getQuery().toString())) {
                    mHandler.postDelayed(searchRunnable, 1000);
                }
                else {
                    findViewById(R.id.layoutContent).setVisibility(View.GONE);
                    findViewById(R.id.layoutEmpty).setVisibility(View.VISIBLE);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mHandler.removeCallbacks(searchRunnable);
                if (!TextUtils.isEmpty(searchView.getQuery().toString())) {
                    mHandler.postDelayed(searchRunnable, 1000);
                }
                else {
                    findViewById(R.id.layoutContent).setVisibility(View.GONE);
                    findViewById(R.id.layoutEmpty).setVisibility(View.VISIBLE);
                }
                return true;
            }

        });
    }

    Handler mHandler = new Handler();

    Runnable searchRunnable = new Runnable() {

        @Override
        public void run() {
            if (!TextUtils.isEmpty(searchView.getQuery().toString())) {
                new SearchTopicsTask(searchView.getQuery().toString()).execute();
            }
            else {
                findViewById(R.id.layoutContent).setVisibility(View.GONE);
                findViewById(R.id.layoutEmpty).setVisibility(View.VISIBLE);
            }
        }

    };

    @Override
    protected IPaging<StatusContent, StatusContents> configPaging() {
        return new PageIndexPaging<StatusContent, StatusContents>();
    }

    @Override
    protected void requestData(RefreshMode mode) {
        if (mode == RefreshMode.update)
            new UpdateTask().execute();
    }

    SearchTopicsTask mTask;
    class SearchTopicsTask extends TimelineTask {

        String q;

        public SearchTopicsTask(String q) {
            super(RefreshMode.reset);

            this.q = q;

            if (mTask != null)
                mTask.cancel(true);
            if (updateTask != null)
                updateTask.cancel(true);

            mTask = this;
        }

        @Override
        protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage,
                                                  Void... params) throws TaskException {
            StatusContents datas = SinaSDK.getInstance(AppContext.getToken()).searchTopics(nextPage, q, "30");

            // 把图片塞进去
            for (StatusContent data : datas.getStatuses()) {
                if (data.getPic_urls() == null && !TextUtils.isEmpty(data.getThumbnail_pic())) {
                    PicUrls picUrls = new PicUrls();
                    picUrls.setThumbnail_pic(data.getThumbnail_pic());

                    data.setPic_urls(new PicUrls[]{ picUrls });
                }
            }

            return datas;
        }

        @Override
        protected void onSuccess(StatusContents result) {
            super.onSuccess(result);

            if (!TextUtils.isEmpty(searchView.getQuery()) && isContentEmpty())
                txtEmpty.setText(R.string.searh_topic_empty);
            else
                txtEmpty.setText("");
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            mTask = null;
        }

    }

    UpdateTask updateTask;
    class UpdateTask extends TimelineTask {

        UpdateTask() {
            super(RefreshMode.update);
            updateTask = this;
        }

        @Override
        protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... params) throws TaskException {
            StatusContents datas = SinaSDK.getInstance(AppContext.getToken())
                        .searchTopics(nextPage, searchView.getQuery().toString(), "30");

            // 把图片塞进去
            for (StatusContent data : datas.getStatuses()) {
                if (data.getPic_urls() == null && !TextUtils.isEmpty(data.getThumbnail_pic())) {
                    PicUrls picUrls = new PicUrls();
                    picUrls.setThumbnail_pic(data.getThumbnail_pic());

                    data.setPic_urls(new PicUrls[]{ picUrls });
                }
            }

            return datas;
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            updateTask = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        searchView.clearFocus();
    }

}
