package org.aisen.weibo.sina.ui.fragment.secondgroups;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.aisen.android.support.bean.TabItem;
import org.aisen.android.ui.fragment.ATabsTabLayoutFragment;
import org.aisen.weibo.sina.R;

import java.util.ArrayList;

/**
 * Created by wangdan on 16/3/14.
 */
public class JokesPagerFragment extends ATabsTabLayoutFragment<TabItem> {

    public static JokesPagerFragment newInstance() {
        return new JokesPagerFragment();
    }

    @Override
    public int inflateContentView() {
        return org.aisen.android.R.layout.comm_ui_tabs;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setTabLayout((TabLayout) getActivity().findViewById(R.id.tabLayout));

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected ArrayList<TabItem> generateTabs() {
        ArrayList<TabItem> items = new ArrayList<>();

        // 纯文
        items.add(new TabItem("1", getString(R.string.jokes_text)));
        // 图文
//        items.add(new TabItem("2", getString(R.string.jokes_image)));

        return items;
    }

    @Override
    protected Fragment newFragment(TabItem bean) {
        if ("1".equals(bean.getType())) {
            return JokeTextFragment.newInstance();
        }
        else if ("2".equals(bean.getType())) {
            return JokeTextFragment.newInstance();
        }

        return null;
    }

}
