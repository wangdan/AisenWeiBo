package org.aisen.weibo.sina.support.utils;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.biz.BizLogic;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.http.GithubResourceDownloadHttpUtility;
import org.sina.android.SinaSDK;
import org.sina.android.bean.Groups;
import org.sina.android.bean.Token;
import org.sina.android.bean.TokenInfo;
import org.sina.android.bean.WeiBoUser;

import android.os.AsyncTask;

import com.m.common.context.GlobalContext;
import com.m.common.utils.ActivityHelper;
import com.m.common.utils.DateUtils;
import com.m.common.utils.Logger;
import com.m.common.utils.SystemUtility;
import com.m.common.utils.SystemUtility.NetWorkType;
import com.m.support.bizlogic.ABaseBizlogic;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;

public class CheckChangedUtils {

	public static void check(WeiBoUser user, Token token) {
		checkLogedInUserInfo(user, token);
		
		checkAccountTokenInfo();
	}
	
	/**
	 * 检查分组信息、用户信息
	 */
	public static void checkLogedInUserInfo(final WeiBoUser user, final Token token) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					WeiBoUser userInfo = SinaSDK.getInstance(token).userShow(user.getIdstr(), null);
					
					if (AppContext.isLogedin() && AppContext.getUser().getIdstr().equals(userInfo.getIdstr())) {
						Groups groups = SinaSDK.getInstance(token).friendshipGroups();

						// 未登录或者登录用户切换了
						if (!AppContext.isLogedin() || !AppContext.getUser().getId().equals(user.getIdstr()))
							return null;
						
						try {
							// 刷新首页
							if (AppContext.getGroups() == null || 
									AppContext.getGroups().getLists().size() != groups.getLists().size()) {
								ActivityHelper.getInstance().putBooleanShareData("offlineChanneChanged", true);
							}
						} catch (Exception e) {
						}
						// 更新上下文
						AppContext.refresh(userInfo, groups);
						
						// 更新DB
						AccountBean bean = new AccountBean();
						bean.setUserId(user.getIdstr());
						bean.setGroups(groups);
						bean.setUser(userInfo);
						AccountDB.newAccount(bean);
					}
				} catch (Exception e) {
				}
				
				return null;
			}
			
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 	}
	
	static void checkAccountTokenInfo() {
		new GetTokenInfoTask().execute();
	}
	
	static class GetTokenInfoTask extends WorkTask<Void, Void, TokenInfo> {

		@Override
		public TokenInfo workInBackground(Void... params) throws TaskException {
			List<AccountBean> accountList = AccountDB.query();
			
			for (AccountBean account : accountList) {
				TokenInfo tokenInfo = null;
				try {
					tokenInfo = SinaSDK.getInstance(account.getToken()).getTokenInfo(account.get_token());
				} catch (TaskException e) {
					e.printStackTrace();
					if ("21327".equals(e.getErrorCode()) ||
							"21317".equals(e.getErrorCode())) {
						tokenInfo = new TokenInfo();
						tokenInfo.setCreate_at("0");
						tokenInfo.setExpire_in("0");
					}
				}
				
				if (tokenInfo != null) {
					account.setTokenInfo(tokenInfo);
					
					long validSecond = Long.parseLong(tokenInfo.getCreate_at()) + Long.parseLong(tokenInfo.getExpire_in());
					
					if (validSecond == 0) {
						Logger.w(ABaseBizlogic.TAG, account.getUser().getScreen_name() + "授权已失效");
					}
					else {
						Calendar cal = Calendar.getInstance();
						cal.setTimeInMillis(validSecond * 1000);
						Logger.w(ABaseBizlogic.TAG, account.getUser().getScreen_name() + DateUtils.formatDate(cal.getTimeInMillis(), DateUtils.TYPE_01));
					}
					
					AccountDB.newAccount(account);
				}
			}
			
			return null;
		}
		
	}
	
	private static CheckWallpaperTask checkWallpaperTask;
	
	public static void checkWallpaper() {
		if (ActivityHelper.getInstance().getBooleanShareData("DownloadWallpaper", true) && 
				checkWallpaperTask == null && 
				SystemUtility.getNetworkType() != NetWorkType.none)
			new CheckWallpaperTask().execute();
		else 
			Logger.d(GithubResourceDownloadHttpUtility.TAG, "壁纸都存在了");
	}
	
	static class CheckWallpaperTask extends WorkTask<Void, Void, Void> {

		CheckWallpaperTask() {
			checkWallpaperTask = this;
		}
		
		@Override
		public Void workInBackground(Void... params) throws TaskException {
			final String TAG = GithubResourceDownloadHttpUtility.TAG;
			
			String dirFile = GlobalContext.getInstance().getFilesDir().getAbsolutePath();
			int downloadCount = 0;
			
			for (String fileName : AisenUtil.wallpaperNames) {
				File file = new File(dirFile + File.separator + fileName);
				if (file.exists()) {
					Logger.d(TAG, String.format("壁纸 %s 已存在", file.getName()));
					
					downloadCount++;
				}
				else {
					if (BizLogic.newInstance().githubResDownload(fileName, dirFile)) {
						downloadCount++;
						
						Logger.d(TAG, "下载了壁纸 file = " + fileName);
					}
				}
			}
			
			Logger.d(TAG, "downloaded wallpaper count = " + downloadCount);
			if (downloadCount == 8) {
				ActivityHelper.getInstance().putBooleanShareData("DownloadWallpaper", false);
				
				Logger.d(TAG, "所有壁纸下载完成了");
			}
			else {
				Logger.d(TAG, "所有壁纸下载完成了一部分，还有一部分下次启动程序时检测下载");
			}
			
			return null;
		}
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			checkWallpaperTask = null;
		}
		
	}
	
}
