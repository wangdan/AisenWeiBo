package org.aisen.weibo.sina.support.utils;

import com.alibaba.fastjson.JSON;
import com.m.common.context.GlobalContext;
import com.m.common.utils.FileUtils;
import com.m.common.utils.ViewUtils;
import com.m.network.task.TaskException;
import com.m.network.task.WorkTask;

import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.bean.AdToken;
import org.aisen.weibo.sina.support.biz.BaseBizlogic;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.sina.android.bean.TokenInfo;

import java.io.File;

/**
 * Created by wangdan on 15/5/1.
 */
public class AdTokenUtils {

    public static void load(boolean showMsg) {
        AdToken token = AppContext.getAdvancedToken();
        if (token != null) {
            long validSecond = Long.parseLong(token.getCreate_at()) + Long.parseLong(token.getExpire_in());
            if (System.currentTimeMillis() > validSecond * 1000) {
                new LoadAdTokenTask().execute(showMsg);
            }
        } else {
            new LoadAdTokenTask().execute(showMsg);
        }
    }

    static class LoadAdTokenTask extends WorkTask<Boolean, Void, Boolean> {

        String path;

        LoadAdTokenTask() {

            path = GlobalContext.getInstance().getDataPath() + File.separator + "token.json";
        }

        @Override
        protected void onPrepare() {
            super.onPrepare();

            if (getParams()[0])
                ViewUtils.showMessage("开始获取高级token");
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
                    String json = FileUtils.readFileToString(new File(path));
                    AdToken token = JSON.parseObject(json, AdToken.class);
                    AppContext.setAdvancedToken(token);

                    SinaDB.getSqlite().deleteAll(null, TokenInfo.class);
                    SinaDB.getSqlite().insert(null, token);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (getParams()[0])
                ViewUtils.showMessage(aBoolean ? "成功获取高级token" : "获取高级token失败");
        }

    }

}
