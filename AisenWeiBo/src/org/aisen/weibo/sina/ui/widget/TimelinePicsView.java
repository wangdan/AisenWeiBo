package org.aisen.weibo.sina.ui.widget;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.compress.Timeline9ggBitmapCompress;
import org.aisen.weibo.sina.support.compress.TimelineBitmapCompress;
import org.aisen.weibo.sina.support.compress.TimelineThumbBitmapCompress;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.android.loader.BitmapLoader;
import org.android.loader.core.BitmapOwner;
import org.android.loader.core.ImageConfig;
import org.sina.android.bean.PicUrls;
import org.sina.android.bean.StatusContent;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.m.common.utils.Logger;
import com.m.common.utils.SystemUtility;
import com.m.common.utils.SystemUtility.NetWorkType;
import com.m.ui.fragment.ABaseFragment;

/**
 * timeline的图片容器，根据图片个数动态布局ImageView
 * 
 * @author wangdan
 *
 */
public class TimelinePicsView extends ViewGroup implements BitmapOwner {

	public static final String TAG = TimelinePicsView.class.getSimpleName();

	private int mWidth;
	
	private int gap;
	
	private PicUrls[] picUrls;
	
	private Rect[] picRects;
	
	private StatusContent mStatusContent;
	
	private boolean large = true;
	
	private BizFragment bizFragment;
	private ABaseFragment ownerFragment;
	
	public TimelinePicsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init();
	}

	public TimelinePicsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init();
	}

	public TimelinePicsView(Context context) {
		super(context);
		
		init();
	}
	
	private void init() {
		mWidth = SystemUtility.getScreenWidth() - 2 * getResources().getDimensionPixelSize(R.dimen.horizontal_margin);
		gap = getResources().getDimensionPixelSize(R.dimen.gap_pics);
		
		Logger.v(TAG, String.format("screenWidth = %d", SystemUtility.getScreenWidth()));
		Logger.v(TAG, String.format("gap = %d, width = %d", gap, mWidth));
	}
	
	private void recyle() {
		for (int i = 0; i < getChildCount(); i++) {
			ImageView imgView = (ImageView) getChildAt(i);
			
			imgView.setImageDrawable(BitmapLoader.getLoadingDrawable(imgView));
		}
	}
	
	private void setMobileTimelinePicsView() {
		picRects = null;
		
		int size = picUrls.length;
		
		int imgW = Math.round((mWidth - 2 * gap) * 1.0f / 3.0f);
		int imgH = imgW;
		LinearLayout.LayoutParams layoutParams = null;
		
		// 4个特殊情况，上2个下2个
		if (size == 4) {
			
			layoutParams = new LinearLayout.LayoutParams(mWidth, imgH * 2 + gap);
			
			picRects = new Rect[4];
			
			Rect rect = new Rect(0, 0, imgW, imgH);
			picRects[0] = rect;
			rect = new Rect(imgW + gap, 0, imgW * 2 + gap, imgH);
			picRects[1] = rect;
			rect = new Rect(0, imgH + gap, imgW, imgH * 2 + gap);
			picRects[2] = rect;
			rect = new Rect(imgW + gap, imgH + gap, imgW * 2 + gap, imgH * 2 + gap);
			picRects[3] = rect;
		}
		else {
			int height = 0;
			switch (size) {
			case 1:
			case 2:
			case 3:
				height = imgH;
				break;
			case 5:
			case 6:
				height = imgH * 2 + gap;
				break;
			case 7:
			case 8:
			case 9:
				height = imgH * 3 + gap * 2;
				break;
			}
			layoutParams = new LinearLayout.LayoutParams(mWidth, height);

			picRects = getSmallRectArr(size);
		}
		
		setLayoutParams(layoutParams);
		
		displayPics();
		
		// 重新绘制
		requestLayout();
	}
	
	private Rect[] getSmallRectArr(int size) {
		int imgW = Math.round((mWidth - 2 * gap) * 1.0f / 3.0f);
		int imgH = imgW;
		
		Rect[] tempRects = new Rect[9];
		
		Rect rect = new Rect(0, 0, imgW, imgH);
		tempRects[0] = rect;
		rect = new Rect(imgW + gap, 0, imgW * 2 + gap, imgH);
		tempRects[1] = rect;
		rect = new Rect(mWidth - imgW, 0, mWidth, imgH);
		tempRects[2] = rect;
		
		rect = new Rect(0, imgH + gap, imgW, imgH * 2 + gap);
		tempRects[3] = rect;
		rect = new Rect(imgW + gap, imgH + gap, imgW * 2 + gap, imgH * 2 + gap);
		tempRects[4] = rect;
		rect = new Rect(mWidth - imgW, imgH + gap, mWidth, imgH * 2 + gap);
		tempRects[5] = rect;
		
		rect = new Rect(0, imgH * 2 + gap * 2, imgW, imgH * 3 + gap * 2);
		tempRects[6] = rect;
		rect = new Rect(imgW + gap, imgH * 2 + gap * 2, imgW * 2 + gap, imgH * 3 + gap * 2);
		tempRects[7] = rect;
		rect = new Rect(mWidth - imgW, imgH * 2 + gap * 2, mWidth, imgH * 3 + gap * 2);
		tempRects[8] = rect;
		
		Rect[] result = new Rect[size];
		for (int i = 0; i < size; i++)
			result[i] = tempRects[i];
		
		return result;
	}
	
	private void setWifiTimelinePicsView() {
		picRects = null;
		
		int size = picUrls.length;
		int random = 0;
		
		LinearLayout.LayoutParams layoutParams = null;
		Rect[] tempRects = new Rect[size];
		switch (size) {
		case 1:
			
			int imgW = mWidth;
			int imgH = Math.round(imgW * 4.0f / 3.0f);
			
			Rect rect = new Rect((mWidth - imgW) / 2, 0, (mWidth - imgW) / 2 + imgW, imgH);
			tempRects[0] = rect;
			
			layoutParams = new LinearLayout.LayoutParams(mWidth, imgH);
			break;
		case 2:
			imgW = (mWidth - gap) / 2;
			imgH = Math.round(imgW * 4.0f / 3.0f);
			
			rect = new Rect(0, 0, imgW, imgH);
			tempRects[0] = rect;
			rect = new Rect(imgW + gap, 0, mWidth, imgH);
			tempRects[1] = rect;
			
			layoutParams = new LinearLayout.LayoutParams(mWidth, imgH);
			break;
		case 3:
			int imgW02 = Math.round((mWidth - gap) * 3.0f / 7.0f);
			int imgH02 = imgW02;
			int imgW01 = Math.round((mWidth - gap) * 4.0f / 7.0f);
			int imgH01 = imgH02 * 2 + gap;
			
			try {
				random = (int) (Long.parseLong(mStatusContent.getId()) % 2);
			} catch (Exception e) {
			}
			// 见/doc/3_0.png
			if (random == 0) {
				rect = new Rect(0, 0, imgW01, imgH01);
				tempRects[0] = rect;
				rect = new Rect(gap + imgW01, 0, mWidth, imgH02);
				tempRects[1] = rect;
				rect = new Rect(gap + imgW01, imgH02 + gap, mWidth, imgH01);
				tempRects[2] = rect;
			}
			// 见/doc/3_1.png
			else if (random == 1) {
				rect = new Rect(0, 0, imgW02, imgH02);
				tempRects[0] = rect;
				rect = new Rect(0, imgH02 + gap, imgW02, imgH01);
				tempRects[1] = rect;
				rect = new Rect(gap + imgW02, 0, mWidth, imgH01);
				tempRects[2] = rect;
			}
			
			layoutParams = new LinearLayout.LayoutParams(mWidth, imgH01);
			break;
		case 4:
			imgW = Math.round((mWidth - gap) * 1.0f / 2);
			imgH = Math.round(imgW * 4.0f / 3.0f);
			
			rect = new Rect(0, 0, imgW, imgH);
			tempRects[0] = rect;
			rect = new Rect(gap + imgW, 0, mWidth, imgH);
			tempRects[1] = rect;
			rect = new Rect(0, imgH + gap, imgW, imgH * 2 + gap);
			tempRects[2] = rect;
			rect = new Rect(gap + imgW, imgH + gap, mWidth, imgH * 2 + gap);
			tempRects[3] = rect;
			
			layoutParams = new LinearLayout.LayoutParams(mWidth, imgH * 2 + gap);
			break;
		case 5:
			imgW01 = Math.round((mWidth - gap) * 1.0f / 2);
			imgH01 = Math.round(imgW01 * 4.0f / 3.0f);
			
			imgW02 = Math.round((mWidth - gap * 2) * 1.0f / 3);
			imgH02 = Math.round(imgW02 * 4.0f / 3.0f);
			
			try {
				random = (int) (Long.parseLong(mStatusContent.getId()) % 2);
			} catch (Exception e) {
			}
			int height = imgH01 + imgH02 + gap;
			// 见/doc/5_0.png
			if (random == 0) {
				rect = new Rect(0, 0, imgW01, imgH01);
				tempRects[0] = rect;
				rect = new Rect(gap + imgW01, 0, mWidth, imgH01);
				tempRects[1] = rect;
				rect = new Rect(0, imgH01 + gap, imgW02, height);
				tempRects[2] = rect;
				rect = new Rect(imgW02 + gap, imgH01 + gap, imgW02 * 2 + gap, height);
				tempRects[3] = rect;
				rect = new Rect(mWidth - imgW02, imgH01 + gap, mWidth, height);
				tempRects[4] = rect;
			}
			// 见/doc/5_1.png
			else if (random == 1) {
				rect = new Rect(0, 0, imgW02, imgH02);
				tempRects[0] = rect;
				rect = new Rect(imgW02 + gap, 0, imgW02 * 2 + gap, imgH02);
				tempRects[1] = rect;
				rect = new Rect(mWidth - imgW02, 0, mWidth, imgH02);
				tempRects[2] = rect;
				rect = new Rect(0, imgH02 + gap, imgW01, height);
				tempRects[3] = rect;
				rect = new Rect(gap + imgW01, imgH02 + gap, mWidth, height);
				tempRects[4] = rect;
			}
			
			layoutParams = new LinearLayout.LayoutParams(mWidth, imgH01 + imgH02 + gap);
			break;
		case 6:
			imgW01 = Math.round((mWidth - 2 * gap) * 1.0f / 3.0f);
			imgH01 = Math.round(imgW01 * 4.0f / 3.0f);
			
			imgW02 = imgW01 * 2 + gap;
			imgH02 = imgH01 * 2 + gap;
			
			height = imgH01 + imgH02 + gap;
			
			try {
				random = (int) (Long.parseLong(mStatusContent.getId()) % 3);
			} catch (Exception e) {
			}
			// 见/doc/6_0.png
			if (random == 0) {
				rect = new Rect(0, 0, imgW01, imgH01);
				tempRects[0] = rect;
				rect = new Rect(gap + imgW01, 0, imgW01 * 2 + gap, imgH01);
				tempRects[1] = rect;
				rect = new Rect(mWidth - imgW01, 0, mWidth, imgH01);
				tempRects[2] = rect;
				rect = new Rect(0, imgH01 + gap, imgW02, height);
				tempRects[3] = rect;
				rect = new Rect(imgW02 + gap, imgH01 + gap, mWidth, height - imgH01 - gap);
				tempRects[4] = rect;
				rect = new Rect(imgW02 + gap, height - imgH01, mWidth, height);
				tempRects[5] = rect;
			}
			// 见/doc/6_1.png
			else if (random == 1) {
				rect = new Rect(0, 0, imgW01, imgH01);
				tempRects[0] = rect;
				rect = new Rect(0, imgH01 + gap, imgW01, imgH01 * 2 + gap);
				tempRects[1] = rect;
				rect = new Rect(gap + imgW01, 0, mWidth, imgH02);
				tempRects[2] = rect;
				rect = new Rect(0, height - imgH01, imgW01, height);
				tempRects[3] = rect;
				rect = new Rect(gap + imgW01, height - imgH01, gap + imgW01 * 2, height);
				tempRects[4] = rect;
				rect = new Rect(imgW02 + gap, height - imgH01, mWidth, height);
				tempRects[5] = rect;
			}
			// 见/doc/6_2.png
			else if (random == 2) {
				rect = new Rect(0, 0, imgW02, imgH02);
				tempRects[0] = rect;
				rect = new Rect(imgW02 + gap, 0, mWidth, imgH01);
				tempRects[1] = rect;
				rect = new Rect(gap + imgW02, imgH01 + gap, mWidth, imgH01 * 2 + gap);
				tempRects[2] = rect;
				rect = new Rect(0, height - imgH01, imgW01, height);
				tempRects[3] = rect;
				rect = new Rect(gap + imgW01, height - imgH01, gap + imgW01 * 2, height);
				tempRects[4] = rect;
				rect = new Rect(imgW02 + gap, height - imgH01, mWidth, height);
				tempRects[5] = rect;
			}
			// 见/doc/6_3.png
			else if (random == 3) {
				rect = new Rect(0, 0, imgW01, imgH01);
				tempRects[0] = rect;
				rect = new Rect(gap + imgW01, 0, imgW01 * 2 + gap, imgH01);
				tempRects[1] = rect;
				rect = new Rect(mWidth - imgW01, 0, mWidth, imgH01);
				tempRects[2] = rect;
				rect = new Rect(0, imgH01 + gap, imgW01, imgH01 * 2 + gap);
				tempRects[3] = rect;
				rect = new Rect(0, height - imgH01, imgW01, height);
				tempRects[4] = rect;
				rect = new Rect(imgW01 + gap, imgH01 + gap, mWidth, height);
				tempRects[5] = rect;
			}
			layoutParams = new LinearLayout.LayoutParams(mWidth, height);
			break;
		case 7:
			imgW01 = Math.round((mWidth - 2 * gap) * 1.0f / 3.0f);
			imgH01 = Math.round(imgW01 * 4.0f / 3.0f);
			
			imgW02 = mWidth;
			imgH02 = Math.round(imgW02 * 3.0f / 4.0f);
			
			height = imgH01 * 2 + imgH02 + gap * 2;
			
			rect = new Rect(0, 0, imgW01, imgH01);
			tempRects[0] = rect;
			rect = new Rect(gap + imgW01, 0, imgW01 * 2 + gap, imgH01);
			tempRects[1] = rect;
			rect = new Rect(mWidth - imgW01, 0, mWidth, imgH01);
			tempRects[2] = rect;
			
			rect = new Rect(0, imgH01 + gap, imgW02, imgH01 + gap + imgH02);
			tempRects[3] = rect;
			
			rect = new Rect(0, imgH01 + gap * 2 + imgH02, imgW01, height);
			tempRects[4] = rect;
			rect = new Rect(gap + imgW01, imgH01 + gap * 2 + imgH02, imgW01 * 2 + gap, height);
			tempRects[5] = rect;
			rect = new Rect(mWidth - imgW01, imgH01 + gap * 2 + imgH02, mWidth, height);
			tempRects[6] = rect;
			
			layoutParams = new LinearLayout.LayoutParams(mWidth, height);
			break;
		case 8:
			imgW01 = Math.round((mWidth - 2 * gap) * 1.0f / 3.0f);
			imgH01 = Math.round(imgW01 * 4.0f / 3.0f);
			
			imgW02 = Math.round((mWidth - gap) * 1.0f / 2.0f);
			imgH02 = Math.round(imgW02 * 4.0f / 3.0f);
			
			height = imgH01 * 2 + imgH02 + gap * 2;
			
			rect = new Rect(0, 0, imgW01, imgH01);
			tempRects[0] = rect;
			rect = new Rect(gap + imgW01, 0, imgW01 * 2 + gap, imgH01);
			tempRects[1] = rect;
			rect = new Rect(mWidth - imgW01, 0, mWidth, imgH01);
			tempRects[2] = rect;
			
			rect = new Rect(0, imgH01 + gap, imgW02, imgH01 + gap + imgH02);
			tempRects[3] = rect;
			rect = new Rect(imgW02 + gap, imgH01 + gap, mWidth, imgH01 + gap + imgH02);
			tempRects[4] = rect;
			
			rect = new Rect(0, imgH01 + gap * 2 + imgH02, imgW01, height);
			tempRects[5] = rect;
			rect = new Rect(gap + imgW01, imgH01 + gap * 2 + imgH02, imgW01 * 2 + gap, height);
			tempRects[6] = rect;
			rect = new Rect(mWidth - imgW01, imgH01 + gap * 2 + imgH02, mWidth, height);
			tempRects[7] = rect;
			
			layoutParams = new LinearLayout.LayoutParams(mWidth, height);
			break;
		case 9:
			imgW01 = Math.round((mWidth - 2 * gap) * 1.0f / 3.0f);
			imgH01 = Math.round(imgW01 * 4.0f / 3.0f);
			
			height = imgH01 * 2 + imgH01 + gap * 2;
			
			rect = new Rect(0, 0, imgW01, imgH01);
			tempRects[0] = rect;
			rect = new Rect(gap + imgW01, 0, imgW01 * 2 + gap, imgH01);
			tempRects[1] = rect;
			rect = new Rect(mWidth - imgW01, 0, mWidth, imgH01);
			tempRects[2] = rect;
			
			rect = new Rect(0, imgH01 + gap, imgW01, imgH01 * 2 + gap);
			tempRects[3] = rect;
			rect = new Rect(gap + imgW01, imgH01 + gap, imgW01 * 2 + gap, imgH01 * 2 + gap);
			tempRects[4] = rect;
			rect = new Rect(mWidth - imgW01, imgH01 + gap, mWidth, imgH01 * 2 + gap);
			tempRects[5] = rect;
			
			rect = new Rect(0, imgH01 + gap * 2 + imgH01, imgW01, height);
			tempRects[6] = rect;
			rect = new Rect(gap + imgW01, imgH01 + gap * 2 + imgH01, imgW01 * 2 + gap, height);
			tempRects[7] = rect;
			rect = new Rect(mWidth - imgW01, imgH01 + gap * 2 + imgH01, mWidth, height);
			tempRects[8] = rect;
			
			layoutParams = new LinearLayout.LayoutParams(mWidth, height);
			break;
		}
		
		setLayoutParams(layoutParams);
		
		picRects = tempRects;
		
		displayPics();
		
		// 重新绘制
		requestLayout();
	}
	
	public void displayPics() {
		if (picRects == null || picUrls == null || picUrls.length == 0)
			return;
		
		for (int i = 0; i < getChildCount(); i++) {
			ImageView imgView = (ImageView) getChildAt(i);
			
			// 隐藏多余的View
			if (i >= picRects.length) {
				getChildAt(i).setVisibility(View.GONE);
				
				imgView.setImageDrawable(BitmapLoader.getLoadingDrawable(imgView));
			}
			else {
				Rect imgRect = picRects[i];
				
				imgView.setVisibility(View.VISIBLE);
				imgView.setLayoutParams(new ViewGroup.LayoutParams(imgRect.right - imgRect.left, imgRect.bottom - imgRect.top));
				
				String url = getStatusMulImage(picUrls[i].getThumbnail_pic());

				ImageConfig config = new ImageConfig();
				if (large)
					config.setId("status_" + large + "_" + AppSettings.getPicLargeMode());
				else 
					config.setId("status_thumb");
				config.setLoadfaildBitmapRes(R.drawable.bg_timeline_loading);
				config.setLoadingBitmapRes(R.drawable.bg_timeline_loading);
				config.setMaxWidth(imgRect.right - imgRect.left);
				config.setMaxHeight(imgRect.bottom - imgRect.top);
				if (url.equals(picUrls[i].getThumbnail_pic()))
					config.setBitmapCompress(TimelineThumbBitmapCompress.class);
				else if (AppSettings.getPicLargeMode() == 0)
					config.setBitmapCompress(Timeline9ggBitmapCompress.class);
				else
					config.setBitmapCompress(TimelineBitmapCompress.class);
					
				BitmapLoader.getInstance().display(this, url, imgView, config);
				
				if (bizFragment != null) {
					bizFragment.previousPics(imgView, mStatusContent, i);
				}
			}
		}
	}
	
	private String getStatusMulImage(String thumbImage) {
		if (large) {
			return thumbImage.replace("thumbnail", "bmiddle");
		}
		else {
			return thumbImage;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (picRects == null)
			return;
		
		for (int i = 0; i < getChildCount(); i++) {
			// 隐藏多余的View
			if (i < picRects.length) {
				Rect imgRect = picRects[i];
				
				View childView = getChildAt(i);
				childView.layout(imgRect.left, imgRect.top, imgRect.right, imgRect.bottom);
			}
			else {
				break;
			}
		}
	}
	
	public void release() {
		Logger.v(TAG, "释放资源");
		
		mStatusContent = null;
		
		for (int i = 0; i < getChildCount(); i++) {
			ImageView imgView = (ImageView) getChildAt(i);
			imgView.setImageDrawable(BitmapLoader.getLoadingDrawable(imgView));
		}
	}
	
	public void setPics(StatusContent status, BizFragment bizFragment, ABaseFragment ownerFragment) {
		this.bizFragment = bizFragment;
		this.ownerFragment = ownerFragment;
		Logger.v(TAG, "加载图片");
		
		@SuppressWarnings("unused")
		boolean replace = false;
		// 如果内容发送了改变
		if (mStatusContent != null && mStatusContent.getId().equals(status.getId()))
			replace = true;
		
		// 如果图片模式是小图
		if (AppSettings.getPictureMode() == 0) {
			if (large)
				replace = true;
			
			large = false;
		}
		// 如果图片模式是大图
		else if (AppSettings.getPictureMode() == 1) {
			if (!large)
				replace = true;
			
			large = true;
		}
		// 图片模式是自动，且当前是WIFI网络
		else if (AppSettings.getPictureMode() == 2 && SystemUtility.getNetworkType() == NetWorkType.wifi) {
			// 如果当前不是large
			if (!large) 
				replace = true;
			
			large = true;
		}
		// 图片模式是自动，且当前是Mobile网络
		else if (AppSettings.getPictureMode() == 2 && SystemUtility.getNetworkType() != NetWorkType.wifi) {
			// 如果当前不是large
			if (large) 
				replace = true;
			
			large = false;
		}
		
		mStatusContent = status;

		PicUrls[] picUrls = mStatusContent.getPic_urls();

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
		
		this.picUrls = picUrls;
		
		if (picUrls == null || picUrls.length == 0) {
			recyle();
			
			setVisibility(View.GONE);
		}
		else {
			setVisibility(View.VISIBLE);
			
			if (large && AppSettings.getPicLargeMode() == 0) {
				setWifiTimelinePicsView();
			}
			else {
				setMobileTimelinePicsView();
			}
		}
	}

	@Override
	public boolean canDisplay() {
		if (ownerFragment != null)
			return ownerFragment.canDisplay();
		
		return true;
	}

}
