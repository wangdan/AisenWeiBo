package org.aisen.weibo.sina.ui.fragment.menu;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.display.FadeInDisplayer;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.InjectUtility;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.utils.AccountUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.activity.base.MainActivity;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.friendship.FriendshipPagerFragment;

import java.util.ArrayList;
import java.util.List;

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
        if (mHeaderView == null) {
            mHeaderView = inflater.inflate(R.layout.layout_menu_header, parent, false);
            int height = GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.material_drawer_account_header_height);
            if (Build.VERSION.SDK_INT >= 19) {
                height += SystemUtils.getStatusBarHeight(menuFragment.getActivity());
            }
            parent.addView(mHeaderView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height));

            // 绑定视图
            InjectUtility.initInjectedView(GlobalContext.getInstance(), this, mHeaderView);
        }
        View view = mHeaderView.findViewById(R.id.material_drawer_account_header);
        if (Build.VERSION.SDK_INT >= 19) {
            view.setPadding(view.getPaddingStart(), SystemUtils.getStatusBarHeight(menuFragment.getActivity()),
                                    view.getPaddingRight(), view.getPaddingBottom());
        }

        final WeiBoUser user = AppContext.getAccount().getUser();

        // 头像
        ImageView imgPhoto = (ImageView) mHeaderView.findViewById(R.id.material_drawer_account_header_current);
        imgPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (txtFollowersNewHint != null && txtFollowersNewHint.getVisibility() == View.VISIBLE) {
                    ((MainActivity) menuFragment.getActivity()).closeDrawer();

                    FriendshipPagerFragment.launch(menuFragment.getActivity(), AppContext.getAccount().getUser(), 1);
                }
                else {
                    BizFragment.createBizFragment(menuFragment).launchProfile(AppContext.getAccount().getUser());
                }
            }

        });
        BitmapLoader.getInstance().display(menuFragment,
                user.getAvatar_large(), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
        // 名字
        TextView txtName = (TextView) mHeaderView.findViewById(R.id.material_drawer_account_header_name);
        txtName.setText(user.getScreen_name());

        // 背景
        final ImageView imgCover = (ImageView) mHeaderView.findViewById(R.id.material_drawer_account_header_background);
        new WorkTask<Void, Void, Bitmap>() {

            @Override
            public Bitmap workInBackground(Void... params) throws TaskException {
                try {
                    BitmapLoader.BitmapBytesAndFlag bitmapBytesAndFlag = BitmapLoader.getInstance().doDownload(user.getCover_image_phone(), new ImageConfig());

                    return BitmapFactory.decodeByteArray(bitmapBytesAndFlag.bitmapBytes, 0, bitmapBytesAndFlag.bitmapBytes.length);
                } catch (Exception e) {
                }

                throw new TaskException("", "");
            }

            @Override
            protected void onSuccess(Bitmap bitmap) {
                super.onSuccess(bitmap);

                new FadeInDisplayer().loadCompletedisplay(imgCover, new BitmapDrawable(GlobalContext.getInstance().getResources(), bitmap));
            }

            @Override
            protected void onFailure(TaskException exception) {
                super.onFailure(exception);

                imgCover.setImageDrawable(new BitmapDrawable(GlobalContext.getInstance().getResources(),
                                                    BitmapFactory.decodeResource(GlobalContext.getInstance().getResources(), R.drawable.bg_banner_dialog)));
            }

        }.execute();

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

    Handler mHandler = new Handler();

    public void setAccounts() {
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                mHeaderView.findViewById(R.id.imgMove).setVisibility(View.GONE);

                List<AccountBean> accounts = AccountUtils.queryAccount();
                List<AccountBean> newAccounts = new ArrayList<>();
                for (AccountBean account : accounts) {
                    if (!AppContext.getAccount().getUid().equals(account.getUid())) {
                        newAccounts.add(account);
                    }
                }

                setAccountViews(newAccounts, mHeaderView.findViewById(R.id.layAccountFirst), 0);
                setAccountViews(newAccounts, mHeaderView.findViewById(R.id.layAccountSecond), 1);
//        setAccountViews(newAccounts, mHeaderView.findViewById(R.id.layAccountThird), 2);
            }

        }, 600);
    }

    private void setAccountViews(final List<AccountBean> accounts, final View view, final int index) {
        ImageView image = (ImageView) view.findViewById(R.id.imgAccount);
        ImageView cover = (ImageView) view.findViewById(R.id.imgCover);
        ColorDrawable grayDrawable = new ColorDrawable(Color.parseColor("#29000000"));
        cover.setImageDrawable(grayDrawable);

        view.setVisibility(accounts.size() >= index + 1 ? View.VISIBLE : View.GONE);

        if (view.getVisibility() == View.VISIBLE) {
            BitmapLoader.getInstance().display(menuFragment,
                    accounts.get(index).getUser().getAvatar_large(), image, ImageConfigUtils.getLargePhotoConfig());

            view.setTag(accounts.get(index));
            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    MobclickAgent.onEvent(menuFragment.getActivity(), "menuheader_account_change");

                    AccountBean account = (AccountBean) v.getTag();

                    changeAccount(account, v);
                }

            });
        }
    }

    private void changeAccount(final AccountBean account, final View view) {
        final ImageView imgMove = (ImageView) mHeaderView.findViewById(R.id.imgMove);
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        imgMove.setImageBitmap(bitmap);

        Rect targetRect = new Rect();
        mHeaderView.findViewById(R.id.material_drawer_account_header_current).getGlobalVisibleRect(targetRect);
        Rect fromRect = new Rect();
        view.getGlobalVisibleRect(fromRect);
        Rect rootRect = new Rect();
        mHeaderView.getGlobalVisibleRect(rootRect);

        int border = Utils.dip2px(GlobalContext.getInstance(), 1);

        int fromX = fromRect.left;
        int toX = targetRect.left + border;
        int fromY = fromRect.top - rootRect.top;
        int toY = targetRect.top - rootRect.top + border;

        PropertyValuesHolder moveX = PropertyValuesHolder.ofFloat("translationX", fromX, toX);
        PropertyValuesHolder moveY = PropertyValuesHolder.ofFloat("translationY", fromY, toY);
        PropertyValuesHolder widthP =
                PropertyValuesHolder.ofFloat("_width", view.getWidth(), targetRect.right - targetRect.left - 2 * border);
        PropertyValuesHolder heightP =
                PropertyValuesHolder.ofFloat("_height", view.getHeight(), targetRect.bottom - targetRect.top - 2 * border);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(imgMove, moveX, moveY, widthP, heightP);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Object widthObj = animation.getAnimatedValue("_width");
                Object heightObj = animation.getAnimatedValue("_height");
                if (widthObj != null && heightObj != null) {
                    imgMove.setLayoutParams(new FrameLayout.LayoutParams(Math.round(Float.parseFloat(widthObj.toString())),
                            Math.round(Float.parseFloat(heightObj.toString()))));
                }
            }

        });
        anim.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                imgMove.setVisibility(View.VISIBLE);
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                AccountFragment.login(account, true);

                setAccounts();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

        });
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(200);
        anim.start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.material_drawer_account_header_text_section) {
//            switchAccountLayout();
            AccountFragment.launch(menuFragment.getActivity());

            MobclickAgent.onEvent(menuFragment.getActivity(), "menuheader_show_accounts");
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
