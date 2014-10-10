package org.aisen.weibo.sina.support.utils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.db.EmotionsDB;
import org.android.loader.BitmapLoader;
import org.android.loader.core.MyBitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.widget.TextView;

import com.m.common.context.GlobalContext;
import com.m.common.utils.BitmapUtil;
import com.m.common.utils.BitmapUtil.BitmapType;
import com.m.common.utils.Logger;
import com.m.support.highlighttext.MyURLSpan;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.activity.BaseActivity;

public class LinkUtil {

	private static final String TAG = LinkUtil.class.getSimpleName();
	
	public static void addLinks(TextView textView) {
//		if (true) return;
		
//		MyLinkify.TransformFilter mentionFilter = new MyLinkify.TransformFilter() {
//
//			@Override
//			public String transformUrl(Matcher match, String url) {
//				return match.group(1);
//			}
//		};

		SpannableString value = SpannableString.valueOf(textView.getText());

		Pattern pattern = Pattern.compile("@([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)");
		String scheme = "org.aisen.weibo.sina.userinfo://";
		Linkify.addLinks(value, pattern, scheme);
//		MyLinkify.addLinks(textView, pattern, scheme, null, mentionFilter);
//
//		MyLinkify.addLinks(textView, MyLinkify.WEB_URLS);
		Linkify.addLinks(value, Pattern.compile("http://[a-zA-Z0-9+&@#/%?=~_\\-|!:,\\.;]*[a-zA-Z0-9+&@#/%=~_|]"), "http://");

		Pattern dd = Pattern.compile("#([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)#");
		scheme = "org.aisen.weibo.sina.topics://";
		Linkify.addLinks(value, dd, scheme);
//		MyLinkify.addLinks(textView, dd, scheme, null, mentionFilter);

		URLSpan[] urlSpans = value.getSpans(0, value.length(), URLSpan.class);
		MyURLSpan weiboSpan = null;
		for (URLSpan urlSpan : urlSpans) {
			weiboSpan = new MyURLSpan(urlSpan.getURL());
			int start = value.getSpanStart(urlSpan);
			int end = value.getSpanEnd(urlSpan);
			value.removeSpan(urlSpan);
			value.setSpan(weiboSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		textView.setText(value);
	}

	public static void addEmotions(TextView textView) {
//		if (true) return;
		
		SpannableString value = SpannableString.valueOf(textView.getText());
		Matcher localMatcher = Pattern.compile("\\[(\\S+?)\\]").matcher(value);
		while (localMatcher.find()) {
			String key = localMatcher.group(0);

			int k = localMatcher.start();
			int m = localMatcher.end();

			byte[] data = EmotionsDB.getEmotion(key);
			if (data == null)
				continue;
			
			Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
			int size = BaseActivity.getRunningActivity().getResources().getDimensionPixelSize(R.dimen.emotion_size);
			b = BitmapUtil.zoomBitmap(b, size);
			ImageSpan l = new ImageSpan(GlobalContext.getInstance(), b, ImageSpan.ALIGN_BASELINE);
			value.setSpan(l, k, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			textView.setText(value);
		}
	}

	private static HashMap<String, Boolean> loadingEmotions = new HashMap<String, Boolean>();

	static class LoadEmotionTask extends WorkTask<Void, Void, Bitmap> {

		private final SpannableString value;
		private final TextView textView;
		private final String key;
		private final CharSequence text;
		private final int k;
		private final int m;

		public LoadEmotionTask(String key, TextView textView, SpannableString value, int k, int m) {
			this.key = key;
			this.textView = textView;
			this.value = value;
			text = value.toString();
			loadingEmotions.put(key, true);
			this.k = k;
			this.m = m;
		}

		@Override
		public Bitmap workInBackground(Void... params) throws TaskException {
			byte[] data = EmotionsDB.getEmotion(key);
			if (data != null) {
				Logger.v(TAG, String.format("load emotion--->%s", key));
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				int size = BaseActivity.getRunningActivity().getResources().getDimensionPixelSize(R.dimen.emotion_size);
				bitmap = BitmapUtil.zoomBitmap(bitmap, size);
				if (bitmap != null)
					return bitmap;
			} else {
				Logger.v(TAG, String.format("emotion is not exist--->%s", key));
			}

			return null;
		}

		@Override
		protected void onSuccess(Bitmap bitmap) {
			super.onSuccess(bitmap);
			BitmapLoader.getInstance().getImageCache().addBitmapToMemCache(key, null, new MyBitmap(bitmap, BitmapType.jpg, key));

			if (textView.getText().toString().equals(text.toString())) {
				ImageSpan localImageSpan = new ImageSpan(GlobalContext.getInstance(), bitmap, ImageSpan.ALIGN_BASELINE);
				value.setSpan(localImageSpan, k, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}

		@Override
		protected void onFinished() {
			super.onFinished();
			loadingEmotions.remove(key);
		}
	}

}
