package org.aisen.weibo.sina.ui.fragment.comment;

import android.view.View;

import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;

/**
 * Created by wangdan on 16/1/9.
 */
public class CommentHeader01ItemView extends ARecycleViewItemView<StatusComment> {

    public static final int COMMENT_HEADER_01 = 1001;

    public static final int COMMENT_HEADER_01_RES = R.layout.item_comment_header01;

    public CommentHeader01ItemView(View itemView) {
        super(itemView);
    }

    @Override
    public void onBindData(View convertView, StatusComment data, int position) {

    }

}
