package org.aisen.weibo.sina.ui.activity.base;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.support.action.IAction;
import org.aisen.android.support.permissions.APermissionsAction;
import org.aisen.android.support.permissions.DefPermissionsSubject;
import org.aisen.android.support.permissions.IPermissionsObserver;
import org.aisen.android.support.permissions.IPermissionsSubject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.utils.AccountUtils;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;
import org.aisen.weibo.sina.ui.fragment.account.WebLoginFragment;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;

/**
 * Created by wangdan on 15/12/13.
 */
public class SplashActivity extends BaseActivity implements IPermissionsSubject {

    private DefPermissionsSubject defPermissionsSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_splash);

        defPermissionsSubject = new DefPermissionsSubject();

        APermissionsAction permissionsAction = new APermissionsAction(this, null, this, Manifest.permission.WRITE_EXTERNAL_STORAGE) {

            @Override
            protected boolean handlePermissionNone() {
                Logger.d(APermissionsAction.TAG, "没有权限");

                return super.handlePermissionNone();
            }

            @Override
            protected void onPermissionDenied(boolean alwaysDenied) {
                Logger.d(APermissionsAction.TAG, "权限被拒绝了 : " + alwaysDenied);
            }

        };
        new IAction(this, permissionsAction) {

            @Override
            public void doAction() {
                Logger.d(APermissionsAction.TAG, "权限通过， doit");
            }

        }.run();

        if (true) return;

        if (AppContext.isLoggedIn()) {

            toMain(750);
        }
        else {
            toLogin();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacks(toMainRunnable);
    }

    private Handler mHandler = new Handler() {

    };

    private Runnable toMainRunnable = new Runnable() {

        @Override
        public void run() {
            MainActivity.login();
            if (AppContext.isLoggedIn()) {
                MainActivity.runCheckAccountTask(AppContext.getAccount());
            }

            finish();
        }

    };

    private void toMain(int delay) {
        mHandler.postDelayed(toMainRunnable, delay);
    }

    private void toLogin() {
        if (AccountUtils.queryAccount().size() == 0) {
            WebLoginFragment.launch(this, WebLoginFragment.Client.aisen, null, null, BizFragment.REQUEST_CODE_AUTH);
        }
        else {
            AccountFragment.launch(this);

            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BizFragment.REQUEST_CODE_AUTH) {
            if (resultCode == Activity.RESULT_CANCELED) {
                new MaterialDialog.Builder(this)
                        .forceStacking(true)
                        .content(R.string.login_faild_remind)
                        .positiveText(R.string.login_ok)
                        .negativeText(R.string.login_exit)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {

                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                toLogin();
                            }

                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {

                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                finish();
                            }

                        })
                        .show();
            }
            else if (resultCode == Activity.RESULT_OK) {
                AccountBean accountBean = (AccountBean) data.getSerializableExtra("account");

                AccountUtils.newAccount(accountBean);
                AccountUtils.setLogedinAccount(accountBean);
                AppContext.setAccount(accountBean);

                toMain(0);
            }
        }
    }

    @Override
    public void attach(IPermissionsObserver observer) {
        defPermissionsSubject.attach(observer);
    }

    @Override
    public void detach(IPermissionsObserver observer) {
        defPermissionsSubject.detach(observer);
    }

    @Override
    public void notifyActivityResult(int requestCode, String[] permissions, int[] grantResults) {
        defPermissionsSubject.notifyActivityResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        notifyActivityResult(requestCode, permissions, grantResults);
    }

}
