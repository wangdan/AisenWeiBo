package org.aisen.weibo.sina.ui.widget.sheetfab;

/**
 * Created by Gordon Wong on 7/10/2015.
 *
 * Interface for FABs that support a set of animations.
 */
public interface AnimatedFab {

	/**
	 * Shows the FAB.
	 */
	void show();

	/**
	 * Shows the FAB and sets the FAB's translation.
	 *
	 * @param translationX translation X value
	 * @param translationY translation Y value
	 */
	void show(float translationX, float translationY);

	/**
	 * Hides the FAB.
	 */
	void hide();

}
