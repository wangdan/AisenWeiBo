package org.aisen.weibo.sina.support.db;

import java.util.List;

import org.aisen.weibo.sina.support.bean.AccountBean;

import org.aisen.orm.extra.Extra;
import org.aisen.orm.utils.FieldUtils;

public class AccountDB {

	public static void setLogedinAccount(AccountBean bean) {
		// 删除之前登录的账户
		String whereClause = String.format(" %s = ? ", FieldUtils.KEY);
		String[] whereArgs = new String[]{ "Logedin" };
		SinaDB.getSqlite().delete(AccountBean.class, whereClause, whereArgs);

		if (bean != null) {
			// 设置当前登录账户
			SinaDB.getSqlite().insertOrReplace(new Extra(null, "Logedin"), bean);
		}
		else {
			SinaDB.getSqlite().delete(AccountBean.class, whereClause, whereArgs);
		}
	}
	
	public static AccountBean getLogedinAccount() {
		String selection = String.format(" %s = ? ", FieldUtils.KEY);
		String[] selectionArgs = new String[]{ "Logedin" };
		
		List<AccountBean> beans = SinaDB.getSqlite().select(AccountBean.class, selection, selectionArgs);
		if (beans.size() > 0)
			return beans.get(0);
		
		return null;
	}
	
	public static void newAccount(AccountBean bean) {
		SinaDB.getSqlite().insertOrReplace(null, bean);
	}
	
	public static List<AccountBean> query() {
		String selection = String.format(" %s = '' ", FieldUtils.KEY);
		String[] selectionArgs = null;
		
		return SinaDB.getSqlite().select(AccountBean.class, selection, selectionArgs);
	}
	
	public static void remove(String id) {
		SinaDB.getSqlite().deleteById(null, AccountBean.class, id);
	}
	
}
