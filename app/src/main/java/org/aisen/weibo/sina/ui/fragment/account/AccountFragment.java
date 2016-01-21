package org.aisen.weibo.sina.ui.fragment.account;

import android.app.Activity;

import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;

/**
 * Created by wangdan on 16/1/21.
 */
public class AccountFragment extends ABaseFragment {

    public static final String TAG = "Account";

    public static void launch(Activity from) {
        SinaCommonActivity.launch(from, AccountFragment.class, null);
    }

    @Override
    public int inflateContentView() {
        return 0;
    }

}
