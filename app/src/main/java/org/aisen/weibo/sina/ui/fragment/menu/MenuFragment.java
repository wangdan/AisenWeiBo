package org.aisen.weibo.sina.ui.fragment.menu;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.ui.activity.base.MainActivity;

/**
 * Created by wangdan on 15/4/23.
 */
public class MenuFragment extends ABaseFragment {

    public static MenuFragment newInstance() {
        return new MenuFragment();
    }

    @ViewInject(id = R.id.menuNavigation)
    NavigationView mNavigationView;
    View mHeaderView;

    private int selectedMenuItemId = -1;

    @Override
    protected int inflateContentView() {
        return R.layout.ui_menu;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        mHeaderView = mNavigationView.getHeaderView(0);
        ImageView imgCover = (ImageView) mHeaderView.findViewById(R.id.imgCover);
        ImageView imgPhoto = (ImageView) mHeaderView.findViewById(R.id.imgPhoto);

//        ImageLoader.getInstance().displayImage("http://img.t.sinajs.cn/t5/skin/public/covervip/2044.jpg", imgCover);
//        ImageLoader.getInstance().displayImage("http://tp1.sinaimg.cn/2486736244/180/5731430361/1", imgPhoto);

        if (savedInstanceSate == null) {
            selectedMenuItemId = R.id.drawPics;
        }
        else {
            selectedMenuItemId = savedInstanceSate.getInt("selectedMenuItemId", R.id.drawPics);
        }

        onMenuSelected(selectedMenuItemId, -1);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("selectedMenuItemId", selectedMenuItemId);
    }

    public void onMenuSelected(int menuItemId, int previousMenuItemId) {
        if (getActivity() != null) {
            MenuItem menuItem = mNavigationView.getMenu().findItem(menuItemId);
            mNavigationView.setItemBackgroundResource(R.drawable.abc_list_pressed_holo_light);

            MenuItem preMenuItem = null;
            if (previousMenuItemId > 0) {
                preMenuItem = mNavigationView.getMenu().findItem(previousMenuItemId);
                mNavigationView.setItemBackground(new ColorDrawable(Color.TRANSPARENT));
            }

            ((MainActivity) getActivity()).onMenuSelected(menuItem, preMenuItem);
        }

        selectedMenuItemId = menuItemId;
    }

}
