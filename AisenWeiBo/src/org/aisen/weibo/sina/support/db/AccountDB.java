package org.aisen.weibo.sina.support.db;

import java.util.List;

import org.aisen.weibo.sina.support.bean.AccountBean;

import com.m.support.sqlite.property.Extra;
import com.m.support.sqlite.util.FieldUtils;

public class AccountDB {

	public static void setLogedinAccount(AccountBean bean) {
		// 删除之前登录的账户
		String whereClause = String.format(" %s = ? ", FieldUtils.KEY);
		String[] whereArgs = new String[]{ "Logedin" };
		SinaDB.getSqlite().delete(AccountBean.class, whereClause, whereArgs);
		
		// 设置当前登录账户
		SinaDB.getSqlite().insert(new Extra(null, null, "Logedin"), bean);
	}
	
	public static AccountBean getLogedinAccount() {
		String selection = String.format(" %s = ? ", FieldUtils.KEY);
		String[] selectionArgs = new String[]{ "Logedin" };
		
		List<AccountBean> beans = SinaDB.getSqlite().selectAll(AccountBean.class, selection, selectionArgs);
		if (beans.size() > 0)
			return beans.get(0);
		
		return null;
	}
	
	public static void newAccount(AccountBean bean) {
		SinaDB.getSqlite().insert(null, bean);
	}
	
	public static List<AccountBean> query() {
		String selection = String.format(" %s is null ", FieldUtils.KEY);
		String[] selectionArgs = null;
		
		return SinaDB.getSqlite().selectAll(AccountBean.class, selection, selectionArgs);
	}
	
	public static void remove(String id) {
		SinaDB.getSqlite().deleteAll(new Extra(id), AccountBean.class);
	}
	
}
