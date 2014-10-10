package org.aisen.weibo.sina.support.utils;

import java.util.Calendar;
import java.util.List;

import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.sina.android.SinaSDK;
import org.sina.android.bean.Groups;
import org.sina.android.bean.Token;
import org.sina.android.bean.TokenInfo;
import org.sina.android.bean.WeiBoUser;

import android.os.AsyncTask;

import com.m.common.utils.ActivityHelper;
import com.m.common.utils.DateUtils;
import com.m.common.utils.Logger;
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
	
}
