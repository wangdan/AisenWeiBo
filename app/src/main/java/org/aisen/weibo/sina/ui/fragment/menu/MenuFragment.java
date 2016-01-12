package org.aisen.weibo.sina.ui.fragment.menu;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.aisen.android.common.md.MDHelper;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 维护左侧抽屉
 *
 * Created by wangdan on 15/4/23.
 */
public class MenuFragment extends ABaseFragment {

    public static MenuFragment newInstance() {
        return new MenuFragment();
    }

    @ViewInject(id = R.id.layHeader)
    View mHeaderView;
    @ViewInject(id = R.id.layMenuItems)
    LinearLayout layMenuItems;

    private int selectedPosition = R.string.menu_sinaweibo;

    @Override
    protected int inflateContentView() {
        return R.layout.ui_menu;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        if (savedInstanceSate != null) {
            selectedPosition = savedInstanceSate.getInt("selectedPosition", selectedPosition);
        }

        setupHeader();
        setupMenuItems();
        setSelectedMenuItem(selectedPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("selectedPosition", selectedPosition);
    }

    private void setupHeader() {
        ImageView imgCover = (ImageView) mHeaderView.findViewById(R.id.imgCover);
        ImageView imgPhoto = (ImageView) mHeaderView.findViewById(R.id.imgPhoto);
    }

    private void setupMenuItems() {
        List<NavMenuItem> items = generateMenuItems();
        for (int i = 0; i < items.size(); i++) {
            final NavMenuItem item = items.get(i);
            // 分隔符
            if (item instanceof NavMenuSeparator) {
                View.inflate(getActivity(), R.layout.lay_navmenu_separator, layMenuItems);
            }
            // 抽屉菜单
            else {
                View viewItem = View.inflate(getActivity(), R.layout.lay_navmenu_item, null);
                viewItem.setId(item.titleRes);
                viewItem.setTag(item);
                viewItem.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        setSelectedMenuItem(item.titleRes);
                    }

                });

                ImageView imgIcon = (ImageView) viewItem.findViewById(R.id.icon);
                imgIcon.setImageResource(item.iconRes);

                TextView txtTitle = (TextView) viewItem.findViewById(R.id.title);
                txtTitle.setText(item.titleRes);

                layMenuItems.addView(viewItem, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    private void setSelectedMenuItem(int itemId) {
        int selectedColor = MDHelper.resolveColor(getActivity(), R.attr.colorPrimary, getResources().getColor(R.color.app_body_text_1));
        int defColor = getResources().getColor(R.color.app_body_text_1);

        if (selectedPosition != itemId) {
            setNavMenuItemState(selectedPosition, false, selectedColor, defColor);
        }

        setNavMenuItemState(itemId, true, selectedColor, defColor);

        selectedPosition = itemId;
    }

    private void setNavMenuItemState(int itemId, boolean selected, int selectedColor, int defColor) {
        View viewItem = layMenuItems.findViewById(itemId);
        NavMenuItem item = (NavMenuItem) viewItem.getTag();

        viewItem.setSelected(selected);

        ImageView imgIcon = (ImageView) viewItem.findViewById(R.id.icon);
        Drawable drawableIcon = DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), item.iconRes));
        ColorStateList mIconTints = new ColorStateList(
                                                        new int[][]{ { android.R.attr.state_selected },
                                                                     { } },
                                                        new int[]{ selectedColor, defColor });
        if (mIconTints != null) {
            DrawableCompat.setTintList(drawableIcon, mIconTints);
        }
        imgIcon.setImageDrawable(drawableIcon);

        TextView txtTitle = (TextView) viewItem.findViewById(R.id.title);
        txtTitle.setTextColor(mIconTints);
    }

    public List<NavMenuItem> generateMenuItems() {
        List<NavMenuItem> items = new ArrayList<>();

        items.add(new NavMenuItem(R.drawable.ic_view_day_grey600_24dp, R.string.menu_sinaweibo));
        items.add(new NavMenuItem(R.drawable.ic_view_day_grey600_24dp, R.string.menu_sinaweibo));
        items.add(new NavMenuSeparator());
        items.add(new NavMenuItem(R.drawable.ic_view_day_grey600_24dp, R.string.menu_sinaweibo));
        items.add(new NavMenuItem(R.drawable.ic_view_day_grey600_24dp, R.string.menu_sinaweibo));

        return items;
    }

    public static class NavMenuItem implements Serializable {

        private static final long serialVersionUID = -5100620719172179926L;

        public int titleRes;

        public int iconRes;

        public NavMenuItem() {

        }

        public NavMenuItem(int iconRes, int titleRes) {
            this.iconRes = iconRes;
            this.titleRes = titleRes;
        }

    }

    public static class NavMenuSeparator extends NavMenuItem {

        private static final long serialVersionUID = -4260703722925344923L;

    }

}
