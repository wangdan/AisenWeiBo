package org.aisen.android.ui.widget.swipyrefresh;

/**
 * Created by oliviergoutay on 1/23/15.
 */
public enum SwipyRefreshLayoutDirection {

    TOP(0),
    BOTTOM(1),
    BOTH(2);

    private int mValue;

    SwipyRefreshLayoutDirection(int value) {
        this.mValue = value;
    }

    public static SwipyRefreshLayoutDirection getFromInt(int value) {
        for (SwipyRefreshLayoutDirection direction : SwipyRefreshLayoutDirection.values()) {
            if (direction.mValue == value) {
                return direction;
            }
        }
        return BOTH;
    }

}
