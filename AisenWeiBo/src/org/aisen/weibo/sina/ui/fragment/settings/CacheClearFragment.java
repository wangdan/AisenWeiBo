package org.aisen.weibo.sina.ui.fragment.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.m.common.context.GlobalContext;
import com.m.common.utils.DateUtils;
import com.m.common.utils.Logger;
import com.m.common.utils.SystemUtils;
import com.m.network.task.TaskException;
import com.m.network.task.WorkTask;
import com.m.ui.fragment.ABaseFragment;

import java.io.File;
import java.text.DecimalFormat;

/**
 * 清理缓存
 * 
 * @author wangdan
 *
 */
public class CacheClearFragment extends ABaseFragment implements OnPreferenceClickListener {

	private static final int RETAIN_TIME = 2 * 24 * 60 * 60 * 1000;
	private Preference clearCachePref;
	private ProgressDialog mProgressDialog;
	private long cacheSize = 0;
	private String cachePath;
	
	@Override
	protected int inflateContentView() {
		return 0;
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
			new AlertDialog.Builder(getActivity()).setTitle("清理建议")
								.setMessage("【确定】将清理掉SD卡中所有的图片缓存，建议保留近期缓存节省流量开销")
								.setNegativeButton("取消", null)
								.setNeutralButton("确定", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										clearCache(true);
									}
								})
								.setPositiveButton("保留最新", new DialogInterface.OnClickListener() {
									
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
						if (clear && file.delete())
							SystemUtils.scanPhoto(file);
					}
				}
				
			}
			
			@Override
			protected void onProgressUpdate(String... values) {
				super.onProgressUpdate(values);
				
				if (values != null && values.length > 0) {
					int value = Integer.parseInt(values[0]);
					
					mProgressDialog.incrementProgressBy(value / 1024);
				}
			}
			
			protected void onFinished() {
				if (mProgressDialog != null && mProgressDialog.isShowing())
					mProgressDialog.dismiss();
			};
			
		}.execute();
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setIconAttribute(android.R.attr.alertDialogIcon);
        mProgressDialog.setTitle("缓存清理中");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(((int) (cacheSize / 1024)));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface arg0) {
				task.cancel(true);
				
				calculateCacheFileSize();
			}
		});
        mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE ,"取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				task.cancel(true);
			}
        });
        mProgressDialog.show();
	}
	
	private void calculateCacheFileSize() {
		cacheSize = 0;

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
				}
			}

		}.execute();
	}
	
	public static void clearCompress() {
		new WorkTask<Void, Void, Void>() {

			@Override
			public Void workInBackground(Void... params) throws TaskException {
				String path = GlobalContext.getInstance().getImagePath() + File.separator + "compression";
				
				File cacheRootFile = new File(path);
				
				deleteFile(cacheRootFile);
				
				return null;
			}
			
			void deleteFile(File file) {
				if (!isCancelled()) {	
					if (file.isDirectory()) {
						File[] childFiles = file.listFiles();
						for (File childFile : childFiles)
							deleteFile(childFile);
					} else {
						if (file.delete())
							SystemUtils.scanPhoto(file);
					}
				}
				
			}
			
		}.execute();
	}

}
