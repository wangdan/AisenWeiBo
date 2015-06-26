package org.aisen.weibo.sina.ui.fragment.basic;

import org.aisen.weibo.sina.R;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.view.LayoutInflater;

import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;

/**
 *
 * Created by wangdan on 15/6/7.
 */
public class NavigationViewFragment extends ABaseFragment {

	@ViewInject(idStr = "navigation")
    NavigationView navigationView;

    @Override
    protected int inflateContentView() {
        return R.layout.as_ui_navigationview;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);
    }
}
