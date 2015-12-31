package org.aisen.weibo.sina.ui.activity.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.ui.fragment.account.LoginFragment;

/**
 * Created by wangdan on 15/12/13.
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppContext.isLoggedIn()) {
            setContentView(R.layout.ui_splash);

            toMain();
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
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }

    };

    private void toMain() {
        mHandler.postDelayed(toMainRunnable, 1500);
    }

    private void toLogin() {
        LoginFragment.launch(this);
    }

}
