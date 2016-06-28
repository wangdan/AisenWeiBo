package org.aisen.weibo.sina.ui.fragment.secondgroups;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.support.paging.PageIndexPaging;
import org.aisen.android.ui.fragment.AWaterfallSwipeRefreshFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.android.ui.widget.pla.PLAAdapterView;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.WallpaperBean;
import org.aisen.weibo.sina.support.bean.WallpaperBeans;
import org.aisen.weibo.sina.support.sdk.SDK;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.activity.picture.WallpaperSettingActivity;

import java.io.File;
import java.util.List;

/**
 * 壁纸列表
 *
 * Created by wangdan on 16/3/23.
 */
public class WallpaperFragment extends AWaterfallSwipeRefreshFragment<WallpaperBean, WallpaperBeans> {

    public static WallpaperFragment newInstance() {
        return new WallpaperFragment();
    }

    @Override
    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);

        int padding = Utils.dip2px(getActivity(), 4);
        getRefreshView().setPadding(padding, 0, padding, 0);
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

        WallpaperBean bean = getAdapterItems().get(position);
        File file = BitmapLoader.getInstance().getCacheFile(bean.getIndexThumbnailUrl());
        if (file != null && file.exists()) {
            WallpaperSettingActivity.launch(getActivity(), file, bean.getNormalUrl());
        }
    }

    @Override
    public IItemViewCreator configItemViewCreator() {
        return new IItemViewCreator() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(R.layout.item_wallpaper, parent, false);
            }

            @Override
            public IITemView newItemView(View convertView, int viewType) {
                return new WallpaperItemView(convertView);
            }

        };
    }

    @Override
    protected IPaging<WallpaperBean, WallpaperBeans> newPaging() {
        return new PageIndexPaging<>();
    }

    @Override
    public void requestData(RefreshMode mode) {
        new WallpaperTask(mode == RefreshMode.refresh ? RefreshMode.reset : mode).execute();
    }

    class WallpaperItemView extends ARecycleViewItemView<WallpaperBean> {

        @ViewInject(id = R.id.img)
        ImageView img;

        int width;

        public WallpaperItemView(View itemView) {
            super(getActivity(), itemView);

            width = ActivityHelper.getIntShareData(GlobalContext.getInstance(), "WallpaperWidth", 0);
        }

        @Override
        public void onBindData(final View convertView, final WallpaperBean data, final int position) {
            if (width == 0) {
                img.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {
                        img.getViewTreeObserver().removeOnPreDrawListener(this);

                        width = img.getWidth();
                        ActivityHelper.putIntShareData(GlobalContext.getInstance(), "WallpaperWidth", width);

                        onBindData(convertView, data, position);
                        return true;
                    }

                });
            }
            else {
                int imageW = 2160;
                int imageH = 1920;
                int height = width * imageH / imageW;
                img.setLayoutParams(new CardView.LayoutParams(width, height));

                ImageConfig config = new ImageConfig();
                config.setId("Wallpaper");
                config.setMaxWidth(width);
                config.setLoadfaildRes(R.drawable.screenshot_default);
                config.setLoadingRes(R.drawable.screenshot_default);
                BitmapLoader.getInstance().display(WallpaperFragment.this, data.getIndexThumbnailUrl(), img, config);
            }
        }

    }

    class WallpaperTask extends APagingTask<Void, Void, WallpaperBeans> {

        public WallpaperTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected List<WallpaperBean> parseResult(WallpaperBeans wallpaperBeans) {
            return wallpaperBeans.getItem().getWallpaperList();
        }

        @Override
        protected WallpaperBeans workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... params) throws TaskException {
            int page = 1;
            if (!TextUtils.isEmpty(nextPage))
                page = Integer.parseInt(nextPage);

            WallpaperBeans beans = SDK.newInstance(getTaskCacheMode(this)).getWallpaper(page);
            if (beans.getItem().getWallpaperList().size() == 0) {
                beans.setEndPaging(true);
            }

            return beans;
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(getActivity(), "壁纸列表页");
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), "壁纸列表页");
    }

}
