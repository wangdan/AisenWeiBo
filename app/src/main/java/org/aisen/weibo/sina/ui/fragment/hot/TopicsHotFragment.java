package org.aisen.weibo.sina.ui.fragment.hot;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.action.IAction;
import org.aisen.android.support.bean.TabItem;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.fragment.ARecycleViewSwipeRefreshFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.itemview.DefDividerItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.WebHotTopicsBean;
import org.aisen.weibo.sina.sinasdk.bean.WebHotTopicssBean;
import org.aisen.weibo.sina.support.action.WebLoginAction;
import org.aisen.weibo.sina.support.paging.TopicsHotPaging;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;

import java.util.List;

/**
 * 热门话题
 *
 * Created by wangdan on 16/8/14.
 */
public class TopicsHotFragment extends ARecycleViewSwipeRefreshFragment<WebHotTopicsBean, WebHotTopicssBean, WebHotTopicsBean> {

    public static void launch(Activity from) {
        SinaCommonActivity.launch(from , TopicsHotFragment.class, null);
    }

    public static TopicsHotFragment newInstance(TabItem item) {
        Bundle args = new Bundle();
        args.putSerializable("menu", item);

        TopicsHotFragment fragment = new TopicsHotFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private TabItem mMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMenu = savedInstanceState == null ? (TabItem) getArguments().getSerializable("menu")
                                           : (TabItem) savedInstanceState.getSerializable("menu");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("menu", mMenu);
    }

    @Override
    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);

        DefDividerItemView divider = new DefDividerItemView(getActivity(), R.color.divider_timeline_item);
        divider.setSize(1.0f);
        getRefreshView().addItemDecoration(divider);
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        getSwipeRefreshLayout().setEnabled(false);
    }

    @Override
    protected IPaging<WebHotTopicsBean, WebHotTopicssBean> newPaging() {
        return new TopicsHotPaging();
    }

    @Override
    public IItemViewCreator<WebHotTopicsBean> configItemViewCreator() {
        return new IItemViewCreator<WebHotTopicsBean>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(R.layout.item_topics_hot, parent, false);
            }

            @Override
            public IITemView<WebHotTopicsBean> newItemView(View convertView, int viewType) {
               return new TopicsHotItemView(getActivity(), convertView);
            }

        };
    }

    class TopicsHotItemView extends ARecycleViewItemView<WebHotTopicsBean> {

        @ViewInject(id = R.id.imgPic)
        ImageView imgPic;
        @ViewInject(id = R.id.txtTitle)
        TextView txtTitle;
        @ViewInject(id = R.id.txtDesc1)
        TextView txtDesc1;
        @ViewInject(id = R.id.txtDesc2)
        TextView txtDesc2;

        private ImageConfig config;

        public TopicsHotItemView(Activity context, View itemView) {
            super(context, itemView);

            config = new ImageConfig();
            config.setLoadingRes(R.drawable.bg_timeline_loading);
            config.setLoadfaildRes(R.drawable.bg_timeline_loading);
        }

        @Override
        public void onBindData(View convertView, WebHotTopicsBean data, int position) {
            String pic = data.getPic();
            if (SystemUtils.getNetworkType(getActivity()) == SystemUtils.NetWorkType.wifi) {
                pic = pic.replace("thumbnail", "large");
                config.setId("large");
            }
            BitmapLoader.getInstance().display(TopicsHotFragment.this, pic, imgPic, config);
            txtTitle.setText(data.getTitle_sub());
            txtDesc1.setText(data.getDesc1());
            txtDesc2.setText(data.getDesc2());
        }

    }

    @Override
    public void requestData(final RefreshMode mode) {
        new IAction(getActivity(), new WebLoginAction(getActivity(), BizFragment.createBizFragment(this))) {

            @Override
            public void doAction() {
                new TopicsHotTask(mode != RefreshMode.update ? RefreshMode.reset : mode).execute();
            }

        }.run();
    }

    class TopicsHotTask extends APagingTask<Void, Void, WebHotTopicssBean> {

        public TopicsHotTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected List<WebHotTopicsBean> parseResult(WebHotTopicssBean webHotTopicssBean) {
            return webHotTopicssBean.getList();
        }

        @Override
        protected WebHotTopicssBean workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... params) throws TaskException {
            String sinceId = null;
            int page = 0;

            if (!TextUtils.isEmpty(nextPage)) {
                if (nextPage.startsWith("page_")) {
                    page = Integer.parseInt(nextPage.replace("page_", ""));
                }
                else {
                    sinceId = nextPage;
                }
            }

            return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).webGetHotTopics(mMenu.getType(), sinceId, page);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);

        TopicsHotTimelinePagerFragment.launch(getActivity(), getAdapterItems().get(position));
    }

    @Override
    public boolean onToolbarDoubleClick() {
        requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);
        getRefreshView().scrollToPosition(0);

        return true;
    }

}
