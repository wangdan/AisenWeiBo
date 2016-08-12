package org.aisen.weibo.sina.ui.fragment.comment;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.widget.MDButton;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.action.DoLikeAction;
import org.aisen.weibo.sina.support.bean.LikeBean;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;
import org.aisen.weibo.sina.ui.widget.CommentPictureView;

/**
 * Created by wangdan on 16/1/8.
 */
public class TimelineCommentItemView extends ARecycleViewItemView<StatusComment> implements View.OnClickListener, DoLikeAction.OnLikeCallback {

    public static final int LAYOUT_RES = R.layout.item_timeline_comment;

    @ViewInject(id = R.id.imgPhoto)
    ImageView imgPhoto;
    @ViewInject(id = R.id.txtName)
    TextView txtName;
    @ViewInject(id = R.id.txtDesc)
    TextView txtDesc;
    @ViewInject(id = R.id.txtContent)
    AisenTextView txtContent;
    @ViewInject(id = R.id.pic)
    CommentPictureView imgPic;

    @ViewInject(id = R.id.layRe)
    View layRe;
    @ViewInject(id = R.id.imgRePhoto)
    ImageView imgRePhoto;
    @ViewInject(id = R.id.txtReContent)
    AisenTextView txtReContent;
    @ViewInject(id = R.id.btnLike)
    MDButton btnLike;

    private ABaseFragment mFragment;
    private BizFragment bizFragment;
    int firstTop;
    int normalTop;
    private Context context;

    public TimelineCommentItemView(ABaseFragment fragment, View itemView) {
        super(fragment.getActivity(), itemView);

        this.mFragment = fragment;
        this.context = GlobalContext.getInstance();
        bizFragment = BizFragment.createBizFragment(fragment);

        firstTop = Utils.dip2px(getContext(), 16);
        normalTop = Utils.dip2px(getContext(), 8);
    }

    @Override
    public void onBindData(View convertView, StatusComment data, int position) {
        WeiBoUser user = data.getUser();
        if (user != null) {
            BitmapLoader.getInstance().display(mFragment,
                                                    AisenUtils.getUserPhoto(user),
                                                    imgPhoto, ImageConfigUtils.getLargePhotoConfig());
            bizFragment.userShow(imgPhoto, user);
            txtName.setText(AisenUtils.getUserScreenName(user));
        }
        else {
            bizFragment.userShow(imgPhoto, null);
            txtName.setText(R.string.error_cmts);
            imgPhoto.setImageResource(R.drawable.user_placeholder);
        }

        txtContent.setContent(AisenUtils.getCommentText(data.getText()));
        AisenUtils.setTextSize(txtContent);

        String createAt = AisenUtils.convDate(data.getCreated_at());
        String from = String.format("%s", Html.fromHtml(data.getSource()));
        String desc = String.format("%s %s", createAt, from);
        txtDesc.setText(desc);

        int top = position == 0 ? firstTop : normalTop;
        convertView.setPadding(convertView.getPaddingLeft(), top, convertView.getPaddingRight(), convertView.getPaddingBottom());

        if (data.isPicture()) {
            imgPic.setVisibility(View.VISIBLE);

            imgPic.display(data.getVideoUrl().getUrl_short());
        }
        else {
            imgPic.setVisibility(View.GONE);
        }

        // 源评论
        if (data.getReply_comment() != null) {
            layRe.setVisibility(View.VISIBLE);

            txtReContent.setContent(AisenUtils.getCommentText(data.getReply_comment().getText()));
            AisenUtils.setTextSize(txtReContent);

            if (data.getReply_comment().getUser() != null) {
                BitmapLoader.getInstance().display(mFragment,
                                                    AisenUtils.getUserPhoto(data.getReply_comment().getUser()),
                                                    imgRePhoto,
                                                    ImageConfigUtils.getLargePhotoConfig());
                bizFragment.userShow(imgRePhoto, data.getReply_comment().getUser());
            }
            else {
                bizFragment.userShow(imgRePhoto, null);
            }
        }
        else {
            layRe.setVisibility(View.GONE);
        }

        btnLike.setTag(data);
        setLikeBtn(data);
        btnLike.setOnClickListener(this);
    }

    private void setLikeBtn(StatusComment comment) {
        if (comment.isLiked()) {
            if (comment.getLikedCount() > 0)
                btnLike.setText(String.format(context.getString(R.string.attitudes_format), AisenUtils.getCounter(comment.getLikedCount(), "+1")));
            else
                btnLike.setText(String.format(context.getString(R.string.attitudes_format), "+1"));
        }
        else {
            if (comment.getLikedCount() > 0)
                btnLike.setText(String.format(context.getString(R.string.attitudes_format), AisenUtils.getCounter(comment.getLikedCount())));
            else
                btnLike.setText("赞");
        }
    }

    @Override
    public void onClick(View v) {
        StatusComment comment = (StatusComment) v.getTag();

        if (comment != null && v.getId() == R.id.btnLike) {
            LikeBean likeBean = DoLikeAction.likeCache.get(comment.getLikeId());
            boolean liked = likeBean == null || !likeBean.isLiked();
            comment.setLiked(liked);

            setLikeBtn(comment);

            bizFragment.doLike(comment, liked, v, this);
        }
    }

    @Override
    public void onLikeFaild(BizFragment.ILikeBean data) {
        ((StatusComment) data).setLiked(!((StatusComment) data).isLiked());

        setLikeBtn(((StatusComment) data));
    }

    @Override
    public void onLikeSuccess(BizFragment.ILikeBean data, View likeView) {
        if (likeView.getTag() == data) {
            bizFragment.animScale(likeView);

            setLikeBtn((StatusComment) data);
        }
    }

}
