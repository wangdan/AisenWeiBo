package org.aisen.weibo.sina.ui.fragment.comment;

import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.compress.TimelineThumbBitmapCompress;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;

/**
 * 评论列表ItemView
 *
 * Created by wangdan on 16/1/7.
 */
public class CommentItemView extends ARecycleViewItemView<StatusComment> implements View.OnClickListener {

    @ViewInject(id = R.id.imgPhoto)
    ImageView imgPhoto;
    @ViewInject(id = R.id.txtName)
    TextView txtName;
    @ViewInject(id = R.id.txtDesc)
    TextView txtDesc;
    @ViewInject(id = R.id.txtContent)
    AisenTextView txtContent;

    @ViewInject(id = R.id.layRe)
    View layRe;
    @ViewInject(id = R.id.imgRePhoto)
    ImageView imgRePhoto;
    @ViewInject(id = R.id.txtReContent)
    AisenTextView txtReContent;

//    @ViewInject(id = R.id.layStatus)
    View layStatus;
//    @ViewInject(id = R.id.layDivider)
    View layDivider;
//    @ViewInject(id = R.id.txtStatusContent)
    AisenTextView txtStatusContent;
//    @ViewInject(id = R.id.img)
    ImageView imgView;

    @ViewInject(id = R.id.btnMenus)
    protected View btnMenus;

    private ABaseFragment fragment;

    private StatusContent mStatus;

    public CommentItemView(ABaseFragment fragment, View itemView) {
        super(fragment.getActivity(), itemView);

        this.fragment = fragment;
    }

    @Override
    public void onBindData(View convertView, StatusComment data, int position) {
        WeiBoUser user = data.getUser();
        if (user != null) {
            BitmapLoader.getInstance().display(fragment,
                    AisenUtils.getUserPhoto(user),
                    imgPhoto, ImageConfigUtils.getLargePhotoConfig());
            BizFragment.createBizFragment(fragment).userShow(imgPhoto, user);
            txtName.setText(AisenUtils.getUserScreenName(user));
        }
        else {
            BizFragment.createBizFragment(fragment).userShow(imgPhoto, null);
            txtName.setText(R.string.error_cmts);
            imgPhoto.setImageResource(R.drawable.user_placeholder);
        }

        txtContent.setContent(AisenUtils.getCommentText(data.getText()));
        AisenUtils.setTextSize(txtContent);

        String createAt = AisenUtils.convDate(data.getCreated_at());
        String from = String.format("%s", Html.fromHtml(data.getSource()));
        String desc = String.format("%s %s", createAt, from);
        txtDesc.setText(desc);

        // 源评论
        if (data.getReply_comment() != null) {
            layRe.setVisibility(View.VISIBLE);

            txtReContent.setContent(AisenUtils.getCommentText(data.getReply_comment().getText()));
            AisenUtils.setTextSize(txtReContent);

            if (data.getReply_comment().getUser() != null) {
                BitmapLoader.getInstance().display(fragment,
                        AisenUtils.getUserPhoto(data.getReply_comment().getUser()),
                        imgRePhoto, ImageConfigUtils.getLargePhotoConfig());
                BizFragment.createBizFragment(fragment).userShow(imgRePhoto, data.getReply_comment().getUser());
            }
            else {
                BizFragment.createBizFragment(fragment).userShow(imgRePhoto, null);
            }
        }
        else {
            layRe.setVisibility(View.GONE);
        }

        if (layStatus != null) {
            if (data.getStatus() != null && mStatus == null) {
                layDivider.setVisibility(View.VISIBLE);
                layStatus.setVisibility(View.VISIBLE);
                layStatus.setTag(data.getStatus());
                layStatus.setOnClickListener(this);

                txtStatusContent.setContent(data.getStatus().getText());
                AisenUtils.setTextSize(txtStatusContent);

                String image = null;

                // 先取微博的第一张图
                StatusContent status = data.getStatus();
                if (status != null && status.getRetweeted_status() != null)
                    status = status.getRetweeted_status();
                if (status == null || status.getPic_urls() == null || status.getPic_urls().length == 0) {
                }
                else {
                    image = status.getPic_urls()[0].getThumbnail_pic();
                }
                // 没图就取头像
                if (TextUtils.isEmpty(image) && status.getUser() != null) {
                    image = status.getUser().getAvatar_large();
                }
                if (!TextUtils.isEmpty(image)) {
                    imgView.setVisibility(View.VISIBLE);
                    ImageConfig config = new ImageConfig();
                    config.setId("comments");
                    config.setLoadfaildRes(R.drawable.bg_timeline_loading);
                    config.setLoadingRes(R.drawable.bg_timeline_loading);
                    config.setMaxWidth(300);
                    config.setMaxHeight(300);
                    config.setBitmapCompress(TimelineThumbBitmapCompress.class);

                    BitmapLoader.getInstance().display(fragment, image, imgView, config);
                }
                else {
                    imgView.setVisibility(View.GONE);
                }

                BizFragment.createBizFragment(fragment).bindOnTouchListener(txtStatusContent);
            }
            else {
                layDivider.setVisibility(View.GONE);
                layStatus.setVisibility(View.GONE);
            }
        }

        if (btnMenus != null) {
            btnMenus.setTag(data);
            btnMenus.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {

    }

}
