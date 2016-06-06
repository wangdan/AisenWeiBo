package org.aisen.weibo.sina.ui.fragment.secondgroups;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.android.ui.fragment.AWaterfallSwipeRefreshFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.android.ui.widget.pla.PLAAdapterView;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.support.bean.JokeBean;
import org.aisen.weibo.sina.support.bean.JokeBeans;
import org.aisen.weibo.sina.support.paging.JokePaging;
import org.aisen.weibo.sina.support.sdk.SDK;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.activity.picture.PicsActivity;

import java.util.List;

/**
 * 0:纯文 1:图文
 *
 * Created by wangdan on 16/3/14.
 */
public class JokesFragment extends AWaterfallSwipeRefreshFragment<JokeBean, JokeBeans> implements ATabsFragment.ITabInitData {

    final static int[] themeColorArr = {
            R.color.md_red_700,
            R.color.md_pink_700,
            R.color.md_purple_700,
            R.color.md_deep_purple_700,
            R.color.md_indigo_700,
            R.color.md_blue_700,
            R.color.md_light_blue_700,
            R.color.md_cyan_700,
            R.color.md_teal_700,
            R.color.md_green_700,
            R.color.md_light_green_700,
            R.color.md_lime_700,
            R.color.md_orange_700,
            R.color.md_deep_orange_700,
            R.color.md_brown_700,
            R.color.md_grey_700,
            R.color.md_blue_grey_700
    };

    public static JokesFragment newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt("type", type);

        JokesFragment fragment = new JokesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private int type;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        type = savedInstanceState == null ? getArguments().getInt("type", 0) : savedInstanceState.getInt("type", 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("type", type);
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        setViewPadding(getContentView());
    }

    private void setViewPadding(View viewGroup) {
        viewGroup.setPadding(viewGroup.getPaddingLeft(), viewGroup.getPaddingTop(),
                viewGroup.getPaddingRight(), Utils.dip2px(getActivity(), 56));
    }

    @Override
    public void onItemClick(PLAAdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);

        final JokeBean bean = getAdapterItems().get(position);
        // 纯文
        if (bean.getItemType() == 0) {
            AisenUtils.showMenuDialog(this, view, getResources().getStringArray(R.array.jokes_menu), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        MobclickAgent.onEvent(getActivity(), "joke_text_copy");

                        AisenUtils.copyToClipboard(bean.getExcerpt());
                    }
                    else if (which == 1) {
                        MobclickAgent.onEvent(getActivity(), "joke_text_share");

                        startActivity(Utils.getShareIntent("", bean.getExcerpt(), ""));
                    }
                }

            });
        }
        // 图文
        else if (bean.getItemType() == 1) {
            StatusContent statusContent = new StatusContent();
            PicUrls picUrls = new PicUrls();
            picUrls.setThumbnail_pic(bean.getImgUrl());
            statusContent.setText(bean.getExcerpt());
            statusContent.setPic_urls(new PicUrls[]{ picUrls });

            PicsActivity.launch(getActivity(), statusContent, 0);
        }
    }

    @Override
    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);

        int padding = Utils.dip2px(getActivity(), 4);
        getRefreshView().setPadding(padding, 0, padding, 0);
    }

    @Override
    protected IPaging<JokeBean, JokeBeans> newPaging() {
        return new JokePaging();
    }

    @Override
    public void requestData(RefreshMode mode) {
        boolean load = true;

        // 如果还没有加载过数据，切且显示的是当前的页面
        if (getTaskCount(PAGING_TASK_ID) == 0) {
            load = AisenUtils.checkTabsFragmentCanRequestData(this);
        }

        if (load) {
            if (mode == RefreshMode.refresh) {
                mode = RefreshMode.reset;
            }

            new JokeTask(mode).execute();
        }
    }

    @Override
    public void onTabRequestData() {
        // 如果还没有加载过数据，就开始加载
        if (getTaskCount(PAGING_TASK_ID) == 0) {
            requestData(RefreshMode.reset);
        }
    }

    @Override
    public IItemViewCreator<JokeBean> configItemViewCreator() {
        return new IItemViewCreator<JokeBean>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                if (viewType == 0) {
                    return inflater.inflate(R.layout.item_joke_text, parent, false);
                }
                else if (viewType  == 1) {
                    return inflater.inflate(R.layout.item_joke_image, parent, false);
                }

                return null;
            }

            @Override
            public IITemView<JokeBean> newItemView(View convertView, int viewType) {
                if (viewType == 0) {
                    return new JokeTextItemView(convertView);
                }
                else if (viewType == 1) {
                    return new JokeImageItemView(convertView);
                }

                return null;
            }

        };
    }

    class JokeTextItemView extends ARecycleViewItemView<JokeBean> {

        @ViewInject(id = R.id.txtJoke)
        TextView txtJoke;
        @ViewInject(id = R.id.cardView)
        CardView cardView;

        public JokeTextItemView(View itemView) {
            super(getActivity(), itemView);
        }

        @Override
        public void onBindData(View convertView, JokeBean data, int position) {
            txtJoke.setText(data.getExcerpt() + "");
            setCardViewBackground(data);
        }

        void setCardViewBackground(JokeBean data) {
            int color = themeColorArr[(int) (data.getId() % themeColorArr.length)];
            cardView.setCardBackgroundColor(getResources().getColor(color));
        }

    }

    class JokeImageItemView extends JokeTextItemView {

        @ViewInject(id = R.id.img)
        ImageView img;

        int width;

        public JokeImageItemView(View itemView) {
            super(itemView);

            width = (SystemUtils.getScreenWidth(getActivity()) - Utils.dip2px(getActivity(), 8) * 3) / 2;
        }

        @Override
        public void onBindData(View convertView, JokeBean data, int position) {
            super.onBindData(convertView, data, position);

            int imageW = data.getImgWidth();
            int imageH = data.getImgHeight();
            int height = width * imageH / imageW;
            img.setLayoutParams(new LinearLayout.LayoutParams(width, height));

            ImageConfig config = new ImageConfig();
            config.setId("Jokes");
            config.setMaxWidth(width);
            config.setLoadfaildRes(R.drawable.bg_timeline_loading);
            config.setLoadingRes(R.drawable.bg_timeline_loading);
            BitmapLoader.getInstance().display(JokesFragment.this, data.getImgUrl(), img, config);
        }

        @Override
        void setCardViewBackground(JokeBean data) {

        }

    }

    class JokeTask extends APagingTask<Void, Void, JokeBeans> {

        public JokeTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected List<JokeBean> parseResult(JokeBeans jokeBeans) {
            return jokeBeans.getData().getContents();
        }

        @Override
        protected JokeBeans workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... params) throws TaskException {
            long newsid = 0;
            String direction = "up";

            // 下拉刷新
            if (mode == RefreshMode.refresh) {
                direction = "down";
                if (!TextUtils.isEmpty(previousPage)) newsid = Long.parseLong(previousPage);
            }
            // 上拉刷新
            else if (mode == RefreshMode.update) {
                direction = "up";
                if (!TextUtils.isEmpty(nextPage)) newsid = Long.parseLong(nextPage);
            }

            JokeBeans beans = SDK.newInstance(getTaskCacheMode(this)).getJokes(newsid, direction, 20, type);
            if (beans != null && beans.getData() != null && beans.getData().getContents() != null) {
                for (JokeBean jokeBean : beans.getData().getContents()) {
                    jokeBean.setItemType(type);
                }
            }

            if (mode == RefreshMode.update &&
                    beans.getData().getContents().size() == 0) {
                beans.setEndPaging(true);
            }

            return beans;
        }

    }

    @Override
    public boolean onToolbarDoubleClick() {
        if (AisenUtils.checkTabsFragmentCanRequestData(this)) {
            requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);
            getRefreshView().setSelectionFromTop(0, 0);

            return true;
        }

        return false;
    }

}
