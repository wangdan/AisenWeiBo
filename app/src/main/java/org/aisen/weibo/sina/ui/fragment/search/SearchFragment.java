package org.aisen.weibo.sina.ui.fragment.search;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lapism.searchview.adapter.SearchAdapter;
import com.lapism.searchview.adapter.SearchItem;
import com.lapism.searchview.view.SearchCodes;
import com.lapism.searchview.view.SearchView;

import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.support.paging.PageIndexPaging;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.android.ui.fragment.itemview.AHeaderItemViewCreator;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索用户或者微博
 *
 * Created by wangdan on 16/2/24.
 */
public class SearchFragment extends ATimelineFragment {

    public static void launch(Activity from, String q) {
        FragmentArgs args = new FragmentArgs();
        args.add("q", q);

        SinaCommonActivity.launch(from, SearchFragment.class, args);

        from.overridePendingTransition(-1, -1);
    }

    @ViewInject(id = R.id.searchView)
    SearchView mSearchView;
    private SearchAdapter searchAdapter;
    private String q;
    private List<SearchItem> suggestList;

    public SearchFragment() {
        setArguments(new Bundle());
    }

    @Override
    public int inflateContentView() {
        return -1;
    }

    @Override
    public int inflateActivityContentView() {
        return R.layout.ui_search_activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setupContentView(inflater, (ViewGroup) ((BaseActivity) getActivity()).getRootView(), savedInstanceState);

        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        q = savedInstanceState == null ? getArguments().getString("q", "") : savedInstanceState.getString("q");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("q", q);
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        setupSearchView();
    }

    @Override
    protected AHeaderItemViewCreator<StatusContent> configHeaderViewCreator() {
        return new AHeaderItemViewCreator<StatusContent>() {

            @Override
            public int[][] setHeaderLayoutRes() {
                return new int[][]{ { R.layout.ui_search_headerview, 100 } };
            }

            @Override
            public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                return new SearchHeaderView(convertView);
            }

        };
    }

    private void setupSearchView() {
        // SearchView basic attributes  ------------------------------------------------------------
        int mVersion = SearchCodes.VERSION_MENU_ITEM;
        int mStyle = SearchCodes.STYLE_MENU_ITEM_CLASSIC;
        int mTheme = SearchCodes.THEME_LIGHT;

        mSearchView.setVersion(mVersion);
        mSearchView.setStyle(mStyle);
        mSearchView.setTheme(mTheme);
        mSearchView.setDivider(false);
        mSearchView.setHint(R.string.search_hint);
        mSearchView.setHintSize(getResources().getDimension(R.dimen.search_text_medium));
        mSearchView.setVoice(false);
        mSearchView.setAnimationDuration(300);
        mSearchView.setShadowColor(ContextCompat.getColor(getActivity(), R.color.background_dim_overlay));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    onQuery(query);
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mHander.removeCallbacks(searchsSuggestRunnable);
                mHander.postDelayed(searchsSuggestRunnable, 1000);

                return true;
            }

        });
        // 修改Back键的点击事件
        try {
            Field backField = SearchView.class.getDeclaredField("mBackImageView");
            backField.setAccessible(true);
            ImageView editText = (ImageView) backField.get(mSearchView);
            editText.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    getActivity().overridePendingTransition(-1, -1);

                    getActivity().finish();
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSearchView.show(true);
        suggestList = new ArrayList<>();
        searchAdapter = new SearchAdapter(getActivity(), new ArrayList<SearchItem>(), suggestList, SearchCodes.THEME_LIGHT);
        mSearchView.setAdapter(searchAdapter);
        if (!TextUtils.isEmpty(q))
            mSearchView.setQuery(q);
    }

    @Override
    public void requestData(RefreshMode mode) {
        if (mode == RefreshMode.update) {
            new SearchTopicsTask(mode).execute();
        }
    }

    private void onQuery(String q) {
        this.q = q;

        new SearchTopicsTask(RefreshMode.reset).execute();
    }

    @Override
    protected void setupSwipeRefreshLayout() {
        super.setupSwipeRefreshLayout();

        getSwipeRefreshLayout().setEnabled(false);
    }

    @Override
    protected IPaging<StatusContent, StatusContents> newPaging() {
        return new PageIndexPaging<>();
    }

    /**
     * 搜索话题线程
     *
     */
    class SearchTopicsTask extends ATimelineTask {

        public SearchTopicsTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        public StatusContents getStatusContents(Params params) throws TaskException {
            String nextPage = "1";
            if (params.containsKey("max_id")) {
                nextPage = params.getParameter("max_id");
            }

            SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).searchsSuggest("ana", AppContext.getAccount().getCookie());

            StatusContents datas = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).searchTopics(nextPage, q, "30");

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
    }

    /**
     * 延迟一秒再搜搜建议
     */
    Handler mHander = new Handler();
    Runnable searchsSuggestRunnable = new Runnable() {

        @Override
        public void run() {
            new SearchSuggestTask().execute(q);
        }

    };

    /**
     * 搜索建议
     *
     */
    SearchSuggestTask searchSuggestTask;
    class SearchSuggestTask extends WorkTask<String, Void, String[]> {

        public SearchSuggestTask() {
            if (searchSuggestTask != null) {
                searchSuggestTask.cancel(true);
            }

            searchSuggestTask = this;
        }

        @Override
        public String[] workInBackground(String... params) throws TaskException {
            return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).searchsSuggest(params[0], AppContext.getAccount().getCookie());
        }

        @Override
        protected void onSuccess(String[] result) {
            super.onSuccess(result);

            if (isCancelled()) {
                return;
            }

            if (result.length > 0) {
                suggestList.clear();
                for (String s : result) {
                    suggestList.add(new SearchItem(s));
                }
                searchAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            searchSuggestTask = null;
        }

    }

}
