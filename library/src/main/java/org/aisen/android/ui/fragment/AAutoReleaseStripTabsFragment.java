package org.aisen.android.ui.fragment;

import android.app.Fragment;
import android.os.Handler;
import android.view.View;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.bitmaploader.BitmapLoader;

import java.io.Serializable;

/**
 * 自动释放和刷新页面<br/>
 * 如果Tab页面的图片资源非常多，可以继承这个自动进行释放
 *
 * Created by wangdan on 15/4/22.
 */
public abstract class AAutoReleaseStripTabsFragment<T extends AStripTabsFragment.StripTabItem> extends AStripTabsFragment<T> {

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);

        // 释放资源
        mHandler.removeCallbacks(releaseFragmentRunnable);
        mHandler.postDelayed(releaseFragmentRunnable, Math.round(2.0f * 1000));
        // 刷新当前显示
        mHandler.removeCallbacks(refreshFragmentRunnable);
        mHandler.postDelayed(refreshFragmentRunnable, 700);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacks(refreshFragmentRunnable);
        mHandler.removeCallbacks(releaseFragmentRunnable);
    }

    Runnable refreshFragmentRunnable = new Runnable() {

        @Override
        public void run() {
            Fragment fragment = fragments.get(mViewPagerAdapter.makeFragmentName(mCurrentPosition));
            if (fragment != null && fragment instanceof ARefreshFragment) {
                Logger.w(String.format("刷新第%d个fragment的资源", mCurrentPosition));
                BitmapLoader.getInstance().clearCache();

                ((ARefreshFragment<Serializable, Serializable, View>) fragment).refreshUI();
            }
        }
    };

    Runnable releaseFragmentRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.w(String.format("准备释放第%d个fragment的资源", mCurrentPosition + 1));
            Logger.w(String.format("准备释放第%d个fragment的资源", mCurrentPosition - 1));
            releaseFragment(mCurrentPosition + 1);
            releaseFragment(mCurrentPosition - 1);
        }
    };

    public void releaseFragment(int position) {
        if (position < mItems.size() && position >= 0) {
            Fragment fragment = fragments.get(mViewPagerAdapter.makeFragmentName(position));
            if (fragment != null && fragment instanceof ARefreshFragment) {
                Logger.w(String.format("释放第%d个fragment的资源", position));

                ((ARefreshFragment<Serializable, Serializable, View>) fragment).releaseImageViewByIds();
            }
            else {
                Logger.e(String.format("释放的第%d个fragment不存在", position));
            }
        }
    }

    Handler mHandler = new Handler() {

    };

}
