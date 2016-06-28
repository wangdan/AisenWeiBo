package org.aisen.weibo.sina.ui.activity.base;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.md.MDHelper;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.Logger;
import org.aisen.weibo.sina.ui.widget.sheetfab.MaterialSheetFab;
import org.aisen.weibo.sina.ui.widget.sheetfab.MaterialSheetFabEventListener;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.action.IAction;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.android.ui.fragment.ATabsTabLayoutFragment;
import org.aisen.android.ui.widget.AsToolbar;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.service.OfflineService;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.TokenInfo;
import org.aisen.weibo.sina.support.action.WebLoginAction;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.utils.AccountUtils;
import org.aisen.weibo.sina.support.utils.OfflineUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.ui.activity.profile.WeiboClientActivity;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;
import org.aisen.weibo.sina.ui.fragment.account.WebLoginFragment;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.comment.CommentPagerFragment;
import org.aisen.weibo.sina.ui.fragment.comment.NotificationPagerFragment;
import org.aisen.weibo.sina.ui.fragment.draft.DraftFragment;
import org.aisen.weibo.sina.ui.fragment.friendship.FriendshipPagerFragment;
import org.aisen.weibo.sina.ui.fragment.mention.MentionPagerFragment;
import org.aisen.weibo.sina.ui.fragment.menu.FabGroupsFragment;
import org.aisen.weibo.sina.ui.fragment.menu.MenuFragment;
import org.aisen.weibo.sina.ui.fragment.search.SearchFragment;
import org.aisen.weibo.sina.ui.fragment.secondgroups.JokesPagerFragment;
import org.aisen.weibo.sina.ui.fragment.secondgroups.WallpaperFragment;
import org.aisen.weibo.sina.ui.fragment.settings.CacheClearFragment;
import org.aisen.weibo.sina.ui.fragment.settings.NotificationSettingsFragment;
import org.aisen.weibo.sina.ui.fragment.settings.OtherItemFragment;
import org.aisen.weibo.sina.ui.fragment.settings.SettingsPagerFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineDefFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineGroupsFragment;
import org.aisen.weibo.sina.ui.widget.MainFloatingActionButton;

import java.util.ArrayList;

/**
 * 首页，维护各菜单的切换，响应各种Intent事件<br/>
 * 参照这两个链接：
 * 1、http://www.soloho.cc/blog/how-do-i-use-drawerlayout-to-display-over-the-actionbar-or-toolbar-and-under-the-status-bar
 * 2、http://www.jianshu.com/p/ab937c80ed6e
 *
 * Created by wangdan on 15/4/23.
 */
public class MainActivity extends BaseActivity
                            implements FabGroupsFragment.OnFabGroupSelectedCallback, MenuFragment.OnMenuCallback, AisenActivityHelper.EnableSwipeback {

    public static final String ACTION_LOGIN = "org.aisen.sina.weibo.ACTION_LOGIN";
    public static final String ACTION_NOTIFICATION = "org.aisen.sina.weibo.ACTION_NOTIFICATION";
    public static final String ACTION_NOTIFICATION_MS = "org.aisen.sina.weibo.ACTION_NOTIFICATION_MS";
    public static final String ACTION_NOTIFICATION_MC = "org.aisen.sina.weibo.ACTION_NOTIFICATION_MC";
    public static final int REQUEST_CODE_AUTH = 11156;

    private static MainActivity mInstance;

    @ViewInject(id = R.id.drawer)
    private DrawerLayout mDrawerLayout;
    @ViewInject(id = R.id.fab)
    MainFloatingActionButton fabBtn;
    @ViewInject(id = R.id.appbar)
    AppBarLayout appBarLayout;
    @ViewInject(id = R.id.tabLayout)
    TabLayout tabLayout;

    private ActionBarDrawerToggle drawerToggle;
    private MaterialSheetFab materialSheetFab;
    private MenuFragment menuFragment;
    private FabGroupsFragment fabGroupsFragment;

    private int newIntentNotificationIndex = -1;
    private String toolbarTitle;

    public static void login() {
        Intent intent = new Intent(GlobalContext.getInstance(), MainActivity.class);
        intent.setAction(ACTION_LOGIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        GlobalContext.getInstance().startActivity(intent);
    }

    public static MainActivity getInstance() {
        return mInstance;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mInstance = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_main);

        CacheClearFragment.clearCompress();

        BizFragment bizFragment = BizFragment.createBizFragment(this);
        bizFragment.createFabAnimator(fabBtn);
        fabBtn.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                // 触发一次刷新
                ((AsToolbar) getToolbar()).performDoublcClick();
                appBarLayout.setExpanded(true);

                return true;
            }

        });
        bizFragment.getFabAnimator().setDuration(200);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        setupDrawer(savedInstanceState);
        setupMenu(savedInstanceState);
        setupFab(savedInstanceState);
        setupAppBarLayout(savedInstanceState);

        if (savedInstanceState != null) {
            toolbarTitle = savedInstanceState.getString("toolbarTitle");
            if (!TextUtils.isEmpty(toolbarTitle))
                getSupportActionBar().setTitle(toolbarTitle);
        }

        mInstance = this;

        OtherItemFragment.checkPhotoPermission(this, true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!TextUtils.isEmpty(toolbarTitle))
            outState.putString("toolbarTitle", toolbarTitle);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent == null)
            return;

        String action = intent.getAction();

        // 切换账号
        if (ACTION_LOGIN.equals(action)) {
            setupFab(null);

            menuFragment.changeAccount();
        }
        else {
            if (ACTION_NOTIFICATION.equals(action) && MenuFragment.MENU_FRIENDSHIP == Integer.parseInt(intent.getStringExtra("type"))) {
                FriendshipPagerFragment.launch(this, AppContext.getAccount().getUser(), 1);
            }
            else {
                int menuId = getActionType(intent, action);

                menuFragment.triggerMenuClick(menuId);
            }
        }
    }

    private int getActionType(Intent intent, String action) {
        int type = MenuFragment.MENU_MAIN;
        // 处理点击Notification时，设置显示菜单
        if (ACTION_LOGIN.equals(action)) {
            type = MenuFragment.MENU_MAIN;
        }
        // 新通知
        else if (ACTION_NOTIFICATION.equals(action)) {
            type = Integer.parseInt(intent.getStringExtra("type"));

            if (type == MenuFragment.MENU_CMT) {
                newIntentNotificationIndex = 0;

                type = MenuFragment.MENU_NOTIFICATION;
            }
        }
        // 新提及微博
        else if (ACTION_NOTIFICATION_MS.equals(action)) {
            ActivityHelper.putShareData(this, "showMensitonType", "showMentionStatus");

            newIntentNotificationIndex = 1;
            type = MenuFragment.MENU_NOTIFICATION;
        }
        // 新提及评论
        else if (ACTION_NOTIFICATION_MC.equals(action)) {
            ActivityHelper.putShareData(this, "showMensitonType", "showMentionCmt");

            newIntentNotificationIndex = 2;
            type = MenuFragment.MENU_NOTIFICATION;
        }
        return type;
    }

    private void setupDrawer(Bundle savedInstanceState) {
        drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                getToolbar(), R.string.draw_open, R.string.draw_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

        };
        mDrawerLayout.setDrawerListener(drawerToggle);
    }

    private void setupMenu(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            int menuId = MenuFragment.MENU_MAIN;
            Intent intent = getIntent();
            if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
                menuId = getActionType(intent, intent.getAction());
            }

            if (menuId == MenuFragment.MENU_MD) {
                menuFragment = MenuFragment.newInstance(MenuFragment.MENU_MD);
                getFragmentManager().beginTransaction().add(R.id.menu_frame, menuFragment, "MenuFragment").commit();
            }
            else if (menuId == MenuFragment.MENU_FRIENDSHIP) {
                menuFragment = MenuFragment.newInstance(MenuFragment.MENU_MAIN);
                getFragmentManager().beginTransaction().add(R.id.menu_frame, menuFragment, "MenuFragment").commit();
                FriendshipPagerFragment.launch(this, AppContext.getAccount().getUser(), 1);
            }
            else {
                menuFragment = MenuFragment.newInstance(menuId);
                getFragmentManager().beginTransaction().add(R.id.menu_frame, menuFragment, "MenuFragment").commit();
            }
        }
        else {
            menuFragment = (MenuFragment) getFragmentManager().findFragmentByTag("MenuFragment");
        }
    }

    private void setupFab(Bundle savedInstanceState) {
        ArrayList<Group> groupList = new ArrayList<>();
        // 全部微博
        Group group = new Group();
        group.setName(getString(R.string.timeline_all));
        group.setId("statusesFriendsTimeLine");
        groupList.add(group);
        // 互相关注
        group = new Group();
        group.setName(getString(R.string.timeline_bilateral));
        group.setId("statusesBilateralTimeLine");
        groupList.add(group);
        // 发给我的
        group = new Group();
        group.setName(getString(R.string.timeline_tome));
        group.setId("statusesToMe");
        groupList.add(group);
        // 分组微博
        groupList.addAll(AppContext.getAccount().getGroups().getLists());

        fabGroupsFragment = (FabGroupsFragment) getFragmentManager().findFragmentById(R.id.fragmentFabGroups);
        fabGroupsFragment.resetSelectedPosition();
        fabGroupsFragment.setItems(groupList);

        View sheetView = findViewById(R.id.fab_sheet);
        View overlay = findViewById(R.id.overlay);
        int sheetColor = getResources().getColor(R.color.comm_white);
        int fabColor = MDHelper.resolveColor(this, R.attr.colorPrimary, Color.BLACK);

        materialSheetFab = new MaterialSheetFab(fabBtn, sheetView, overlay, sheetColor, fabColor);
        materialSheetFab.setEventListener(new MaterialSheetFabEventListener() {

            @Override
            public void onShowSheet() {
                super.onShowSheet();

                fabGroupsFragment.show();

                MobclickAgent.onEvent(MainActivity.this, "fab_groups");
            }

        });
        materialSheetFab.showFab();
    }

    private void setupAppBarLayout(Bundle savedInstanceState) {
        // 随着ToolBar的移动，来控制Fab的显示和隐藏
//        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//                float percent = Math.abs(verticalOffset) * 1.0f / SystemUtils.getActionBarHeight(MainActivity.this);
//
//                int translationY = fabBtn.getHeight() + getResources().getDimensionPixelSize(R.dimen.fab_spacing);
//                fabBtn.setTranslationY(translationY * (percent));
//            }
//
//        });
        if (savedInstanceState != null) {
            Fragment fragment = getFragmentManager().findFragmentByTag("MainFragment");
            // 如果是TabsFragment，显示TabLayout
            if (fragment != null) {
                if (fragment instanceof ATabsFragment) {
                    tabLayout.setVisibility(View.VISIBLE);
                }
                else {
                    tabLayout.setVisibility(View.GONE);
                }
                // 显示AppBarLayout
                appBarLayout.setExpanded(true, true);
            }
        }
    }

    /**
     * 选择了侧边栏，切换侧边栏菜单
     *
     * @param item
     * @return
     */
    @Override
    public void onMenuClicked(MenuFragment.NavMenuItem item, boolean closeDrawer) {
        invalidateOptionsMenu();

        ABaseFragment fragment = null;

        // 切换ContentFragment，或者跳转到新的界面
        switch (item.id) {
        // 首页
        case MenuFragment.MENU_MAIN:
            fabGroupsFragment.triggerLastPosition();
            break;
        // 通知
        case MenuFragment.MENU_NOTIFICATION:
            fragment = NotificationPagerFragment.newInstance(newIntentNotificationIndex);

            newIntentNotificationIndex = -1;
            break;
        // 提及
        case MenuFragment.MENU_MENTION:
            fragment = MentionPagerFragment.newInstance();
            break;
        // 评论
        case MenuFragment.MENU_CMT:
            fragment = CommentPagerFragment.newInstance();
            break;
        // 私信
        case MenuFragment.MENU_MD:
            new IAction(MainActivity.this, new WebLoginAction(MainActivity.this, BizFragment.createBizFragment(this))) {

                @Override
                public void doAction() {
                    WeiboClientActivity.launchDM(MainActivity.this);
                }

            }.run();

            MobclickAgent.onEvent(MainActivity.this, "md");
            break;
        // 热门微博
        case MenuFragment.MENU_HOT_STATUS:
            WeiboClientActivity.launchHotStatuses(this);

            MobclickAgent.onEvent(MainActivity.this, "hot_status");
            break;
        // 草稿箱
        case MenuFragment.MENU_DRAT:
            fragment = DraftFragment.newInstance();
            break;
        // 设置
        case MenuFragment.MENU_SETTINGS:
            SettingsPagerFragment.launch(this);
            break;
        // 轻松一刻
        case MenuFragment.MENU_JOKE:
            fragment = JokesPagerFragment.newInstance();

            MobclickAgent.onEvent(this, "menu_joke");
            break;
        // 精美壁纸
        case MenuFragment.MENU_WALLPAPER:
            fragment = WallpaperFragment.newInstance();

            MobclickAgent.onEvent(this, "menu_wallpaper");
            break;
        }

        if (fragment != null) {
            setFragemnt(fragment, getString(item.toolbarRes));
        }

        // 隐藏Fab按钮
        if (item.id == 1) {
            fabBtn.setVisibility(View.VISIBLE);

            // 显示Fab
            BizFragment.createBizFragment(this).getFabAnimator().show();
        }
        else {
            fabBtn.setVisibility(View.GONE);
        }

        // 关闭侧边栏
        if (closeDrawer) {
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    closeDrawer();
                }

            }, 300);
        }
    }

    @Override
    public boolean onMenuSelected(MenuFragment.NavMenuItem item) {
        // 设置可以选中的菜单项
        switch (item.id) {
            // 首页
            case MenuFragment.MENU_MAIN:
            // 通知
            case MenuFragment.MENU_NOTIFICATION:
            // 提及
            case MenuFragment.MENU_MENTION:
            // 评论
            case MenuFragment.MENU_CMT:
            // 草稿箱
            case MenuFragment.MENU_DRAT:
            // 轻松一刻
            case MenuFragment.MENU_JOKE:
            // 精美壁纸
            case MenuFragment.MENU_WALLPAPER:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onMenuSameClicked(MenuFragment.NavMenuItem item) {
        invalidateOptionsMenu();

        // 重复点击了通知
        if (item.id == MenuFragment.MENU_NOTIFICATION) {
            Fragment fragment = getFragmentManager().findFragmentByTag("MainFragment");
            if (fragment instanceof ATabsTabLayoutFragment && newIntentNotificationIndex != -1) {
                ATabsTabLayoutFragment tabsTabLayoutFragment = (ATabsTabLayoutFragment) fragment;

                tabsTabLayoutFragment.getViewPager().setCurrentItem(newIntentNotificationIndex);
                ((APagingFragment) tabsTabLayoutFragment.getCurrentFragment()).requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);

                newIntentNotificationIndex = -1;
            }
        }

        closeDrawer();

        return true;
    }

    /**
     * 选择了分组，切换查看分组微博
     *
     * @param position
     * @param group
     */
    @Override
    public void onGroupSelected(int position, Group group) {
        Fragment fragment = null;
        if (position <= 2) {
            fragment = TimelineDefFragment.newInstance(group.getId());
        }
        else {
            fragment = TimelineGroupsFragment.newInstance(group);
        }

        setFragemnt(fragment, group.getName());

        if (materialSheetFab.isSheetVisible()) {
            materialSheetFab.hideSheet();
        }
    }

    private void setFragemnt(final Fragment fragment, CharSequence title) {
        if (fragment == null)
            return;

        toolbarTitle = title.toString();
        getSupportActionBar().setTitle(toolbarTitle);

        // 如果是TabsFragment，显示TabLayout
        if (fragment instanceof ATabsFragment) {
            tabLayout.setVisibility(View.VISIBLE);
        }
        else {
            tabLayout.setVisibility(View.GONE);
        }
        // 显示AppBarLayout
        appBarLayout.setExpanded(true, true);

        // .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        View view = findViewById(R.id.content_frame);
        view.setAlpha(0.0f);
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", view.getAlpha(), 1.0f);
        anim.setDuration(600);
        anim.start();
        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, "MainFragment").commit();
    }

    private Handler mHandler = new Handler();

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (drawerToggle != null)
            drawerToggle.syncState();
    }

    public boolean isDrawerOpened() {
        return mDrawerLayout.isDrawerOpen(Gravity.LEFT) || mDrawerLayout.isDrawerOpen(Gravity.RIGHT);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.publish).setVisible(AppSettings.getFabBtnType() == 1);

        if (OfflineService.getInstance() == null || OfflineService.getInstance().getStatus() == OfflineService.OfflineStatus.init ||
                OfflineService.getInstance().getStatus() == OfflineService.OfflineStatus.finished) {
            menu.findItem(R.id.toggle_offline).setVisible(true);
            menu.findItem(R.id.stop_offline).setVisible(false);
        }
        else {
            menu.findItem(R.id.toggle_offline).setVisible(false);
            menu.findItem(R.id.stop_offline).setVisible(true);
        }
        menu.findItem(R.id.notification_settings).setVisible(menuFragment.getSelectedId() == MenuFragment.MENU_NOTIFICATION);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item))
            return true;

        if (android.R.id.home == item.getItemId()) {
            if (mDrawerLayout.isDrawerVisible(GravityCompat.START))
                mDrawerLayout.closeDrawers();
            else
                mDrawerLayout.openDrawer(GravityCompat.START);

            return true;
        }

        // 关于
//        if (item.getItemId() == R.id.about)
//            AboutWebFragment.launchAbout(this);
        // 意见反馈
//        else if (item.getItemId() == R.id.feedback)
//            PublishActivity.publishFeedback(this);
        // 退出
        if (item.getItemId() == R.id.exitapp) {
            finish();

            MobclickAgent.onEvent(this, "exitapp");
        }
        // 新微博
        else if (item.getItemId() == R.id.publish)
            PublishActivity.publishStatus(this, null);
        // 开始离线
        else if (item.getItemId() == R.id.toggle_offline) {
            OfflineUtils.toggleOffline(this);

            MobclickAgent.onEvent(this, "toggle_offline");
        }
        // 停止离线
        else if (item.getItemId() == R.id.stop_offline) {
            OfflineService.stopOffline();

            MobclickAgent.onEvent(this, "stop_offline");
        }
        // 通知设置
        else if (item.getItemId() == R.id.notification_settings)
            NotificationSettingsFragment.launch(this);
        // 搜索
        else if (item.getItemId() == R.id.search) {
            new IAction(MainActivity.this, new WebLoginAction(MainActivity.this, BizFragment.createBizFragment(this))) {

                @Override
                public void doAction() {
                    SearchFragment.launch(MainActivity.this, "");

                    MobclickAgent.onEvent(MainActivity.this, "toggle_search");
                }

            }.run();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean canFinish = false;

    @Override
    public boolean onBackClick() {
        if (AppSettings.isAppResident()) {
//            if (menuFragment.backToMain()) {
//                return true;
//            } else {
                if (isDrawerOpened()) {
                    closeDrawer();

                    return true;
                }
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
//            }
            return true;
        }
        else {
            if (!canFinish) {
                canFinish = true;

                showMessage(R.string.hint_exit);

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        canFinish = false;
                    }

                }, 1500);

                return true;
            }

            return super.onBackClick();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 修复偶尔Fab按钮不见了
        if (menuFragment != null && menuFragment.getSelectedId() == 1) {
            if (fabBtn.getVisibility() != View.VISIBLE) {
                fabBtn.setVisibility(View.VISIBLE);
            }
        }

        if (!AppContext.isLoggedIn()) {
            finish();

            return;
        }

        if (AppContext.getAccount().getAccessToken().isExpired()) {
            requestLogin(this, AppContext.getAccount());
        }

        invalidateOptionsMenu();
    }

    @Override
    protected int configTheme() {
        return ThemeUtils.themeArr[AppSettings.getThemeColor()][1];
    }

    @Override
    public boolean canSwipe() {
        return false;
    }

    public static void runCheckAccountTask(AccountBean account) {
        // 已经过期了就不用检查了
        if (!account.getAccessToken().isExpired()) {
            new CheckAccountValidTask().execute(account);
        }
    }

    public static class CheckAccountValidTask extends WorkTask<AccountBean, Void, TokenInfo> {

        @Override
        public TokenInfo workInBackground(AccountBean... params) throws TaskException {
            Logger.w("run CheckAccountValidTask");

            TokenInfo token = null;
            TokenInfo adToken = null;

            try {
                AccountBean account = params[0];
                // Aisen授权
                try {
                    token = SinaSDK.getInstance(account.getAccessToken()).getTokenInfo(account.getAccessToken().getToken());
                } catch (TaskException e) {
                    e.printStackTrace();
                    if ("21327".equals(e.getCode()) ||
                            "21317".equals(e.getCode())) {
                        token = new TokenInfo();
                        token.setExpire_in(0);
                    }
                }
                if (token != null)
                    account.getAccessToken().setExpires_in(token.getExpire_in());
                // Weico授权
                try {
                    if (account.getAdvancedToken() != null)
                        adToken = SinaSDK.getInstance(account.getAdvancedToken()).getTokenInfo(account.getAdvancedToken().getToken());
                    else {
                        adToken = new TokenInfo();
                        adToken.setExpire_in(0);
                    }
                } catch (TaskException e) {
                    e.printStackTrace();
                    if ("21327".equals(e.getCode()) ||
                            "21317".equals(e.getCode())) {
                        adToken = new TokenInfo();
                        adToken.setExpire_in(0);
                    }
                }
                if (account.getAdvancedToken() != null && adToken != null)
                    account.getAdvancedToken().setExpires_in(adToken.getExpire_in());
            } catch (Throwable e) {
            }

            if (token != null) {
                token.setUid(params[0].getUid());
            }
            return token;
        }

        @Override
        protected void onSuccess(TokenInfo tokenInfo) {
            super.onSuccess(tokenInfo);

            // 同一登录账户
            if (tokenInfo != null && AppContext.isLoggedIn() && AppContext.getAccount().getUid().equals(tokenInfo.getUid())) {
                if (BaseActivity.getRunningActivity() != null && BaseActivity.getRunningActivity() instanceof MainActivity) {
                    if (getParams()[0].getAccessToken().isExpired())
                        requestLogin(BaseActivity.getRunningActivity(), getParams()[0]);
                }
            }
        }

    }

    private static void requestLogin(final Activity activity, final AccountBean account) {
        new AlertDialogWrapper.Builder(activity)
                .setMessage(R.string.account_account_expired)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.account_relogin, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                     WebLoginFragment.launch(activity, WebLoginFragment.Client.aisen, account.getAccount(), account.getPassword(), REQUEST_CODE_AUTH);
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_AUTH) {
            if (resultCode == Activity.RESULT_OK) {
                AccountBean accountBean = (AccountBean) data.getSerializableExtra("account");

                AppContext.getAccount().setAccessToken(accountBean.getAccessToken());
                if (accountBean.getUser() != null) {
                    AppContext.getAccount().setUser(accountBean.getUser());
                }
                if (accountBean.getGroups() != null) {
                    AppContext.getAccount().setGroups(accountBean.getGroups());
                }

                AccountUtils.newAccount(AppContext.getAccount());
                AccountUtils.setLogedinAccount(AppContext.getAccount());

                login();
            }
        }
    }

}
