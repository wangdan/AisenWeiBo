package org.aisen.weibo.sina.ui.fragment.comment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.aisen.android.support.bean.TabItem;
import org.aisen.android.ui.fragment.ATabsTabLayoutFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.service.UnreadService;
import org.aisen.weibo.sina.sinasdk.bean.UnreadCount;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.fragment.mention.MentionCmtsFragment;
import org.aisen.weibo.sina.ui.fragment.mention.MentionTimelineFragment;

import java.util.ArrayList;

/**
 * Created by wangdan on 16/2/22.
 */
public class NotificationPagerFragment extends ATabsTabLayoutFragment<TabItem> {

    public static NotificationPagerFragment newInstance(int index) {
        if (index == -1) {
            index = 0;

            UnreadCount count = AppContext.getAccount().getUnreadCount();
            if (count != null && count.getCmt() == 0) {
                if (count.getMention_status() > 0) {
                    index = 1;
                }
                else if (count.getMention_cmt() > 0) {
                    index = 2;
                }
            }
        }

        NotificationPagerFragment fragment = new NotificationPagerFragment();

        Bundle args = new Bundle();
        args.putString(SET_INDEX, String.valueOf(index));
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int inflateContentView() {
        return org.aisen.android.R.layout.comm_ui_tabs;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setTabLayout((TabLayout) getActivity().findViewById(R.id.tabLayout));

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected ArrayList<TabItem> generateTabs() {
        ArrayList<TabItem> items = new ArrayList<>();

        // 收到的评论
        if (AppContext.getAccount().getUnreadCount().getCmt() > 0) {
            items.add(new TabItem("1", String.format(getString(R.string.notification_cmt_format), AppContext.getAccount().getUnreadCount().getCmt())));
        }
        else {
            items.add(new TabItem("1", getString(R.string.notification_cmt)));
        }
        // 提及我的微博
        if (AppContext.getAccount().getUnreadCount().getMention_status() > 0) {
            items.add(new TabItem("3", String.format(getString(R.string.notification_mention_status_format), AppContext.getAccount().getUnreadCount().getMention_status())));
        }
        else {
            items.add(new TabItem("3", getString(R.string.notification_mention_status)));
        }
        // 提及我的微博
        if (AppContext.getAccount().getUnreadCount().getMention_cmt() > 0) {
            items.add(new TabItem("2", String.format(getString(R.string.notification_mention_cmt_format), AppContext.getAccount().getUnreadCount().getMention_cmt())));
        }
        else {
            items.add(new TabItem("2", getString(R.string.notification_mention_cmt)));
        }
        items.add(new TabItem("4", getString(R.string.notification_cmt_sendbyme)));

        return items;
    }

    @Override
    protected Fragment newFragment(TabItem bean) {
        // 我收到的
        if ("1".equals(bean.getType())) {
            return CommentsFragment.newInstance(CommentsFragment.Type.toMe);
        }
        // 我发出的
        else if ("4".equals(bean.getType())) {
            return CommentsFragment.newInstance(CommentsFragment.Type.byMe);
        }
        // 提及的微博
        else if ("3".equals(bean.getType())) {
            return MentionTimelineFragment.newInstance();
        }
        // 提及的评论
        else if ("2".equals(bean.getType())) {
            return MentionCmtsFragment.newInstance();
        }

        return null;
    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(getActivity(), "通知页");

        IntentFilter filter = new IntentFilter();
        filter.addAction(UnreadService.ACTION_UNREAD_CHANGED);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), "通知页");

        getActivity().unregisterReceiver(receiver);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
                if (UnreadService.ACTION_UNREAD_CHANGED.equals(intent.getAction())) {
                    if (getTablayout().getTabCount() < 4)
                        return;

                    String cmtText = getString(R.string.notification_cmt);
                    String cmtTextFormat = String.format(getString(R.string.notification_cmt_format), AppContext.getAccount().getUnreadCount().getCmt());
                    TabLayout.Tab tab = getTablayout().getTabAt(0);
                    if (AppContext.getAccount().getUnreadCount().getCmt() == 0) {
                        tab.setText(cmtText);
                    }
                    else {
                        tab.setText(cmtTextFormat);
                    }

                    String mentionStatusText = getString(R.string.notification_mention_status);
                    String mentionStatusTextFormat = String.format(getString(R.string.notification_mention_status_format), AppContext.getAccount().getUnreadCount().getMention_status());
                    tab = getTablayout().getTabAt(1);
                    if (AppContext.getAccount().getUnreadCount().getMention_status() == 0) {
                        tab.setText(mentionStatusText);
                    }
                    else {
                        tab.setText(mentionStatusTextFormat);
                    }

                    String mentionCmtText = getString(R.string.notification_mention_cmt);
                    String mentionCmtTextFormat = String.format(getString(R.string.notification_mention_cmt_format), AppContext.getAccount().getUnreadCount().getMention_cmt());
                    tab = getTablayout().getTabAt(2);
                    if (AppContext.getAccount().getUnreadCount().getMention_cmt() == 0) {
                        tab.setText(mentionCmtText);
                    }
                    else {
                        tab.setText(mentionCmtTextFormat);
                    }
                }
            }
        }

    };

}
