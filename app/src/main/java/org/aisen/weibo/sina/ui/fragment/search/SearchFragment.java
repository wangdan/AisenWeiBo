package org.aisen.weibo.sina.ui.fragment.search;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.lapism.searchview.adapter.SearchAdapter;
import com.lapism.searchview.adapter.SearchItem;
import com.lapism.searchview.view.SearchCodes;
import com.lapism.searchview.view.SearchView;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.ViewUtils;
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
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.SearchsResultUser;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索用户或者微博，SearchView这个库还是有很多坑，哎
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

    static final String TAG = "SearchFragment";

    @ViewInject(id = R.id.searchView)
    SearchView mSearchView;
    private EditText editSearch;
    private View shadowView;
    private SearchsSuggestAdapter searchAdapter;
    private String q;
    private String suggest;
    private List<SearchItem> suggestList;
    private SearchHeaderView searchHeaderView;

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
    public int setActivityTheme() {
        return ThemeUtils.themeArr[AppSettings.getThemeColor()][3];
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
            public int[][] setHeaders() {
                return new int[][]{ { R.layout.ui_search_headerview, 100 } };
            }

            @Override
            public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                searchHeaderView = new SearchHeaderView(SearchFragment.this, convertView);
                return searchHeaderView;
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
                if (suggestList != null && searchAdapter != null)
                    onQuerySuggestChange(newText);

                return true;
            }

        });
        mSearchView.setOnSearchViewListener(new SearchView.SearchViewListener() {

            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {
                if (getSwipeRefreshLayout().getVisibility() == View.VISIBLE) {
                    getSwipeRefreshLayout().setVisibility(View.GONE);
                }

                getActivity().finish();

                getActivity().overridePendingTransition(0, 0);
            }

        });
        try {
            Field editField = SearchView.class.getDeclaredField("mEditText");
            editField.setAccessible(true);
            editSearch = (EditText) editField.get(mSearchView);
            editSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                }

            });

            Field shadowField = SearchView.class.getDeclaredField("mShadow");
            shadowField.setAccessible(true);
            shadowView = (View) shadowField.get(mSearchView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        suggestList = new ArrayList<>();
//        searchAdapter = new SearchAdapter(getActivity(), new ArrayList<SearchItem>(), suggestList, SearchCodes.THEME_LIGHT);
        searchAdapter = new SearchsSuggestAdapter(getActivity(), new ArrayList<SearchItem>(), suggestList, SearchCodes.THEME_LIGHT);
        searchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (suggestList.size() > position) {
                    onQuery(suggestList.get(position).get_text().toString());

                    editSearch.setText(q);
                    editSearch.setSelection(q.length());
                }
            }

        });
        mSearchView.setAdapter(searchAdapter);
        if (!TextUtils.isEmpty(q))
            mSearchView.setQuery(q);
        mSearchView.show(true);

        searchIn();
    }

    private void searchIn() {
        mHander.postDelayed(new Runnable() {

            @Override
            public void run() {
                try {
                    Method inMethod = SearchView.class.getDeclaredMethod("in");
                    inMethod.setAccessible(true);
                    inMethod.invoke(mSearchView);

                    editSearch.requestFocus();
                    SystemUtils.showKeyBoard(getActivity(), editSearch);
                } catch (Exception e) {
                    Logger.printExc(SearchFragment.class, e);
                }
            }

        }, 400);
    }

    @Override
    public void requestData(RefreshMode mode) {
        if (mode == RefreshMode.update && !TextUtils.isEmpty(q)) {
            new SearchStatussTask(mode, false).execute();
        }
    }

    private void onQuery(String q) {
        this.q = q;

        if (!TextUtils.isEmpty(q)) {
            // 搜索之前先停止搜索建议
            cancelSearchSuggestTask();

            new SearchStatussTask(RefreshMode.reset, true).execute();
        }
    }

    private void onQuerySuggestChange(String suggest) {
        this.suggest = suggest;

        if (!TextUtils.isEmpty(suggest) && !suggest.equals(q)) {
            mHander.removeCallbacks(searchsSuggestRunnable);
            mHander.postDelayed(searchsSuggestRunnable, 500);
        }
        // 清除数据
        else {
            suggestList.clear();
            searchAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void setupSwipeRefreshLayout() {
        super.setupSwipeRefreshLayout();

        getSwipeRefreshLayout().setEnabled(false);
    }

    private void refreshUsersUI(ArrayList<SearchsResultUser> users) {
        searchHeaderView.setUsers(users);
    }

    @Override
    protected IPaging<StatusContent, StatusContents> newPaging() {
        return new PageIndexPaging<>();
    }

    /**
     * 搜索话题线程
     *
     */
    class SearchStatussTask extends ATimelineTask {

        boolean showDialog = false;

        public SearchStatussTask(RefreshMode mode, boolean showDialog) {
            super(mode);

            this.showDialog = showDialog;
        }

        @Override
        protected void onPrepare() {
            super.onPrepare();

            if (showDialog) {
                ViewUtils.createProgressDialog(getActivity(), "", ThemeUtils.getThemeColor()).show();
            }
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            if (showDialog)
                ViewUtils.dismissProgressDialog();
        }

        @Override
        public StatusContents getStatusContents(Params params) throws TaskException {
            String nextPage = "1";
            if (params.containsKey("max_id")) {
                nextPage = params.getParameter("max_id");
            }

            // 1、搜索用户
            if ("1".equals(nextPage)) {
                ArrayList<SearchsResultUser> userList = SinaSDK.getInstance(AppContext.getAccount().getAccessToken())
                                                                .searchsResultUsers(q, AppContext.getAccount().getCookie());
                mHander.obtainMessage(100, userList).sendToTarget();
            }

            // 2、搜索微博
            ArrayList<StatusContent> statusList = SinaSDK.getInstance(AppContext.getAccount().getAccessToken())
                                            .searchsResultStatuss(q, Integer.parseInt(nextPage), AppContext.getAccount().getCookie());

            StatusContents datas = new StatusContents();
            datas.setStatuses(statusList);

            return datas;
        }

        @Override
        protected void onSuccess(StatusContents statusContents) {
            super.onSuccess(statusContents);

            if (mode != RefreshMode.update) {
                getRefreshView().scrollToPosition(0);
            }
            if (getSwipeRefreshLayout().getVisibility() != View.VISIBLE)
                getSwipeRefreshLayout().setVisibility(View.VISIBLE);
            if (shadowView.getVisibility() == View.VISIBLE) {
                outSearchWithData();
            }
        }

    }

    private void outSearchWithData() {
        mSearchView.out();
        editSearch.clearFocus();
        editSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    searchIn();

                    editSearch.setOnFocusChangeListener(null);
                }
            }

        });
        shadowView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                outSearchWithData();
            }

        });
    }

    /**
     * 延迟一秒再搜搜建议
     */
    Handler mHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 100) {
                ArrayList<SearchsResultUser> userList = (ArrayList<SearchsResultUser>) msg.obj;

                refreshUsersUI(userList);
            }
        }

    };
    Runnable searchsSuggestRunnable = new Runnable() {

        @Override
        public void run() {
            new SearchSuggestTask().execute(suggest);
        }

    };

    private void cancelSearchSuggestTask() {
        if (searchSuggestTask != null) {
            searchSuggestTask.cancel(true);
        }
    }

    /**
     * 搜索建议
     *
     */
    SearchSuggestTask searchSuggestTask;
    class SearchSuggestTask extends WorkTask<String, Void, String[]> {

        public SearchSuggestTask() {
            cancelSearchSuggestTask();

            searchSuggestTask = this;
        }

        @Override
        public String[] workInBackground(String... params) throws TaskException {
            return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).searchsSuggest(params[0], AppContext.getAccount().getCookie());
        }

        @Override
        protected void onSuccess(String[] result) {
            super.onSuccess(result);

            if (isCancelByUser() || getActivity() == null || shadowView.getVisibility() != View.VISIBLE) {
                return;
            }

            if (result.length > 0) {
                suggestList.clear();
                for (String s : result) {
                    Logger.d(TAG, "suggest = %s", s);

                    suggestList.add(new SearchItem(s));
                }
//                searchAdapter.getFilter().filter(getParams()[0], mSearchView);
                searchAdapter.setSearchList(suggestList);
            }

            mSearchView.onFilterComplete(result.length);
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            searchSuggestTask = null;
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(getActivity(), "搜索页面");
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), "搜索页面");
    }

}
