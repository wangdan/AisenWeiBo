package org.aisen.weibo.sina.ui.widget.sheetfab.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import org.aisen.weibo.sina.ui.widget.io.codetail.animation.SupportAnimator;
import org.aisen.weibo.sina.ui.widget.sheetfab.MaterialSheetFab.RevealXDirection;
import org.aisen.weibo.sina.ui.widget.sheetfab.MaterialSheetFab.RevealYDirection;

import java.lang.reflect.Method;

/**
 * Created by Gordon Wong on 7/5/2015.
 *
 * Animates the material sheet into and out of view.
 */
public class MaterialSheetAnimation {

	private static final String SUPPORT_CARDVIEW_CLASSNAME = "android.support.v7.widget.CardView";
	private static final int SHEET_REVEAL_OFFSET_Y = 5;

	private View sheet;
	private int sheetColor;
	private int fabColor;
	private Interpolator interpolator;
	private RevealXDirection revealXDirection;
	private RevealYDirection revealYDirection;
	private Method setCardBackgroundColor;
	private boolean isSupportCardView;

	public MaterialSheetAnimation(View sheet, int sheetColor, int fabColor,
			Interpolator interpolator) {
		this.sheet = sheet;
		this.sheetColor = sheetColor;
		this.fabColor = fabColor;
		this.interpolator = interpolator;
		// Default reveal direction is up and to the left (for FABs in the bottom right corner)
		revealXDirection = RevealXDirection.LEFT;
		revealYDirection = RevealYDirection.UP;
		isSupportCardView = sheet.getClass().getName().equals(SUPPORT_CARDVIEW_CLASSNAME);
		// Get setCardBackgroundColor() method
		if (isSupportCardView) {
			try {
				// noinspection unchecked
				setCardBackgroundColor = sheet.getClass()
						.getDeclaredMethod("setCardBackgroundColor", int.class);
			} catch (Exception e) {
				setCardBackgroundColor = null;
			}
		}
	}

	/**
	 * Aligns the sheet's position with the FAB.
	 * 
	 * @param fab Floating action button
	 */
	public void alignSheetWithFab(View fab) {
		// NOTE: View.getLocationOnScreen() returns the view's coordinates on the screen
		// whereas other view methods like getRight() and getY() return coordinates relative
		// to the view's parent. Using those methods can lead to incorrect calculations when
		// the two views do not have the same parent.

		// Get FAB's coordinates
		int[] fabCoords = new int[2];
		fab.getLocationOnScreen(fabCoords);

		// Get sheet's coordinates
		int[] sheetCoords = new int[2];
		sheet.getLocationOnScreen(sheetCoords);

		// NOTE: Use the diffs between the positions of the FAB and sheet to align the sheet.
		// We have to use the diffs because the coordinates returned by getLocationOnScreen()
		// include the status bar and any other system UI elements, meaning the coordinates
		// aren't representative of the usable screen space.
		int leftDiff = sheetCoords[0] - fabCoords[0];
		int rightDiff = (sheetCoords[0] + sheet.getWidth()) - (fabCoords[0] + fab.getWidth());
		int topDiff = sheetCoords[1] - fabCoords[1];
		int bottomDiff = (sheetCoords[1] + sheet.getHeight()) - (fabCoords[1] + fab.getHeight());

		// NOTE: Preserve the sheet's margins to allow users to shift the sheet's position
		// to compensate for the fact that the design support library's FAB has extra
		// padding within the view
		ViewGroup.MarginLayoutParams sheetLayoutParams = (ViewGroup.MarginLayoutParams) sheet
				.getLayoutParams();

		// Set sheet's new coordinates (only if there is a change in coordinates because
		// setting the same coordinates can cause the view to "drift" - moving 0.5 to 1 pixels
		// around the screen)
		if (rightDiff != 0) {
			float sheetX = sheet.getX();
			// Align the right side of the sheet with the right side of the FAB if
			// doing so would not move the sheet off the screen
			if (rightDiff <= sheetX) {
				sheet.setX(sheetX - rightDiff - sheetLayoutParams.rightMargin);
				revealXDirection = RevealXDirection.LEFT;
			}
			// Otherwise, align the left side of the sheet with the left side of the FAB
			else if (leftDiff != 0 && leftDiff <= sheetX) {
				sheet.setX(sheetX - leftDiff + sheetLayoutParams.leftMargin);
				revealXDirection = RevealXDirection.RIGHT;
			}
		}

		if (bottomDiff != 0) {
			float sheetY = sheet.getY();
			// Align the bottom of the sheet with the bottom of the FAB
			if (bottomDiff <= sheetY) {
				sheet.setY(sheetY - bottomDiff - sheetLayoutParams.bottomMargin);
				revealYDirection = RevealYDirection.UP;
			}
			// Otherwise, align the top of the sheet with the top of the FAB
			else if (topDiff != 0 && topDiff <= sheetY) {
				sheet.setY(sheetY - topDiff + sheetLayoutParams.topMargin);
				revealYDirection = RevealYDirection.DOWN;
			}
		}
	}

	/**
	 * Shows the sheet by morphing the FAB into the sheet.
	 *
	 * @param fab Floating action button
	 * @param showSheetDuration Duration of the sheet animation in milliseconds. Use 0 for no
	 *            animation.
	 * @param showSheetColorDuration Duration of the color animation in milliseconds. Use 0 for no
	 *            animation.
	 * @param listener Listener for animation events.
	 */
	public void morphFromFab(View fab, long showSheetDuration, long showSheetColorDuration,
			AnimationListener listener) {
		sheet.setVisibility(View.VISIBLE);
		revealSheetWithFab(fab, getFabRevealRadius(fab), getSheetRevealRadius(), showSheetDuration,
				fabColor, sheetColor, showSheetColorDuration, listener);
	}

	/**
	 * Hides the sheet by morphing the sheet into the FAB.
	 *
	 * @param fab Floating action button
	 * @param hideSheetDuration Duration of the sheet animation in milliseconds. Use 0 for no
	 *            animation.
	 * @param hideSheetColorDuration Duration of the color animation in milliseconds. Use 0 for no
	 *            animation.
	 * @param listener Listener for animation events.
	 */
	public void morphIntoFab(View fab, long hideSheetDuration, long hideSheetColorDuration,
			AnimationListener listener) {
		revealSheetWithFab(fab, getSheetRevealRadius(), getFabRevealRadius(fab), hideSheetDuration,
				sheetColor, fabColor, hideSheetColorDuration, listener);
	}

	protected void revealSheetWithFab(View fab, float startRadius, float endRadius,
			long sheetDuration, int startColor, int endColor, long sheetColorDuration,
			AnimationListener listener) {
		if (listener != null) {
			listener.onStart();
		}

		// Pass listener to the animation that will be the last to finish
		AnimationListener revealListener = (sheetDuration >= sheetColorDuration) ? listener : null;
		AnimationListener colorListener = (sheetColorDuration > sheetDuration) ? listener : null;

		// Start animations
		startCircularRevealAnim(sheet, getSheetRevealCenterX(), getSheetRevealCenterY(fab),
				startRadius, endRadius, sheetDuration, interpolator, revealListener);
		startColorAnim(sheet, startColor, endColor, sheetColorDuration, interpolator,
				colorListener);
	}

	protected void startCircularRevealAnim(View view, int centerX, int centerY, float startRadius,
			float endRadius, long duration, Interpolator interpolator,
			final AnimationListener listener) {
		// Use native circular reveal on Android 5.0+
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			// Native circular reveal uses coordinates relative to the view
			int relativeCenterX = (int) (centerX - view.getX());
			int relativeCenterY = (int) (centerY - view.getY());
			// Setup animation
			Animator anim = ViewAnimationUtils.createCircularReveal(view, relativeCenterX,
					relativeCenterY, startRadius, endRadius);
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
		} else {
			// Circular reveal library uses absolute coordinates
			// Setup animation
			SupportAnimator anim = org.aisen.weibo.sina.ui.widget.io.codetail.animation.ViewAnimationUtils
					.createCircularReveal(view, centerX, centerY, startRadius, endRadius);
			anim.setDuration((int) duration);
			anim.setInterpolator(interpolator);
			// Add listener
			anim.addListener(new SupportAnimator.SimpleAnimatorListener() {
				@Override
				public void onAnimationStart() {
					if (listener != null) {
						listener.onStart();
					}
				}

				@Override
				public void onAnimationEnd() {
					if (listener != null) {
						listener.onEnd();
					}
				}
			});
			// Start animation
			anim.start();
		}
	}

	protected void startColorAnim(final View view, final int startColor, final int endColor,
			long duration, Interpolator interpolator, final AnimationListener listener) {
		// Setup animation
		ValueAnimator anim = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
		anim.setDuration(duration);
		anim.setInterpolator(interpolator);
		// Add listeners
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
		anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				// Update background color
				Integer color = (Integer) animator.getAnimatedValue();

				// Use CardView.setCardBackgroundColor() to avoid crashes on Android < 5.0 and to
				// properly set the card's background color without removing the card's other styles
				// See https://github.com/gowong/material-sheet-fab/pull/2 and
				// https://code.google.com/p/android/issues/detail?id=77843
				if (isSupportCardView) {
					// Use setCardBackground() method if it is available
					if (setCardBackgroundColor != null) {
						try {
							setCardBackgroundColor.invoke(sheet, color);
						} catch (Exception e) {
							// Ignore exceptions since there's no other way set a support CardView's
							// background color
						}
					}
				}
				// Set background color for all other views
				else {
					view.setBackgroundColor(color);
				}
			}
		});
		// Start animation
		anim.start();
	}

	public void setSheetVisibility(int visibility) {
		sheet.setVisibility(visibility);
	}

	public boolean isSheetVisible() {
		return sheet.getVisibility() == View.VISIBLE;
	}

	/**
	 * @return Sheet reveal's center X coordinate
	 */
	public int getSheetRevealCenterX() {
		return (int) (sheet.getX() + (sheet.getWidth() / 2));
	}

	/**
	 * @return Sheet reveal's center Y coordinate
	 */
	public int getSheetRevealCenterY(View fab) {
		if (revealYDirection == RevealYDirection.UP) {
			return (int) (sheet.getY()
					+ (sheet.getHeight() * (SHEET_REVEAL_OFFSET_Y - 1) / SHEET_REVEAL_OFFSET_Y)
					- (fab.getHeight() / 2));
		}
		return (int) (sheet.getY() + (sheet.getHeight() / SHEET_REVEAL_OFFSET_Y)
				+ (fab.getHeight() / 2));
	}

	protected float getSheetRevealRadius() {
		return Math.max(sheet.getWidth(), sheet.getHeight());
	}

	protected float getFabRevealRadius(View fab) {
		return Math.max(fab.getWidth(), fab.getHeight()) / 2;
	}

	public RevealXDirection getRevealXDirection() {
		return revealXDirection;
	}

	public RevealYDirection getRevealYDirection() {
		return revealYDirection;
	}
}
