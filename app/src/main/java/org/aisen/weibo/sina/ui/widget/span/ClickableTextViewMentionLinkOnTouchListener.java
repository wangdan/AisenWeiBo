package org.aisen.weibo.sina.ui.widget.span;

import android.graphics.Color;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.aisen.android.support.textspan.LongClickableLinkMovementMethod;
import org.aisen.android.support.textspan.MyURLSpan;

/**
 * Created by wangdan on 16/8/6.
 */
public class ClickableTextViewMentionLinkOnTouchListener implements View.OnTouchListener {

    private boolean find = false;

    private int color;

    public ClickableTextViewMentionLinkOnTouchListener(int color) {
        this.color = color;
    }

    public ClickableTextViewMentionLinkOnTouchListener() {
        this.color = Color.parseColor("#33969696");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Layout layout = ((TextView) v).getLayout();

        if (layout == null)
            return false;

        int x = (int) event.getX();
        int y = (int) event.getY();

        int line = layout.getLineForVertical(y);
        int offset = layout.getOffsetForHorizontal(line, x);

        TextView tv = (TextView) v;
        SpannableString value = SpannableString.valueOf(tv.getText());

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                MyURLSpan[] urlSpans = value.getSpans(0, value.length(), MyURLSpan.class);
                int findStart = 0;
                int findEnd = 0;
                for (MyURLSpan urlSpan : urlSpans) {
                    int start = value.getSpanStart(urlSpan);
                    int end = value.getSpanEnd(urlSpan);
                    if (start <= offset && offset <= end) {
                        find = true;
                        findStart = start;
                        findEnd = end;

                        break;
                    }
                }

                float lineWidth = layout.getLineWidth(line);

                find &= (lineWidth >= x);

                if (!find) {
                    WebURLEmotionSpan[] webURLEmotionSpans = value.getSpans(0, value.length(), WebURLEmotionSpan.class);
                    findStart = 0;
                    findEnd = 0;
                    for (WebURLEmotionSpan webURLEmotionSpan : webURLEmotionSpans) {
                        int start = value.getSpanStart(webURLEmotionSpan);
                        int end = value.getSpanEnd(webURLEmotionSpan);
                        if (start <= offset && offset <= end) {
                            find = true;
                            findStart = start;
                            findEnd = end;

                            value.removeSpan(webURLEmotionSpan);
                            webURLEmotionSpan.setClickDown(true);
                            value.setSpan(webURLEmotionSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                            tv.setText(value);

                            break;
                        }
                    }

                    lineWidth = layout.getLineWidth(line);

                    find &= (lineWidth >= x);
                }

                if (find) {
                    try {
                        LongClickableLinkMovementMethod.getInstance().onTouchEvent(tv, value, event);
                    } catch (NullPointerException e) {
                    }
                    BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(color);
                    int lineHeight = tv.getLineHeight();
                    value.setSpan(backgroundColorSpan, findStart, findEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    // Android has a bug, sometime TextView wont change its value
                    // when you modify SpannableString,
                    // so you must setText again, test on Android 4.3 Nexus4
                    tv.setText(value);
                }

                return find;
            case MotionEvent.ACTION_MOVE:
                if (find) {
                    LongClickableLinkMovementMethod.getInstance().onTouchEvent(tv, value, event);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                WebURLEmotionSpan[] webURLEmotionSpans = value.getSpans(0, value.length(), WebURLEmotionSpan.class);
                for (WebURLEmotionSpan webURLEmotionSpan : webURLEmotionSpans) {
                    int start = value.getSpanStart(webURLEmotionSpan);
                    int end = value.getSpanEnd(webURLEmotionSpan);

                    if (webURLEmotionSpan.isClickDown()) {

                        if (MotionEvent.ACTION_UP == event.getActionMasked()) {
                            webURLEmotionSpan.onClick(tv);
                        }

                        value.removeSpan(webURLEmotionSpan);
                        webURLEmotionSpan.setClickDown(false);
                        value.setSpan(webURLEmotionSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        tv.setText(value);

                        break;
                    }
                }
                if (find) {
                    LongClickableLinkMovementMethod.getInstance().onTouchEvent(tv, value, event);
                    LongClickableLinkMovementMethod.getInstance().removeLongClickCallback();
                }
                BackgroundColorSpan[] backgroundColorSpans = value.getSpans(0, value.length(), BackgroundColorSpan.class);
                for (BackgroundColorSpan backgroundColorSpan : backgroundColorSpans) {
                    value.removeSpan(backgroundColorSpan);
                }
                tv.setText(value);
                find = false;
                break;
        }

        return false;

    }

}
