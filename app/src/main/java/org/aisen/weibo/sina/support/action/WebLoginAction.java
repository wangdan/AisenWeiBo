package org.aisen.weibo.sina.support.action;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.TextUtils;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.aisen.android.support.action.IAction;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;

import java.util.Random;

/**
 * Created by wangdan on 15/5/1.
 */
public class WebLoginAction extends IAction {

    BizFragment mBizFragment;

    int requestCode;

    public WebLoginAction(Activity context, BizFragment bizFragment) {
        super(context, null);

        requestCode = new Random().nextInt(1000);
        mBizFragment = bizFragment;
    }

    @Override
    protected boolean interrupt() {
        if (TextUtils.isEmpty(AppContext.getAccount().getCookie())) {
            doInterrupt();

            return true;
        }

        return false;
    }

    @Override
    public void doInterrupt() {
        new AlertDialogWrapper.Builder(mBizFragment.getActivity()).setMessage(R.string.acount_timeout)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBizFragment.requestWebLogin(WebLoginAction.this);
                    }

                })
                .show();
    }

    @Override
    public void doAction() {
        if (getChild() != null) {
            getChild().run();
        }
    }
    
    public int getRequestCode() {
        return requestCode;
    }

}
