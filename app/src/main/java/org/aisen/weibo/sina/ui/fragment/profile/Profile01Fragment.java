package org.aisen.weibo.sina.ui.fragment.profile;

import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;

/**
 * Created by wangdan on 16/1/12.
 */
public class Profile01Fragment extends ABaseFragment {

    public static Profile01Fragment newInstance() {
        return new Profile01Fragment();
    }

    @Override
    protected int inflateContentView() {
        return R.layout.ui_profile_tab1;
    }

}
