package org.aisen.weibo.sina.ui.fragment.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.fragment.AGridSwipyRefreshFragment;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.bean.PhotoBean;
import org.aisen.weibo.sina.support.bean.PhotosBean;
import org.aisen.weibo.sina.support.compress.TimelineThumbBitmapCompress;
import org.aisen.weibo.sina.support.paging.PhotosPaging;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.activity.picture.PhotosActivity;
import org.aisen.weibo.sina.ui.widget.GifHintImageView;
import org.aisen.weibo.sina.ui.widget.TimelinePicsView;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户相册
 *
 * Created by wangdan on 15/4/15.
 */
public class PhotosFragment extends AGridSwipyRefreshFragment<PhotoBean, PhotosBean>
                                implements ProfilePagerFragment.IUserProfileRefresh, ATabsFragment.ITabInitData {

    public static PhotosFragment newInstance(WeiBoUser user) {
        PhotosFragment fragment = new PhotosFragment();

        Bundle args = new Bundle();
        args.putSerializable("user", user);
        fragment.setArguments(args);

        return fragment;
    }

    public static PhotosBean photos;

    private WeiBoUser mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = savedInstanceState == null ? (WeiBoUser) getArguments().getSerializable("user")
                : (WeiBoUser) savedInstanceState.getSerializable("user");
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_photos;
    }

    @Override
    protected void setupRefreshConfig(RefreshConfig config) {
        super.setupRefreshConfig(config);

        config.emptyHint = getString(R.string.profile_photos_empty);
        config.footerMoreEnable = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("user", mUser);
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        setViewVisiable(getLoadingLayout(), View.VISIBLE);
        setViewVisiable(getEmptyLayout(), View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getRefreshView().setNestedScrollingEnabled(true);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PhotosBean photos = new PhotosBean();
        photos.setList(getAdapterItems());

        PhotosFragment.photos = photos;

        PhotosActivity.launch(this, position, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();

        photos = null;
    }

    @Override
    public IItemViewCreator<PhotoBean> configItemViewCreator() {
        return new IItemViewCreator<PhotoBean>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(R.layout.item_profile_photos, parent, false);
            }

            @Override
            public IITemView<PhotoBean> newItemView(View convertView, int viewType) {
                return new PhotoItemView(convertView);
            }

        };
    }

    @Override
    protected IPaging<PhotoBean, PhotosBean> newPaging() {
        return new PhotosPaging();
    }

    @Override
    public void onTabRequestData() {
        // 如果还没有加载过数据，就开始加载
        if (getTaskCount(PAGING_TASK_ID) == 0) {
            requestData(RefreshMode.reset);
        }
    }

    @Override
    public void requestData(RefreshMode mode) {
        boolean load = true;

        // 如果还没有加载过数据，切且显示的是当前的页面
        if (getTaskCount(PAGING_TASK_ID) == 0) {
            load = AisenUtils.checkTabsFragmentCanRequestData(this);
        }

        if (load)
            new PhotosTask(mode == RefreshMode.refresh ? RefreshMode.reset : mode).execute();
    }

    class PhotosTask extends APagingTask<Void, Void, PhotosBean> {

        public PhotosTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected PhotosBean workInBackground(RefreshMode mode, String previousPage, String nextPage,
                                              Void... p) throws TaskException {
            Params params = new Params();

            if (mode == RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
                params.addParameter("since_id", previousPage);

            if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
                params.addParameter("max_id", nextPage);

            if (AppContext.getAccount().getUser().getIdstr().equals(mUser.getIdstr())) {
                params.addParameter("user_id", mUser.getIdstr());
            }
            else {
                params.addParameter("screen_name", mUser.getScreen_name());
            }

            params.addParameter("count", "50");
            // 相册
            params.addParameter("feature", "2");

            Token token = AppContext.getAccount().getAdvancedToken();
            if (token == null)
                token = AppContext.getAccount().getAccessToken();

            StatusContents statuses = SinaSDK.getInstance(token, getTaskCacheMode(this)).statusesUserTimeLine(params);

            // 过滤转发
            List<StatusContent> statusList = new ArrayList<StatusContent>();
            for (StatusContent status : statuses.getStatuses()) {
                if (status.getRetweeted_status() == null)
                    statusList.add(status);
            }
            statuses.setStatuses(statusList);

            PhotosBean photos = new PhotosBean();
            photos.setFromCache(statuses.isFromCache());
            photos.setOutofdate(statuses.isOutofdate());
            photos.setEndPaging(statuses.getStatuses().size() == 3);
            photos.setList(new ArrayList<PhotoBean>());

            for (StatusContent status : statusList) {
                PicUrls[] picUrlsArr = status.getPic_urls();
                for (PicUrls picUrls : picUrlsArr) {
                    PhotoBean photo = new PhotoBean();
                    photo.setPhoto(picUrls);
                    photo.setStatus(status);

                    photos.getList().add(photo);
                }
            }

            return photos;
        }

        @Override
        protected List<PhotoBean> parseResult(PhotosBean result) {
            return result.getList();
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            showMessage(exception.getMessage());
        }

    }

    class PhotoItemView extends ARecycleViewItemView<PhotoBean> {

        int size = 0;
        TimelinePicsView.TimelineImageConfig config = new TimelinePicsView.TimelineImageConfig();

        @ViewInject(id = R.id.imgPhoto)
        GifHintImageView imgPhoto;

        public PhotoItemView(View itemView) {
            super(getActivity(), itemView);

            int gap = getResources().getDimensionPixelSize(R.dimen.gap_photo);
            size = (SystemUtils.getScreenWidth(getContext()) - gap * 4) / 3;

            config.setLoadfaildRes(R.drawable.bg_timeline_loading);
            config.setLoadingRes(R.drawable.bg_timeline_loading);
            config.setMaxWidth(size);
            config.setMaxHeight(size);
        }

        @Override
        public void onBindData(View convertView, PhotoBean data, int position) {
            convertView.setLayoutParams(new AbsListView.LayoutParams(size, size));

            imgPhoto.setHint(data.getPhoto().getThumbnail_pic());
            config.setShowWidth(size);
            config.setShowHeight(size);
            config.setSize(1);

            String image = AisenUtils.getStatusMulImage(getContext(), data.getPhoto().getThumbnail_pic());
            Logger.w(image);
            boolean large = image.indexOf("bmiddle") != -1;

            if (large) {
                config.setId("status_large");

                config.setMaxWidth(size);
            } else {
                config.setId("status_thumb");
            }
            config.setBitmapCompress(TimelineThumbBitmapCompress.class);

            BitmapLoader.getInstance().display(PhotosFragment.this,
                                                image,
                                                imgPhoto,
                                                config);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Activity.RESULT_OK == resultCode && requestCode == 1000) {
            int index = data.getIntExtra("index", 0);
            getRefreshView().setSelection(index);
        }
    }

    @Override
    public void refreshProfile() {
        requestDataDelay(100);
    }

    @Override
    public boolean onToolbarDoubleClick() {
        if (AisenUtils.checkTabsFragmentCanRequestData(this)) {
            requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getRefreshView().setSelectionFromTop(0, 0);
            }

            return true;
        }

        return false;
    }

}