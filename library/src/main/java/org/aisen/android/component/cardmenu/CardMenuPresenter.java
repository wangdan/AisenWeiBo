package org.aisen.android.component.cardmenu;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.GravityCompat;
import android.support.v7.view.menu.BaseMenuPresenter;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.view.menu.MenuView;
import android.support.v7.view.menu.SubMenuBuilder;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by wangdan on 16/9/13.
 */
public class CardMenuPresenter extends BaseMenuPresenter {

    private OpenOverflowRunnable mPostedOpenRunnable;

    private OverflowPopup mOverflowPopup;

    private ActionButtonSubmenu mActionButtonPopup;

    private View anchorView;

    int mOpenSubMenuId;

    CardMenuBuilder cardMenuBuilder;

    private final int popupStyleAttr;

    /**
     * Construct a new BaseMenuPresenter.
     *
     * @param context       Context for generating system-supplied views
     */
    public CardMenuPresenter(Context context, View anchorView, CardMenuBuilder cardMenuBuilder, CardMenuOptions options) {
        super(context, options.actionMenuLayoutRes, options.actionMenuItemLayoutRes);
        this.anchorView = anchorView;
        this.cardMenuBuilder = cardMenuBuilder;
        this.popupStyleAttr = options.popupStyleAttr;
    }

    @Override
    public void bindItemView(MenuItemImpl item, MenuView.ItemView itemView) {
//        itemView.initialize(item, 0);
//
//        final ActionMenuView menuView = (ActionMenuView) mMenuView;
//        final ActionMenuItemView actionItemView = (ActionMenuItemView) itemView;
//        actionItemView.setItemInvoker(menuView);

//        if (mPopupCallback == null) {
//            mPopupCallback = new ActionMenuPopupCallback();
//        }
//        actionItemView.setPopupCallback(mPopupCallback);
    }

    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        if (!subMenu.hasVisibleItems()) return false;

        SubMenuBuilder topSubMenu = subMenu;
        while (topSubMenu.getParentMenu() != mMenu) {
            topSubMenu = (SubMenuBuilder) topSubMenu.getParentMenu();
        }


        mOpenSubMenuId = subMenu.getItem().getItemId();

        boolean preserveIconSpacing = false;
        final int count = subMenu.size();
        for (int i = 0; i < count; i++) {
            MenuItem childItem = subMenu.getItem(i);
            if (childItem.isVisible() && childItem.getIcon() != null) {
                preserveIconSpacing = true;
                break;
            }
        }

        subMenu.addMenuPresenter(this, mContext);
        mActionButtonPopup = new ActionButtonSubmenu(mContext, subMenu, anchorView);
        mActionButtonPopup.setForceShowIcon(preserveIconSpacing);
        mActionButtonPopup.show();

        super.onSubMenuSelected(subMenu);
        return true;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState();
        state.openSubMenuId = mOpenSubMenuId;
        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState saved = (SavedState) state;
        if (saved.openSubMenuId > 0) {
            MenuItem item = mMenu.findItem(saved.openSubMenuId);
            if (item != null) {
                SubMenuBuilder subMenu = (SubMenuBuilder) item.getSubMenu();
                onSubMenuSelected(subMenu);
            }
        }
    }

    public boolean showCardMenu() {
        // 暂时不判断menuView
        // && mMenuView != null
        if (!isOverflowMenuShowing() && mMenu != null &&
                mPostedOpenRunnable == null && !mMenu.getNonActionItems().isEmpty()) {
            OverflowPopup popup = new OverflowPopup(mContext, mMenu, anchorView, true, popupStyleAttr);
            mPostedOpenRunnable = new OpenOverflowRunnable(popup);
            // Post this for later; we might still need a layout for the anchor to be right.
//            ((View) mMenuView).post(mPostedOpenRunnable);
            anchorView.post(mPostedOpenRunnable);
            // ActionMenuPresenter uses null as a callback argument here
            // to indicate overflow is opening.
            super.onSubMenuSelected(null);

            return true;
        }
        return false;
    }

    public boolean isOverflowMenuShowing() {
        return mOverflowPopup != null && mOverflowPopup.isShowing();
    }

    /**
     * Hide the overflow menu if it is currently showing.
     *
     * @return true if the overflow menu was hidden, false otherwise.
     */
    public boolean hideOverflowMenu() {
        if (mPostedOpenRunnable != null && mMenuView != null) {
            ((View) mMenuView).removeCallbacks(mPostedOpenRunnable);
            mPostedOpenRunnable = null;
            return true;
        }

        MenuPopupHelper popup = mOverflowPopup;
        if (popup != null) {
            popup.dismiss();
            return true;
        }
        return false;
    }

    /**
     * Dismiss all popup menus - overflow and submenus.
     * @return true if popups were dismissed, false otherwise. (This can be because none were open.)
     */
    public boolean dismissPopupMenus() {
        boolean result = hideOverflowMenu();
        result |= hideSubMenus();
        return result;
    }

    /**
     * Dismiss all submenu popups.
     *
     * @return true if popups were dismissed, false otherwise. (This can be because none were open.)
     */
    public boolean hideSubMenus() {
        if (mActionButtonPopup != null) {
            mActionButtonPopup.dismiss();
            return true;
        }
        return false;
    }

    private class ActionButtonSubmenu extends MenuPopupHelper {
        public ActionButtonSubmenu(Context context, SubMenuBuilder subMenu, View anchorView) {
            super(context, subMenu, anchorView, false, popupStyleAttr);
//            super(context, subMenu, cardMenuBuilder.getOptions(), anchorView, false);

            if (cardMenuBuilder.getOptions().dropDownGravity != -1) {
                setGravity(cardMenuBuilder.getOptions().dropDownGravity);
            }
            else {
                setGravity(GravityCompat.END);
            }
        }

        @Override
        public void onDismiss() {
            mActionButtonPopup = null;
            mOpenSubMenuId = 0;

            super.onDismiss();
        }
    }

    private class OverflowPopup extends MenuPopupHelper {

        OverflowPopup(Context context, MenuBuilder menu, View anchorView,
                             boolean overflowOnly, int popupStyleAttr) {
            super(context, menu, anchorView, overflowOnly, popupStyleAttr);

            if (cardMenuBuilder.getOptions().dropDownGravity != -1) {
                setGravity(cardMenuBuilder.getOptions().dropDownGravity);
            }
            else {
                setGravity(GravityCompat.END);
            }
        }

        @Override
        public void onDismiss() {
            super.onDismiss();

            if (mMenu != null) {
                mMenu.close();
            }

            mOverflowPopup = null;
        }

    }

    private class OpenOverflowRunnable implements Runnable {
        private OverflowPopup mPopup;

        public OpenOverflowRunnable(OverflowPopup popup) {
            mPopup = popup;
        }

        public void run() {
            mMenu.changeMenuMode();
//            final View menuView = (View) mMenuView;
            final View menuView = anchorView;
            if (menuView != null && menuView.getWindowToken() != null && mPopup.tryShow()) {
                mOverflowPopup = mPopup;
            }
            mPostedOpenRunnable = null;
        }
    }

    @Override
    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        dismissPopupMenus();
        super.onCloseMenu(menu, allMenusAreClosing);
    }

    private static class SavedState implements Parcelable {
        public int openSubMenuId;

        SavedState() {
        }

        SavedState(Parcel in) {
            openSubMenuId = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(openSubMenuId);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
