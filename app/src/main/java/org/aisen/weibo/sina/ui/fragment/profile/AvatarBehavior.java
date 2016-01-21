package org.aisen.weibo.sina.ui.fragment.profile;

import android.content.Context;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import org.aisen.android.ui.widget.CircleImageView;
import org.aisen.weibo.sina.R;

/**
 * Created by wangdan on 16/1/20.
 */
public class AvatarBehavior extends CoordinatorLayout.Behavior<CircleImageView> {

    static final String TAG = "AvatarBehavior";

    Context context;

    private ImageView imgAvatar;
    private ImageView imgCover;
    private Toolbar toolbar;
    private View layRef;
    private View layDetail;
    private View viewToolbarBg;

    private float avatarMinY = 0.0f;
    private float scaleHeight = 0.0f;
    private int avatarSize = 0;
    private float avatarX = 0;
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
            layRef = parent.findViewById(R.id.layRef);
            layDetail = parent.findViewById(R.id.layDetail);
            imgCover = (ImageView) parent.findViewById(R.id.imgCover);
            viewToolbarBg = parent.findViewById(R.id.viewToolbarBg);
            viewToolbarBg.setAlpha(0);
        }

        if (avatarMinY == 0.0f && toolbar.getHeight() > 0) {
            int defHeight = layDetail.getHeight();
            CollapsingToolbarLayout.LayoutParams lp = (CollapsingToolbarLayout.LayoutParams) imgCover.getLayoutParams();
            if (lp.height != defHeight + coverHeight) {
                lp.height = defHeight + coverHeight;
                imgCover.setLayoutParams(lp);
                imgCover.setPadding(imgCover.getPaddingLeft(), imgCover.getPaddingTop(), imgCover.getPaddingRight(), defHeight);

                return false;
            }

            avatarMinY = toolbar.getHeight() + getStatusBarHeight();
            avatarSize = imgAvatar.getWidth();
            avatarX = context.getResources().getDimensionPixelSize(R.dimen.padding_normal);
            scaleHeight = avatarSize * 3.0f / 4;
            avatarX = coverHeight - scaleHeight - avatarMinY;
        }

        return avatarMinY > 0;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, CircleImageView child, View dependency) {
        child.setY(layRef.getY() - avatarSize * 3.0f / 4);

        child.setAlpha((imgAvatar.getY() - avatarMinY) * 1.0f / avatarX);
        viewToolbarBg.setAlpha(1.0f - (layRef.getY() - avatarMinY) * 1.0f / (coverHeight - avatarMinY));

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
