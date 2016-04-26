package org.aisen.weibo.sina.ui.fragment.settings;

import android.content.DialogInterface;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.utils.DateUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.MyApplication;

import java.io.File;
import java.text.DecimalFormat;

/**
 * 清理缓存
 * 
 * @author wangdan
 *
 */
public class CacheClearFragment extends ABaseFragment implements OnPreferenceClickListener {

	private static final int RETAIN_TIME = 24 * 60 * 60 * 1000;
	private Preference clearCachePref;
//	private ProgressDialog mProgressDialog;
    private MaterialDialog materialDialog;
	private long cacheSize = 0;
	private int cacheCount = 0;
	private String cachePath;
	
	@Override
	public int inflateContentView() {
		return -1;
	}
	
	public void setPreference(Preference pref, String path) {
		this.clearCachePref = pref;
		this.cachePath = path;
		
		clearCachePref.setSummary("0 Kb");
		clearCachePref.setOnPreferenceClickListener(this);
		calculateCacheFileSize();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (clearCachePref.getKey().equals(preference.getKey())) {
			new AlertDialogWrapper.Builder(getActivity()).setTitle("清理建议")
								.setMessage("【确定】将清理掉SD卡中所有的图片缓存，建议保留近期缓存节省流量开销")
								.setNeutralButton("取消", null)
								.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        clearCache(true);
                                    }
                                })
								.setNegativeButton("保留最新", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        clearCache(false);
                                    }
                                })
								.show();
		}
		return true;
	}
	
	private void clearCache(final boolean all) {
		MobclickAgent.onEvent(getActivity(), all ? "clear_cache_all" : "clear_cache_outofdate");

		final WorkTask<Void, String, Void> task = new WorkTask<Void, String, Void>() {

			@Override
			public Void workInBackground(Void... params) throws TaskException {
				File cacheRootFile = new File(cachePath);
				
				deleteFile(cacheRootFile, all);
				
				return null;
			}
			
			void deleteFile(File file, boolean all) {
				if (!file.exists())
					return;
					
//				try {
//					Thread.sleep(10);
//				} catch (Exception e) {
//				}
				
				if (!isCancelled()) {	
					if (file.isDirectory()) {
						File[] childFiles = file.listFiles();
						for (File childFile : childFiles)
							deleteFile(childFile, all);
					} else {
						publishProgress(String.valueOf(file.length()));
						
						boolean clear = all;
						if (!clear) {
							Logger.v("ClearCache", String.format("文件最后修改时间是%s", DateUtils.formatDate(file.lastModified(), DateUtils.TYPE_01)));
							clear = System.currentTimeMillis() - file.lastModified() >= RETAIN_TIME;
							if (clear)
								Logger.v("ClearCache", "缓存超过2天，删除该缓存");
						}
						else {
							file.delete();
						}
//						if (clear && file.delete())
//							SystemUtils.scanPhoto(file);
					}
				}
				
			}
			
			@Override
			protected void onProgressUpdate(String... values) {
				super.onProgressUpdate(values);
				
				if (values != null && values.length > 0 && materialDialog != null && getActivity() != null) {
					int value = Integer.parseInt(values[0]);


//                    materialDialog.incrementProgress(value / 1024);
					materialDialog.incrementProgress(1);
//                    if (value * 1.0f / 1024 / 1024 > 1)
//                        materialDialog.setContent(String.format("%s M", new DecimalFormat("#.00").format(value * 1.0d / 1024 / 1024)));
//                    else
//                        materialDialog.setContent(String.format("%d Kb", value / 1024));
				}
			}
			
			protected void onFinished() {
                if (materialDialog != null && materialDialog.isShowing())
                    materialDialog.dismiss();
			};
			
		}.execute();

        materialDialog = new MaterialDialog.Builder(getActivity())
                                    .title(R.string.settings_cache_clear)
//                                    .content(R.string.please_wait)
                                    .contentGravity(GravityEnum.CENTER)
                                    .dismissListener(new DialogInterface.OnDismissListener() {

										@Override
										public void onDismiss(DialogInterface dialog) {
											task.cancel(true);

											calculateCacheFileSize();
										}

									})
                                    .positiveText(R.string.cancel)
//                                    .progress(false, ((int) (cacheSize / 1024)), true)
									.progress(false, cacheCount, true)
									.show();
	}
	
	private void calculateCacheFileSize() {
		cacheSize = 0;
		cacheCount = 0;

		new WorkTask<Void, Void, Void>() {

			@Override
			protected void onPrepare() {
				super.onPrepare();
				
				clearCachePref.setEnabled(false);
				clearCachePref.setSummary("正在计算...");
			};
			
			@Override
			public Void workInBackground(Void... params) throws TaskException {
				calculateFileSize(cachePath);
				
				return null;
			}
			
			@Override
			protected void onFinished() {
				super.onFinished();
				
				clearCachePref.setEnabled(true);
				
				if (cacheSize * 1.0f / 1024 / 1024 > 1)
					clearCachePref.setSummary(String.format("%s M", new DecimalFormat("#.00").format(cacheSize * 1.0d / 1024 / 1024)));
				else
					clearCachePref.setSummary(String.format("%d Kb", cacheSize / 1024));
			};

			void calculateFileSize(String path) {
				File file = new File(path);
				if (file.isDirectory()) {
					File[] childFiles = file.listFiles();
					for (File childFile : childFiles)
						calculateFileSize(childFile.getAbsolutePath());
				} else {
					cacheSize += file.length();

					cacheCount += 1;
				}
			}

		}.execute();
	}
	
	public static void clearCompress() {
		new WorkTask<Void, Void, Void>() {

			@Override
			public Void workInBackground(Void... params) throws TaskException {
				File cacheRootFile = new File(MyApplication.getImagePath());

				deleteFile(cacheRootFile, false);

				return null;
			}

			void deleteFile(File file, boolean all) {
				if (!file.exists())
					return;

				if (!isCancelled()) {
					if (file.isDirectory()) {
						File[] childFiles = file.listFiles();
						for (File childFile : childFiles)
							deleteFile(childFile, all);
					} else {
						boolean clear = all;
						if (!clear) {
							Logger.v("ClearCache", String.format("文件最后修改时间是%s", DateUtils.formatDate(file.lastModified(), DateUtils.TYPE_01)));
							clear = System.currentTimeMillis() - file.lastModified() >= RETAIN_TIME;
							if (clear) {
								Logger.v("ClearCache", "缓存超过1天，删除该缓存");
								file.delete();
							}
						}
						else {
							file.delete();
						}
					}
				}

			}
			
		}.execute();
	}

}
