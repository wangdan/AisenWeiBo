package org.aisen.weibo.sina.ui.widget.photoview;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.aisen.weibo.sina.ui.widget.photoview.AttacherInterface.OnMatrixChangedListener;
import org.aisen.weibo.sina.ui.widget.photoview.AttacherInterface.OnPhotoTapListener;
import org.aisen.weibo.sina.ui.widget.photoview.AttacherInterface.OnViewTapListener;

public class PhotoView extends ImageView implements IPhotoView {

    protected PhotoViewAttacher mAttacher;
    protected PhotoViewGestureAttacher mGestureAttacherr;

    private ScaleType mPendingScaleType;

    public PhotoViewAttacher getAttacher() {
        return mAttacher;
    }

    public PhotoView(Context context) {
        this(context, false);
    }

    public PhotoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, false);
    }

    public PhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, false);
    }

    public PhotoView(Context context, boolean needGestures) {
        this(context, null, needGestures);
    }

    public PhotoView(Context context, AttributeSet attr, boolean needGestures) {
        this(context, attr, 0, needGestures);
    }

    public PhotoView(Context context, AttributeSet attr, int defStyle, boolean needGestures) {
        super(context, attr, defStyle);
        super.setScaleType(ScaleType.MATRIX);
        if (needGestures) {
            mGestureAttacherr = new PhotoViewGestureAttacher(this);
            mAttacher = null;
        } else {
            mGestureAttacherr = null;
            mAttacher = new PhotoViewAttacher(this);
        }

        if (null != mPendingScaleType) {
            setScaleType(mPendingScaleType);
            mPendingScaleType = null;
        }
    }

    public boolean canZoom() {
        if (mAttacher == null) {
            return mGestureAttacherr.canZoom();
        } else {
            return mAttacher.canZoom();
        }
    }

    public RectF getDisplayRect() {
        if (mAttacher == null) {
            return mGestureAttacherr.getDisplayRect();
        } else {
            return mAttacher.getDisplayRect();
        }
    }

    public float getMinScale() {
        if (mAttacher == null) {
            return mGestureAttacherr.getMinScale();
        } else {
            return mAttacher.getMinScale();
        }
    }

    public float getMidScale() {
        if (mAttacher == null) {
            return mGestureAttacherr.getMidScale();
        } else {
            return mAttacher.getMidScale();
        }
    }

    public float getMaxScale() {
        if (mAttacher == null) {
            return mGestureAttacherr.getMaxScale();
        } else {
            return mAttacher.getMaxScale();
        }
    }

    public float getScale() {
        if (mAttacher == null) {
            return mGestureAttacherr.getScale();
        } else {
            return mAttacher.getScale();
        }
    }

    public ScaleType getScaleType() {
        if (mAttacher == null) {
            return mGestureAttacherr.getScaleType();
        } else {
            return mAttacher.getScaleType();
        }
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        if (mAttacher == null) {
            mGestureAttacherr.setAllowParentInterceptOnEdge(allow);
        } else {
            mAttacher.setAllowParentInterceptOnEdge(allow);
        }
    }

    public void setMinScale(float minScale) {
        if (mAttacher == null) {
            mGestureAttacherr.setMinScale(minScale);
        } else {
            mAttacher.setMinScale(minScale);
        }
    }

    public void setMidScale(float midScale) {
        if (mAttacher == null) {
            mGestureAttacherr.setMidScale(midScale);
        } else {
            mAttacher.setMidScale(midScale);
        }
    }

    public void setMaxScale(float maxScale) {
        if (mAttacher == null) {
            mGestureAttacherr.setMaxScale(maxScale);
        } else {
            mAttacher.setMaxScale(maxScale);
        }
    }

    public void update() {
        if (mGestureAttacherr != null) {
            mGestureAttacherr.update();
        } else if (mAttacher != null) {
            mAttacher.update();
        }
    }

    // setImageBitmap calls through to this method
    public void setImageDrawable(Drawable drawable) {
        try {
            super.setImageDrawable(drawable);
            update();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setImageResource(int resId) {
        super.setImageResource(resId);
        update();
    }

    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        update();
    }

    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        if (mGestureAttacherr != null) {
            mGestureAttacherr.setOnMatrixChangeListener(listener);
        } else if (mAttacher != null) {
            mAttacher.setOnMatrixChangeListener(listener);
        }
    }

    public void setOnLongClickListener(OnLongClickListener l) {
        if (mGestureAttacherr != null) {
            mGestureAttacherr.setOnLongClickListener(l);
        } else if (mAttacher != null) {
            mAttacher.setOnLongClickListener(l);
        }
    }

    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        if (mGestureAttacherr != null) {
            mGestureAttacherr.setOnPhotoTapListener(listener);
        } else if (mAttacher != null) {
            mAttacher.setOnPhotoTapListener(listener);
        }
    }

    public void setOnViewTapListener(OnViewTapListener listener) {
        if (mGestureAttacherr != null) {
            mGestureAttacherr.setOnViewTapListener(listener);
        } else if (mAttacher != null) {
            mAttacher.setOnViewTapListener(listener);
        }
    }

    public void setScaleType(ScaleType scaleType) {
        if (mGestureAttacherr != null) {
            mGestureAttacherr.setScaleType(scaleType);
        } else if (mAttacher != null) {
            mAttacher.setScaleType(scaleType);
        } else {
            mPendingScaleType = scaleType;
        }
    }

    public void setZoomable(boolean zoomable) {
        if (mGestureAttacherr != null) {
            mGestureAttacherr.setZoomable(zoomable);
        } else if (mAttacher != null) {
            mAttacher.setZoomable(zoomable);
        }
    }

    public void zoomTo(float scale, float focalX, float focalY) {
        if (mGestureAttacherr != null) {
            mGestureAttacherr.zoomTo(scale, focalX, focalY);
        } else if (mAttacher != null) {
            mAttacher.zoomTo(scale, focalX, focalY);
        }
    }

    protected void onDetachedFromWindow() {
        if (mGestureAttacherr != null) {
            mGestureAttacherr.cleanup();
        } else if (mAttacher != null) {
            mAttacher.cleanup();
        }
        super.onDetachedFromWindow();
    }

}
