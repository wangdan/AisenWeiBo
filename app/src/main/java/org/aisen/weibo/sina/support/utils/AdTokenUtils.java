package org.aisen.weibo.sina.support.utils;

import com.alibaba.fastjson.JSON;
import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.FileUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;

import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.biz.BaseBizlogic;
import org.aisen.weibo.sina.support.biz.GithubResourceDownloadHttpUtility;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.sinasdk.bean.AccessToken;

import java.io.File;

/**
 * Created by wangdan on 15/5/1.
 */
public class AdTokenUtils {

    public static void load() {
        new LoadAdTokenTask(true).execute();
    }

    public static void loadIfExpired() {
        AccessToken token = AppContext.getAdvancedToken();
        if (token == null || token.isExpired()) {
            new LoadAdTokenTask(false).execute();
        }
    }

    static class LoadAdTokenTask extends WorkTask<Boolean, Void, Boolean> {

        String path;
        boolean showMsg;

        LoadAdTokenTask(boolean showMsg) {

            this.showMsg = showMsg;
            path = GlobalContext.getInstance().getDataPath();
        }

        @Override
        protected void onPrepare() {
            super.onPrepare();

            if (showMsg)
                ViewUtils.showMessage("开始设置");
        }

        @Override
        public Boolean workInBackground(Boolean... params) throws TaskException {
            return BaseBizlogic.newInstance().githubResDownload("ad_token.json", path);
        }

        @Override
        protected void onSuccess(Boolean aBoolean) {
            super.onSuccess(aBoolean);

            if (aBoolean) {
                try {
                    String json = new String(FileUtils.readFileToBytes(new File(path + "ad_token.json")), "utf-8");
                    Logger.d(GithubResourceDownloadHttpUtility.TAG, json + "");
                    AccessToken token = JSON.parseObject(json, AccessToken.class);
                    token.setAppKey(SettingUtility.getStringSetting("weico_key"));
                    token.setAppScreet(SettingUtility.getStringSetting("weico_screet"));
                    AppContext.setAdvancedToken(token);

                    SinaDB.getSqlite().deleteAll(null, AccessToken.class);
                    SinaDB.getSqlite().insert(null, token);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (showMsg)
                ViewUtils.showMessage(aBoolean ? "成功设置" : "设置失败");
        }

    }

}
