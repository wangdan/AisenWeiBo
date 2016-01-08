package org.aisen.weibo.sina.support.utils;

import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.aisen.android.support.textspan.ClickableTextViewMentionLinkOnTouchListener;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

/**
 * Created by wangdan on 16/1/7.
 */
public class AisenHelper {

    public static void previousPics(View view, StatusContent bean, int selectedIndex) {
//        Object[] tag = new Object[] { bean, selectedIndex };
//        view.setTag(tag);
//        view.setOnClickListener(PreviousArrOnClickListener);
    }

    public static void userShow(View view, WeiBoUser user) {
//        view.setTag(user);
//        view.setOnClickListener(UserShowListener);
    }

    /* 设置有@用户、话题的onTouch事件 */
    public static void bindOnTouchListener(TextView textView) {
        textView.setClickable(false);
        textView.setOnTouchListener(onTouchListener);
    }

    private static View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        ClickableTextViewMentionLinkOnTouchListener listener = new ClickableTextViewMentionLinkOnTouchListener();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return listener.onTouch(v, event);

        }
    };

}
