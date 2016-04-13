package org.aisen.weibo.sina.ui.widget.photoview;

import android.graphics.RectF;
import android.view.View;

public class AttacherInterface {

    /**
     * Interface definition for a callback to be invoked when the internal Matrix has changed for
     * this View.
     *
     * @author Chris Banes
     */
    public static interface OnMatrixChangedListener {
        /**
         * Callback for when the Matrix displaying the Drawable has changed. This could be because
         * the View's bounds have changed, or the user has zoomed.
         *
         * @param rect - Rectangle displaying the Drawable's new bounds.
         */
        void onMatrixChanged(RectF rect);
    }

    /**
     * Interface definition for a callback to be invoked when the Photo is tapped with a single tap.
     *
     * @author Chris Banes
     */
    public static interface OnPhotoTapListener {

        /**
         * A callback to receive where the user taps on a photo. You will only receive a callback if
         * the user taps on the actual photo, tapping on 'whitespace' will be ignored.
         *
         * @param view - View the user tapped.
         * @param x    - where the user tapped from the of the Drawable, as percentage of the Drawable
         *             width.
         * @param y    - where the user tapped from the top of the Drawable, as percentage of the
         *             Drawable height.
         */
        void onPhotoTap(View view, float x, float y);
    }

    /**
     * Interface definition for a callback to be invoked when the ImageView is tapped with a single
     * tap.
     *
     * @author Chris Banes
     */
    public static interface OnViewTapListener {

        /**
         * A callback to receive where the user taps on a ImageView. You will receive a callback if
         * the user taps anywhere on the view, tapping on 'whitespace' will not be ignored.
         *
         * @param view - View the user tapped.
         * @param x    - where the user tapped from the left of the View.
         * @param y    - where the user tapped from the top of the View.
         */
        void onViewTap(View view, float x, float y);
    }
}
