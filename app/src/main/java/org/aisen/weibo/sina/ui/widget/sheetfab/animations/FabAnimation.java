package org.aisen.weibo.sina.ui.widget.sheetfab.animations;

import android.view.View;
import android.view.animation.Interpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;

import org.aisen.weibo.sina.ui.widget.io.codetail.animation.arcanimator.ArcAnimator;
import org.aisen.weibo.sina.ui.widget.io.codetail.animation.arcanimator.Side;

/**
 * Created by Gordon Wong on 7/9/2015.
 *
 * Animates the FAB when showing and hiding the material sheet.
 */
public class FabAnimation {

	protected View fab;
	protected Interpolator interpolator;

	public FabAnimation(View fab, Interpolator interpolator) {
		this.fab = fab;
		this.interpolator = interpolator;
	}

	/**
	 * Animates the FAB as if the FAB is morphing into a sheet.
	 *
	 * @param endX The X coordinate that the FAB will be moved to.
	 * @param endY The Y coordinate that the FAB will be moved to.
	 * @param side The side of the arc animation.
	 * @param arcDegrees Amount of arc in FAB movement animation.
	 * @param scaleFactor Amount to scale FAB.
	 * @param duration Duration of the animation in milliseconds. Use 0 for no animation.
	 * @param listener Listener for animation events.
	 */
	public void morphIntoSheet(int endX, int endY, Side side, int arcDegrees, float scaleFactor,
			long duration, AnimationListener listener) {
		morph(endX, endY, side, arcDegrees, scaleFactor, duration, listener);
	}

	/**
	 * Animates the FAB as if a sheet is being morphed into a FAB.
	 *
	 * @param endX The X coordinate that the FAB will be moved to.
	 * @param endY The Y coordinate that the FAB will be moved to.
	 * @param side The side of the arc animation.
	 * @param arcDegrees Amount of arc in FAB movement animation.
	 * @param scaleFactor Amount to scale FAB.
	 * @param duration Duration of the animation in milliseconds. Use 0 for no animation.
	 * @param listener Listener for animation events.
	 */
	public void morphFromSheet(int endX, int endY, Side side, int arcDegrees, float scaleFactor,
			long duration, AnimationListener listener) {
		fab.setVisibility(View.VISIBLE);
		morph(endX, endY, side, arcDegrees, scaleFactor, duration, listener);
	}

	protected void morph(float endX, float endY, Side side, float arcDegrees, float scaleFactor,
			long duration, AnimationListener listener) {
		// Move the FAB
		startArcAnim(fab, endX, endY, arcDegrees, side, duration, interpolator, listener);

		// Scale the size of the FAB
		fab.animate().scaleXBy(scaleFactor).scaleYBy(scaleFactor).setDuration(duration)
				.setInterpolator(interpolator).start();
	}

	protected void startArcAnim(View view, float endX, float endY, float degrees, Side side,
			long duration, Interpolator interpolator, final AnimationListener listener) {
		// Setup animation
		// Cast end coordinates to ints so that the FAB will be animated to the same position even
		// when there are minute differences in the coordinates
		ArcAnimator anim = ArcAnimator.createArcAnimator(view, (int) endX, (int) endY, degrees,
				side);
		anim.setDuration(duration);
		anim.setInterpolator(interpolator);
		// Add listener
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				if (listener != null) {
					listener.onStart();
				}
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (listener != null) {
					listener.onEnd();
				}
			}
		});
		// Start animation
		anim.start();
	}

}
