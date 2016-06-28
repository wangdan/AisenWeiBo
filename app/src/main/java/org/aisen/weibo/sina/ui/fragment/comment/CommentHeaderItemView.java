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
import org.aisen.android.common.utils.Utils;
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

    @ViewInject(id = R.id.txtContent)
    AisenTextView txtContent;

    @ViewInject(id = R.id.layRe)
    View layRe;

    @ViewInject(id = R.id.txtReContent)
    AisenTextView txtReContent;

    @ViewInject(id = R.id.layPicturs)
    public TimelinePicsView layPicturs;

    @ViewInject(id = R.id.txtPics)
    TextView txtPics;
    @ViewInject(id = R.id.txtVisiable)
    TextView txtVisiable;

    @ViewInject(id = R.id.layReStatusContainer)
    View layReStatusContainer;

    private int textSize = 0;
    private static Map<String, String> groupMap;
    private int vPadding;

    private ABaseFragment fragment;

    private StatusContent statusContent;

    public CommentHeaderItemView(ABaseFragment fragment, View itemView, StatusContent statusContent) {
        super(fragment.getActivity(), itemView);

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

        // 文本
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
        if (AppSettings.isPicNone() && !(fragment instanceof TimelineDetailPagerFragment)) {
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
            layPicturs.setPics(s, fragment);
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

        // 有转发微博时，设置查看原微博评论的事件
        if (statusContent.getRetweeted_status() != null && statusContent.getRetweeted_status().getUser() != null) {
            layReStatusContainer.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    TimelineDetailPagerFragment.launch(fragment.getActivity(), statusContent.getRetweeted_status());
                }

            });
        }

        // 如果没有原微博和图片，把bottom的间隙都去掉
        if (statusContent.getRetweeted_status() == null &&
                (statusContent.getPic_urls() == null || statusContent.getPic_urls().length == 0)) {
            txtContent.setPadding(txtContent.getPaddingLeft(), txtContent.getPaddingTop(), txtContent.getPaddingRight(), 0);
            layReStatusContainer.setVisibility(View.GONE);
        }
        // 如果没有图片，有原微博，底部加点空隙
        if (statusContent.getRetweeted_status() != null &&
                (statusContent.getPic_urls() == null || statusContent.getPic_urls().length == 0)) {
            txtReContent.setPadding(txtReContent.getPaddingLeft(), txtReContent.getPaddingTop(), txtReContent.getPaddingRight(), Utils.dip2px(getContext(), 8));
        }
    }

    private void setUserInfo(WeiBoUser user, TextView txtName, ImageView imgPhoto, ImageView imgVerified) {
        if (user != null) {
            txtName.setText(AisenUtils.getUserScreenName(user));

            if (imgPhoto != null) {
                BitmapLoader.getInstance().display(fragment, AisenUtils.getUserPhoto(user), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
                BizFragment.createBizFragment(fragment).userShow(imgPhoto, user);
            }

            AisenUtils.setImageVerified(imgVerified, user);
        }
        else {
            if (imgPhoto != null) {
                imgPhoto.setImageDrawable(new ColorDrawable(Color.GRAY));
                BizFragment.createBizFragment(fragment).userShow(imgPhoto, user);
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
