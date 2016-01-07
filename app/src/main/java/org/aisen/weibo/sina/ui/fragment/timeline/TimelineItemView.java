package org.aisen.weibo.sina.ui.fragment.timeline;

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
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.utils.AisenHelper;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.fragment.comment.TimelineCommentFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;
import org.aisen.weibo.sina.ui.widget.TimelinePicsView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangdan on 16/1/4.
 */
public class TimelineItemView extends ARecycleViewItemView<StatusContent> implements View.OnClickListener {

    public static final int LAYOUT_RES = R.layout.item_timeline;

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

    private int textSize = 0;
    private static Map<String, String> groupMap;
    private int vPadding;

    private ABaseFragment fragment;

    public TimelineItemView(View convertView, ABaseFragment fragment) {
        super(convertView);

        this.fragment = fragment;

        textSize = AppSettings.getTextSize();
        vPadding = GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.comm_v_gap);

        Groups groups = AppContext.getAccount().getGroups();
        if (groups != null && (groupMap == null || groupMap.size() != groups.getLists().size())) {
            groupMap = new HashMap<>();

            for (Group group : groups.getLists())
                groupMap.put(group.getIdstr(), group.getName());
        }
    }

    @Override
    public void onBindData(View convertView, StatusContent data, int position) {
        WeiBoUser user = data.getUser();

        // userInfo
        setUserInfo(user, txtName, imgPhoto, imgVerified);

        // desc
        String createAt = "";
        if (!TextUtils.isEmpty(data.getCreated_at()))
            createAt = AisenUtils.convDate(data.getCreated_at());
        String from = "";
        if (!TextUtils.isEmpty(data.getSource()))
            from = String.format("%s", Html.fromHtml(data.getSource()));
        String desc = String.format("%s %s", createAt, from);
        txtDesc.setText(desc);

        // counter
        if (TextUtils.isEmpty(data.getReposts_count()) || Integer.parseInt(data.getReposts_count()) == 0) {
            txtRepost.setVisibility(View.GONE);
        }
        else {
            txtRepost.setVisibility(View.VISIBLE);
            txtRepost.setText(AisenUtils.getCounter(Integer.parseInt(data.getReposts_count())));
        }
        if (btnRepost != null) {
            btnRepost.setTag(data);
            btnRepost.setOnClickListener(this);

            if (data.getVisible() == null || "0".equals(data.getVisible().getType()))
                btnRepost.setVisibility(View.VISIBLE);
            else
                btnRepost.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(data.getComments_count()) || Integer.parseInt(data.getComments_count()) == 0) {
            txtComment.setVisibility(View.GONE);
        }
        else {
            txtComment.setVisibility(View.VISIBLE);
            txtComment.setText(AisenUtils.getCounter(Integer.parseInt(data.getComments_count())));
        }
        if (btnComment != null) {
            btnComment.setTag(data);
            btnComment.setOnClickListener(this);
        }
//        LikeBean likeBean = DoLikeAction.likeCache.get(data.getId() + "");
        if (btnLike != null) {
            btnLike.setTag(data);
            btnLike.setOnClickListener(this);

            if (data.getAttitudes_count() > 0)
                txtLike.setText(data.getAttitudes_count() + "");
            else
                txtLike.setText("");
//            imgLike.setSelected(likeBean != null && likeBean.isLiked());
        }
        // 文本
//		txtContent.setText(data.getText());
        txtContent.setContent(data.getText());
        setTextSize(txtContent, textSize);

        // reContent
        StatusContent reContent = data.getRetweeted_status();
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
        StatusContent s = data.getRetweeted_status() != null ? data.getRetweeted_status() : data;
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
            layPicturs.setPics(data, fragment);
        }

        // group visiable
        txtVisiable.setVisibility(View.GONE);
        if (data.getVisible() != null && groupMap != null) {
            String name = groupMap.get(data.getVisible().getList_id());
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

        btnMenus.setTag(data);
        btnMenus.setOnClickListener(this);
    }

    private void setUserInfo(WeiBoUser user, TextView txtName, ImageView imgPhoto, ImageView imgVerified) {
        if (user != null) {
            txtName.setText(AisenUtils.getUserScreenName(user));

            if (imgPhoto != null) {
                BitmapLoader.getInstance().display(fragment, AisenUtils.getUserPhoto(user), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
                AisenHelper.userShow(imgPhoto, user);
            }

            AisenUtils.setImageVerified(imgVerified, user);
        }
        else {
            if (imgPhoto != null) {
                imgPhoto.setImageDrawable(new ColorDrawable(Color.GRAY));
                AisenHelper.userShow(imgPhoto, null);
            }

            imgVerified.setVisibility(View.GONE);
        }
    }

    public static void setTextSize(TextView textView, float size) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    @Override
    public void onClick(View v) {

    }

}
