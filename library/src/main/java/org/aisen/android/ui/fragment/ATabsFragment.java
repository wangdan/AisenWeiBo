package org.aisen.android.ui.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;

import org.aisen.android.R;
import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.ui.fragment.adapter.FragmentPagerAdapter;
import org.aisen.android.support.bean.TabItem;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 维护一个ViewPager的Fragemnt
 *
 * Created by wangdan on 15/12/22.
 */
public abstract class ATabsFragment<T extends TabItem> extends ABaseFragment
                            implements ViewPager.OnPageChangeListener {

    static final String TAG = "AFragment-Tabs";

    public static final String SET_INDEX = "org.aisen.android.ui.SET_INDEX";// 默认选择第几个

    @ViewInject(idStr = "viewPager")
    ViewPager mViewPager;

    FragmentPagerAdapter mInnerAdapter;

    ArrayList<T> mItems;
    Map<String, Fragment> fragments;
    int mCurrentPosition = 0;

    private boolean isRetainFragments = false;// 如果系统先调用onSaveInstanceState方法，说明添加的Fragments是需要保留的

    @Override
    public int inflateContentView() {
        return R.layout.comm_ui_tabs;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mCurrentPosition = mViewPager.getCurrentItem();
        outState.putSerializable("items", mItems);
        outState.putInt("current", mCurrentPosition);

        isRetainFragments = true;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, final Bundle savedInstanceState) {
        super.layoutInit(inflater, savedInstanceState);

        mItems = savedInstanceState == null ? generateTabs()
                                            : (ArrayList<T>) savedInstanceState.getSerializable("items");

        mCurrentPosition = savedInstanceState == null ? 0
                                                      : savedInstanceState.getInt("current");

        if (delayTabInit() == 0) {
            setTabInit(savedInstanceState);
        }
        else {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    setTabInit(savedInstanceState);
                }

            }, delayTabInit());
        }
    }

    protected void setTabInit(final Bundle savedInstanceSate) {
        if (getActivity() == null) {
            return;
        }
        else if (getActivity() instanceof BaseActivity) {
            if (((BaseActivity) getActivity()).isDestory()) {
                return;
            }
        }

        if (savedInstanceSate == null) {
            if (getArguments() != null && getArguments().containsKey(SET_INDEX)) {
                mCurrentPosition = Integer.parseInt(getArguments().getSerializable(SET_INDEX).toString());
            }
            else {
                if (configLastPositionKey() != null) {
                    // 记录了最后阅读的标签
                    String type = ActivityHelper.getShareData(GlobalContext.getInstance(), "PagerLastPosition" + configLastPositionKey(), "");
                    if (!TextUtils.isEmpty(type)) {
                        for (int i = 0; i < mItems.size(); i++) {
                            TabItem item = mItems.get(i);
                            if (type.equals(item.getType())) {
                                mCurrentPosition = i;
                                break;
                            }
                        }
                    }
                }
            }
        }

        Logger.d(TAG, "CurrentPosition " + mCurrentPosition);

        fragments = new HashMap<>();

        // 初始化的时候，移除一下Fragment
        if (savedInstanceSate == null) {
            for (int i = 0; i < mItems.size(); i++) {
                Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(makeFragmentName(i));
                if (fragment != null) {
                    getActivity().getFragmentManager().beginTransaction()
                            .remove(fragment).commit();
                }
            }
        }

        setupViewPager(savedInstanceSate);
    }

    protected void setupViewPager(final Bundle savedInstanceSate) {
        mInnerAdapter = new InnerAdapter(getFragmentManager());
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setAdapter(mInnerAdapter);
        if (mCurrentPosition >= mInnerAdapter.getCount())
            mCurrentPosition = 0;
        mViewPager.setCurrentItem(mCurrentPosition);
        mViewPager.addOnPageChangeListener(this);
    }

    protected void destoryFragments() {
        if (getActivity() != null) {
            if (getActivity() instanceof BaseActivity) {
                BaseActivity mainActivity = (BaseActivity) getActivity();
                if (mainActivity.isDestory())
                    return;
            }

            try {
                FragmentTransaction trs = getFragmentManager().beginTransaction();
                Set<String> keySet = fragments.keySet();
                for (String key : keySet) {
                    if (fragments.get(key) != null) {
                        trs.remove(fragments.get(key));

                        Logger.d(TAG, "remove fragment , key = " + key);
                    }
                }
                trs.commit();
            } catch (Throwable e) {
                Logger.printExc(getClass(), e);
            }
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPosition = position;

        if (configLastPositionKey() != null) {
            ActivityHelper.putShareData(GlobalContext.getInstance(), "PagerLastPosition" + configLastPositionKey(), mItems.get(position).getType());
        }

        // 查看是否需要拉取数据
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof ITabInitData) {
            ((ITabInitData) fragment).onTabRequestData();
        }
    }

    @Override
    public void onPageScrollStateChanged(int position) {

    }

    public String makeFragmentName(int position) {
        return mItems.get(position).getTitle();
    }

    public List<T> getTabItems() {
        return mItems;
    }

    public PagerAdapter getPageAdapter() {
        return mInnerAdapter;
    }

    // 是否保留最后阅读的标签
    protected String configLastPositionKey() {
        return null;
    }

    /**
     * 标签页，标签不能重复
     *
     * @return
     */
    abstract protected ArrayList<T> generateTabs();

    abstract protected Fragment newFragment(T bean);

    // 延迟一点初始化tabs，用于在首页切换菜单的时候，太多的tab页导致有点点卡顿
    protected int delayTabInit() {
        return 0;
    }

    @Override
    public void onDestroy() {
        try {
            if (!isRetainFragments) {
                destoryFragments();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    public Fragment getCurrentFragment() {
        if (mViewPager == null || mInnerAdapter == null || mInnerAdapter.getCount() < mCurrentPosition)
            return null;

        return fragments.get(makeFragmentName(mCurrentPosition));
    }

    public Fragment getFragment(String tabTitle) {
        if (fragments == null || TextUtils.isEmpty(tabTitle))
            return null;

        for (int i = 0; i < mItems.size(); i++) {
            if (tabTitle.equals(mItems.get(i).getTitle())) {
                return fragments.get(makeFragmentName(i));
            }
        }

        return null;
    }

    public Fragment getFragment(int index) {
        if (mItems.size() > index) {
            return fragments.get(makeFragmentName(index));
        }

        return null;
    }

    public Map<String, Fragment> getFragments() {
        return fragments;
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    class InnerAdapter extends FragmentPagerAdapter {

        public InnerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = fragments.get(makeFragmentName(position));
            if (fragment == null) {
                fragment = newFragment(mItems.get(position));

                fragments.put(makeFragmentName(position), fragment);
            }

            return fragment;
        }

        @Override
        protected void freshUI(int position, Fragment fragment) {
            super.freshUI(position, fragment);

            if (!fragments.containsValue(fragment)) {
                fragments.put(makeFragmentName(position), fragment);
            }
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mItems.get(position).getTitle();
        }

        @Override
        protected String makeFragmentName(int position) {
            return ATabsFragment.this.makeFragmentName(position);
        }

    }

    // 这个接口用于多页面时，只有当前的页面才加载数据，其他不显示的页面暂缓加载
    // 当每次onPagerSelected的时候，再调用这个接口初始化数据
    public interface ITabInitData {

        void onTabRequestData();

    }

}
