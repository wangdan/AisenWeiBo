package org.aisen.android.support.textspan;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcel;
import android.provider.Browser;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;

import org.aisen.android.R;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.widget.MToast;

public class MyURLSpan extends ClickableSpan {

	private final String mURL;
	
	private int color;

	public MyURLSpan(String url) {
		mURL = url;
	}
	
	public MyURLSpan(Parcel src) {
		mURL = src.readString();
	}

	public int getSpanTypeId() {
		return 11;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mURL);
	}

	public String getURL() {
		return mURL;
	}

	@Override
	public void onClick(View widget) {
		Logger.v(MyURLSpan.class.getSimpleName(), String.format("the link(%s) was clicked ", getURL()));

		Uri uri = Uri.parse(getURL());
		Context context = widget.getContext();
		if (uri.getScheme().startsWith("http")) {
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			intent.setData(uri);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
			context.startActivity(intent);
		}
	}

	public void onLongClick(View widget) {
		Uri data = Uri.parse(getURL());
		if (data != null) {
			String d = data.toString();
			String newValue = "";
			if (d.startsWith("org.aisen.android.ui")) {
				int index = d.lastIndexOf("/");
				newValue = d.substring(index + 1);
			} else if (d.startsWith("http")) {
				newValue = d;
			}
			if (!TextUtils.isEmpty(newValue)) {
				ClipboardManager cm = (ClipboardManager) widget.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
				cm.setPrimaryClip(ClipData.newPlainText("ui", newValue));

				MToast.showMessage(widget.getContext(), String.format(widget.getContext().getString(R.string.comm_hint_copied), newValue));
			}
		}
	}

	@Override
	public void updateDrawState(TextPaint tp) {
		if (color == 0) {
			int[] attrs = new int[] { R.attr.colorPrimary };
			Activity activity = BaseActivity.getRunningActivity();
			if (activity != null) {
				TypedArray ta = activity.obtainStyledAttributes(attrs);
				tp.setColor(ta.getColor(0, Color.BLUE));
			}

		}
		else {
			tp.setColor(color);
		}

//        tp.setUnderlineText(true);
	}
	
	public void setColor(int color) {
		this.color = color;
	}

}
