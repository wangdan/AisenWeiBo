package org.aisen.weibo.sina.ui.fragment.comment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.Groups;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;
import org.aisen.weibo.sina.ui.widget.TimelinePicsView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangdan on 16/1/9.
 */
public class CommentHeaderItemView extends ARecycleViewItemView<StatusComment> implements View.OnClickListener {

    public static final int COMMENT_HEADER_01 = 1001;

    public static final int COMMENT_HEADER_01_RES = R.layout.item_timeline_comment_header;

    @ViewInject(id = R.id.imgPhoto)
    ImageView imgPhoto;
    @ViewInject(id = R.id.txtName)
    TextView txtName;
    @ViewInject(id = R.id.imgVerified)
    ImageView imgVerified;
    @ViewInject(id = R.id.txtDesc)
    TextView txtDesc;

    @ViewInject(id = R.id.btnLike)
    View btnLike;
    @ViewInject(id = R.id.imgLike)
    ImageView imgLike;
    @ViewInject(id = R.id.txtLike)
    TextView txtLike;
    @ViewInject(id = R.id.btnRepost)
    View btnRepost;
    @ViewInject(id = R.id.txtRepost)
    protected TextView txtRepost;
    @ViewInject(id = R.id.btnCmt)
    View btnComment;
    @ViewInject(id = R.id.txtComment)
    protected TextView txtComment;

    @ViewInject(id = R.id.txtContent)
    AisenTextView txtContent;

    @ViewInject(id = R.id.layRe)
    View layRe;

    @ViewInject(id = R.id.txtReContent)
    AisenTextView txtReContent;

    @ViewInject(id = R.id.layPicturs)
    public TimelinePicsView layPicturs;

    @ViewInject(id = R.id.btnMenus)
    View btnMenus;

    @ViewInject(id = R.id.txtPics)
    TextView txtPics;
    @ViewInject(id = R.id.txtVisiable)
    TextView txtVisiable;

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

    private int textSize = 0;
    private static Map<String, String> groupMap;
    private int vPadding;

    private ABaseFragment fragment;

    private StatusContent statusContent;

    public CommentHeaderItemView(ABaseFragment fragment, View itemView, StatusContent statusContent) {
        super(itemView);

        this.fragment = fragment;
        this.statusContent = statusContent;

        textSize = AppSettings.getTextSize();
        vPadding = GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.comm_v_gap);

        Groups groups = AppContext.getAccount().getGroups();
        if (groups != null && (groupMap == null || groupMap.size() != groups.getLists().size())) {
            groupMap = new HashMap<>();

            for (Group group : groups.getLists())
                groupMap.put(group.getIdstr(), group.getName());
        }

        onBindView(itemView);
        onBindData(itemView, null, 0);
    }

    @Override
    public void onBindData(View convertView, StatusComment comment, int position) {
        WeiBoUser user = statusContent.getUser();

        // userInfo
        setUserInfo(user, txtName, imgPhoto, imgVerified);

        // desc
        String createAt = "";
        if (!TextUtils.isEmpty(statusContent.getCreated_at()))
            createAt = AisenUtils.convDate(statusContent.getCreated_at());
        String from = "";
        if (!TextUtils.isEmpty(statusContent.getSource()))
            from = String.format("%s", Html.fromHtml(statusContent.getSource()));
        String desc = String.format("%s %s", createAt, from);
        txtDesc.setText(desc);

        // counter
        if (TextUtils.isEmpty(statusContent.getReposts_count()) || Integer.parseInt(statusContent.getReposts_count()) == 0) {
            txtRepost.setVisibility(View.GONE);
        }
        else {
            txtRepost.setVisibility(View.VISIBLE);
            txtRepost.setText(AisenUtils.getCounter(Integer.parseInt(statusContent.getReposts_count())));
        }
        if (btnRepost != null) {
            btnRepost.setTag(statusContent);
            btnRepost.setOnClickListener(this);

            if (statusContent.getVisible() == null || "0".equals(statusContent.getVisible().getType()))
                btnRepost.setVisibility(View.VISIBLE);
            else
                btnRepost.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(statusContent.getComments_count()) || Integer.parseInt(statusContent.getComments_count()) == 0) {
            txtComment.setVisibility(View.GONE);
        }
        else {
            txtComment.setVisibility(View.VISIBLE);
            txtComment.setText(AisenUtils.getCounter(Integer.parseInt(statusContent.getComments_count())));
        }
        if (btnComment != null) {
            btnComment.setTag(statusContent);
            btnComment.setOnClickListener(this);
        }
//        LikeBean likeBean = DoLikeAction.likeCache.get(statusContent.getId() + "");
        if (btnLike != null) {
            btnLike.setTag(statusContent);
            btnLike.setOnClickListener(this);

            if (statusContent.getAttitudes_count() > 0)
                txtLike.setText(statusContent.getAttitudes_count() + "");
            else
                txtLike.setText("");
//            imgLike.setSelected(likeBean != null && likeBean.isLiked());
        }
        // 文本
//		txtContent.setText(statusContent.getText());
        txtContent.setContent(statusContent.getText());
        setTextSize(txtContent, textSize);

        // reContent
        StatusContent reContent = statusContent.getRetweeted_status();
        if (reContent == null) {
            layRe.setVisibility(View.GONE);
        }
        else {
            layRe.setVisibility(View.VISIBLE);
            layRe.setTag(reContent);

            WeiBoUser reUser = reContent.getUser();

            String reUserName = "";
            if (reUser != null && !TextUtils.isEmpty(reUser.getScreen_name()))
                reUserName = String.format("@%s :", reUser.getScreen_name());
            txtReContent.setContent(reUserName + reContent.getText());
            // 正文
            setTextSize(txtReContent, textSize);
        }

        // pictures
        StatusContent s = statusContent.getRetweeted_status() != null ? statusContent.getRetweeted_status() : statusContent;
        if (AppSettings.isPicNone() && !(fragment instanceof TimelineCommentFragment)) {
            layPicturs.setVisibility(View.GONE);
            if (s.getPic_urls() != null && s.getPic_urls().length > 0) {
                txtPics.setText(String.format("%dPics", s.getPic_urls().length));
                txtPics.setVisibility(View.VISIBLE);
                txtPics.setTag(s);
                txtPics.setOnClickListener(this);
            }
            else
                txtPics.setVisibility(View.GONE);
        }
        else {
            txtPics.setVisibility(View.GONE);
            layPicturs.setPics(statusContent, fragment);
        }

        // group visiable
        txtVisiable.setVisibility(View.GONE);
        if (statusContent.getVisible() != null && groupMap != null) {
            String name = groupMap.get(statusContent.getVisible().getList_id());
            if (!TextUtils.isEmpty(name)) {
                txtVisiable.setText(String.format(fragment.getString(R.string.publish_group_visiable), name));
                txtVisiable.setVisibility(View.VISIBLE);

                if (layPicturs.getVisibility() == View.GONE) {
                    txtVisiable.setPadding(0, 0, 0, 0);
                }
                else {
                    txtVisiable.setPadding(0, vPadding, 0, 0);
                }
            }
        }

        if (fragment instanceof TimelineCommentFragment) {
            if (statusContent.getRetweeted_status() != null) {
                setRepostClickListener(txtRepost, statusContent);
                if (statusContent.getRetweeted_status().getUser() != null)
                    setRepostClickListener(txtReRepost, statusContent.getRetweeted_status());
            }
            else {
                setRepostClickListener(txtReRepost, statusContent);
            }
        }

        if (statusContent.getRetweeted_status() != null && statusContent.getRetweeted_status().getUser() != null) {
            layReStatusContainer.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    TimelineCommentFragment.launch(fragment.getActivity(), statusContent.getRetweeted_status());
                }

            });
        }

        // 有转发微博
        if (statusContent.getRetweeted_status() != null) {
            setLikeText(statusContent, txtAttribute);
            setTextCount(txtComment, statusContent.getComments_count(), COMMENT_FORMAT);
            setTextCount(txtRepost, statusContent.getReposts_count(), REPOST_FORMAT);

            setLikeText(statusContent.getRetweeted_status(), txtReAttribute);
            setTextCount(txtReComment, statusContent.getRetweeted_status().getComments_count(), COMMENT_FORMAT);
            setTextCount(txtReRepost, statusContent.getRetweeted_status().getReposts_count(), REPOST_FORMAT);
        }
        else {
            layStatusBar.setVisibility(View.GONE);

            setLikeText(statusContent, txtReAttribute);
            setTextCount(txtReComment, statusContent.getComments_count(), COMMENT_FORMAT);
            setTextCount(txtReRepost, statusContent.getReposts_count(), REPOST_FORMAT);
        }
    }

    private void setUserInfo(WeiBoUser user, TextView txtName, ImageView imgPhoto, ImageView imgVerified) {
        if (user != null) {
            txtName.setText(AisenUtils.getUserScreenName(user));

            if (imgPhoto != null) {
                BitmapLoader.getInstance().display(fragment, AisenUtils.getUserPhoto(user), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
                BizFragment.getBizFragment(fragment).userShow(imgPhoto, user);
            }

            AisenUtils.setImageVerified(imgVerified, user);
        }
        else {
            if (imgPhoto != null) {
                imgPhoto.setImageDrawable(new ColorDrawable(Color.GRAY));
                BizFragment.getBizFragment(fragment).userShow(imgPhoto, user);
            }

            imgVerified.setVisibility(View.GONE);
        }
    }

    public static void setTextSize(TextView textView, float size) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    private void setLikeText(final StatusContent data, final TextView likeTxt) {
//        final LikeBean reLikeBean = DoLikeAction.likeCache.get(data.getId() + "");
//
//        likeTxt.setTag(data);
//        likeTxt.setVisibility(View.VISIBLE);
//        String meLike = reLikeBean != null && reLikeBean.isLiked() ? "+1" : "";
//        likeTxt.setText(String.format(ATTRIBUTE_FORMAT, data.getAttitudes_count(), meLike));
        likeTxt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                final LikeBean reLikeBean = DoLikeAction.likeCache.get(data.getId() + "");

//                final boolean like = reLikeBean == null || !reLikeBean.isLiked();

//                bizFragment.doLike(data, like, likeTxt,
//                        new DoLikeAction.OnLikeCallback() {
//
//                            @Override
//                            public void onLikeRefreshUI() {
//
//                            }
//
//                            @Override
//                            public void onLikeRefreshView(StatusContent data, View likeView) {
//                                animScale(likeView);
//
//                                setLikeText(data, (TextView) likeView);
//                            };
//
//                        });
            }

        });
    }

    private void setRepostClickListener(View view, final StatusContent status) {
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                TimelineRepostFragment.launch(fragment.getActivity(), status);
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

    @Override
    public void onClick(View v) {

    }

}
