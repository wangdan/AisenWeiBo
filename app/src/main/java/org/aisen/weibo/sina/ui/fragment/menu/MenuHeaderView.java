package org.aisen.weibo.sina.ui.fragment.menu;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.support.inject.InjectUtility;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.activity.base.MainActivity;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;
import org.aisen.weibo.sina.ui.fragment.friendship.FriendshipPagerFragment;
import org.aisen.weibo.sina.ui.fragment.profile.ProfilePagerFragment;

/**
 * Created by wangdan on 16/1/25.
 */
public class MenuHeaderView implements View.OnClickListener {

    private MenuFragment menuFragment;

    private View mHeaderView;

    @ViewInject(id = R.id.txtFollowersNewHint)
    TextView txtFollowersNewHint;
    @ViewInject(id = R.id.layAccountMore)
    View layAccountMore;

    public MenuHeaderView(MenuFragment menuFragment) {
        this.menuFragment = menuFragment;
    }

    public void setupHeaderView(FrameLayout parent) {
        LayoutInflater inflater = LayoutInflater.from(menuFragment.getActivity());

        // 初始化View
        mHeaderView = inflater.inflate(R.layout.layout_menu_header, parent, false);
        int height = GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.material_drawer_account_header_height);
        if (Build.VERSION.SDK_INT >= 19) {
            height += SystemUtils.getStatusBarHeight(menuFragment.getActivity());
        }
        parent.addView(mHeaderView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height));
        View view = mHeaderView.findViewById(R.id.material_drawer_account_header);
        if (Build.VERSION.SDK_INT >= 19) {
            view.setPadding(view.getPaddingStart(), SystemUtils.getStatusBarHeight(menuFragment.getActivity()),
                                    view.getPaddingRight(), view.getPaddingBottom());
        }

        // 绑定视图
        InjectUtility.initInjectedView(this, mHeaderView);

        WeiBoUser user = AppContext.getAccount().getUser();

        // 头像
        ImageView imgPhoto = (ImageView) mHeaderView.findViewById(R.id.material_drawer_account_header_current);
        imgPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.imgPhoto) {
                    if (txtFollowersNewHint != null && txtFollowersNewHint.getVisibility() == View.VISIBLE) {
                        ((MainActivity) menuFragment.getActivity()).closeDrawer();

                        FriendshipPagerFragment.launch(menuFragment.getActivity(), AppContext.getAccount().getUser(), 1);
                    }
                    else {
                        ProfilePagerFragment.launch(menuFragment.getActivity(), AppContext.getAccount().getUser());;
                    }
                }
            }

        });
//        imgPhoto.setOnLongClickListener(viewOnLongClickListener);
        BitmapLoader.getInstance().display(menuFragment,
                user.getAvatar_large(), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
        // 名字
        TextView txtName = (TextView) mHeaderView.findViewById(R.id.material_drawer_account_header_name);
        txtName.setText(user.getScreen_name());

        // 背景
        ImageConfig coverConfig = new ImageConfig();
        coverConfig.setLoadfaildRes(R.drawable.bg_banner_dialog);
        coverConfig.setLoadingRes(R.drawable.bg_banner_dialog);
        final ImageView imgCover = (ImageView) mHeaderView.findViewById(R.id.material_drawer_account_header_background);
        BitmapLoader.getInstance().display(menuFragment, user.getCover_image_phone(), imgCover, coverConfig);

        // 显示账号
        mHeaderView.findViewById(R.id.material_drawer_account_header_text_section).setOnClickListener(this);
    }

    // 设置未读粉丝提醒
    void setUnreadFollowers() {
        if (!AppContext.isLoggedIn()) {
            return;
        }

        TextView txtFollowersNewHint = (TextView) mHeaderView.findViewById(R.id.txtFollowersNewHint);
        if (AppContext.getAccount().getUnreadCount() == null || AppContext.getAccount().getUnreadCount().getFollower() == 0) {
            txtFollowersNewHint.setVisibility(View.GONE);
        }
        else {
            txtFollowersNewHint.setVisibility(View.VISIBLE);
            txtFollowersNewHint.setText(String.valueOf(AppContext.getAccount().getUnreadCount().getFollower()));
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.material_drawer_account_header_text_section) {
//            switchAccountLayout();
            AccountFragment.launch(menuFragment.getActivity());
        }
    }

//    private AnimatorSet animatorSet;
//    private void switchAccountLayout() {
//        if (animatorSet != null) {
//            return;
//        }
//
//        final View layAccounts = menuFragment.findViewById(R.id.layAccounts);
//        View layMenuItems = menuFragment.findViewById(R.id.layMenuItems);
//
//        // 打开界面
//        boolean toOpen = layAccounts.getVisibility() == View.GONE;
//
//        if (accountFragment.getAccounts().size() > 0) {
//            layAccountMore.setVisibility(toOpen ? View.VISIBLE : View.GONE);
//        }
//        else {
//            layAccountMore.setVisibility(View.GONE);
//        }
//
//        // 即将打开的视图
//        final View toOpenView = toOpen ? layAccounts : layMenuItems;
//        // 即将关闭的视图
//        final View toCloseView = toOpen ? layMenuItems : layAccounts;
//
//        PropertyValuesHolder openAlpha = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
//        PropertyValuesHolder closeAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
//        ObjectAnimator openAnim = ObjectAnimator.ofPropertyValuesHolder(toOpenView, openAlpha);
//        ObjectAnimator closeAnim = ObjectAnimator.ofPropertyValuesHolder(toCloseView, closeAlpha);
//        AnimatorSet animSet = new AnimatorSet();
//        animSet.addListener(new Animator.AnimatorListener() {
//
//            @Override
//            public void onAnimationStart(Animator animation) {
//                toOpenView.setVisibility(View.VISIBLE);
//                toOpenView.setAlpha(0.0f);
//
//                toCloseView.setVisibility(View.VISIBLE);
//                toCloseView.setAlpha(1.0f);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                toOpenView.setVisibility(View.VISIBLE);
//                toCloseView.setVisibility(View.GONE);
//
//                accountFragment.getRefreshView().setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
//
//                animatorSet = null;
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//
//        });
//        animatorSet = animSet;
//        animSet.playTogether(openAnim, closeAnim);
//        animSet.setDuration(300);
//        animSet.start();
//    }

}
