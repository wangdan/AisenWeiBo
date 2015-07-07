package org.aisen.weibo.sina.ui.activity.basic;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.melnykov.fab.FloatingActionButton;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemBarUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.ARefreshFragment;
import org.aisen.android.ui.fragment.AStripTabsFragment;
import org.aisen.android.ui.fragment.ATabLayoutFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.AccessToken;
import org.aisen.weibo.sina.sinasdk.bean.TokenInfo;
import org.aisen.weibo.sina.support.action.DMAction;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.bean.MenuBean;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.OfflineUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.sys.service.OfflineService;
import org.aisen.weibo.sina.ui.activity.profile.WeiboClientActivity;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;
import org.aisen.weibo.sina.ui.fragment.account.WeicoLoginFragment;
import org.aisen.weibo.sina.ui.fragment.basic.BizFragment;
import org.aisen.weibo.sina.ui.fragment.basic.MenuFragment;
import org.aisen.weibo.sina.ui.fragment.basic.MenuGenerator;
import org.aisen.weibo.sina.ui.fragment.comment.CommentTabsFragment;
import org.aisen.weibo.sina.ui.fragment.draft.DraftFragment;
import org.aisen.weibo.sina.ui.fragment.friendship.FriendshipTabsFragment;
import org.aisen.weibo.sina.ui.fragment.mention.MentionTabsFragment;
import org.aisen.weibo.sina.ui.fragment.search.SearchTopicsFragment;
import org.aisen.weibo.sina.ui.fragment.search.SearchUserFragment;
import org.aisen.weibo.sina.ui.fragment.settings.AboutWebFragment;
import org.aisen.weibo.sina.ui.fragment.settings.SettingsPagerFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.GroupSortFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineTabsFragment;

/**
 * Created by wangdan on 15/4/12.
 */
public class MainActivity extends BaseActivity implements AisenActivityHelper.EnableSwipeback, View.OnLongClickListener {

    public static final String ACTION_LOGIN = "org.aisen.sina.weibo.ACTION_LOGIN";
    public static final String ACTION_NOTIFICATION = "org.aisen.sina.weibo.ACTION_NOTIFICATION";
    public static final String ACTION_NOTIFICATION_MS = "org.aisen.sina.weibo.ACTION_NOTIFICATION_MS";
    public static final String ACTION_NOTIFICATION_MC = "org.aisen.sina.weibo.ACTION_NOTIFICATION_MC";

    public static final String FRAGMENT_TAG = "MainFragment";

    public static void login() {
        Intent intent = new Intent(GlobalContext.getInstance(), MainActivity.class);
        intent.setAction(ACTION_LOGIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        GlobalContext.getInstance().startActivity(intent);
    }

    public static Fragment getContentFragment(MainActivity activity) {
        return activity.getFragmentManager().findFragmentByTag("MainFragment");
    }

    @ViewInject(id = R.id.drawer)
    private DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    private MenuBean lastSelectedMenu;

    private MenuFragment menuFragment;

    @ViewInject(id = R.id.fab, click = "fabBtnCLicked")
    private FloatingActionButton btnFab;

    private int fabType = -1;

    private static MainActivity mInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        OfflineService.setOfflineFinished(AppContext.getUser(), false);

        AisenUtils.setStatusBar(this);

        mInstance = this;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.as_ui_main);

//        AdTokenUtils.loadIfExpired();
        BizFragment.getBizFragment(this);

        if (Build.VERSION.SDK_INT >= 19) {
            ViewGroup drawerRoot = (ViewGroup) findViewById(R.id.layDrawerRoot);
            drawerRoot.setPadding(drawerRoot.getPaddingLeft(),
                                    SystemBarUtils.getStatusBarHeight(this),
                                    drawerRoot.getPaddingRight(),
                                    drawerRoot.getBottom());
        }
        if (Build.VERSION.SDK_INT == 19) {
            ViewGroup rootMain = (ViewGroup) findViewById(R.id.layMainRoot);
            rootMain.setPadding(rootMain.getPaddingLeft(),
                                    rootMain.getPaddingTop(),
                                    rootMain.getPaddingRight(),
                                    rootMain.getBottom() + SystemBarUtils.getNavigationBarHeight(this));
        }

        mToolbar = getToolbar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                getToolbar(), R.string.draw_open, R.string.draw_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                invalidateOptionsMenu();

                if (isToolbarShown())
                    btnFab.show(true);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                invalidateOptionsMenu();

                btnFab.hide(true);
            }

        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        lastSelectedMenu = savedInstanceState == null ? null : (MenuBean) savedInstanceState.getSerializable("menu");

        if (ActivityHelper.getBooleanShareData("isFirstLaunch", true)) {
            ActivityHelper.putBooleanShareData("isFirstLaunch", false);

            mDrawerLayout.openDrawer(Gravity.LEFT);
            btnFab.hide();

            getSupportActionBar().setTitle(R.string.draw_timeline);
        } else {
            if (lastSelectedMenu != null)
                getSupportActionBar().setTitle(lastSelectedMenu.getTitleRes());
            else
                getSupportActionBar().setTitle(R.string.draw_timeline);
        }

        if (savedInstanceState == null) {
            String action = getIntent() != null ? getIntent().getAction() : null;
            String type = getActionType(getIntent(), action);

            menuFragment = MenuFragment.newInstance(type);
            getFragmentManager().beginTransaction().add(R.id.menu_frame, menuFragment, "MenuFragment").commit();
            
//            getFragmentManager().beginTransaction().hide(menuFragment).commit();
//            getFragmentManager().beginTransaction().add(R.id.menu_frame, new NavigationViewFragment(), "NavigationView").commit();

            // 添加右侧的抽屉
//            GroupsMenuFragment groupsMenuFragment = GroupsMenuFragment.newInstance();
//            getFragmentManager().beginTransaction().add(R.id.groups_frame, groupsMenuFragment, "GroupsMenuFragment").commit();
        } else {
            menuFragment = (MenuFragment) getFragmentManager().findFragmentByTag("MenuFragment");

            // 2014-8-30 解决因为状态保存而导致的耗时阻塞
            if (lastSelectedMenu.getType().equals("1"))
                onMenuSelected(lastSelectedMenu, true, null);
        }

        // 更新FAB的颜色
        btnFab.setColorNormal(AisenUtils.getThemeColor(this));
        btnFab.setColorPressed(AisenUtils.getThemeColor(this));
        btnFab.setColorRipple(AisenUtils.getThemeColor(this));
//        btnFab.setOnLongClickListener(this);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                new CheckTokenInfoTask(AppContext.getAccount()).execute();
            }

        }, 2000);
    }

    @Override
    protected int configTheme() {
        return ThemeUtils.themeArr[AppSettings.getThemeColor()][1];
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (lastSelectedMenu != null)
            outState.putSerializable("menu", lastSelectedMenu);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent == null)
            return;

        String action = intent.getAction();

        MenuBean menuBean = MenuGenerator.generateMenu(getActionType(intent, action));

        lastSelectedMenu = menuBean;

        onMenuSelected(menuBean, true, null);

        if ("1".equals(menuBean.getType())) {
            menuFragment.setAccountItem();
            menuFragment.setSelectedMenu(menuBean);
        }

        if (isDrawerOpened())
            closeDrawer();
    }

    private String getActionType(Intent intent, String action) {
        String type = null;
        // 处理点击Notification时，设置显示菜单
        if (ACTION_LOGIN.equals(action)) {
            type = "1";
        }
        else if (ACTION_NOTIFICATION.equals(action)) {
            type = intent.getStringExtra("type");
        }
        else if (ACTION_NOTIFICATION_MS.equals(action)) {
            ActivityHelper.putShareData("showMensitonType", "showMentionStatus");

            type = "2";
        }
        else if (ACTION_NOTIFICATION_MC.equals(action)) {
            ActivityHelper.putShareData("showMensitonType", "showMentionCmt");

            type = "2";
        }
        return type;
    }

    public boolean onMenuSelected(MenuBean menu, boolean replace, View view) {
        if (!replace && lastSelectedMenu != null && lastSelectedMenu.getType().equals(menu.getType())) {
            closeDrawer();
            return true;
        }

        int type = Integer.parseInt(menu.getType());

        ABaseFragment fragment = null;
        if (mStripView != null)
            mStripView.clearAnimation();
        mStripView = null;
        if (animatorSet != null) {
            animatorSet.removeAllListeners();
            animatorSet.end();
            animatorSet = null;
        }
        mToolbar.setTranslationY(0);

        switch (type) {
        // 微博首页
        case 1:
            fragment = TimelineTabsFragment.newInstance();
            break;
        // 提及
        case 2:
            fragment = MentionTabsFragment.newInstance();
            break;
        // 评论
        case 3:
            fragment = CommentTabsFragment.newInstance();
            break;
        // 朋友关系
        case 4:
            fragment = FriendshipTabsFragment.newInstance();
            break;
        // 设置
        case 5:
            closeDrawer();

            SettingsPagerFragment.launch(this);
            return true;
        // 草稿箱
        case 6:
            fragment = DraftFragment.newInstance();
            break;
        // 私信
        case 10:
            new DMAction(this).run();
            return true;
        // 热门微博
        case 11:
            WeiboClientActivity.launchHotStatuses(this);

            closeDrawer();
            return true;
        }

        if (fragment == null)
            return true;

        getSupportActionBar().setSubtitle(null);
        getSupportActionBar().setTitle(menu.getTitleRes());

        FragmentTransaction ft = getFragmentManager().beginTransaction();
//		ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
//									android.R.animator.fade_in, android.R.animator.fade_out);
        ft.replace(R.id.content_frame, fragment, FRAGMENT_TAG).commit();

        lastSelectedMenu = menu;
        menuFragment.setSelectedMenu(menu);

        setFabType();

        return false;
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawers();
    }

    public boolean isDrawerOpened() {
        return mDrawerLayout.isDrawerOpen(Gravity.LEFT) || mDrawerLayout.isDrawerOpen(Gravity.RIGHT);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!AppContext.isLogedin()) {
            finish();

            return;
        }

        if (AppContext.getAccount().getToken().isExpired() ||
                AppContext.getAdvancedToken() == null || AppContext.getAdvancedToken().isExpired()) {
            AccountFragment.launch(MainActivity.this);

            finish();
        }

        // 离线完成，重新加载微博数据
        if (lastSelectedMenu != null &&
                "1".equalsIgnoreCase(lastSelectedMenu.getType())) {
            if (OfflineService.isOfflineFinished(AppContext.getUser())) {
                OfflineService.setOfflineFinished(AppContext.getUser(), false);

                onMenuSelected(MenuGenerator.generateMenu("1"), true, null);
            }
        }

        setFabType();

        invalidateOptionsMenu();
    }

    private void setFabType() {
        if (fabType != AppSettings.getFabBtnType()) {
            fabType = AppSettings.getFabBtnType();
            invalidateOptionsMenu();
            if (fabType == 0) {
                btnFab.setImageResource(R.drawable.ic_menu_edit_white);
            }
            else {
                btnFab.setImageResource(R.drawable.ic_refresh_light);
            }
        }
        btnFab.setVisibility(canFragmentRefresh() ? View.VISIBLE : View.INVISIBLE);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) btnFab.getLayoutParams();
        params.gravity = Gravity.BOTTOM;
        if (AppSettings.getFabBtnPosition() == 0)
            params.gravity |= Gravity.LEFT;
        else
            params.gravity |= Gravity.RIGHT;
    }

    private boolean canFragmentRefresh() {
        int menu = Integer.parseInt(lastSelectedMenu.getType());

        return menu == 1 || menu == 2 ||
                menu == 3 || menu == 4;
    }

    void fabBtnCLicked(View v) {
        if (AppSettings.getFabBtnType() == 0) {
            PublishActivity.publishStatus(this, null);
        }
        else {
            Fragment fragment = getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
            if (fragment != null && fragment instanceof AStripTabsFragment) {
                fragment = ((AStripTabsFragment) fragment).getCurrentFragment();
                if (fragment != null && fragment instanceof ARefreshFragment) {
                    ((ARefreshFragment) fragment).setRefreshingRequestData();
                }
            }
        }
    }

    private boolean canFinish = false;

    @Override
    public boolean onBackClick() {
        if (AppSettings.isAppResident()) {
            if (lastSelectedMenu != null && !"1".equals(lastSelectedMenu.getType())) {
                onMenuSelected(MenuGenerator.generateMenu("1"), true, null);
                return true;
            } else {
                if (isDrawerOpened()) {
                    closeDrawer();

                    return true;
                }
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
            return true;
        }
        else {
            if (!canFinish) {
                canFinish = true;

                showMessage(R.string.comm_hint_exit);

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        canFinish = false;
                    }

                }, 1500);

                return true;
            }

            setMDestory(true);
            return super.onBackClick();
        }
    }

    private Toolbar mToolbar;
    private View mStripView;

    private boolean isToolbarShown() {
        return mToolbar != null && mToolbar.getTranslationY() >= 0;
    }

    public void hideToolbar() {
        if (isToolbarShown()) {
            toggleToolbarShown(false);
        }
    }

    public void showToolbar() {
        if (!isToolbarShown()) {
            toggleToolbarShown(true);
        }
    }

    private AnimatorSet animatorSet;
    public void toggleToolbarShown(boolean shown) {
        if (mStripView == null) {
            Fragment fragment = getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
            if (fragment != null && fragment instanceof AStripTabsFragment) {
                mStripView = ((AStripTabsFragment) fragment).getSlidingTabLayout();
            }
            else if (fragment != null && fragment instanceof ATabLayoutFragment) {
                mStripView = ((ATabLayoutFragment) fragment).getTabLayout();
            }
        }

        if (mToolbar == null)
            return;

        if (animatorSet != null && animatorSet.isRunning())
            return;

        if (isToolbarShown() && shown)
            return;
        else if (!isToolbarShown() && !shown)
            return;

        if (!btnFab.isVisible() && shown) {
            btnFab.show(true);
        }
        else if (btnFab.isVisible() && !shown) {
            btnFab.hide(true);
        }

        PropertyValuesHolder toolBarHolder = null;
        if (shown) {
            toolBarHolder = PropertyValuesHolder.ofFloat("translationY", -1 * mToolbar.getHeight(), 0);
        }
        else {
            toolBarHolder = PropertyValuesHolder.ofFloat("translationY", 0, -1 * mToolbar.getHeight());
        }
        ObjectAnimator toolbarObjectAnim = ObjectAnimator.ofPropertyValuesHolder(mToolbar, toolBarHolder);
        toolbarObjectAnim.setDuration(150);

        ObjectAnimator stripObjectAnim = null;
        if (mStripView != null) {
            PropertyValuesHolder stripHolder = null;
            if (shown) {
                stripHolder = PropertyValuesHolder.ofFloat("translationY", -1 * mStripView.getHeight(), 0);
            }
            else {
                stripHolder = PropertyValuesHolder.ofFloat("translationY", 0, -1 * mStripView.getHeight());
            }
            stripObjectAnim = ObjectAnimator.ofPropertyValuesHolder(mStripView, stripHolder);
            stripObjectAnim.setDuration(150);
        }

        AnimatorSet animSet = new AnimatorSet();
        animatorSet = animSet;
        if (shown) {
            if (stripObjectAnim != null) {
//                animSet.playSequentially(toolbarObjectAnim, stripObjectAnim);
                animSet.play(toolbarObjectAnim);
                stripObjectAnim.setStartDelay(100);
                animSet.play(stripObjectAnim);
            }
            else {
                animSet.play(toolbarObjectAnim);
            }
        }
        else {
            if (stripObjectAnim != null) {
//                animSet.playSequentially(stripObjectAnim, toolbarObjectAnim);
                animSet.play(stripObjectAnim);
                toolbarObjectAnim.setStartDelay(100);
                animSet.play(toolbarObjectAnim);
            }
            else {
                animSet.play(toolbarObjectAnim);
            }
        }
        animSet.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.friendGroups).setVisible(false);

        menu.findItem(R.id.publish).setVisible(AppSettings.getFabBtnType() == 1);

        menu.findItem(R.id.about).setVisible(false);
        menu.findItem(R.id.feedback).setVisible(false);

        if (OfflineService.getInstance() == null || OfflineService.getInstance().getStatus() == OfflineService.OfflineStatus.init ||
                OfflineService.getInstance().getStatus() == OfflineService.OfflineStatus.finished) {
            menu.findItem(R.id.toggle_offline).setVisible(true);
            menu.findItem(R.id.stop_offline).setVisible(false);
        }
        else {
            menu.findItem(R.id.toggle_offline).setVisible(false);
            menu.findItem(R.id.stop_offline).setVisible(true);
        }

        // 显示的是首页
        if (lastSelectedMenu != null) {
            if (lastSelectedMenu.getType().equals("1"))
                menu.findItem(R.id.friendGroups).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item))
            return true;

        if (android.R.id.home == item.getItemId()) {
            if (mDrawerLayout.isDrawerVisible(GravityCompat.START))
                mDrawerLayout.closeDrawers();
            else
                mDrawerLayout.openDrawer(GravityCompat.START);

            return true;
        }

        // 关于
        if (item.getItemId() == R.id.about)
            AboutWebFragment.launchAbout(this);
            // 意见反馈
        else if (item.getItemId() == R.id.feedback)
            PublishActivity.publishFeedback(this);
            // 好友分组
        else if (item.getItemId() == R.id.friendGroups)
            GroupSortFragment.lanuch(this);
            // 搜索用户或者微博
//        else if (item.getItemId() == R.id.search)
//            SearchActivity.launch(this);
        // 退出
        else if (item.getItemId() == R.id.exitapp)
            finish();
        // 新微博
        else if (item.getItemId() == R.id.publish)
            PublishActivity.publishStatus(this, null);
        // 搜人
        else if (item.getItemId() == R.id.search_user)
            SearchUserFragment.launch(this);
        // 搜微博
        else if (item.getItemId() == R.id.search_status)
            SearchTopicsFragment.launch(this);
        // 开始离线
        else if (item.getItemId() == R.id.toggle_offline)
            OfflineUtils.toggleOffline(this);
        // 停止离线
        else if (item.getItemId() == R.id.stop_offline)
            OfflineService.stopOffline();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean canSwipe() {
        return false;
    }


    @Override
    public boolean onLongClick(View v) {
        String username = "";
        String password = "";

        username = AppContext.getAccount().getAccount();
        password = AppContext.getAccount().getPassword();

        // 这里导出Hprof文件，跟踪分析内存
//        String path = SdcardUtils.getSdcardPath() + File.separator + "aisenweibo" + File.separator;
//        path = path + "hprof" + File.separator;
//        HprofUtils.dumpHprof(path);

        WeicoLoginFragment.launch(this, username, password, 1000);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && RESULT_OK == resultCode) {
            showMessage(R.string.weico_success);

            AccessToken token = (AccessToken) data.getSerializableExtra("token");
            Logger.e(token);

            SinaDB.getSqlite().deleteAll(null, AccessToken.class);
            SinaDB.getSqlite().insert(null, token);
            AppContext.setAdvancedToken(token);
        }
        else if (requestCode == 2000 && RESULT_OK == resultCode) {
            login();
        }
    }

    class CheckTokenInfoTask extends WorkTask<Void, Void, Boolean> {

        AccountBean account;

        public CheckTokenInfoTask(AccountBean account) {
            this.account = account;
        }

        @Override
        public Boolean workInBackground(Void... params) throws TaskException {
            TokenInfo tokenInfo = null;
            // Aisen授权
            try {
                tokenInfo = SinaSDK.getInstance(account.getToken()).getTokenInfo(account.getToken().getToken());
            } catch (TaskException e) {
                e.printStackTrace();
                if ("21327".equals(e.getCode()) ||
                        "21317".equals(e.getCode())) {
                    tokenInfo = new TokenInfo();
                    tokenInfo.setExpire_in(0);
                }
                else {
                    return false;
                }
            }
            account.getToken().setExpires_in(tokenInfo.getExpire_in());
            // Weico授权
            try {
                if (account.getAdvancedToken() != null)
                    tokenInfo = SinaSDK.getInstance(account.getAdvancedToken()).getTokenInfo(account.getAdvancedToken().getToken());
                else {
                    tokenInfo = new TokenInfo();
                    tokenInfo.setExpire_in(0);
                }
            } catch (TaskException e) {
                e.printStackTrace();
                if ("21327".equals(e.getCode()) ||
                        "21317".equals(e.getCode())) {
                    tokenInfo = new TokenInfo();
                    tokenInfo.setExpire_in(0);
                }
                else {
                    return false;
                }
            }
            if (account.getAdvancedToken() != null)
                account.getAdvancedToken().setExpires_in(tokenInfo.getExpire_in());

            // 刷新用户信息
            AccountDB.newAccount(account);
            if (AppContext.getAccount() != null && AppContext.getAccount().getUserId().equals(account.getUserId())) {
                AppContext.getAccount().setToken(account.getToken());
                AppContext.setAdvancedToken(account.getAdvancedToken());
                AccountDB.setLogedinAccount(AppContext.getAccount());
            }

            return account.getToken().isExpired() ||
                    account.getAdvancedToken() == null || account.getAdvancedToken().isExpired();
        }

        @Override
        protected void onSuccess(Boolean result) {
            super.onSuccess(result);

            // 授权过期了，需要重新授权
            if (result &&
                    AppContext.getAccount() != null && AppContext.getAccount().getUserId().equals(account.getUserId())) {
                AccountFragment.launch(MainActivity.this);

                AppContext.logout();
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mInstance = null;
    }

    public MenuBean getSelectedMenu() {
        return lastSelectedMenu;
    }

    public static MainActivity getInstance() {
        return mInstance;
    }

}
