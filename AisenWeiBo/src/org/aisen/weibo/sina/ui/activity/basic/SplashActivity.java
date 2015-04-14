package org.aisen.weibo.sina.ui.activity.basic;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;

import android.os.Bundle;

import com.m.ui.activity.basic.BaseActivity;

/**
 * 欢迎页
 */
public class SplashActivity extends BaseActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.as_ui_splash);

        if (AppContext.isLogedin()) {
            delayToMain();
        }
        else {
            toAccount();
        }

	}

    private void check() {

    }

    private void delayToMain() {

    }

    private void toMain() {

    }

    private void toAccount() {
        AccountFragment.launch(this);
        finish();
    }

}
