package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;

import org.aisen.android.common.utils.BitmapUtil;
import org.aisen.android.common.utils.Utils;
import org.aisen.weibo.sina.R;

/**
 * Created by wangdan on 16/2/4.
 */
public class ProfileCollapsingToolbarLayout extends CollapsingToolbarLayout {

    public static final String TAG = ProfileCollapsingToolbarLayout.class.getSimpleName();

    private ImageView imgCover;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private View layDetail;
    private View layRealDetail;
    private View layName;

    private int statusbarHeight;
    private boolean scrimsShown;

    private Drawable mInsetForeground;
    private Rect mTempRect = new Rect();

    private int verticalOffset;

    private boolean onCoverSet = false;
    private int appbarHeight;
    private int coverHeight;// 封面高度

    private int avatarSize;// 头像尺寸
    private int finalAvatarSize;// 缩小后的头像尺寸
    private int avatarMarginLeft;// 头像左侧Margin
    private int avatarFinalMarginLeft;// 头像缩小后的Margin
    private Bitmap avatarBitmap;// 头像位图
    private Matrix avatarMatrix;// 头像矩阵

    private Bitmap layNameBitmap;// 包括名字、性别、认证三个要素的截图
    private Matrix layNameMatrix;
    private int layNameMarginTop;// 名字顶部的margin
    private int layNameMarginLeft;
    private int finalLayNameSize;

    private float maxVerticalOffset;// Collapsing最大距离

    public ProfileCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setup(context, attrs, 0);
    }

    public ProfileCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setup(context, attrs, defStyleAttr);
    }

    private void setup(Context context, AttributeSet attrs, int defStyle) {
        statusbarHeight = getStatusBarHeight();

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ProfileCollapsingToolbarLayout, defStyle, 0);
        if (a == null) {
            return;
        }
        mInsetForeground = a.getDrawable(R.styleable.ProfileCollapsingToolbarLayout_profileInsetForeground);
        a.recycle();

        coverHeight = getResources().getDimensionPixelSize(R.dimen.profile_cover);

        avatarSize = Utils.dip2px(context, 100);
        finalAvatarSize = Utils.dip2px(context, 36);
        avatarFinalMarginLeft = Utils.dip2px(context, 16);
        layNameMarginTop = Utils.dip2px(context, 32);
        finalLayNameSize = Utils.dip2px(context, 28);
        layNameMarginLeft = Utils.dip2px(context, 12);
        avatarMarginLeft = getResources().getDimensionPixelSize(R.dimen.padding_normal);
        Bitmap sourceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user_placeholder);
        avatarBitmap = BitmapUtil.setImageCorner(sourceBitmap, sourceBitmap.getWidth());
    }

    private void maybeSetup() {
        if (appBarLayout == null) {
            return;
        }

        // 将Cover的高度重新Measure一次
        if (!onCoverSet && appBarLayout.getHeight() > 0) {
            onCoverSet = true;

            CollapsingToolbarLayout.LayoutParams lp = (CollapsingToolbarLayout.LayoutParams) imgCover.getLayoutParams();
            lp.height = layDetail.getHeight() + coverHeight;
            imgCover.setLayoutParams(lp);
            imgCover.setPadding(imgCover.getPaddingLeft(), imgCover.getPaddingTop(), imgCover.getPaddingRight(), layDetail.getHeight());
        }

        if (appbarHeight != appBarLayout.getHeight()) {
            appbarHeight = appBarLayout.getHeight();

            // 计算Detail的layout_collapseParallaxMultiplier，使其收起来时刚好高度为ToolBar的高度
            CollapsingToolbarLayout.LayoutParams params = (CollapsingToolbarLayout.LayoutParams) layDetail.getLayoutParams();
            // 最大移动的距离
            maxVerticalOffset = appBarLayout.getHeight() - (statusbarHeight + toolbar.getHeight() + tabLayout.getHeight()) - 2;
            // 计算移动后的top减去移动前的top就是需要offset，再用offset计算出multiplier
            float multiplier = ((maxVerticalOffset + statusbarHeight) -
                    (collapsingToolbarLayout.getHeight() - layDetail.getHeight())) * 1.0f / maxVerticalOffset;
            if (params.getParallaxMultiplier() != multiplier) {
                params.setParallaxMultiplier(multiplier);
            }

            avatarMatrix = new Matrix();

            setNameBitmap();
        }
    }

    private void setNameBitmap() {
        if (layName == null)
            return;

        layNameBitmap = Bitmap.createBitmap(layName.getWidth(), layName.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(layNameBitmap);
        layName.draw(canvas);
        layName.setVisibility(View.INVISIBLE);
        layNameMatrix = new Matrix();
    }

    public void resetNameBitmap() {
        layName.setVisibility(View.VISIBLE);
        onCoverSet = false;
        layNameBitmap = null;
    }

    public void setAvatarBitmap(Bitmap bitmap) {
        avatarBitmap = BitmapUtil.setImageCorner(bitmap, bitmap.getWidth());
        innerOffsetChangedListener.onOffsetChanged(appBarLayout, verticalOffset);
        invalidate();
    }

    @Override
    public void setScrimsShown(boolean shown, boolean animate) {
        super.setScrimsShown(shown, animate);

        scrimsShown = shown;
    }

    private boolean isScrimsShown() {
        return scrimsShown;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (avatarMatrix != null) {
            final int saveCount = canvas.save();

            if (!isScrimsShown()) {
                mTempRect.set(0, -verticalOffset, getWidth(), statusbarHeight + -verticalOffset);
                mInsetForeground.setBounds(mTempRect);
                mInsetForeground.draw(canvas);
            }

            if (layNameBitmap != null) {
                canvas.drawBitmap(layNameBitmap, layNameMatrix, null);
            }

            canvas.drawBitmap(avatarBitmap, avatarMatrix, null);

            canvas.restoreToCount(saveCount);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Add an OnOffsetChangedListener if possible
        final ViewParent parent = getParent();
        if (parent instanceof AppBarLayout) {
            ((AppBarLayout) parent).addOnOffsetChangedListener(innerOffsetChangedListener);

            layDetail = findViewById(R.id.layDetail);
            layRealDetail = findViewById(R.id.layRealDetail);
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            appBarLayout = (AppBarLayout) parent;
            collapsingToolbarLayout = this;
            imgCover = (ImageView) findViewById(R.id.imgCover);
            tabLayout = (TabLayout) appBarLayout.findViewById(R.id.tabLayout);
            layName = findViewById(R.id.layName);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // Remove our OnOffsetChangedListener if possible and it exists
        final ViewParent parent = getParent();
        if (parent instanceof AppBarLayout) {
            ((AppBarLayout) parent).removeOnOffsetChangedListener(innerOffsetChangedListener);
        }

        super.onDetachedFromWindow();
    }

    private AppBarLayout.OnOffsetChangedListener innerOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {

        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            maybeSetup();

            if (layNameBitmap == null) {
                setNameBitmap();
            }

            ProfileCollapsingToolbarLayout.this.verticalOffset = verticalOffset;

            // offset移动的比例
            float factor = -verticalOffset * 1.0f / maxVerticalOffset;

            if (avatarMatrix != null) {
                // 从avatarSize变化到finalAvatarSize，是根据这个factor逐渐变化的，计算它的dsize变化值
                float dsize = (avatarSize - finalAvatarSize) * factor;
                // 计算现在需要显示的avatar尺寸
                float avatarToSize = avatarSize - dsize;
                // 缩放
                float scale = avatarToSize * 1.0f / avatarBitmap.getWidth();
                avatarMatrix.setScale(scale, scale);

                // 初始化Top
                float startAvatarTop = getHeight() - layDetail.getHeight() - avatarToSize * 3.0f / 4;
                // 最终显示的Top
                float toAvatartTop = maxVerticalOffset + statusbarHeight + (toolbar.getHeight() - finalAvatarSize) * 1.0f / 2;
                float avatarTop = startAvatarTop - (startAvatarTop - toAvatartTop) * factor;

                // 初始化MarginLeft
                float startMargin = avatarMarginLeft;
                float toMargin = avatarFinalMarginLeft;
                float margin = startMargin - (startMargin - toMargin) * factor;

                // 平移
                avatarMatrix.postTranslate(margin, avatarTop);
            }

            if (layNameMatrix != null) {
                float dsize = (layNameBitmap.getHeight() - finalLayNameSize) * factor;
                float nameToSize = layNameBitmap.getHeight() - dsize;
                float scale = nameToSize * 1.0f / layNameBitmap.getHeight();
                layNameMatrix.setScale(scale, scale);

                float startNameTop = getHeight() - layDetail.getHeight() + layNameMarginTop;
                float toNameTop = maxVerticalOffset + statusbarHeight + (toolbar.getHeight() - finalLayNameSize) * 1.0f / 2;
                float nameTop = startNameTop - (startNameTop - toNameTop) * factor;

                float startMargin = avatarMarginLeft;
                float toMargin = avatarFinalMarginLeft + finalAvatarSize + layNameMarginLeft;
                float margin = startMargin - (startMargin - toMargin) * factor;

                layNameMatrix.postTranslate(margin, nameTop);
            }

            if (layRealDetail != null) {
                layRealDetail.setAlpha(1.0f - factor * 0.7f);
            }
        }

    };

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
