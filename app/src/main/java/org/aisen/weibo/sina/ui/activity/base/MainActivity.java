package org.aisen.weibo.sina.ui.activity.base;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.md.MDHelper;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.component.sheetfab.MaterialSheetFab;
import org.aisen.android.component.sheetfab.MaterialSheetFabEventListener;
import org.aisen.android.support.action.IAction;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.service.OfflineService;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.support.action.WebLoginAction;
import org.aisen.weibo.sina.support.utils.OfflineUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.ui.activity.profile.WeiboClientActivity;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.comment.CommentPagerFragment;
import org.aisen.weibo.sina.ui.fragment.draft.DraftFragment;
import org.aisen.weibo.sina.ui.fragment.mention.MentionPagerFragment;
import org.aisen.weibo.sina.ui.fragment.menu.FabGroupsFragment;
import org.aisen.weibo.sina.ui.fragment.menu.MenuFragment;
import org.aisen.weibo.sina.ui.fragment.settings.AboutWebFragment;
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

    private static MainActivity mInstance;

    @ViewInject(id = R.id.drawer)
    private DrawerLayout mDrawerLayout;
    @ViewInject(id = R.id.fab)
    MainFloatingActionButton fabBtn;
    @ViewInject(id = R.id.appbar)
    AppBarLayout appBarLayout;
    @ViewInject(id = R.id.tabLayout)
    TabLayout tabLayout;
    @ViewInject(id = R.id.content_frame)
    FrameLayout contentFrame;

    private ActionBarDrawerToggle drawerToggle;
    private MaterialSheetFab materialSheetFab;
    private MenuFragment menuFragment;
    private FabGroupsFragment fabGroupsFragment;

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

        BizFragment bizFragment = BizFragment.createBizFragment(this);
        bizFragment.createFabAnimator(fabBtn);
        bizFragment.getFabAnimator().setDuration(200);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        setupDrawer(savedInstanceState);
        setupMenu(savedInstanceState);
        setupFab(savedInstanceState);
        setupAppBarLayout(savedInstanceState);

        mInstance = this;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent == null)
            return;

        String action = intent.getAction();

        int menuId = getActionType(intent, action);

        menuFragment.setSelectedMenuItem(menuId);

        if (isDrawerOpened())
            closeDrawer();
    }

    private int getActionType(Intent intent, String action) {
        int type = 1;
        // 处理点击Notification时，设置显示菜单
        if (ACTION_LOGIN.equals(action)) {
            type = 1;
        }
        else if (ACTION_NOTIFICATION.equals(action)) {
            type = Integer.parseInt(intent.getStringExtra("type"));
        }
        else if (ACTION_NOTIFICATION_MS.equals(action)) {
            ActivityHelper.putShareData("showMensitonType", "showMentionStatus");

            type = 2;
        }
        else if (ACTION_NOTIFICATION_MC.equals(action)) {
            ActivityHelper.putShareData("showMensitonType", "showMentionCmt");

            type = 2;
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
            menuFragment = MenuFragment.newInstance();
            getFragmentManager().beginTransaction().add(R.id.menu_frame, menuFragment, "MenuFragment").commit();
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
    }

    /**
     * 选择了侧边栏，切换侧边栏菜单
     *
     * @param item
     * @return
     */
    @Override
    public boolean onMenuClicked(MenuFragment.NavMenuItem item) {
        ABaseFragment fragment = null;

        // 切换ContentFragment，或者跳转到新的界面
        switch (item.id) {
        // 首页
        case MenuFragment.MENU_MAIN:
            fabGroupsFragment.triggerLastPosition();
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
            break;
        // 热门微博
        case MenuFragment.MENU_HOT_STATUS:
            break;
        // 草稿箱
        case MenuFragment.MENU_DRAT:
            fragment = DraftFragment.newInstance();
            break;
        // 设置
        case MenuFragment.MENU_SETTINGS:
            SettingsPagerFragment.launch(this);
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
        closeDrawer();
        // 设置可以选中的菜单项
        switch (item.id) {
        // 首页
        case MenuFragment.MENU_MAIN:
        // 提及
        case MenuFragment.MENU_MENTION:
        // 评论
        case MenuFragment.MENU_CMT:
        // 草稿箱
        case MenuFragment.MENU_DRAT:
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean onMenuSameClicked() {
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

    private void setFragemnt(Fragment fragment, CharSequence title) {
        if (fragment == null)
            return;

        closeDrawer();

        getSupportActionBar().setTitle(title);

        // 如果是TabsFragment，显示TabLayout
        if (fragment instanceof ATabsFragment) {
            tabLayout.setVisibility(View.VISIBLE);
        }
        else {
            tabLayout.setVisibility(View.GONE);
        }
        // 显示AppBarLayout
        appBarLayout.setExpanded(true, true);

        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, "MainFragment").commit();
    }

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
        if (item.getItemId() == R.id.about)
            AboutWebFragment.launchAbout(this);
        // 意见反馈
        else if (item.getItemId() == R.id.feedback)
            PublishActivity.publishFeedback(this);
            // 退出
        else if (item.getItemId() == R.id.exitapp)
            finish();
            // 新微博
        else if (item.getItemId() == R.id.publish)
            PublishActivity.publishStatus(this, null);
            // 开始离线
        else if (item.getItemId() == R.id.toggle_offline)
            OfflineUtils.toggleOffline(this);
            // 停止离线
        else if (item.getItemId() == R.id.stop_offline)
            OfflineService.stopOffline();

        return super.onOptionsItemSelected(item);
    }

    private boolean canFinish = false;

    @Override
    public boolean onBackClick() {
        if (AppSettings.isAppResident()) {
            if (menuFragment.backToMain()) {
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

        if (!AppContext.isLoggedIn()) {
            finish();

            return;
        }

        if (AppContext.getAccount().getAccessToken().isExpired()) {
            AccountFragment.launch(MainActivity.this);

            finish();
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

}
