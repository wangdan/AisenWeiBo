package org.aisen.weibo.sina.ui.fragment.search;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import org.aisen.android.component.container.FragmentContainerActivity;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.adapter.ABaseAdapter;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ASwipeRefreshListFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.ui.activity.profile.UserProfileActivity;
import org.aisen.weibo.sina.ui.fragment.profile.UserTimelineFragment;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.SuggestionsUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdan on 15/4/28.
 */
public class SearchUserFragment extends ASwipeRefreshListFragment<SuggestionsUser, SuggestionsUser[]> {

    public static void launch(Activity from) {
        FragmentContainerActivity.launch(from, SearchUserFragment.class, null);
    }

    @ViewInject(id = R.id.searchView)
    SearchView searchView;

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        BaseActivity baseActivity = (BaseActivity) getActivity();
        baseActivity.getSupportActionBar().setTitle(R.string.title_search_user);
        baseActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchView.setQueryHint(getString(R.string.search_user_hint));
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserProfileActivity.launch(getActivity(), getAdapterItems().get(position).getScreen_name());
    }

    Handler mHandler = new Handler();

    Runnable searchRunnable = new Runnable() {

        @Override
        public void run() {
            if (!TextUtils.isEmpty(searchView.getQuery().toString())) {
                new SearchUsersTask().execute(searchView.getQuery().toString());
            }
            else {
                findViewById(R.id.layoutContent).setVisibility(View.GONE);
                findViewById(R.id.layoutEmpty).setVisibility(View.VISIBLE);
            }
        }

    };

    @Override
    protected int inflateContentView() {
        return R.layout.as_ui_search_user;
    }

    @Override
    protected ABaseAdapter.AbstractItemView<SuggestionsUser> newItemView() {
        return new SearchUserItemView();
    }

    @Override
    protected void requestData(RefreshMode mode) {

    }

    SearchUsersTask mTask;
    class SearchUsersTask extends PagingTask<String, Void, SuggestionsUser[]> {

        public SearchUsersTask() {
            super("SearchUsersTask", RefreshMode.reset);
            if (mTask != null)
                mTask.cancel(true);

            mTask = this;
        }

        @Override
        protected List<SuggestionsUser> parseResult(SuggestionsUser[] result) {
            List<SuggestionsUser> list = new ArrayList<SuggestionsUser>();

            for (SuggestionsUser user : result)
                list.add(user);

            return list;
        }

        @Override
        protected SuggestionsUser[] workInBackground(RefreshMode mode, String previousPage, String nextPage,
                                                     String... params) throws TaskException {
            return SinaSDK.getInstance(AppContext.getToken()).searchSuggestionsUsers(params[0], 100);
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            showMessage(exception.getMessage());
        }

    }

    class SearchUserItemView extends ABaseAdapter.AbstractItemView<SuggestionsUser> {

        @ViewInject(id = R.id.imgPhoto)
        ImageView imgPhoto;
        @ViewInject(id = R.id.txtName)
        TextView txtName;
        @ViewInject(id = R.id.txtRemark)
        TextView txtRemark;
        @ViewInject(id = R.id.txtDivider)
        TextView txtDivider;
        @ViewInject(id = R.id.layDivider)
        View layDivider;

        @Override
        public int inflateViewId() {
            return R.layout.as_item_friend;
        }

        @Override
        public void bindingData(View convertView, SuggestionsUser data) {
            imgPhoto.setVisibility(View.GONE);
            txtName.setText(data.getScreen_name());
            txtRemark.setText("");
            layDivider.setVisibility(View.GONE);
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        searchView.clearFocus();
    }
}
