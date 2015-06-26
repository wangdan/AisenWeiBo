package org.aisen.weibo.sina.ui.fragment.timeline;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.support.adapter.ABaseAdapter;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.ARefreshFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.action.DoLikeAction;
import org.aisen.weibo.sina.support.bean.LikeBean;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.fragment.basic.BizFragment;
import org.aisen.weibo.sina.ui.fragment.comment.TimelineCommentFragment;
import org.aisen.weibo.sina.ui.fragment.mention.MentionTimelineFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;
import org.aisen.weibo.sina.ui.widget.TimelinePicsView;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangdan on 15/4/15.
 */
public class TimelineItemView extends ABaseAdapter.AbstractItemView<StatusContent>
                implements View.OnClickListener, DoLikeAction.OnLikeCallback {

    protected ABaseFragment fragment;
    protected BizFragment bizFragment;

    private boolean showRetweeted;
    private StatusContent reStatus;

    public TimelineItemView(ABaseFragment fragment, boolean showRetweeted) {
        this(fragment, null, showRetweeted);
    }

    // 2014-08-24 新增这个构造方法，解决转发列表，点击转发菜单时，没有带上原微博的BUG
    public TimelineItemView(ABaseFragment fragment, StatusContent reStatue, boolean showRetweeted) {
        this();

        this.fragment = fragment;
        this.reStatus = reStatue;
        try {
            bizFragment = BizFragment.getBizFragment(fragment);
        } catch (Exception e) {
        }
        this.showRetweeted = showRetweeted;
    }

    private TimelineItemView() {
        vPadding = GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.comm_v_gap);
        if (AppContext.getGroups() != null && (groupMap == null || groupMap.size() != AppContext.getGroups().getLists().size())) {
            groupMap = new HashMap<String, String>();

            for (Group group : AppContext.getGroups().getLists())
                groupMap.put(group.getIdstr(), group.getName());
        }
    }

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

    private int vPadding;
    private static Map<String, String> groupMap;

    private float textSize;

    @Override
    public int inflateViewId() {
        return R.layout.as_item_timeline;
    }

    @Override
    public void bindingData(View convertView, StatusContent data) {
        if (bizFragment == null) {
            try {
                bizFragment = BizFragment.getBizFragment(fragment);
            } catch (Exception e) {
            }
            if (bizFragment == null)
                return;
        }

        if (textSize != AppSettings.getTextSize())
            textSize = AppSettings.getTextSize();

        WeiBoUser user = data.getUser();

        // userInfo
        setUserInfo(user, txtName, imgPhoto, imgVerified);
//		setTextSize(txtName, textSize);

        // desc
        String createAt = "";
        if (!TextUtils.isEmpty(data.getCreated_at()))
            createAt = AisenUtils.convDate(data.getCreated_at());
        String from = "";
        if (!TextUtils.isEmpty(data.getSource()))
            from = String.format("%s", Html.fromHtml(data.getSource()));
        String desc = String.format("%s %s", createAt, from);
        txtDesc.setText(desc);
//		setTextSize(txtDesc, textSize * 0.9f);

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
        LikeBean likeBean = DoLikeAction.likeCache.get(data.getId() + "");
        if (btnLike != null) {
            btnLike.setTag(data);
            btnLike.setOnClickListener(this);

            if (data.getAttitudes_count() > 0)
                txtLike.setText(data.getAttitudes_count() + "");
            else
                txtLike.setText("");
            imgLike.setSelected(likeBean != null && likeBean.isLiked());
        }
        // 文本
//		txtContent.setText(data.getText());
        txtContent.setContent(data.getText());
        setTextSize(txtContent, textSize);

        // reContent
        StatusContent reContent = data.getRetweeted_status();
        if (reContent == null || !showRetweeted) {
            layRe.setVisibility(View.GONE);
        }
        else {
            layRe.setVisibility(View.VISIBLE);
            layRe.setTag(reContent);
//			layRe.setOnClickListener(this);

            WeiBoUser reUser = reContent.getUser();

            // reUserInfo
//            setUserInfo(reUser, txtReName, imgRePhoto, imgReVerified);

            // re desc
//            from = "";
//            createAt = "";
//            if (!TextUtils.isEmpty(reContent.getCreated_at()))
//                createAt = AisenUtils.convDate(reContent.getCreated_at());
//            if (!TextUtils.isEmpty(reContent.getSource()))
//                from = String.format("%s", Html.fromHtml(reContent.getSource()));
//            desc = String.format("%s %s", createAt, from);
//            txtReDesc.setText(desc);

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
            layPicturs.setPics(showRetweeted ? s : data, bizFragment, fragment);
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

        if (reStatus != null)
            data.setRetweeted_status(reStatus);
        btnMenus.setTag(data);
        btnMenus.setOnClickListener(this);
    }

    public static void setTextSize(TextView textView, float size) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    private void setUserInfo(WeiBoUser user, TextView txtName, ImageView imgPhoto, ImageView imgVerified) {
        if (user != null) {
            txtName.setText(AisenUtils.getUserScreenName(user));

            if (imgPhoto != null) {
                BitmapLoader.getInstance().display(fragment, AisenUtils.getUserPhoto(user), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
                bizFragment.userShow(imgPhoto, user);
            }

            AisenUtils.setImageVerified(imgVerified, user);
        }
        else {
            if (imgPhoto != null) {
                imgPhoto.setImageDrawable(new ColorDrawable(Color.GRAY));
                bizFragment.userShow(imgPhoto, null);
            }

            imgVerified.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        // 查看转发微博信息
        if (v.getId() == R.id.layRe) {
            StatusContent reContent = (StatusContent) v.getTag();
            TimelineRepostFragment.launch(fragment.getActivity(), reContent);
        }
        // 转发
        else if (v.getId() == R.id.btnRepost) {
            StatusContent status = (StatusContent) v.getTag();
            BizFragment.getBizFragment(fragment).statusRepost(status);
        }
        // 评论
        else if (v.getId() == R.id.btnCmt) {
            StatusContent status = (StatusContent) v.getTag();
            BizFragment.getBizFragment(fragment).commentCreate(status);
        }
        // 点赞
        else if (v.getId() == R.id.btnLike) {
            StatusContent status = (StatusContent) v.getTag();

            LikeBean likeBean = DoLikeAction.likeCache.get(status.getId() + "");
            boolean like = likeBean == null || !likeBean.isLiked();

            v.findViewById(R.id.imgLike).setSelected(like);

            BizFragment.getBizFragment(fragment).doLike(status, like, v, this);
        }
        // 溢出菜单
        else if (v.getId() == R.id.btnMenus) {
            final String[] timelineMenuArr = GlobalContext.getInstance().getResources().getStringArray(R.array.timeline_menus);
            final StatusContent status = (StatusContent) v.getTag();

            List<String> menuList = new ArrayList<String>();
            if (status.getRetweeted_status() != null && status.getRetweeted_status().getUser() != null)
                menuList.add(timelineMenuArr[0]);
//			menuList.add(timelineMenuArr[3]);
//			if (status.getVisible() == null || "0".equals(status.getVisible().getType()))
//				menuList.add(timelineMenuArr[2]);
            menuList.add(timelineMenuArr[4]);
            menuList.add(timelineMenuArr[5]);
            menuList.add(timelineMenuArr[1]);
            if (status.getUser() != null && status.getUser().getIdstr().equals(AppContext.getUser().getIdstr()))
                menuList.add(timelineMenuArr[6]);
            if (fragment instanceof MentionTimelineFragment)
                menuList.add(timelineMenuArr[7]);

            final String[] menus = new String[menuList.size()];
            for (int i = 0; i < menuList.size(); i++)
                menus[i] = menuList.get(i);

            AisenUtils.showMenuDialog(fragment,
                    v,
                    menus,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AisenUtils.timelineMenuSelected(fragment, menus[which], status);
                        }
                    });
        }
    }

    @Override
    public void onLikeRefreshUI() {
        if (fragment != null && fragment instanceof ARefreshFragment)
            ((ARefreshFragment) fragment).refreshUI();
    }

    @Override
    public void onLikeRefreshView(StatusContent data, final View likeView) {
        if (fragment.getActivity() == null)
            return;

        if (likeView.getTag() == data) {
            animScale(likeView);
        }
    }

    protected void animScale(final View likeView) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.start();
        likeView.startAnimation(scaleAnimation);
        likeView.postDelayed(new Runnable() {

            @Override
            public void run() {
                ScaleAnimation scaleAnimation = new ScaleAnimation(1.5f, 1.0f, 1.5f, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(200);
                scaleAnimation.setFillAfter(true);
                likeView.startAnimation(scaleAnimation);
            }

        }, 200);
    }

}
