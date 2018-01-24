package org.aisen.weibo.sina.ui.fragment.images;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.BitmapOwner;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.download.SdcardDownloader;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.ui.fragment.ARecycleViewFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.support.bean.SavedImageBean;
import org.aisen.weibo.sina.support.sdk.SDK;
import org.aisen.weibo.sina.ui.activity.picture.PicsActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdan on 16/7/19.
 */
public class SavedImagesFragment extends ARecycleViewFragment<SavedImageBean, ArrayList<SavedImageBean>, SavedImageBean> {

    public static SavedImagesFragment newInstance() {
        return new SavedImagesFragment();
    }

    public static final int ITEM_SPACE = 3; // dip
    public static final int ITEM_COUNT = 3; // dip

    @Override
    protected void setupRefreshConfig(RefreshConfig config) {
        super.setupRefreshConfig(config);

        config.footerMoreEnable = false;
    }

    @Override
    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);

        getRefreshView().addItemDecoration(new SpaceItemDecoration(getActivity(), ITEM_COUNT));
    }

    @Override
    protected RecyclerView.LayoutManager configLayoutManager() {
        return new GridLayoutManager(getActivity(), ITEM_COUNT, OrientationHelper.VERTICAL, false);
    }

    @Override
    public IItemViewCreator<SavedImageBean> configItemViewCreator() {
        return new IItemViewCreator<SavedImageBean>() {
            @Override
            public View newContentView(LayoutInflater layoutInflater, ViewGroup viewGroup, int i) {
                return layoutInflater.inflate(NormalPhotoItem.RES, viewGroup, false);
            }

            @Override
            public IITemView<SavedImageBean> newItemView(View view, int i) {
                return new NormalPhotoItem(getActivity(), SavedImagesFragment.this, view);
            }

        };
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);

        StatusContent bean = new StatusContent();
        bean.setPic_urls(new PicUrls[1]);
        bean.getPic_urls()[0] = new PicUrls();
        bean.getPic_urls()[0].setThumbnail_pic(Uri.fromFile(new File(getAdapterItems().get(position).getPath())).toString());
        PicsActivity.launch(getActivity(), bean, 0);
    }

    @Override
    public void requestData(RefreshMode refreshMode) {
        new AllPhotosTask(RefreshMode.reset).execute();
    }

    class AllPhotosTask extends APagingTask<Void, Void, ArrayList<SavedImageBean>> {

        public AllPhotosTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected List<SavedImageBean> parseResult(ArrayList<SavedImageBean> photosBean) {
            return photosBean;
        }

        @Override
        protected ArrayList<SavedImageBean> workInBackground(RefreshMode refreshMode, String s, String s1, Void... voids) throws TaskException {
            return SDK.newInstance().getSavedImages();
        }

    }

    public static class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private final int space;
        private final int count;

        public SpaceItemDecoration(Context context, int count) {
            this.space = Utils.dip2px(context, ITEM_SPACE);
            this.count = count;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            //不是第一个的格子都设一个左边和底部的间距
            outRect.left = space;
            outRect.bottom = space;
            //由于每行都只有3个，所以第一个都是3的倍数，把左边距设为0
            if (parent.getChildLayoutPosition(view) % count == 0) {
                outRect.left = 0;
            }
        }

    }

    public static class NormalPhotoItem extends ARecycleViewItemView<SavedImageBean> {

        public static final int RES = R.layout.item_image_normal;

        private final BitmapOwner owner;

        final int size;

        public NormalPhotoItem(Activity context, BitmapOwner owner, View itemView) {
            super(context, itemView);

            this.owner = owner;
            int spaceW = 0;// Utils.dip2px(context, AllPhotosFragment.ITEM_SPACE) * (AllPhotosFragment.ITEM_COUNT - 1);
            this.size = (SystemUtils.getScreenWidth(GlobalContext.getInstance()) - spaceW) / itemCount();
        }

        @Override
        public void onBindData(View view, SavedImageBean photoBean, int i) {
            ImageConfig config = new ImageConfig();
            config.setLoadingRes(R.drawable.bg_timeline_loading);
            config.setMaxWidth(SystemUtils.getScreenWidth(getContext()) / itemCount());
            config.setMaxHeight(SystemUtils.getScreenWidth(getContext()) / itemCount());
            config.setDownloaderClass(SdcardDownloader.class);
            BitmapLoader.getInstance().display(owner, photoBean.getPath(), (ImageView) view, config);

            GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.width = size;
            layoutParams.height = size;
        }

        protected int itemCount() {
            return ITEM_COUNT;
        }

    }

}
