package org.aisen.weibo.sina.support.db;

import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishBean.PublishStatus;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import com.alibaba.fastjson.JSON;
import org.aisen.orm.extra.Extra;
import org.aisen.orm.utils.FieldUtils;

public class PublishDB {

	public static void addPublish(PublishBean bean, WeiBoUser user) {
		Extra extra = new Extra(user.getIdstr(), null);
		SinaDB.getSqlite().insertOrReplace(extra, bean);
	}

	public static void deletePublish(PublishBean bean, WeiBoUser user) {
		Extra extra = new Extra(user.getIdstr(), null);
		SinaDB.getSqlite().deleteById(extra, PublishBean.class, bean.getId());
	}

	public static void updatePublish(PublishBean bean, WeiBoUser user) {
		Extra extra = new Extra(user.getIdstr(), null);
		SinaDB.getSqlite().insertOrReplace(extra, bean);
	}

	public static ArrayList<PublishBean> getPublishList(WeiBoUser user) {
		String selection = String.format(" %s = ? and %s != ? and %s != ? and %s != ? ", FieldUtils.OWNER, "status", "status", "status");
		String[] selectionArgs = { user.getIdstr(), JSON.toJSONString(PublishStatus.create), JSON.toJSONString(PublishStatus.sending), JSON.toJSONString(PublishStatus.waiting) };
		return (ArrayList<PublishBean>) SinaDB.getSqlite().select(PublishBean.class, selection, selectionArgs, null, null, FieldUtils.CREATEAT + " desc ", null);
	}

	/**
	 * 获取添加状态的发布消息
	 * 
	 * @param user
	 * @return
	 */
	public static List<PublishBean> getPublishOfAddStatus(WeiBoUser user) {
		try {
			String selection = String.format(" %s = ? and %s = ? ", FieldUtils.OWNER, "status");
			String[] selectionArgs = { user.getIdstr(), PublishStatus.create.toString() };

			return SinaDB.getSqlite().select(PublishBean.class, selection, selectionArgs);
		} catch (Exception e) {
		}

		return new ArrayList<PublishBean>();
	}
	
	/**
	 * 定时任务
	 * 
	 * @param user
	 * @return
	 */
	public static List<PublishBean> getTimingPublishStatus(WeiBoUser user) {
		try {
			String selection = String.format(" %s = ? and %s = ? and %s > ? ", FieldUtils.OWNER, "status", "timing");
			String[] selectionArgs = { user.getIdstr(), PublishStatus.draft.toString(), "0" };

			return SinaDB.getSqlite().select(PublishBean.class, selection, selectionArgs);
		} catch (Exception e) {
		}

		return new ArrayList<PublishBean>();
	}
	
}
