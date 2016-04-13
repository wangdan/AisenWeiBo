package org.aisen.weibo.sina.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

	public static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null)
			return;
		if (ACTION_BOOT.equals(intent.getAction())) {

		}		
	}

}
