package org.aisen.weibo.sina.ui.fragment.profile;

import android.content.Context;
import android.graphics.Rect;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.ui.widget.CircleImageView;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.ui.widget.ProfileCollapsingToolbarLayout;

/**
 * Created by wangdan on 16/1/20.
 */
public class AvatarBehavior extends CoordinatorLayout.Behavior<CircleImageView> {

    static final String TAG = "AvatarBehavior";

    Context context;

    private ImageView imgAvatar;
    private ImageView imgCover;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private View layDetail;

    private float multiplier;
    private int avatarSize = 0;
    private int coverHeight;

    public AvatarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        coverHeight = context.getResources().getDimensionPixelSize(R.dimen.profile_cover);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, CircleImageView child, View dependency) {

        if (imgAvatar == null) {
            imgAvatar = (ImageView) child.findViewById(R.id.imgAvatar);
            toolbar = (Toolbar) parent.findViewById(R.id.toolbar);
            layDetail = parent.findViewById(R.id.layDetail);
            appBarLayout = (AppBarLayout) parent.findViewById(R.id.appbar);
            collapsingToolbarLayout = (CollapsingToolbarLayout) parent.findViewById(R.id.collapsingToolbar);
            imgCover = (ImageView) parent.findViewById(R.id.imgCover);
            tabLayout = (TabLayout) parent.findViewById(R.id.tabLayout);
        }

        if (avatarSize == 0 && toolbar.getHeight() > 0) {
            int defHeight = layDetail.getHeight();
            CollapsingToolbarLayout.LayoutParams lp = (CollapsingToolbarLayout.LayoutParams) imgCover.getLayoutParams();
            if (lp.height != defHeight + coverHeight) {
                lp.height = defHeight + coverHeight;
                imgCover.setLayoutParams(lp);
                imgCover.setPadding(imgCover.getPaddingLeft(), imgCover.getPaddingTop(), imgCover.getPaddingRight(), defHeight);

                return false;
            }

            avatarSize = imgAvatar.getWidth();
        }

        // 计算Detail的layout_collapseParallaxMultiplier，使其收起来时刚好高度为ToolBar的高度
        CollapsingToolbarLayout.LayoutParams params = (CollapsingToolbarLayout.LayoutParams) layDetail.getLayoutParams();
        // 最大移动的距离
        int maxOffset = appBarLayout.getHeight() - (getStatusBarHeight() + toolbar.getHeight() + tabLayout.getHeight());
        // 计算移动后的top减去移动前的top就是需要offset，再用offset计算出multiplier
        multiplier = ((maxOffset + getStatusBarHeight()) -
                (collapsingToolbarLayout.getHeight() - layDetail.getHeight())) * 1.0f / maxOffset;
        if (params.getParallaxMultiplier() != multiplier) {
            params.setParallaxMultiplier(multiplier);
        }

        return dependency == layDetail;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, CircleImageView child, View dependency) {

        return true;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
