package org.aisen.weibo.sina.ui.fragment.comment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.utils.Utils;
import org.aisen.android.support.bean.TabItem;
import org.aisen.android.support.inject.InjectUtility;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.ATabsTabLayoutFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.support.action.DoLikeAction;
import org.aisen.weibo.sina.support.bean.LikeBean;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineRepostFragment;
import org.aisen.weibo.sina.ui.widget.TimelineDetailScrollView;

import java.util.ArrayList;

/**
 * 微博详情页
 *
 * Created by wangdan on 16/1/22.
 */
public class TimelineDetailPagerFragment extends ATabsTabLayoutFragment<TabItem>
                                            implements AppBarLayout.OnOffsetChangedListener, View.OnClickListener, DoLikeAction.OnLikeCallback, SwipeRefreshLayout.OnRefreshListener {

    public static void launch(Activity from, StatusContent status) {
        FragmentArgs args = new FragmentArgs();
        args.add("status", status);

        SinaCommonActivity.launch(from, TimelineDetailPagerFragment.class, args);
    }

    public static final String ACTION_REFRESH_CMT_REPLY = "org.aisen.weibo.sina.ACTION_REFRESH_CMT_REPLY";
    public static final String ACTION_REFRESH_CMT_CREATE = "org.aisen.weibo.sina.ACTION_REFRESH_CMT_CREATE";
    public static final String ACTION_REFRESH_REPOST = "org.aisen.weibo.sina.ACTION_REFRESH_ACTION_REFRESH_REPOST";

    @ViewInject(id = R.id.swipeRefreshLayout)
    public SwipeRefreshLayout swipeRefreshLayout;
    @ViewInject(id = R.id.layHeader)
    RelativeLayout layHeader;
    @ViewInject(id = R.id.appbar)
    AppBarLayout appBarLayout;
    @ViewInject(id = R.id.toolbar)
    Toolbar toolbar;
    @ViewInject(id = R.id.layHeaderDivider)
    View layHeaderDivider;
    @ViewInject(id = R.id.txtAttitudes)
    TextView txtAttitudes;
    @ViewInject(id = R.id.action_menu)
    FloatingActionsMenu action_menu;
    @ViewInject(id = R.id.action_a)
    FloatingActionButton action_a;
    @ViewInject(id = R.id.action_b)
    FloatingActionButton action_b;
    @ViewInject(id = R.id.action_c)
    FloatingActionButton action_c;
    @ViewInject(id = R.id.overlay)
    View overlay;
    @ViewInject(id = R.id.laySroll)
    TimelineDetailScrollView laySroll;

    private StatusContent mStatusContent;

    private BizFragment bizFragment;

    @Override
    public int inflateContentView() {
        return -1;
    }

    @Override
    public int inflateActivityContentView() {
        return R.layout.ui_timeline_detail;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        InjectUtility.initInjectedView(getActivity(), this, ((BaseActivity) getActivity()).getRootView());
        layoutInit(inflater, savedInstanceState);

        // 添加HeaderView
        View itemConvertView = inflater.inflate(CommentHeaderItemView.COMMENT_HEADER_01_RES, layHeader, false);
        CommentHeaderItemView headerItemView = new CommentHeaderItemView(this, itemConvertView, mStatusContent);
        headerItemView.onBindData(layHeader, null, 0);
        layHeader.addView(itemConvertView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

        BizFragment.createBizFragment(getActivity()).createFabAnimator(action_menu);

        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStatusContent = savedInstanceState != null ? (StatusContent) savedInstanceState.getSerializable("status")
                                                    : (StatusContent) getArguments().getSerializable("status");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bizFragment = BizFragment.createBizFragment(this);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setTitle("");// R.string.timeline_detail
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setHasOptionsMenu(true);
    }

    @Override
    protected void setupTabLayout(Bundle savedInstanceSate, TabLayout tabLayout) {
        super.setupTabLayout(savedInstanceSate, tabLayout);

        tabLayout.setPadding(Utils.dip2px(getActivity(), 8), tabLayout.getPaddingTop(), tabLayout.getPaddingRight(), tabLayout.getPaddingBottom());
        tabLayout.setTabTextColors(getResources().getColor(R.color.text_54),
                getResources().getColor(R.color.text_80));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("status", mStatusContent);
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceState) {
        super.layoutInit(inflater, savedInstanceState);

        appBarLayout.addOnOffsetChangedListener(this);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        action_a.setOnClickListener(this);
        action_b.setOnClickListener(this);
        action_c.setOnClickListener(this);
        overlay.setOnClickListener(this);
        for (int i = 0; i < action_menu.getChildCount(); i++) {
            if (action_menu.getChildAt(i) instanceof AddFloatingActionButton) {
                action_menu.getChildAt(i).setOnClickListener(this);

                break;
            }
        }

        mHandler.postDelayed(initCurrentFragment, 100);
    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(getActivity(), "微博评论页");

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REFRESH_CMT_CREATE);
        filter.addAction(ACTION_REFRESH_CMT_REPLY);
        filter.addAction(ACTION_REFRESH_REPOST);
        getActivity().registerReceiver(receiver, filter);

        setLikeText();
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), "微博评论页");

        getActivity().unregisterReceiver(receiver);
    }

    private void setLikeText() {
        // 点赞数
        LikeBean likeBean = DoLikeAction.likeCache.get(mStatusContent.getId() + "");
        if (txtAttitudes != null) {
            txtAttitudes.setTag(mStatusContent);
            txtAttitudes.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    LikeBean likeBean = DoLikeAction.likeCache.get(mStatusContent.getId() + "");
                    boolean like = likeBean == null || !likeBean.isLiked();

                    bizFragment.doLike(mStatusContent, like, v, TimelineDetailPagerFragment.this);
                }

            });

            if (likeBean != null && likeBean.isLiked()) {
                if (mStatusContent.getAttitudes_count() > 0)
                    txtAttitudes.setText(String.format(getString(R.string.attitudes_format), AisenUtils.getCounter(mStatusContent.getAttitudes_count(), "+1")));
                else
                    txtAttitudes.setText(String.format(getString(R.string.attitudes_format), "+1"));
            }
            else {
                if (mStatusContent.getAttitudes_count() > 0)
                    txtAttitudes.setText(String.format(getString(R.string.attitudes_format), AisenUtils.getCounter(mStatusContent.getAttitudes_count())));
                else
                    txtAttitudes.setText("");
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);

        if (getCurrentFragment() != null && getCurrentFragment() instanceof APagingFragment &&
                ((APagingFragment) getCurrentFragment()).getRefreshView() != null) {
            laySroll.setRefreshView(((APagingFragment) getCurrentFragment()).getRefreshView());
        }

        // 切换了Page就显示Fab
        BizFragment.createBizFragment(getActivity()).getFabAnimator().show();
    }

    private Handler mHandler = new Handler();

    Runnable initCurrentFragment = new Runnable() {

        @Override
        public void run() {
            if (getCurrentFragment() != null && getCurrentFragment() instanceof APagingFragment &&
                    ((APagingFragment) getCurrentFragment()).getRefreshView() != null) {
                laySroll.setRefreshView(((APagingFragment) getCurrentFragment()).getRefreshView());
            }
            else {
                mHandler.postDelayed(initCurrentFragment, 100);
            }
        }

    };

    @Override
    protected ArrayList<TabItem> generateTabs() {
        ArrayList<TabItem> tabItems = new ArrayList<>();

        if (mStatusContent.getComments_count() > 0 || mStatusContent.getReposts_count() == 0) {
            tabItems.add(new TabItem("1", String.format(getString(R.string.comment_format), AisenUtils.getCounter(mStatusContent.getComments_count()))));
        }
        if (mStatusContent.getReposts_count() > 0) {
            tabItems.add(new TabItem("2", String.format(getString(R.string.repost_format), AisenUtils.getCounter(mStatusContent.getReposts_count()))));
        }

        return tabItems;
    }

    @Override
    protected Fragment newFragment(TabItem bean) {
        // 微博评论
        if ("1".equals(bean.getType())) {
            return TimelineCommentFragment.newInstance(mStatusContent);
        }
        // 微博转发
        else if ("2".equals(bean.getType())) {
            return TimelineRepostFragment.newInstance(mStatusContent);
        }

        return null;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int visibility = View.VISIBLE;
        // 如果是AppbarLayout滑动到了最顶端，要把这个divider隐藏掉
        if (getTablayout().getHeight() + toolbar.getHeight() - appBarLayout.getHeight() == verticalOffset) {
            visibility = View.GONE;
        }
        if (layHeaderDivider.getVisibility() != visibility)
            layHeaderDivider.setVisibility(visibility);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_cmts, menu);
        menu.removeItem(R.id.fav);
        menu.removeItem(R.id.repost);
        menu.removeItem(R.id.comment);
        if (mStatusContent.getUser() == null ||
                !mStatusContent.getUser().getIdstr().equalsIgnoreCase(AppContext.getAccount().getUser().getIdstr()))
            menu.removeItem(R.id.delete);
        AisenUtils.setStatusShareMenu(menu.findItem(R.id.share), mStatusContent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AisenUtils.onMenuClicked(this, item.getItemId(), mStatusContent);

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        // 点击了+按钮
        if (v instanceof AddFloatingActionButton) {
            if (action_menu.isExpanded()) {
                dismissOverlay();
            }
            else {
                showOverlay();

                MobclickAgent.onEvent(getActivity(), "toggle_cmt_fag");
            }

            action_menu.toggle();

            return;
        }
        // 覆盖层
        else if (v.getId() == R.id.overlay) {
        }
        // 收藏
        else if (v.getId() == R.id.action_a) {
            AisenUtils.onMenuClicked(this, R.id.fav, mStatusContent);
        }
        // 转发
        else if (v.getId() == R.id.action_b) {
            AisenUtils.onMenuClicked(this, R.id.repost, mStatusContent);
        }
        // 评论
        else if (v.getId() == R.id.action_c) {
            AisenUtils.onMenuClicked(this, R.id.comment, mStatusContent);
        }

        dismissOverlay();
        action_menu.collapse();
    }

    private void showOverlay() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(overlay, "alpha", 0.0f, 1.0f);
        animator.setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                overlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

        });
        animator.start();
    }

    private void dismissOverlay() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(overlay, "alpha", 1.0f, 0.0f);
        animator.setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                overlay.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

        });
        animator.start();
    }

    @Override
    public void onLikeFaild() {
        setLikeText();
    }

    @Override
    public void onLikeSuccess(StatusContent data, View likeView) {
        if (getActivity() == null)
            return;

        setLikeText();

        if (likeView.getTag() == data) {
            bizFragment.animScale(likeView);
        }
    }

    @Override
    public void onRefresh() {
        APagingFragment fragment = (APagingFragment) getCurrentFragment();

        onRefresh(fragment);
    }

    private void onRefresh(final APagingFragment fragment) {
        if (!isDestory()) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (fragment != null && !fragment.isRefreshing()) {
                        fragment.requestData(APagingFragment.RefreshMode.refresh);
                    }
                }

            }, 1000);
        }
    }

    public void refreshEnd() {
        swipeRefreshLayout.setRefreshing(false);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent != null ? intent.getAction() : "";

            if (ACTION_REFRESH_CMT_CREATE.equals(action) || ACTION_REFRESH_CMT_REPLY.equals(action)) {
                swipeRefreshLayout.setRefreshing(true);

                onRefresh((APagingFragment) getFragment(0));
            }
            else if (ACTION_REFRESH_REPOST.equals(action)) {
                swipeRefreshLayout.setRefreshing(true);

                onRefresh((APagingFragment) getFragment(1));
            }
        }

    };

}
