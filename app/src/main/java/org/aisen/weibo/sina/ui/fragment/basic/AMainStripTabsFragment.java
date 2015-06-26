package org.aisen.weibo.sina.ui.fragment.basic;

import org.aisen.android.ui.fragment.AAutoReleaseStripTabsFragment;
import org.aisen.android.ui.fragment.AStripTabsFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.ui.activity.basic.MainActivity;

/**
 * Created by wangdan on 15/4/25.
 */
public abstract class AMainStripTabsFragment extends AAutoReleaseStripTabsFragment<AStripTabsFragment.StripTabItem> {

    @Override
    protected int inflateContentView() {
        return R.layout.as_ui_main_tabs;
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);

        ((MainActivity) getActivity()).toggleToolbarShown(true);
    }
}
