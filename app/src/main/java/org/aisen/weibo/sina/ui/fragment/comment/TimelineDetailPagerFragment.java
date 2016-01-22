package org.aisen.weibo.sina.ui.fragment.comment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.support.bean.TabItem;
import org.aisen.android.support.inject.InjectUtility;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.android.ui.fragment.ATabsTabLayoutFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineRepostFragment;

import java.util.ArrayList;

/**
 * 微博详情页
 *
 * Created by wangdan on 16/1/22.
 */
public class TimelineDetailPagerFragment extends ATabsTabLayoutFragment<TabItem> implements AppBarLayout.OnOffsetChangedListener {

    public static void launch(Activity from, StatusContent status) {
        FragmentArgs args = new FragmentArgs();
        args.add("status", status);

        SinaCommonActivity.launch(from, TimelineDetailPagerFragment.class, args);
    }

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

    private StatusContent mStatusContent;

    @Override
    public int inflateActivityContentView() {
        return R.layout.ui_timeline_detail;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        InjectUtility.initInjectedView(this, ((BaseActivity) getActivity()).getRootView());
        layoutInit(inflater, savedInstanceState);

        // 添加HeaderView
        View itemConvertView = inflater.inflate(CommentHeaderItemView.COMMENT_HEADER_01_RES, layHeader, false);
        CommentHeaderItemView headerItemView = new CommentHeaderItemView(this, itemConvertView, mStatusContent);
        headerItemView.onBindData(itemConvertView, null, 0);
        layHeader.addView(itemConvertView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

        return null;
    }

    @Override
    public int inflateContentView() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStatusContent = savedInstanceState != null ? (StatusContent) savedInstanceState.getSerializable("status")
                                                    : (StatusContent) getArguments().getSerializable("status");

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.timeline_detail);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setHasOptionsMenu(true);
    }

    @Override
    protected void setupTabLayout(Bundle savedInstanceSate, TabLayout tabLayout) {
        super.setupTabLayout(savedInstanceSate, tabLayout);

        tabLayout.setPadding(Utils.dip2px(16), tabLayout.getPaddingTop(), tabLayout.getPaddingRight(), tabLayout.getPaddingBottom());
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

        // 点赞数
        if (mStatusContent.getAttitudes_count() == 0) {
            txtAttitudes.setText("");
        }
        else {
            txtAttitudes.setText(String.format(getString(R.string.attitudes_format), mStatusContent.getAttitudes_count()));
        }
    }

    @Override
    protected ArrayList<TabItem> generateTabs() {
        ArrayList<TabItem> tabItems = new ArrayList<>();

        if (mStatusContent.getComments_count() > 0 || mStatusContent.getReposts_count() == 0) {
            tabItems.add(new TabItem("1", String.format(getString(R.string.comment_format), mStatusContent.getComments_count())));
        }
        if (mStatusContent.getReposts_count() > 0) {
            tabItems.add(new TabItem("2", String.format(getString(R.string.repost_format), mStatusContent.getReposts_count())));
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

}
