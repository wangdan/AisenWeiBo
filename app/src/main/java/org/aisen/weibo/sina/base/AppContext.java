package org.aisen.weibo.sina.base;

import com.umeng.analytics.MobclickAgent;

import org.aisen.weibo.sina.service.OfflineService;
import org.aisen.weibo.sina.service.UnreadService;
import org.aisen.weibo.sina.sinasdk.bean.UnreadCount;
import org.aisen.weibo.sina.support.action.DoLikeAction;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.utils.AccountUtils;

/**
 * Created by wangdan on 15/12/13.
 */
public class AppContext {

    private static AccountBean mAccount;

    public static boolean isLoggedIn() {
        return mAccount != null && mAccount.getAccessToken() != null;
    }

    public static void logout() {
        mAccount = null;
    }

    public static void login(AccountBean accountBean) {
        AppContext.setAccount(accountBean);

        // 未读消息重置
        if (AppContext.getAccount().getUnreadCount() == null) {
            AppContext.getAccount().setUnreadCount(UnreadService.getUnreadCount());
        }
        if (AppContext.getAccount().getUnreadCount() == null) {
            AppContext.getAccount().setUnreadCount(new UnreadCount());
        }
        // 开启未读服务
        UnreadService.startService();

        // 刷新定时任务
        MyApplication.refreshPublishAlarm();

        // 处理点赞数据
        DoLikeAction.refreshLikeCache();

        // 停止离线服务
        if (OfflineService.getInstance() != null)
            OfflineService.stopOffline();

        // 设置友盟的账号统计
        MobclickAgent.onProfileSignIn(accountBean.getUid());
    }

    public static void setAccount(AccountBean account) {
        mAccount = account;
    }

    public static AccountBean getAccount() {
        return mAccount;
    }

    public static void clearCookie() {
        mAccount.setCookie(null);

        AccountUtils.updateAccount(mAccount);
        AccountUtils.setLogedinAccount(mAccount);
    }

}
