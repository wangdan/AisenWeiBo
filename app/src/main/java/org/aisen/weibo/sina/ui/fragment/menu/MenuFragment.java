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

    private OnMenuCallback onMenuCallback;
    private int selectedId;

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

        setupHeader(savedInstanceSate);
        setupMenuItems(savedInstanceSate);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("selectedId", selectedId);
    }

    private void setupHeader(Bundle savedInstanceSate) {
        ImageView imgCover = (ImageView) mHeaderView.findViewById(R.id.imgCover);
        ImageView imgPhoto = (ImageView) mHeaderView.findViewById(R.id.imgPhoto);
    }

    private void setupMenuItems(Bundle savedInstanceSate) {
        List<NavMenuItem> items = generateMenuItems();

        selectedId = savedInstanceSate == null ? items.get(0).id : savedInstanceSate.getInt("selectedId", selectedId);

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
                                onMenuCallback.onMenuSameClicked();
                            }
                            return;
                        }

                        boolean selected = false;
                        if (onMenuCallback != null) {
                            selected = onMenuCallback.onMenuClicked(menuItem);
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

        if (savedInstanceSate == null && onMenuCallback != null) {
            boolean selected = onMenuCallback.onMenuClicked(items.get(0));
            if (selected) {
                setSelectedMenuItem(selectedId);
            }
        }
    }

    private void setSelectedMenuItem(int itemId) {
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

    public List<NavMenuItem> generateMenuItems() {
        List<NavMenuItem> items = new ArrayList<>();

        items.add(new NavMenuItem(1, R.drawable.ic_view_day_grey600_24dp, R.string.menu_sinaweibo));
        items.add(new NavMenuItem(2, R.drawable.ic_drawer_at, R.string.draw_message, R.string.mention_title));
        items.add(new NavMenuItem(3, R.drawable.ic_question_answer_grey600_24dp, R.string.draw_comment));
        items.add(new NavMenuItem(4, R.drawable.ic_email_grey600_24dp, R.string.draw_private_msg));
        items.add(new NavMenuSeparator());
        items.add(new NavMenuItem(5, -1, R.string.draw_hot_statuses));
        items.add(new NavMenuItem(6, -1, R.string.draw_draft));
        items.add(new NavMenuItem(7, -1, R.string.draw_settings));

        return items;
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
        boolean onMenuClicked(NavMenuItem item);

        boolean onMenuSameClicked();

    }

}
