package org.aisen.weibo.sina.ui.widget;

import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppSettings;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class TraditionalTextView extends TextView {

	public TraditionalTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TraditionalTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TraditionalTextView(Context context) {
		super(context);
	}
	
	@Override
	public void setText(CharSequence text, BufferType type) {
		if (!TextUtils.isEmpty(text) && !text.equals(getText())) {
			if (AppSettings.isTraditional())
				text = AisenUtil.convertToTraditional(text.toString());
		}
		
		super.setText(text, type);
	}

}
