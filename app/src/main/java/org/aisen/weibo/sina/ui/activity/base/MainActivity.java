package org.aisen.weibo.sina.ui.activity.base;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;
import android.widget.FrameLayout;

import org.aisen.android.common.md.MDHelper;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.sheetfab.MaterialSheetFab;
import org.aisen.android.component.sheetfab.MaterialSheetFabEventListener;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.comment.CommentPagerFragment;
import org.aisen.weibo.sina.ui.fragment.draft.DraftFragment;
import org.aisen.weibo.sina.ui.fragment.mention.MentionPagerFragment;
import org.aisen.weibo.sina.ui.fragment.menu.FabGroupsFragment;
import org.aisen.weibo.sina.ui.fragment.menu.MenuFragment;
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
public class MainActivity extends BaseActivity implements FabGroupsFragment.OnFabGroupSelectedCallback, MenuFragment.OnMenuCallback {

    public static final String ACTION_LOGIN = "org.aisen.sina.weibo.ACTION_LOGIN";
    public static final String ACTION_NOTIFICATION = "org.aisen.sina.weibo.ACTION_NOTIFICATION";
    public static final String ACTION_NOTIFICATION_MS = "org.aisen.sina.weibo.ACTION_NOTIFICATION_MS";
    public static final String ACTION_NOTIFICATION_MC = "org.aisen.sina.weibo.ACTION_NOTIFICATION_MC";

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_main);

        BizFragment.createBizFragment(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        setupDrawer(savedInstanceState);
        setupMenu(savedInstanceState);
        setupFab(savedInstanceState);
        setupAppBarLayout(savedInstanceState);
    }

    private void setupDrawer(Bundle savedInstanceState) {
        drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                getToolbar(), R.string.draw_open, R.string.draw_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

//                if (materialSheetFab != null) {
//                    materialSheetFab.showFab();
//                }
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

//                if (materialSheetFab != null) {
//                    if (materialSheetFab.isSheetVisible()) {
//                        materialSheetFab.hideSheetThenFab();
//                    }
//                    else if (fabBtn.isShown()) {
//                        fabBtn.hide();
//                    }
//                }
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
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float percent = Math.abs(verticalOffset) * 1.0f / SystemUtils.getActionBarHeight(MainActivity.this);

                int translationY = fabBtn.getHeight() + getResources().getDimensionPixelSize(R.dimen.fab_spacing);
                fabBtn.setTranslationY(translationY * (percent));
            }

        });
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
        case 1:
            fabGroupsFragment.triggerLastPosition();
            break;
        // 提及
        case 2:
            fragment = MentionPagerFragment.newInstance();
            break;
        // 评论
        case 3:
            fragment = CommentPagerFragment.newInstance();
            break;
        // 私信
        case 4:
            break;
        // 热门微博
        case 5:
            break;
        // 草稿箱
        case 6:
            fragment = DraftFragment.newInstance();
            break;
        // 设置
        case 7:
            break;
        }

        if (fragment != null) {
            setFragemnt(fragment, getString(item.toolbarRes));
        }

        // 隐藏Fab按钮
        if (item.id == 1) {
            fabBtn.setVisibility(View.VISIBLE);
        }
        else {
            fabBtn.setVisibility(View.GONE);
        }

        // 关闭侧边栏
        closeDrawer();
        // 设置可以选中的菜单项
        switch (item.id) {
        // 首页
        case 1:
        // 提及
        case 2:
        // 评论
        case 3:
        // 草稿箱
        case 6:
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

    public void closeDrawer() {
        mDrawerLayout.closeDrawers();
    }

}
