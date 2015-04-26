package org.aisen.weibo.sina.ui.fragment.profile;

import com.m.ui.fragment.ABaseFragment;

import org.sina.android.bean.WeiBoUser;

/**
 * Created by wangdan on 15/4/15.
 */
public class PhotosFragment extends ABaseFragment {

    public static ABaseFragment newInstance(WeiBoUser user) {
        return new PhotosFragment();
    }

    @Override
    protected int inflateContentView() {
        return 0;
    }
}
