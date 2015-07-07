package org.aisen.weibo.sina.ui.fragment.comment;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.action.DoLikeAction;
import org.aisen.weibo.sina.support.bean.LikeBean;
import org.aisen.weibo.sina.support.db.LikeDB;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineItemView;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineRepostFragment;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;

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

    @ViewInject(id = R.id.layStatusBar)
    View layStatusBar;
    @ViewInject(id = R.id.txtAttribute)
    TextView txtAttribute;
    @ViewInject(id = R.id.layStatusBar)
    TextView layReStatus;
    @ViewInject(id = R.id.layReStatusContainer)
    View layReStatusContainer;

    @ViewInject(id = R.id.txtReAttribute)
    TextView txtReAttribute;
    @ViewInject(id = R.id.txtReComment)
    TextView txtReComment;
    @ViewInject(id = R.id.txtReRepost)
    TextView txtReRepost;
    @ViewInject(id = R.id.layReStatusBar)
    View layReStatusBar;

    private static final String ATTRIBUTE_FORMAT = "%d%s赞";
    private static final String COMMENT_FORMAT = "%s评论";
    private static final String REPOST_FORMAT = "%s转发";

    @Override
    public int inflateViewId() {
        return R.layout.as_lay_cmts_header;
    }

    @Override
    public void bindingData(View convertView, final StatusContent data) {
        super.bindingData(convertView, data);

        if (fragment instanceof TimelineCommentFragment) {
            if (data.getRetweeted_status() != null) {
                setRepostClickListener(txtRepost, data);
                if (data.getRetweeted_status().getUser() != null)
                    setRepostClickListener(txtReRepost, data.getRetweeted_status());
            }
            else {
                setRepostClickListener(txtReRepost, data);
            }
        }

        if (data.getRetweeted_status() != null && data.getRetweeted_status().getUser() != null) {
            layReStatusContainer.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    TimelineCommentFragment.launch(fragment.getActivity(), data.getRetweeted_status());
                }

            });
        }

        // 有转发微博
        if (data.getRetweeted_status() != null) {
            setLikeText(data, txtAttribute);
            setTextCount(txtComment, data.getComments_count(), COMMENT_FORMAT);
            setTextCount(txtRepost, data.getReposts_count(), REPOST_FORMAT);

            setLikeText(data.getRetweeted_status(), txtReAttribute);
            setTextCount(txtReComment, data.getRetweeted_status().getComments_count(), COMMENT_FORMAT);
            setTextCount(txtReRepost, data.getRetweeted_status().getReposts_count(), REPOST_FORMAT);
        }
        else {
            layStatusBar.setVisibility(View.GONE);

            setLikeText(data, txtReAttribute);
            setTextCount(txtReComment, data.getComments_count(), COMMENT_FORMAT);
            setTextCount(txtReRepost, data.getReposts_count(), REPOST_FORMAT);
        }
    }

    private void setLikeText(final StatusContent data, final TextView likeTxt) {
        final LikeBean reLikeBean = DoLikeAction.likeCache.get(data.getId() + "");

            likeTxt.setTag(data);
            likeTxt.setVisibility(View.VISIBLE);
            String meLike = reLikeBean != null && reLikeBean.isLiked() ? "+1" : "";
            likeTxt.setText(String.format(ATTRIBUTE_FORMAT, data.getAttitudes_count(), meLike));
            likeTxt.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final LikeBean reLikeBean = DoLikeAction.likeCache.get(data.getId() + "");

                    final boolean like = reLikeBean == null || !reLikeBean.isLiked();

                    bizFragment.doLike(data, like, likeTxt,
                                    new DoLikeAction.OnLikeCallback() {

                                            @Override
                                            public void onLikeRefreshUI() {

                                            }

                                            @Override
                                            public void onLikeRefreshView(StatusContent data, View likeView) {
                                                animScale(likeView);

                                                setLikeText(data, (TextView) likeView);
                                            };

                                        });
                }

            });
    }

    private void setRepostClickListener(View view, final StatusContent status) {
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TimelineRepostFragment.launch(fragment.getActivity(), status);
            }

        });
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
