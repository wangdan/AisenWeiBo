package org.aisen.weibo.sina.support.db;

import com.m.component.sqlite.extra.Extra;
import com.m.component.sqlite.utils.FieldUtils;

import org.aisen.weibo.sina.support.bean.AccountBean;

import java.util.List;

public class AccountDB {

	public static void setLogedinAccount(AccountBean bean) {
		// 删除之前登录的账户
		String whereClause = String.format(" %s = ? ", FieldUtils.KEY);
		String[] whereArgs = new String[]{ "Logedin" };
		SinaDB.getSqlite().delete(AccountBean.class, whereClause, whereArgs);
		
		// 设置当前登录账户
		SinaDB.getSqlite().insert(new Extra(null, "Logedin"), bean);
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
		SinaDB.getSqlite().insert(null, bean);
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
