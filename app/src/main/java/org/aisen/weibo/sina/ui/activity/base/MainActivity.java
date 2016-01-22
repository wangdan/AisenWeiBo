package org.aisen.weibo.sina.ui.activity.base;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;

import org.aisen.android.common.md.MDHelper;
import org.aisen.android.component.sheetfab.MaterialSheetFab;
import org.aisen.android.component.sheetfab.MaterialSheetFabEventListener;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.widget.FitWindowsFrameLayout;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.support.utils.SystemBarUtils;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.menu.FabGroupsFragment;
import org.aisen.weibo.sina.ui.fragment.menu.MenuFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineDefFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineGroupsFragment;
import org.aisen.weibo.sina.ui.widget.MainFloatingActionButton;

import java.util.ArrayList;

/**
 *
 * Created by wangdan on 15/4/23.
 */
public class MainActivity extends BaseActivity implements FabGroupsFragment.OnFabGroupSelectedCallback {

    public static final String ACTION_LOGIN = "org.aisen.sina.weibo.ACTION_LOGIN";
    public static final String ACTION_NOTIFICATION = "org.aisen.sina.weibo.ACTION_NOTIFICATION";
    public static final String ACTION_NOTIFICATION_MS = "org.aisen.sina.weibo.ACTION_NOTIFICATION_MS";
    public static final String ACTION_NOTIFICATION_MC = "org.aisen.sina.weibo.ACTION_NOTIFICATION_MC";

    @ViewInject(id = R.id.drawer)
    private DrawerLayout mDrawerLayout;
    @ViewInject(id = R.id.layMainContent)
    FitWindowsFrameLayout mainContent;
    @ViewInject(id = R.id.layMainRoot)
    FitWindowsFrameLayout mainRoot;
    @ViewInject(id = R.id.fab)
    MainFloatingActionButton fabBtn;

    private ActionBarDrawerToggle drawerToggle;
    private MaterialSheetFab materialSheetFab;
    private MenuFragment menuFragment;
    private FabGroupsFragment fabGroupsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SystemBarUtils.setStatusBar(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        mainRoot.setOnFitSystemWindowsListener(new FitWindowsFrameLayout.OnFitSystemWindowsListener() {

            @Override
            public void onFitSystemWindows(Rect insets) {
                mainContent.setFitWindowns(insets);
            }

        });

        setupDrawer(savedInstanceState);
        setupMenu(savedInstanceState);
        setupFab(savedInstanceState);

        BizFragment.getBizFragment(this);
    }

    private void setupDrawer(Bundle savedInstanceState) {
        drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                getToolbar(), R.string.draw_open, R.string.draw_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                if (materialSheetFab != null) {
                    materialSheetFab.showFab();
                }
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (materialSheetFab != null) {
                    if (materialSheetFab.isSheetVisible()) {
                        materialSheetFab.hideSheetThenFab();
                    }
                    else if (fabBtn.isShown()) {
                        fabBtn.hide();
                    }
                }
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
        fabGroupsFragment.triggerLastPosition();
    }

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

        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, "Main").commit();
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
