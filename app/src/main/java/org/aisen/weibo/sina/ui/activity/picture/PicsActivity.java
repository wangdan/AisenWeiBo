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
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.adapter.FragmentPagerAdapter;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.fragment.picture.PictureFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_download_pic, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.downPics) {
			new WorkTask<Void, String, Void>() {

				@Override
				protected void onPrepare() {
					super.onPrepare();

					ViewUtils.createProgressDialog(PicsActivity.this, "开始准备下载图片...", ThemeUtils.getThemeColor()).show();
				}

				@Override
				public Void workInBackground(Void... voids) throws TaskException {
					if (mBean.getRetweeted_status() == null) {
						final PicUrls[] picUrlses = mBean.getPic_urls();
						if (picUrlses != null && picUrlses.length > 0) {
							publishProgress("[" + picUrlses.length + " / " + 0 + "]");

							final CountDownLatch countDownLatch = new CountDownLatch(picUrlses.length);
							for (PicUrls picUrlse : picUrlses) {
								try {
									final String image = picUrlse.getThumbnail_pic().replace("thumbnail", "large");

									final File file = new File(SystemUtils.getSdcardPath() + File.separator + "女神" + File.separator + mBean.getUser().getScreen_name() + File.separator + KeyGenerator.generateMD5(image) + ".jpg");

									if (!file.getParentFile().exists()) {
										file.getParentFile().mkdirs();
									}

									if (!file.exists()) {
										new Thread() {

											@Override
											public void run() {
												try {
													File tempFile = new File(file.getAbsolutePath() + ".temp");
													if (tempFile.exists()) {
														tempFile.delete();
													}
													FileOutputStream out = new FileOutputStream(file);
													Request request = new Request.Builder().url(image).build();
													Response response = GlobalContext.getOkHttpClient().newCall(request).execute();
													if (response.isSuccessful()) {
														InputStream in = response.body().byteStream();

														// 获取图片数据
														byte[] buffer = new byte[1024 * 8];
														int readLen = -1;
														while ((readLen = in.read(buffer)) != -1) {
															out.write(buffer, 0, readLen);
														}
														in.close();
														out.close();
													}
													tempFile.renameTo(file);

//													ImageConfig config = new ImageConfig();
//													config.setId("Large");
//													BitmapLoader.BitmapBytesAndFlag bitmapBytesAndFlag = BitmapLoader.getInstance().doDownload(image, config);
//													byte[] bytes = bitmapBytesAndFlag.bitmapBytes;
//
//													FileUtils.writeFile(file, bytes);
												} catch (Exception e) {
													e.printStackTrace();
												}

												countDownLatch.countDown();
												publishProgress("[" + picUrlses.length + " / " + (picUrlses.length - countDownLatch.getCount()) + "]");

												SystemUtils.scanPhoto(PicsActivity.this, file);
											}

										}.start();
									}
									else {
										countDownLatch.countDown();
									}
								} catch (Throwable e) {
									countDownLatch.countDown();
								}
							}
							try {
								countDownLatch.await(2 * 60 * 1000, TimeUnit.SECONDS);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}

					return null;
				}

				@Override
				protected void onProgressUpdate(String... values) {
					super.onProgressUpdate(values);

					if (values != null && values.length > 0) {
						ViewUtils.updateProgressDialog(values[0]);
					}
				}

				@Override
				protected void onFinished() {
					super.onFinished();

					ViewUtils.dismissProgressDialog();
				}

			}.execute();
		}

		return super.onOptionsItemSelected(item);
	}

}
