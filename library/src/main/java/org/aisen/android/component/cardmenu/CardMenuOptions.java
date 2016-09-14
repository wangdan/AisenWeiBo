package org.aisen.android.component.cardmenu;

/**
 * Created by wangdan on 16/9/13.
 */
public class CardMenuOptions {

    int dropDownGravity = -1;
    // 设置ListPopup的Offset
    int dropDownHorizontalOffset;
    int dropDownVerticalOffset;
    int themeRes;
    int popupStyleAttr;
    int actionMenuLayoutRes;
    int actionMenuItemLayoutRes;

    public CardMenuOptions(int themeRes, int popupStyleAttr, int actionMenuLayoutRes, int actionMenuItemLayoutRes) {
        this.themeRes = themeRes;
        this.popupStyleAttr = popupStyleAttr;
        this.actionMenuLayoutRes = actionMenuLayoutRes;
        this.actionMenuItemLayoutRes = actionMenuItemLayoutRes;
    }

    public CardMenuOptions setGravity(int gravity) {
        dropDownGravity = gravity;
        return this;
    }

    public CardMenuOptions setDropDownHorizontalOffset(int offset) {
        dropDownHorizontalOffset = offset;
        return this;
    }

    public CardMenuOptions setDropDownVerticalOffset(int offset) {
        dropDownVerticalOffset = offset;
        return this;
    }
    
}
