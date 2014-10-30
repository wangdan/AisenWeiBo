package org.aisen.weibo.sina.ui.fragment.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.WallpaperBean;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.widget.WallpaperViewGroup;
import org.android.loader.BitmapLoader;
import org.android.loader.core.ImageConfig;
import org.android.loader.download.AssetsDownloader;
import org.android.loader.download.SdcardDownloader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;

import com.m.common.context.GlobalContext;
import com.m.common.utils.KeyGenerator;
import com.m.common.utils.SystemUtility;
import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.sqlite.util.FieldUtils;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.activity.BaseActivity;
import com.m.ui.fragment.AGridFragment;
import com.m.ui.utils.PhotoChoice;
import com.m.ui.utils.PhotoChoice.PhotoChoiceListener;
import com.m.ui.utils.PhotoChoice.PhotoChoiceMode;
import com.m.ui.utils.ViewUtils;

/**
 * 壁纸设置
 * 
 * @author Jeff.Wang
 *
 * @date 2014年10月19日
 */
public class WallpaperSettingsFragment extends AGridFragment<WallpaperBean, ArrayList<WallpaperBean>>
												implements OnItemClickListener, OnItemLongClickListener,  PhotoChoiceListener {

	public static void launch(Activity from) {
		FragmentContainerActivity.launch(from, WallpaperSettingsFragment.class, null);
	}
	
	private PhotoChoice photoChoice;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_wallpaper;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setDisplayShowHomeEnabled(false);
		getActivity().getActionBar().setTitle(R.string.title_wallpaper);
		
		getRefreshView().setOnItemClickListener(this);
		getRefreshView().setOnItemLongClickListener(this);
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AppContext.setWallpaper(getAdapterItems().get(position));
		
		BaseActivity activity = (BaseActivity) getActivity();
		WallpaperViewGroup wallpaper = (WallpaperViewGroup) activity.getRootView();
		wallpaper.setWallpaper();
		
		getActivity().invalidateOptionsMenu();
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		final WallpaperBean wallpaper = getAdapterItems().get(position);
		if ("10".equals(wallpaper.getType())) {
			new AlertDialog.Builder(getActivity()).setTitle(R.string.remind)
								.setMessage(R.string.settings_wallpaper_delete_remind)
								.setNegativeButton(R.string.no, null)
								.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										SinaDB.getSqlite().delete(null, wallpaper);
										
										requestData(RefreshMode.reset);
										
										showMessage(R.string.settings_wallpaper_delete_success);
									}
								})
								.show();
		}
		else {
			showMessage(R.string.settings_wallpaper_delete_error);
		}
		
		return true;
	}
	
	@Override
	public void onMovedToScrapHeap(View view) {
//		super.onMovedToScrapHeap(view);
	}
	
	@Override
	protected AbstractItemView<WallpaperBean> newItemView() {
		return new WallpaperItemView();
	}

	@Override
	protected void requestData(RefreshMode mode) {
		new WallpaperTask(RefreshMode.reset).execute();
	}
	
	class WallpaperTask extends PagingTask<Void, Void, ArrayList<WallpaperBean>> {

		public WallpaperTask(RefreshMode mode) {
			super("WallpaperTask", mode);
		}

		@Override
		protected List<WallpaperBean> parseResult(ArrayList<WallpaperBean> result) {
			return result;
		}

		@Override
		protected ArrayList<WallpaperBean> workInBackground(RefreshMode mode, String previousPage,
																String nextPage, Void... params) throws TaskException {
			// 将已经下载了的壁纸，更新到数据库
			File dir = new File(GlobalContext.getInstance().getFilesDir().getAbsolutePath());
			String[] fileNames = AisenUtil.wallpaperNames;
			for (String fileName : fileNames) {
//				if ("8_8.jpg".equals(fileName))
//					continue;
				
				File wallpaperFile = new File(dir + File.separator + fileName);
				if (wallpaperFile.exists()) {
					WallpaperBean bean = new WallpaperBean();
					bean.setBeanId(KeyGenerator.generateMD5(wallpaperFile.getAbsolutePath()));
					bean.setPath(wallpaperFile.getAbsolutePath());
					bean.setType("100");
					
					SinaDB.getSqlite().insert(null, bean, false);
				}
			}
			
			ArrayList<WallpaperBean> wallpaperList = new ArrayList<WallpaperBean>();
			
			// 添加默认壁纸
//			wallpaperList.add(AisenUtil.generaterDefaultWallpaper());
			
			// 排除设置的壁纸, 根据type排序[默认壁纸排在第一，自定义添加的排在中间，其他github的壁纸排在第三]
			String selection = String.format(" %s is null ", FieldUtils.KEY);
			List<WallpaperBean> dbWallpaperList = SinaDB.getSqlite().selectAll(WallpaperBean.class, selection, null, " type ", null);
			wallpaperList.addAll(dbWallpaperList);
			
			return wallpaperList;
		}
		
	}
	
	class WallpaperItemView extends AbstractItemView<WallpaperBean> {

		@ViewInject(id = R.id.imgWallpaper)
		ImageView img;
		
		@Override
		public int inflateViewId() {
			return R.layout.item_wallpaper;
		}

		@Override
		public void bindingData(View convertView, WallpaperBean wallpaper) {
			ImageConfig imageConfig = new ImageConfig();
			imageConfig.setLoadingBitmapRes(R.drawable.bg_timeline_loading);
			imageConfig.setLoadfaildBitmapRes(R.drawable.bg_timeline_loading);
			imageConfig.setMaxWidth(SystemUtility.getScreenWidth() / 4);
			
			if ("1".equals(wallpaper.getType())) {
				imageConfig.setDownloaderClass(AssetsDownloader.class);
				imageConfig.setId("def_wallpaper");
				
				BitmapLoader.getInstance().display(null, wallpaper.getPath(), img, imageConfig);
			}
			else if ("10".equals(wallpaper.getType())) {
				imageConfig.setId("custom_wallpaper");
				imageConfig.setDownloaderClass(SdcardDownloader.class);
				
				BitmapLoader.getInstance().display(null, wallpaper.getPath(), img, imageConfig);
			}
  			else if ("100".equals(wallpaper.getType())) {
				imageConfig.setId("github_def_wallpaper");
				imageConfig.setDownloaderClass(SdcardDownloader.class);
				
				BitmapLoader.getInstance().display(null, wallpaper.getPath(), img, imageConfig);
			}
		}
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.wallpaper, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		menu.findItem(R.id.disable).setVisible(AppContext.getWallpaper() != null);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// 取消壁纸设置
		if (item.getItemId() == R.id.disable) {
			AppContext.setWallpaper(null);
			
			BaseActivity activity = (BaseActivity) getActivity();
			WallpaperViewGroup wallpaper = (WallpaperViewGroup) activity.getRootView();
			wallpaper.setWallpaper();
			
			getActivity().invalidateOptionsMenu();
		}
		// 添加自定义壁纸
		else if (item.getItemId() == R.id.add) {
			if (photoChoice == null) {
				String albumPath = GlobalContext.getInstance().getCacheDir().getAbsolutePath();
				File albumFile = new File(albumPath);
				if (!albumFile.exists())
					albumFile.mkdirs();
				photoChoice = new PhotoChoice(getActivity(), WallpaperSettingsFragment.this, albumPath);
				photoChoice.setFileName(String.format("%s.jpg", String.valueOf(System.currentTimeMillis() / 1000)));
			}
			photoChoice.setMode(PhotoChoiceMode.uriType);
			photoChoice.start(WallpaperSettingsFragment.this, 0);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		BaiduAnalyzeUtils.onPageStart("自定义壁纸设置");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("自定义壁纸设置");
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (photoChoice != null)
			photoChoice.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void choiceByte(byte[] datas) {
	}

	@Override
	public void choiceBitmap(Bitmap bitmap) {
	}

	@Override
	public void choieUri(Uri uri, int request) {
		if (uri != null) {
			new WorkTask<String, Void, Boolean>() {

				@Override
				protected void onPrepare() {
					ViewUtils.createNormalProgressDialog(getActivity(), getString(R.string.settings_wallpaper_handle)).show();
				};
				
				@Override
				public Boolean workInBackground(String... params) throws TaskException {
					try {
						InputStream is = GlobalContext.getInstance().getContentResolver().openInputStream(Uri.parse(params[0]));
						
						String key = UUID.randomUUID().toString();
						String dirFile = GlobalContext.getInstance().getFilesDir().getAbsolutePath();
						File tmpFile = new File(dirFile + File.separator + key + ".jpg");
		                FileOutputStream out = new FileOutputStream(tmpFile);
		                int i;
		                byte bs[] = new byte[1024 * 8];
		                while ((i = is.read(bs)) > 0) {
		                    out.write(bs, 0, i);
		                }
		                out.flush();
		                out.close();
		                is.close();
		                
		                WallpaperBean bean = new WallpaperBean();
		                bean.setBeanId(key);
		                bean.setPath(tmpFile.getAbsolutePath());
		                bean.setType("10");
		                
		                SinaDB.getSqlite().insert(null, bean);
					} catch (Exception e) {
						throw new TaskException("", "");
					}
					
					return true;
				}
				
				@Override
				protected void onSuccess(Boolean result) {
					requestData(RefreshMode.reset);
				};

				@Override
				protected void onFailure(TaskException exception) {
					showMessage(R.string.settings_wallpaper_handle_error);
				};
				
				@Override
				protected void onFinished() {
					ViewUtils.dismissNormalProgressDialog();
				};
				
			}.execute(uri.toString());
		}
		else {
			showMessage(R.string.settings_wallpaper_error);
		}
	}

	@Override
	public void unChoice() {
	}
	
}
