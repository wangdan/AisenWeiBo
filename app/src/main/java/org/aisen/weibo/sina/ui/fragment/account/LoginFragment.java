package org.aisen.weibo.sina.ui.fragment.account;

import android.app.Activity;

import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;

/**
 * Created by wangdan on 15/12/21.
 */
public class LoginFragment extends ABaseFragment {

    public static void launch(Activity from) {
        SinaCommonActivity.launch(from, LoginFragment.class, null);
    }

    @Override
    protected int inflateContentView() {
        return R.layout.ui_login;
    }

}
