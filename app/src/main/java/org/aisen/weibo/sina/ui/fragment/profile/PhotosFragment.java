package org.aisen.weibo.sina.ui.fragment.profile;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.container.FragmentContainerActivity;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.adapter.ABaseAdapter;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.ARefreshFragment;
import org.aisen.android.ui.fragment.AStripTabsFragment;
import org.aisen.android.ui.fragment.ASwipyRefreshGridLayout;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.bean.PhotoBean;
import org.aisen.weibo.sina.support.bean.PhotosBean;
import org.aisen.weibo.sina.support.compress.TimelineThumbBitmapCompress;
import org.aisen.weibo.sina.support.paging.PhotosPagingProcessor;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.activity.pics.PhotosActivity;
import org.aisen.weibo.sina.ui.activity.profile.UserProfileActivity;
import org.aisen.weibo.sina.ui.widget.GifHintImageView;
import org.aisen.weibo.sina.ui.widget.TimelinePicsView;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.AccessToken;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户相册
 *
 * Created by wangdan on 15/4/15.
 */
public class PhotosFragment extends ASwipyRefreshGridLayout<PhotoBean, PhotosBean>
                                implements AdapterView.OnItemClickListener,
                                                UserProfilePagerFragment.IUserProfileRefresh,
                                                AStripTabsFragment.IStripTabInitData {

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
    protected int inflateContentView() {
        return R.layout.as_ui_photos;
    }

    @Override
    protected void configRefresh(RefreshConfig config) {
        super.configRefresh(config);

        config.emptyLabel = getString(R.string.profile_photos_empty);
        config.minResultSize = 1;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("user", mUser);
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
    protected ABaseAdapter.AbstractItemView<PhotoBean> newItemView() {
        return new PhotoItemView();
    }

    @Override
    protected IPaging<PhotoBean, PhotosBean> configPaging() {
        return new PhotosPagingProcessor();
    }

    @Override
    public void onStripTabRequestData() {
        // 如果还没有加载过数据，就开始加载
        if (getTaskCount("PhotosTask") == 0) {
            requestData(ARefreshFragment.RefreshMode.reset);
        }
    }

    @Override
    protected void requestData(ARefreshFragment.RefreshMode mode) {
        boolean load = true;

        // 如果还没有加载过数据，切且显示的是当前的页面
        if (getTaskCount("PhotosTask") == 0) {
            Fragment fragment = getPagerCurrentFragment();
            if (fragment == null || fragment != this)
                load = false;
        }

        if (load)
            new PhotosTask(mode == ARefreshFragment.RefreshMode.refresh ? ARefreshFragment.RefreshMode.reset : mode).execute();
    }

    private Fragment getPagerCurrentFragment() {
        if (getActivity() == null)
            return null;

        ABaseFragment aFragment = null;
        if (getActivity() instanceof UserProfileActivity) {
            aFragment = (ABaseFragment) getActivity().getFragmentManager().findFragmentByTag(FragmentContainerActivity.FRAGMENT_TAG);
        }
        if (aFragment instanceof AStripTabsFragment) {
            AStripTabsFragment fragment = (AStripTabsFragment) aFragment;
            return fragment.getCurrentFragment();
        }

        return null;
    }

    class PhotosTask extends PagingTask<Void, Void, PhotosBean> {

        public PhotosTask(ARefreshFragment.RefreshMode mode) {
            super("PhotosTask", mode);
        }

        @Override
        protected PhotosBean workInBackground(ARefreshFragment.RefreshMode mode, String previousPage, String nextPage,
                                              Void... p) throws TaskException {
            Params params = new Params();

            if (mode == ARefreshFragment.RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
                params.addParameter("since_id", previousPage);

            if (mode == ARefreshFragment.RefreshMode.update && !TextUtils.isEmpty(nextPage))
                params.addParameter("max_id", nextPage);

            if (AppContext.getUser().getIdstr().equals(mUser.getIdstr())) {
                params.addParameter("user_id", mUser.getIdstr());
            }
            else {
                params.addParameter("screen_name", mUser.getScreen_name());
            }

            params.addParameter("count", "50");
            // 相册
            params.addParameter("feature", "2");

            Token token = null;
            // 是当前登录用户
            if (AisenUtils.isLoggedUser(mUser)) {
                if (AppContext.getAccount().getAdvancedToken() != null) {
                    token = AppContext.getAdvancedToken();
                    params.addParameter("source", AppContext.getAdvancedToken().getAppKey());
                }
            }
            else {
                if (AppContext.getAdvancedToken() != null) {
                    AccessToken accessToken = AppContext.getAdvancedToken();

                    token = new Token();
                    token.setToken(accessToken.getToken());
                    token.setSecret(accessToken.getSecret());

                    params.addParameter("source", accessToken.getAppKey());
                }
            }
            if (token == null)
                token = AppContext.getToken();

            StatusContents statuses = SinaSDK.getInstance(token, getTaskCacheMode(this)).statusesUserTimeLine(params);

            // 过滤转发
            List<StatusContent> statusList = new ArrayList<StatusContent>();
            for (StatusContent status : statuses.getStatuses()) {
                if (status.getRetweeted_status() == null)
                    statusList.add(status);
            }
            statuses.setStatuses(statusList);

            PhotosBean photos = new PhotosBean();
            photos.setCache(statuses.isCache());
            photos.setExpired(statuses.expired());
            photos.setNoMore(statuses.getStatuses().size() == 3);
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

    @Override
    public void onMovedToScrapHeap(View view) {
//		super.onMovedToScrapHeap(view);
    }

    @Override
    protected int[] configCanReleaseIds() {
        return new int[]{R.id.imgPhoto};
    }

    class PhotoItemView extends ABaseAdapter.AbstractItemView<PhotoBean> {

        PhotoItemView() {
            int gap = getResources().getDimensionPixelSize(R.dimen.gap_photo);
            size = (SystemUtils.getScreenWidth() - gap * 4) / 3;

            config.setLoadfaildRes(R.drawable.bg_timeline_loading);
            config.setLoadingRes(R.drawable.bg_timeline_loading);
            config.setMaxWidth(size);
            config.setMaxHeight(size);
        }

        int size = 0;
        TimelinePicsView.TimelineImageConfig config = new TimelinePicsView.TimelineImageConfig();

        @ViewInject(id = R.id.imgPhoto)
        GifHintImageView imgPhoto;

        @Override
        public int inflateViewId() {
            return R.layout.as_item_profile_photos;
        }

        @Override
        public void bindingData(View convertView, PhotoBean data) {
            convertView.setLayoutParams(new AbsListView.LayoutParams(size, size));

            imgPhoto.setHint(data.getPhoto().getThumbnail_pic());
            config.setShowWidth(size);
            config.setShowHeight(size);
            config.setSize(1);

            String image = AisenUtils.getStatusMulImage(data.getPhoto().getThumbnail_pic());
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
            ((GridView) getRefreshView()).setSelection(index);
        }
    }

    @Override
    protected int delayRlease() {
        return 3 * 1000;
    }

    @Override
    public void refreshProfile() {
        requestDataDelay(100);
    }

}