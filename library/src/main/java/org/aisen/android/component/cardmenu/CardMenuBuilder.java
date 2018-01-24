package org.aisen.android.component.cardmenu;

import android.app.Activity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

/**
 * Created by wangdan on 16/9/13.
 */
public class CardMenuBuilder {

    private final Activity mContext;
    private final CardMenuPresenter mPresenter;
    private final MenuBuilder mMenuBuilder;
    private final CardMenuOptions options;

    private OnCardMenuCallback onCardMenuCallback;

    public CardMenuBuilder(Activity context, View anchorView, CardMenuOptions options) {
        mContext = context;
        this.options = options;
        mMenuBuilder = new MenuBuilder(context);
        mPresenter = new CardMenuPresenter(context, anchorView, this, options);
        mMenuBuilder.addMenuPresenter(mPresenter, new ContextThemeWrapper(context, options.themeRes));
    }

    public MenuItem add(int group, int id, int categoryOrder, CharSequence title) {
        MenuItem item = mMenuBuilder.add(group, id, categoryOrder, title);

        item.setOnMenuItemClickListener(onMenuItemClickListener);

        return item;
    }

    public SubMenu addSubMenu(int group, int id, int categoryOrder, CharSequence title) {
        SubMenu subMenu = mMenuBuilder.addSubMenu(group, id, categoryOrder, title);

        if (subMenu.getItem() instanceof MenuItemImpl) {
            subMenu.getItem().setOnMenuItemClickListener(onMenuItemClickListener);
        }

        return subMenu;
    }

    public MenuItem addSubMenuItem(SubMenu subMenu, int groupId, int itemId, int order, CharSequence title) {
        MenuItem item = subMenu.add(groupId, itemId, order, title);

        item.setOnMenuItemClickListener(onMenuItemClickListener);

        return item;
    }

    public CardMenuBuilder inflate(int menuRes) {
        mContext.getMenuInflater().inflate(menuRes, mMenuBuilder);

        return this;
    }

    public CardMenuBuilder add(int id, int titleRes) {
        add(1, id, 1, mContext.getString(titleRes));

        return this;
    }

    public CardMenuBuilder add(int id, String title) {
        add(1, id, 1, title);

        return this;
    }

    public CardMenuBuilder setOnCardMenuCallback(OnCardMenuCallback onCardMenuCallback) {
        this.onCardMenuCallback = onCardMenuCallback;

        return this;
    }

    public void show() {
        mPresenter.showCardMenu();
    }

    public CardMenuOptions getOptions() {
        return options;
    }

    private MenuItem.OnMenuItemClickListener onMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (onCardMenuCallback != null) {
                return onCardMenuCallback.onCardMenuItemSelected(item);
            }

            return false;
        }

    };

    public interface OnCardMenuCallback {

        boolean onCardMenuItemSelected(MenuItem menuItem);

    }

}
