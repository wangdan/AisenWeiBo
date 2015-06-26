package org.aisen.weibo.sina.ui.fragment.profile;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.display.DefaultDisplayer;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.AAutoReleaseStripTabsFragment;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.ARefreshFragment;
import org.aisen.android.ui.fragment.AStripTabsFragment;
import org.aisen.android.ui.widget.SlidingTabLayout;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.activity.profile.UserProfileActivity;
import org.aisen.weibo.sina.ui.fragment.basic.BizFragment;
import org.aisen.weibo.sina.ui.fragment.friendship.FriendshipTabsFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineFavoritesFragment;
import org.aisen.weibo.sina.ui.widget.ProfileScrollView;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.AccessToken;
import org.aisen.weibo.sina.sinasdk.bean.FriendshipShow;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import java.util.ArrayList;
import java.util.Set;

/**
 * 用户资料Pager<br/>
 * tab1:详情<br/>
 * tab2:微博<br/>
 * tab3:原创微博<br/>
 * tab4:相册<br/>
 *
 * Created by wangdan on 15-2-10.
 */
public class UserProfilePagerFragment extends AAutoReleaseStripTabsFragment<AStripTabsFragment.StripTabItem>
                                            implements View.OnClickListener,
                                                       SwipeRefreshLayout.OnRefreshListener,
                                                       BizFragment.OnCreateFriendshipCallback,
                                                       BizFragment.OnDestoryFriendshipCallback,
                                                       BizFragment.OnDestoryFollowerCallback {

    public static void launch(Activity from, WeiBoUser user) {
        UserProfileActivity.launch(from, user);
    }

    public static ABaseFragment newInstance() {
        UserProfilePagerFragment fragment = new UserProfilePagerFragment();

        Bundle args = new Bundle();
        args.putSerializable("user", AppContext.getUser());
        fragment.setArguments(args);

        return fragment;
    }

    public static ABaseFragment newInstance(WeiBoUser searchResult) {
        UserProfilePagerFragment fragment = new UserProfilePagerFragment();

        Bundle args = new Bundle();
        args.putSerializable("user", searchResult);
        args.putInt("profile_index", 1);
        fragment.setArguments(args);

        return fragment;
    }

    @ViewInject(idStr = "slidingTabs")
    SlidingTabLayout slidingTabs;
    @ViewInject(id = R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @ViewInject(id = R.id.rootScrolView)
    ProfileScrollView rootScrolView;
    // 封面
    @ViewInject(id = R.id.imgCover)
    ImageView imgCover;
    @ViewInject(id = R.id.txtName)
    // 名字
    TextView txtName;
    // 头像
    @ViewInject(id = R.id.imgPhoto)
    ImageView imgPhoto;
    // 认证类别
    @ViewInject(id = R.id.imgVerified)
    ImageView imgVerified;
    // 性别
    @ViewInject(id = R.id.imgGender)
    ImageView imgGender;
    // 粉丝数
    @ViewInject(id = R.id.txtFollowersCounter, click = "onClick")
    TextView txtFollowersCounter;
    // 关注数
    @ViewInject(id = R.id.txtFriendsCounter, click = "onClick")
    TextView txtFriendsCounter;
    // 简介
    @ViewInject(id = R.id.txtDesc)
    TextView txtDesc;

    private WeiBoUser mUserBean;
    private FriendshipShow mFriendship;

    @Override
    protected int inflateContentView() {
        return R.layout.as_ui_profile_pager;
    }

    public int setActivityContentView() {
        return R.layout.as_ui_profile_pager_activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mUserBean = savedInstanceState == null ? (WeiBoUser) getArguments().getSerializable("user")
                                              : (WeiBoUser) savedInstanceState.getSerializable("user");

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        rootScrolView.setUser(mUserBean);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

        }

        if (savedInstanceSate == null)
            new RefreshProfileTask().execute();

        setProfile();

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        activity.getSupportActionBar().setTitle("");
        activity.getToolbar().setBackgroundColor(Color.TRANSPARENT);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mHandler.postDelayed(initCurrentFragment, 300);

        setHasOptionsMenu(true);

        // 这里延迟一点设置第二个页面选中，直接设置会导致顶层的图片没有显示
        if (getArguments() != null && getArguments().getInt("profile_index", 0) == 1) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (getActivity() != null) {
                        getViewPager().setCurrentItem(1);
                    }
                }

            }, 150);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("user", mUserBean);
    }

    @Override
    protected ArrayList<StripTabItem> generateTabs() {
        ArrayList<StripTabItem> items = new ArrayList<StripTabItem>();

        items.add(new StripTabItem("1", getString(R.string.profile_tab1)));
        items.add(new StripTabItem("2", String.format("%s(%s)", getString(R.string.profile_tab2), AisenUtils.getCounter(mUserBean.getStatuses_count()))));
//        items.add(new StripTabItem("3", getString(R.string.profile_tab3)));
        items.add(new StripTabItem("4", getString(R.string.profile_tab4)));
        if (mUserBean.getIdstr().equalsIgnoreCase(AppContext.getUser().getIdstr()))
            items.add(new StripTabItem("5", String.format("%s(%s)", getString(R.string.profile_tab5), AisenUtils.getCounter(mUserBean.getFavourites_count()))));

        return items;
    }

    @Override
    protected Fragment newFragment(StripTabItem bean) {
        switch (Integer.parseInt(bean.getType())) {
        // 详情资料
        case 1:
            return UserProfileTab1Fragment.newInstance(mUserBean);
        // 用户微博
        case 2:
            return UserTimelineFragment.newInstance(mUserBean, "0");
        // 用户原创微博
        case 3:
            return UserTimelineFragment.newInstance(mUserBean, "1");
        // 相册
        case 4:
            return PhotosFragment.newInstance(mUserBean);
        // 收藏
        case 5:
            return TimelineFavoritesFragment.newInstance();
        }

        return null;
    }

    private void setProfile() {
        // 封面
        ImageConfig coverConfig = new ImageConfig();
        coverConfig.setLoadfaildRes(R.drawable.bg_banner_dialog);
        coverConfig.setLoadingRes(R.drawable.bg_banner_dialog);
        coverConfig.setDisplayer(new DefaultDisplayer());
        BitmapLoader.getInstance().display(this, mUserBean.getCover_image_phone(), imgCover, coverConfig);
        // 名字
        // fuck 2014-09-04 当名字过长大于8个字时，截取部分文字
        int maxLength = AisenUtils.getStrLength("一二三四五六七八九十");
        if (AisenUtils.getStrLength(mUserBean.getName()) > maxLength) {
            StringBuffer sb = new StringBuffer();
            int index = 0;
            while (AisenUtils.getStrLength(sb.toString()) < maxLength) {
                if (index >= mUserBean.getName().length())
                    break;

                sb.append(mUserBean.getName().charAt(index));
                index++;
            }
            sb.append("...");
            txtName.setText(sb.toString());
        }
        else {
            txtName.setText(mUserBean.getScreen_name());
        }
        // 头像
        BitmapLoader.getInstance().display(this, AisenUtils.getUserPhoto(mUserBean), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
        // 性别
        imgGender.setVisibility(View.VISIBLE);
        if ("m".equals(mUserBean.getGender()))
            imgGender.setImageResource(R.drawable.list_male);
        else if ("f".equals(mUserBean.getGender()))
            imgGender.setImageResource(R.drawable.list_female);
        else
            imgGender.setVisibility(View.GONE);
        // 认证
        AisenUtils.setImageVerified(imgVerified, mUserBean);
        // 关注数
        txtFriendsCounter.setText(String.format(getString(R.string.profile_friends), AisenUtils.getCounter(mUserBean.getFriends_count())));
        // 粉丝数
        txtFollowersCounter.setText(String.format(getString(R.string.profile_followers), AisenUtils.getCounter(mUserBean.getFollowers_count())));
        // 简介
        txtDesc.setText(mUserBean.getDescription());
        // 简介
        if (!TextUtils.isEmpty(mUserBean.getDescription()))
            txtDesc.setText(mUserBean.getDescription());
        else
            txtDesc.setText(getString(R.string.profile_des_none));
    }

    @Override
    public void onClick(View v) {
        // 关注
        if (v.getId() == R.id.txtFriendsCounter) {
            FriendshipTabsFragment.launch(getActivity(), mUserBean, 0);
        }
        // 粉丝
        else if (v.getId() == R.id.txtFollowersCounter) {
            FriendshipTabsFragment.launch(getActivity(), mUserBean, 1);
        }
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);

        // 设置当前选中tab的滚动视图，用来判断手势
        ABaseFragment fragment = (ABaseFragment) getCurrentFragment();
        if (fragment != null) {
            if (fragment instanceof ARefreshFragment) {
                AbsListView refreshView = ((ARefreshFragment) fragment).getRefreshView();
                rootScrolView.setAbsListView(refreshView);
            } else if (fragment instanceof UserProfileTab1Fragment) {
                UserProfileTab1Fragment tab1Fragment = (UserProfileTab1Fragment) fragment;
                rootScrolView.setAbsListView(tab1Fragment.getScrollView());
            }
        }
    }

    Runnable initCurrentFragment = new Runnable() {

        @Override
        public void run() {
            if (getCurrentFragment() != null && getCurrentFragment() instanceof ARefreshFragment &&
                    ((ARefreshFragment) getCurrentFragment()).getRefreshView() != null) {
                rootScrolView.setAbsListView(((ARefreshFragment) getCurrentFragment()).getRefreshView());
            }
            else {
                mHandler.postDelayed(initCurrentFragment, 100);
            }
        }

    };

    Handler mHandler = new Handler();

    @Override
    public void onRefresh() {
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof IUserProfileRefresh) {
            // 刷新用户关系
            ((IUserProfileRefresh) fragment).refreshProfile();

            // 刷新用户详情资料
            new RefreshProfileTask().execute();
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_profile_pager, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem createItem = menu.findItem(R.id.create);
        MenuItem destoryItem = menu.findItem(R.id.destory);
        MenuItem followerDestoryItem = menu.findItem(R.id.followDestory);

        if (mUserBean == null || AppContext.getUser().getIdstr().equals(mUserBean.getIdstr())) {
            createItem.setVisible(false);
            destoryItem.setVisible(false);
            followerDestoryItem.setVisible(false);
        }
        else {
            createItem.setVisible(mFriendship != null && !mFriendship.getSource().getFollowing());
            destoryItem.setVisible(mFriendship != null && mFriendship.getSource().getFollowing());
            followerDestoryItem.setVisible(mFriendship != null && mFriendship.getTarget().getFollowing());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 关注
        if (item.getItemId() == R.id.create) {
            BizFragment.getBizFragment(this).createFriendship(mUserBean, this);
        }
        // 取消关注
        else if (item.getItemId() == R.id.destory) {
            BizFragment.getBizFragment(this).destoryFriendship(mUserBean, this);
        }
        // 移除粉丝
        else if (item.getItemId() == R.id.followDestory) {
            BizFragment.getBizFragment(this).destoryFollower(mUserBean, this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFriendshipCreated(WeiBoUser targetUser) {
        getProfileFragment().onFriendshipCreated(targetUser);
    }

    @Override
    public void onFriendshipDestoryed(WeiBoUser targetUser) {
        getProfileFragment().onFriendshipDestoryed(targetUser);
    }

    @Override
    public void onDestoryFollower(WeiBoUser user) {
        getProfileFragment().onDestoryFollower(user);
    }

    private UserProfileTab1Fragment getProfileFragment() {
        if (getFragments() == null)
            return null;

        Set<String> keyset = getFragments().keySet();
        for (String key : keyset) {
            Fragment fragment = getFragments().get(key);
            if (fragment instanceof UserProfileTab1Fragment) {
                UserProfileTab1Fragment tab1Fragment = (UserProfileTab1Fragment) fragment;
                return tab1Fragment;
            }
        }

        return null;
    }

    // 刷新用户信息
    class RefreshProfileTask extends WorkTask<Void, Void, WeiBoUser> {

        @Override
        public WeiBoUser workInBackground(Void... params) throws TaskException {
            Token token = null;
            // 是当前登录用户
            if (mUserBean.getIdstr().equals(AppContext.getUser().getIdstr())) {
            }
            else if (mUserBean.getScreen_name().equals(AppContext.getUser().getScreen_name())) {
            }
            else {
                if (AppContext.getAdvancedToken() != null) {
                    AccessToken accessToken = AppContext.getAdvancedToken();

                    token = new Token();
                    token.setToken(accessToken.getToken());
                    token.setSecret(accessToken.getSecret());
                }
            }
            if (AppContext.getAccount().getAdvancedToken() != null)
                token = AppContext.getAccount().getAdvancedToken();
            if (token == null)
                token = AppContext.getToken();

            WeiBoUser userInfo = SinaSDK.getInstance(token).userShow(mUserBean.getIdstr(), null);

            if (AppContext.isLogedin() && userInfo.getIdstr().equals(AppContext.getUser().getIdstr())) {
                // 更新DB
                AccountBean bean = new AccountBean();
                bean.setUserId(userInfo.getIdstr());
                bean.setGroups(AppContext.getGroups());
                bean.setUser(userInfo);
                AccountDB.newAccount(bean);

                // 更新内存
                AppContext.refresh(userInfo, AppContext.getGroups());
            }

            return userInfo;
        }

        @Override
        protected void onSuccess(WeiBoUser result) {
            super.onSuccess(result);

            if (getActivity() == null)
                return;

            // 有些时候获取不到用户信息了
            // 艹他妈的新浪，remark都不让获取了
            if (!TextUtils.isEmpty(result.getRemark()))
                mUserBean.setRemark(result.getRemark());
            if (result.getFollowers_count() > 0)
                mUserBean.setFollowers_count(result.getFollowers_count());
            if (result.getFriends_count() > 0)
                mUserBean.setFriends_count(result.getFriends_count());
            if (!TextUtils.isEmpty(result.getCover_image_phone()))
                mUserBean.setCover_image_phone(result.getCover_image_phone());
            if (!TextUtils.isEmpty(result.getDescription()))
                mUserBean.setDescription(result.getDescription());
            if (!TextUtils.isEmpty(result.getVerified_reason()))
                mUserBean.setVerified_reason(result.getVerified_reason());

            setProfile();
            UserProfileTab1Fragment fragment = getProfileFragment();
            if (fragment != null)
                fragment.setUser(mUserBean);
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            showMessage(exception.getMessage());
        }

    }

    public void setFriendshipShow(FriendshipShow friendship) {
        this.mFriendship = friendship;

        getActivity().invalidateOptionsMenu();
    }

    public interface IUserProfileRefresh {

        public void refreshProfile();

    }

}
