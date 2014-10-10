package org.aisen.weibo.sina.ui.widget;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.compress.TimelineThumbBitmapCompress;
import org.android.loader.BitmapLoader;
import org.android.loader.core.BitmapOwner;
import org.android.loader.core.ImageConfig;
import org.sina.android.bean.PicUrls;
import org.sina.android.bean.StatusContent;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class CommentPicsView extends ViewGroup implements BitmapOwner {

	private PicUrls[] picUrls;
	
	public CommentPicsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CommentPicsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CommentPicsView(Context context) {
		super(context);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		ImageView imageView = (ImageView) getChildAt(0);
		imageView.layout(0, 0, getWidth(), getHeight());
	}
	
	private void recyle() {
		for (int i = 0; i < getChildCount(); i++) {
			ImageView imgView = (ImageView) getChildAt(i);
			
			imgView.setImageDrawable(BitmapLoader.getLoadingDrawable(imgView));
		}
	}
	
	private void setTimelinePicsView() {
		ImageConfig config = new ImageConfig();
		config.setId("comments");
		config.setLoadfaildBitmapRes(R.drawable.bg_timeline_loading);
		config.setLoadingBitmapRes(R.drawable.bg_timeline_loading);
//		config.setMaxWidth(getWidth());
//		config.setMaxHeight(getHeight());
		config.setBitmapCompress(TimelineThumbBitmapCompress.class);
		
		ImageView imageView = (ImageView) getChildAt(0);
		BitmapLoader.getInstance().display(this, picUrls[0].getThumbnail_pic(), imageView, config);
	}
	
	public void setPics(StatusContent status) {
		// 测试
//		int size = ++loadCount % 9 + 1;
//		size = 9;
//		PicUrls p = new PicUrls();
//		p.setThumbnail_pic("http://ww2.sinaimg.cn/bmiddle/661b513cjw1eivtz9s3fcj20bd08ct96.jpg");
//		PicUrls[] tempPicUrls = new PicUrls[size];
//		for (int i = 0; i < tempPicUrls.length; i++) {
//			if (picUrls != null && picUrls.length > i)
//				tempPicUrls[i] = picUrls[i];
//			else
//				tempPicUrls[i] = p;
//		}
//		picUrls = tempPicUrls;
		
		if (status == null || status.getPic_urls() == null || status.getPic_urls().length == 0) {
			recyle();
			
			setVisibility(View.GONE);
		}
		else {
			setVisibility(View.VISIBLE);

			this.picUrls = status.getPic_urls();;
			
			setTimelinePicsView();
		}
	}

	@Override
	public boolean canDisplay() {
		return true;
	}

}
