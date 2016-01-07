package org.aisen.weibo.sina.base;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.weibo.sina.support.sqlit.EmotionsDB;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.AccountUtils;

/**
 * Created by wangdan on 15/12/13.
 */
public class MyApplication extends GlobalContext {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化图片加载
        BitmapLoader.newInstance(this, getImagePath());

        SinaDB.setInitDB();
        // 检查表情
        try {
            EmotionsDB.checkEmotions();
        } catch (Exception e) {
        }

        AppContext.setAccount(AccountUtils.getLogedinAccount());
    }

}
