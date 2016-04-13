package org.aisen.weibo.sina.ui.activity.picture;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ViewGroup;

import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.adapter.FragmentPagerAdapter;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.fragment.picture.PictureFragment;

public class PicsActivity extends BaseActivity implements OnPageChangeListener {

	public static void launch(Activity from, StatusContent bean, int index) {
		Intent intent  = new Intent(from, PicsActivity.class);
		intent.putExtra("bean", bean);
		intent.putExtra("index", index);
		from.startActivity(intent);
	}
	
	@ViewInject(id = R.id.viewPager)
	ViewPager viewPager;
    @ViewInject(id = R.id.layToolbar)
    ViewGroup layToolbar;
	
	private StatusContent mBean;
	private int index;

    MyViewPagerAdapter myViewPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_pictures);
		
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mBean = savedInstanceState == null ? (StatusContent) getIntent().getSerializableExtra("bean")
										   : (StatusContent) savedInstanceState.getSerializable("bean");
		index = savedInstanceState == null ? getIntent().getIntExtra("index", 0)
										: savedInstanceState.getInt("index", 0);

        myViewPagerAdapter = new MyViewPagerAdapter(getFragmentManager());
		viewPager.setAdapter(myViewPagerAdapter);
		viewPager.setOnPageChangeListener(this);
		viewPager.setCurrentItem(index);
		if (size() > 1 && getSupportActionBar() != null)
            getSupportActionBar().setTitle(String.format("%d/%d", index + 1, size()));
		else if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(String.format("%d/%d", 1, 1));

        getToolbar().setBackgroundColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= 19) {
            layToolbar.setPadding(layToolbar.getPaddingLeft(),
                                        layToolbar.getPaddingTop() + SystemUtils.getStatusBarHeight(this),
                                        layToolbar.getPaddingRight(),
                                        layToolbar.getPaddingBottom());
        }
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt("index", index);
		outState.putSerializable("bean", mBean);
	}
	
	private int size() {
		if (mBean.getRetweeted_status() != null) {
			return mBean.getPic_urls().length;
		}
		
		return mBean.getPic_urls().length;
	}
	
	private PicUrls getPicture(int index) {
		if (mBean.getRetweeted_status() != null) {
			return mBean.getPic_urls()[index];
		}
		
		return mBean.getPic_urls()[index];
	}

    public PicUrls getCurrent() {
        return getPicture(viewPager.getCurrentItem());
    }
	
	protected Fragment newFragment(int position) {
		return PictureFragment.newInstance(getPicture(position));
	}

	class MyViewPagerAdapter extends FragmentPagerAdapter {

		public MyViewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = getFragmentManager().findFragmentByTag(makeFragmentName(position));
			if (fragment == null) {
				fragment = newFragment(position);
			}
			
			return fragment;
		}

		@Override
		public int getCount() {
			return size();
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
			
			Fragment fragment = getFragmentManager().findFragmentByTag(makeFragmentName(position));
			if (fragment != null)
				mCurTransaction.remove(fragment);
		}

		@Override
		protected String makeFragmentName(int position) {
			return KeyGenerator.generateMD5(getPicture(position).getThumbnail_pic());
		}

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		
	}

	@Override
	public void onPageSelected(int index) {
		this.index = index;
		
        getSupportActionBar().setTitle(String.format("%d/%d", index + 1, size()));

        PictureFragment fragment = (PictureFragment) myViewPagerAdapter.getItem(index);
        if (fragment != null)
            fragment.onTabRequestData();
	}

	@Override
	protected int configTheme() {
		return R.style.AppTheme_Pics;
	}

	@Override
	public void onResume() {
		super.onResume();

		UMengUtil.onPageStart(this, "图片预览页");
	}

	@Override
	public void onPause() {
		super.onPause();

		UMengUtil.onPageEnd(this, "图片预览页");
	}

}
