package org.aisen.weibo.sina.ui.fragment.menu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.aisen.android.common.md.MDHelper;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.service.UnreadService;
import org.aisen.weibo.sina.service.publisher.PublishManager;
import org.aisen.weibo.sina.sinasdk.bean.UnreadCount;
import org.aisen.weibo.sina.support.sqlit.PublishDB;
import org.aisen.weibo.sina.support.utils.AisenUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 维护左侧抽屉
 *
 * Created by wangdan on 15/4/23.
 */
public class MenuFragment extends ABaseFragment {

    public static final int MENU_MAIN = 1;
    public static final int MENU_MENTION = 2;
    public static final int MENU_CMT = 3;
    public static final int MENU_FRIENDSHIP = 4;
    public static final int MENU_MD = 10;
    public static final int MENU_HOT_STATUS = 11;
    public static final int MENU_DRAT = 6;
    public static final int MENU_SETTINGS = 5;
    public static final int MENU_PROFIL = 0;
    public static final int MENU_NOTIFICATION = 12;
    public static final int MENU_JOKE = 13;
    public static final int MENU_WALLPAPER = 14;

    public static MenuFragment newInstance(int menuId) {
        MenuFragment fragment = new MenuFragment();

        Bundle args = new Bundle();
        if (menuId > 0) {
            args.putInt("menuId", menuId);
        }
        fragment.setArguments(args);

        return fragment;
    }

    private OnMenuCallback onMenuCallback;
    private int selectedId;

    @ViewInject(id = R.id.layMenuItems)
    LinearLayout layMenuItems;

    private MenuHeaderView menuHeaderView;

    @Override
    public int inflateContentView() {
        return R.layout.ui_menu;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() instanceof OnMenuCallback) {
            onMenuCallback = (OnMenuCallback) getActivity();
        }
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        setupMenuItems(savedInstanceSate);
        // 如果是私信，还要默认打开首页，否则是空的页面
        if (selectedId == MenuFragment.MENU_MD) {
            triggerMenuClick(MenuFragment.MENU_MAIN);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        menuHeaderView = new MenuHeaderView(this);
        menuHeaderView.setupHeaderView((FrameLayout) findViewById(R.id.layHeaderContainer));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("selectedId", selectedId);
    }

    /**
     * 点击某个菜单
     *
     * @param menuId
     */
    public void triggerMenuClick(int menuId) {
        View itemView = layMenuItems.findViewById(menuId);
        if (itemView != null) {
            itemView.performClick();
        }
    }

    /**
     * 切换用户
     *
     */
    public void changeAccount() {

        menuHeaderView.setupHeaderView((FrameLayout) findViewById(R.id.layHeaderContainer));

        int menuId = MENU_MAIN;
        View viewItem = layMenuItems.findViewById(menuId);
        NavMenuItem menuItem = (NavMenuItem) viewItem.getTag();
        boolean selected = false;
        if (onMenuCallback != null) {
            onMenuCallback.onMenuClicked(menuItem, false);

            selected = onMenuCallback.onMenuSelected(menuItem);
        }

        // 只记录选中项ID
        if (selected) {
            setSelectedMenuItem(menuItem.id);

            selectedId = menuItem.id;
        }
    }

    private void setupMenuItems(Bundle savedInstanceSate) {
        List<NavMenuItem> items = generateMenuItems();

        selectedId = savedInstanceSate == null ? getArguments().getInt("menuId", items.get(0).id) : savedInstanceSate.getInt("selectedId", selectedId);

        for (int i = 0; i < items.size(); i++) {
            final NavMenuItem item = items.get(i);
            // 分隔符
            if (item instanceof NavMenuSeparator) {
                View.inflate(getActivity(), R.layout.lay_navmenu_separator, layMenuItems);
            }
            // 抽屉菜单
            else {
                View viewItem = View.inflate(getActivity(), R.layout.lay_navmenu_item, null);
                viewItem.setId(item.id);
                viewItem.setTag(item);
                viewItem.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        NavMenuItem menuItem = (NavMenuItem) v.getTag();

                        // 选择相同项时处理
                        if (selectedId == menuItem.id) {
                            if (onMenuCallback != null) {
                                onMenuCallback.onMenuSameClicked(menuItem);
                            }
                            return;
                        }

                        boolean selected = false;
                        if (onMenuCallback != null) {
                            onMenuCallback.onMenuClicked(menuItem, true);

                            selected = onMenuCallback.onMenuSelected(menuItem);
                        }

                        // 只记录选中项ID
                        if (selected) {
                            setSelectedMenuItem(menuItem.id);

                            selectedId = menuItem.id;
                        }
                    }

                });

                ImageView imgIcon = (ImageView) viewItem.findViewById(R.id.icon);
                if (item.iconRes == -1) {
                    imgIcon.setVisibility(View.GONE);
                }
                else {
                    imgIcon.setImageResource(item.iconRes);
                }

                TextView txtTitle = (TextView) viewItem.findViewById(R.id.title);
                txtTitle.setText(item.titleRes);

                layMenuItems.addView(viewItem, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
        }

        if (onMenuCallback != null) {
            View viewItem = layMenuItems.findViewById(selectedId);
            NavMenuItem item = (NavMenuItem) viewItem.getTag();
            if (savedInstanceSate == null) {
                onMenuCallback.onMenuClicked(item, true);
            }
            boolean selected = onMenuCallback.onMenuSelected(item);
            if (selected) {
                setSelectedMenuItem(selectedId);
            }
        }
    }

    public void setSelectedMenuItem(int itemId) {
        int selectedColor = MDHelper.resolveColor(getActivity(), R.attr.colorPrimary, getResources().getColor(R.color.menu_color));
        int defColor = getResources().getColor(R.color.menu_color);

        if (selectedId != itemId) {
            setNavMenuItemState(selectedId, false, selectedColor, defColor);
        }

        setNavMenuItemState(itemId, true, selectedColor, defColor);
    }

    private void setNavMenuItemState(int itemId, boolean selected, int selectedColor, int defColor) {
        View viewItem = layMenuItems.findViewById(itemId);
        NavMenuItem item = (NavMenuItem) viewItem.getTag();

        viewItem.setSelected(selected);

        ImageView imgIcon = (ImageView) viewItem.findViewById(R.id.icon);

        int iconRes = item.iconRes > 0 ? item.iconRes : R.drawable.a_icon_b;
        Drawable drawableIcon = DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), iconRes));
        ColorStateList mIconTints = new ColorStateList(
                                                        new int[][]{ { android.R.attr.state_selected },
                                                                     { } },
                                                        new int[]{ selectedColor, defColor });
        if (mIconTints != null) {
            DrawableCompat.setTintList(drawableIcon, mIconTints);
        }
        if (item.iconRes > 0) {
            imgIcon.setImageDrawable(drawableIcon);
        }

        TextView txtTitle = (TextView) viewItem.findViewById(R.id.title);
        txtTitle.setTextColor(mIconTints);
    }

    private void setNavMenuUnread(int itemId, int count) {
        View viewItem = layMenuItems.findViewById(itemId);

        TextView txtCounter = (TextView) viewItem.findViewById(R.id.txtCounter);
        if (count > 0) {
            txtCounter.setVisibility(View.VISIBLE);
            txtCounter.setText(AisenUtils.getCounter(count));
        }
        else {
            txtCounter.setVisibility(View.GONE);
        }
    }

    public List<NavMenuItem> generateMenuItems() {
        List<NavMenuItem> items = new ArrayList<>();

        items.add(new NavMenuItem(MENU_MAIN, R.drawable.ic_weibo_grey, R.string.menu_sinaweibo));
//        items.add(new NavMenuItem(MENU_MENTION, R.drawable.ic_drawer_at, R.string.draw_message, R.string.mention_title));
//        items.add(new NavMenuItem(MENU_CMT, R.drawable.ic_question_answer_grey600_24dp, R.string.draw_comment));
        items.add(new NavMenuItem(MENU_NOTIFICATION, R.drawable.ic_notification_gray_24, R.string.draw_private_notification));
        items.add(new NavMenuItem(MENU_MD, R.drawable.ic_email_grey600_24dp, R.string.draw_private_msg));
        items.add(new NavMenuSeparator());
        items.add(new NavMenuItem(MENU_JOKE, -1, R.string.draw_joke));
        items.add(new NavMenuItem(MENU_WALLPAPER, -1, R.string.draw_wallpaper));
        items.add(new NavMenuSeparator());
        items.add(new NavMenuItem(MENU_HOT_STATUS, -1, R.string.draw_hot_statuses));
        items.add(new NavMenuItem(MENU_DRAT, -1, R.string.draw_draft));
        items.add(new NavMenuItem(MENU_SETTINGS, -1, R.string.draw_settings));

        return items;
    }

    @Override
    public void onResume() {
        super.onResume();

        menuHeaderView.setUnreadFollowers();
        menuHeaderView.setAccounts();

        // 刷新草稿
        new RefreshDraftTask().execute();
        // 刷新通知
        setUnreadNotification();

        IntentFilter filter = new IntentFilter();
        filter.addAction(UnreadService.ACTION_UNREAD_CHANGED);
        filter.addAction(PublishManager.ACTION_PUBLISH_CHANNGED);
        getActivity().registerReceiver(receiver, filter);
    }

    private void setUnreadNotification() {
        // 通知
        UnreadCount unreadCount = AppContext.getAccount().getUnreadCount();
        int count = 0;
        if (unreadCount != null) {
            count = unreadCount.getCmt() + unreadCount.getMention_cmt() + unreadCount.getMention_status();
        }
        setNavMenuUnread(MENU_NOTIFICATION, count);

        // 私信
        count = 0;
        if (unreadCount != null) {
            count = unreadCount.getDm();
        }
        setNavMenuUnread(MENU_MD, count);
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(receiver);
    }

    public boolean backToMain() {
        if (selectedId != MENU_MAIN) {
            setSelectedMenuItem(MENU_MAIN);

            return true;
        }

        return false;
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
                if (UnreadService.ACTION_UNREAD_CHANGED.equals(intent.getAction())) {
                    // 粉丝
                    menuHeaderView.setUnreadFollowers();

                    // 通知
                    setUnreadNotification();
                }
                else if (PublishManager.ACTION_PUBLISH_CHANNGED.equals(intent.getAction())) {
                    new RefreshDraftTask().execute();
                }
            }
        }

    };

    class RefreshDraftTask extends WorkTask<Void, Void, Integer> {

        @Override
        public Integer workInBackground(Void... params) throws TaskException {
            return PublishDB.getPublishList(AppContext.getAccount().getUser()).size();
        }

        @Override
        protected void onSuccess(Integer result) {
            super.onSuccess(result);

            // 刷新草稿的显示
            refreshDraftMenu(result);
        }

    }

    private void refreshDraftMenu(int count) {
        View viewItem = layMenuItems.findViewById(Integer.parseInt("6"));

        TextView txtCounter = (TextView) viewItem.findViewById(R.id.txtCounter);
        txtCounter.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
        txtCounter.setText(String.valueOf(count));
    }

    public int getSelectedId() {
        return selectedId;
    }

    public static class NavMenuItem implements Serializable {

        private static final long serialVersionUID = -5100620719172179926L;

        public int titleRes;

        public int toolbarRes;

        public int iconRes;

        public int id;

        public NavMenuItem() {
        }

        public NavMenuItem(int id, int iconRes, int titleRes) {
            this(id, iconRes, titleRes, titleRes);
        }

        public NavMenuItem(int id, int iconRes, int titleRes, int toolbarRes) {
            this.id = id;
            this.iconRes = iconRes;
            this.titleRes = titleRes;
            this.toolbarRes = toolbarRes;
        }

    }

    public static class NavMenuSeparator extends NavMenuItem {

        private static final long serialVersionUID = -4260703722925344923L;

    }

    public interface OnMenuCallback {

        /**
         *
         * @param item
         * @return 是否可以选中
         */
        void onMenuClicked(NavMenuItem item, boolean closeDrawer);

        boolean onMenuSelected(NavMenuItem item);

        boolean onMenuSameClicked(NavMenuItem item);

    }

}
