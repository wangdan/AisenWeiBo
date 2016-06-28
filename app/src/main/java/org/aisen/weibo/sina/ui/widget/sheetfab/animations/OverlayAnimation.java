package org.aisen.weibo.sina.ui.widget.sheetfab.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * Created by Gordon Wong on 7/9/2015.
 *
 * Animates the overlay when showing and hiding the material sheet.
 */
public class OverlayAnimation {

	protected View overlay;
	protected Interpolator interpolator;

	public OverlayAnimation(View overlay, Interpolator interpolator) {
		this.overlay = overlay;
		this.interpolator = interpolator;
	}

	/**
	 * Shows the overlay.
	 * 
	 * @param duration Duration of the animation in milliseconds. Use 0 for no animation.
	 * @param listener Listener for animation events.
	 */
	public void show(long duration, final AnimationListener listener) {
		overlay.animate().alpha(1).setDuration(duration).setInterpolator(interpolator)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationStart(Animator animation) {
						overlay.setVisibility(View.VISIBLE);
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
				}).start();
	}

	/**
	 * Hides the overlay.
	 * 
	 * @param duration Duration of the animation in milliseconds. Use 0 for no animation.
	 * @param listener Listener for animation events.
	 */
	public void hide(long duration, final AnimationListener listener) {
		overlay.animate().alpha(0).setDuration(duration).setInterpolator(interpolator)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationStart(Animator animation) {
						if (listener != null) {
							listener.onStart();
						}
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						overlay.setVisibility(View.GONE);
						if (listener != null) {
							listener.onEnd();
						}
					}
				}).start();
	}

}
