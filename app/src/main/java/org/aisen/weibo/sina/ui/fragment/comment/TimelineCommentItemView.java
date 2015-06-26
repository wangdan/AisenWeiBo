package org.aisen.weibo.sina.ui.fragment.comment;

import android.view.View;

import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;

/**
 * Created by wangdan on 15/4/25.
 */
public class TimelineCommentItemView extends CommentItemView {

    @ViewInject(id = R.id.divider)
    View divider;

    public TimelineCommentItemView(ABaseFragment fragment) {
        super(fragment);
    }

    @Override
    public int inflateViewId() {
        return R.layout.as_item_timeline_comment;
    }

    @Override
    public void bindingData(View convertView, StatusComment data) {
        super.bindingData(convertView, data);

        btnMenus.setVisibility(View.GONE);

        if (getPosition() == getSize() - 1) {
            divider.setVisibility(View.INVISIBLE);
        }
        else {
            divider.setVisibility(View.VISIBLE);
        }
    }

}
