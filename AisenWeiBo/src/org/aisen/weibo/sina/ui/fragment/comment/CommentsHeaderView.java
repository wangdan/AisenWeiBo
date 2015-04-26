package org.aisen.weibo.sina.ui.fragment.comment;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.m.support.inject.ViewInject;
import com.m.ui.fragment.ABaseFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineItemView;
import org.sina.android.bean.StatusContent;

/**
 * 评论列表界面的HeaderView
 * <p/>
 * Created by wangdan on 15-2-8.
 */
public class CommentsHeaderView extends TimelineItemView {

    public CommentsHeaderView(ABaseFragment fragment, boolean showRetweeted) {
        super(fragment, showRetweeted);
    }

    public CommentsHeaderView(ABaseFragment fragment, StatusContent reStatue, boolean showRetweeted) {
        super(fragment, reStatue, showRetweeted);
    }

    public CommentsHeaderView() {

    }

    @ViewInject(id = R.id.layStatusBar)
    View layStatusBar;
    @ViewInject(id = R.id.txtAttribute)
    TextView txtAttribute;
    @ViewInject(id = R.id.layStatusBar)
    TextView layReStatus;
    @ViewInject(id = R.id.layReStatusContainer)
    View layReStatusContainer;

    private static final String ATTRIBUTE_FORMAT = "%s个赞";
    private static final String COMMENT_FORMAT = "%s条评论";
    private static final String REPOST_FORMAT = "%s人转发了该条微博";

    @Override
    public int inflateViewId() {
        return R.layout.as_lay_cmts_header;
    }

    @Override
    public void bindingData(View convertView, final StatusContent data) {
        super.bindingData(convertView, data);

        if (data.getRetweeted_status() != null && data.getRetweeted_status().getUser() != null) {
            layReStatusContainer.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    TimelineCommentFragment.launch(fragment.getActivity(), data.getRetweeted_status());
                }

            });
        }

        // 有转发微博
        if (hasStatusBar(data)) {
            setTextCount(txtAttribute, data.getAttitudes_count(), ATTRIBUTE_FORMAT);
            setTextCount(txtComment, data.getComments_count(), COMMENT_FORMAT);
            setTextCount(txtRepost, data.getReposts_count(), REPOST_FORMAT);
        }
        else {
            layStatusBar.setVisibility(View.GONE);
        }
    }

    private void setRepostClickListener(View view, final StatusContent status) {
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                TimelineRepostActivity.launch(fragment, status);
            }

        });
    }

    private boolean hasStatusBar(StatusContent data) {
        if (!TextUtils.isEmpty(data.getAttitudes_count()) && Integer.parseInt(data.getAttitudes_count()) > 0) {
            return true;
        }
        else if (!TextUtils.isEmpty(data.getComments_count()) && Integer.parseInt(data.getComments_count()) > 0) {
            return true;
        }
        else if (!TextUtils.isEmpty(data.getReposts_count()) && Integer.parseInt(data.getReposts_count()) > 0) {
            return true;
        }

        return false;
    }

    private void setTextCount(TextView textView, String count, String formatStr) {
        if (TextUtils.isEmpty(count) || Integer.parseInt(count) == 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(String.format(formatStr, AisenUtils.getCounter(Integer.parseInt(count))));
        }
    }

}
