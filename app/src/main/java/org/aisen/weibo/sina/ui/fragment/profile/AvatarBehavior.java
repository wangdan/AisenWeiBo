package org.aisen.weibo.sina.ui.fragment.profile;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.ui.widget.CircleImageView;
import org.aisen.weibo.sina.R;

/**
 * Created by wangdan on 16/1/20.
 */
public class AvatarBehavior extends CoordinatorLayout.Behavior<CircleImageView> {

    static final String TAG = "AvatarBehavior";

    Context context;

    private ImageView imgAvatar;
    private Toolbar toolbar;
    private View layRef;

    private float avatarMinY = 0.0f;
    private float scaleHeight = 0.0f;
    private int avatarSize = 0;
    private float avatarX = 0;

    public AvatarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, CircleImageView child, View dependency) {

        if (imgAvatar == null) {
            imgAvatar = (ImageView) child.findViewById(R.id.imgAvatar);
            toolbar = (Toolbar) parent.findViewById(R.id.toolbar);
            layRef = parent.findViewById(R.id.layRef);
        }

        if (avatarMinY == 0.0f && toolbar.getHeight() > 0) {
            avatarMinY = toolbar.getHeight() + getStatusBarHeight();
            avatarSize = imgAvatar.getWidth();
            avatarX = Utils.dip2px(16);
            scaleHeight = avatarSize * 3.0f / 4;
            avatarX = layRef.getY() - scaleHeight - avatarMinY;
        }

        return avatarMinY > 0;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, CircleImageView child, View dependency) {
        int setAvatarSize = -1;

        float childToY = layRef.getY() - avatarSize * 3.0f / 4;

        if (childToY > avatarMinY) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            if (lp.width < avatarSize) {
                setAvatarSize = avatarSize;
            }
        }
        else {
            if (layRef.getY() - avatarMinY > 0.0f) {
                float scale = (layRef.getY() - avatarMinY) / scaleHeight;

                setAvatarSize = Math.round(avatarSize * scale);
                childToY = layRef.getY() - setAvatarSize * 3.0f / 4;
            }
            else {
                setAvatarSize = 0;
            }
        }

        if (setAvatarSize != -1) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            if (lp.width != setAvatarSize) {
                lp.width = setAvatarSize;
                lp.height = setAvatarSize;
                child.setLayoutParams(lp);
            }
        }

        float offsetX = (avatarSize - child.getWidth()) * 1.0f / 2;
//        child.setX(avatarX + offsetX);
        child.setY(childToY);

        child.setAlpha((imgAvatar.getY() - avatarMinY) * 1.0f / avatarX);

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
