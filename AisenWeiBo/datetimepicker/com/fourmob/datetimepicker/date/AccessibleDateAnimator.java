package com.fourmob.datetimepicker.date;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ViewAnimator;

public class AccessibleDateAnimator extends ViewAnimator {
    private long mDateMillis;

    public AccessibleDateAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDateMillis(long dateMillis) {
        mDateMillis = dateMillis;
    }

    /**
     * Announce the currently-selected date when launched.
     */
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Clear the event's current text so that only the current date will be spoken.
            event.getText().clear();
            int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR |
                    DateUtils.FORMAT_SHOW_WEEKDAY;

            String dateString = DateUtils.formatDateTime(getContext(), mDateMillis, flags);
            event.getText().add(dateString);
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }
}