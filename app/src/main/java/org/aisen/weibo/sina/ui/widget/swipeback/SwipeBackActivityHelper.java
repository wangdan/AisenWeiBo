package org.aisen.weibo.sina.ui.widget.swipeback;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

import org.aisen.weibo.sina.R;

/**
 * @author Yrom
 */
public class SwipeBackActivityHelper {
	private Activity mActivity;

	private SwipeBackLayout mSwipeBackLayout;

	public SwipeBackActivityHelper(Activity activity) {
		mActivity = activity;
	}

	@SuppressWarnings("deprecation")
	public void onActivityCreate() {
		mActivity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 这行代码在5.0会让主题失效
//		mActivity.getWindow().getDecorView().setBackgroundDrawable(null);
		mSwipeBackLayout = (SwipeBackLayout) LayoutInflater.from(mActivity).inflate(R.layout.swipeback_layout, null);
		mSwipeBackLayout.addSwipeListener(new SwipeBackLayout.SwipeListener() {
			@Override
			public void onScrollStateChange(int state, float scrollPercent) {
			}

			@Override
			public void onEdgeTouch(int edgeFlag) {
				Utils.convertActivityToTranslucent(mActivity);
			}

			@Override
			public void onScrollOverThreshold() {

			}
		});
	}

	public void onPostCreate() {
		mSwipeBackLayout.attachToActivity(mActivity);
	}

	public View findViewById(int id) {
		if (mSwipeBackLayout != null) {
			return mSwipeBackLayout.findViewById(id);
		}
		return null;
	}

	public SwipeBackLayout getSwipeBackLayout() {
		return mSwipeBackLayout;
	}
}
