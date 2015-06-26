package org.aisen.weibo.sina.support.action;

import android.app.Activity;

import org.aisen.android.support.action.IAction;

import org.aisen.weibo.sina.ui.activity.basic.MainActivity;
import org.aisen.weibo.sina.ui.activity.profile.WeiboClientActivity;
import org.aisen.weibo.sina.ui.fragment.basic.BizFragment;

/**
 * Created by wangdan on 15/5/1.
 */
public class DMAction extends IAction {

    MainActivity mainActivity;

    public DMAction(Activity context) {
        super(context, new WebLoginAction(context, BizFragment.getBizFragment(context)));

        mainActivity = (MainActivity) context;
    }

    @Override
    public void doAction() {
        WeiboClientActivity.launchDM(mainActivity);
        mainActivity.closeDrawer();
    }

}
