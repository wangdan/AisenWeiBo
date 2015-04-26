package org.aisen.weibo.sina.ui.activity.basic;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;

import android.content.Intent;
import android.os.Bundle;

import com.m.ui.activity.basic.BaseActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 欢迎页
 */
public class SplashActivity extends BaseActivity implements AisenActivityHelper.EnableSwipeback {
	
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
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
               toMain();
            }

        }, 1500);
    }

    private void toMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void toAccount() {
        AccountFragment.launch(this);
        finish();
    }

    @Override
    public boolean canSwipe() {
        return false;
    }

}
