package org.aisen.weibo.sina.ui.fragment.search;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.m.component.container.FragmentArgs;
import com.m.component.container.FragmentContainerActivity;
import com.m.network.task.TaskException;
import com.m.support.inject.ViewInject;
import com.m.support.paging.IPaging;
import com.m.support.paging.PageIndexPaging;
import com.m.ui.activity.basic.BaseActivity;
import com.m.ui.fragment.AStripTabsFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;
import org.sina.android.SinaSDK;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.StatusContents;

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

    }

    SearchTopicsTask mTask;
    class SearchTopicsTask extends TimelineTask {

        String q;

        public SearchTopicsTask(String q) {
            super(RefreshMode.reset);

            this.q = q;

            if (mTask != null)
                mTask.cancel(true);

            mTask = this;
        }

        @Override
        protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage,
                                                  Void... params) throws TaskException {
            return SinaSDK.getInstance(AppContext.getToken()).searchTopics(nextPage, q, "30");
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

}
