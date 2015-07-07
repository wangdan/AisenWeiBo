package org.aisen.weibo.sina.base;

import org.aisen.weibo.sina.support.action.DoLikeAction;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.sys.service.OfflineService;
import org.aisen.weibo.sina.sys.service.UnreadService;
import org.aisen.weibo.sina.sinasdk.bean.AccessToken;
import org.aisen.weibo.sina.sinasdk.bean.Groups;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.UnreadCount;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import java.util.List;

/**
 * Created by wangdan on 15/4/12.
 */
public class AppContext {

    private static AccountBean accountBean;// 当前登录的用户信息

    private static UnreadCount unreadCount;

    private static AccessToken advancedToken;

    public static boolean isLogedin() {
        return accountBean != null;
    }

    /**
     * 刷新登录用户的用户，分组信息
     *
     * @param user
     * @param groups
     */
    public static void refresh(WeiBoUser user, Groups groups) {
        AppContext.accountBean.setUser(user);
        AppContext.accountBean.setGroups(groups);

        // 刷新DB的信息
        AccountDB.newAccount(AppContext.accountBean);
    }

    public static void login(AccountBean accountBean) {
        boolean startUnreadService = AppContext.accountBean == null ||
                !AppContext.accountBean.getUser().getIdstr().equals(accountBean.getUser().getIdstr());

        AppContext.accountBean = accountBean;

        // 未读消息重置
        if (AppContext.getUnreadCount() == null || startUnreadService) {
            AppContext.unreadCount = UnreadService.getUnreadCount();
        }
        if (AppContext.unreadCount == null)
            AppContext.unreadCount = new UnreadCount();

        // 开启未读服务
        if (startUnreadService)
            UnreadService.startService();

        // 检查更新变化
//        CheckChangedUtils.check(AppContext.getUser(), AppContext.getToken());

        // 刷新定时任务
        MyApplication.refreshPublishAlarm();

        // 处理点赞数据
        DoLikeAction.refreshLikeCache();

        // 停止离线服务
        if (OfflineService.getInstance() != null)
            OfflineService.stopOffline();

        if (accountBean.getAdvancedToken() != null)
            AppContext.setAdvancedToken(accountBean.getAdvancedToken());
        else {
            // 读取高级token
            List<AccessToken> token = SinaDB.getSqlite().select(null, AccessToken.class);
            if (token.size() > 0)
                AppContext.setAdvancedToken(token.get(0));
        }

        MyApplication.setDebugAccount(accountBean);
    }

    public static void logout() {
        // 停止未读服务
        UnreadService.stopService();
        // 移除定时任务
        MyApplication.removeAllPublishAlarm();
        // 清理登录的账号
        AccountDB.setLogedinAccount(null);
        // 退出账号
        accountBean = null;
    }

    public static Token getToken() {
        if (!isLogedin())
            return null;

        return accountBean.getToken();
    }

    public static WeiBoUser getUser() {
        if (!isLogedin())
            return null;

        return accountBean.getUser();
    }

    public static Groups getGroups() {
        if (!isLogedin())
            return null;

        return accountBean.getGroups();
    }

    public static AccountBean getAccount() {
        return AppContext.accountBean;
    }

    public static void setUnreadCount(UnreadCount unreadCount) {
        AppContext.unreadCount = unreadCount;
    }

    public static UnreadCount getUnreadCount() {
        return AppContext.unreadCount;
    }

    public static AccessToken getAdvancedToken() {
        return advancedToken;
    }

    public static void setAdvancedToken(AccessToken token) {
        advancedToken = token;
    }

}
