package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.m.ui.fragment.AAutoReleaseStripTabsFragment;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.AStripTabsFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.sina.android.bean.Group;
import org.sina.android.bean.Groups;
import org.sina.android.bean.WeiBoUser;

import java.util.ArrayList;

/**
 * 时间线的Tab页
 *
 * Created by wangdan on 15/4/14.
 */
public class TimelineTabsFragment extends AAutoReleaseStripTabsFragment<AStripTabsFragment.StripTabItem> {

    public static ABaseFragment newInstance() {
        return new TimelineTabsFragment();
    }

    private WeiBoUser loggedIn;

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        loggedIn = AppContext.getUser();

        // 2014-8-30 解决因为状态保存而导致的耗时阻塞
        if (savedInstanceSate != null) {
            ArrayList<AStripTabsFragment.StripTabItem> mChanneList = generateTabs();
            for (int i = 0; i < mChanneList.size(); i++) {
                ABaseFragment fragment = (ABaseFragment) getActivity().getFragmentManager()
                        .findFragmentByTag(makeFragmentName(i));
                if (fragment != null)
                    getActivity().getFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }

        super.layoutInit(inflater, null);

        setHasOptionsMenu(false);
    }

    @Override
    protected String configLastPositionKey() {
        return AisenUtils.getUserKey("Timeline", loggedIn);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
    }

    @Override
    protected ArrayList<AStripTabsFragment.StripTabItem> generateTabs() {
        ArrayList<AStripTabsFragment.StripTabItem> groupList = new ArrayList<AStripTabsFragment.StripTabItem>();

        // 全部好友
        groupList.add(newGroup("0", getString(R.string.timeline_all), "statusesFriendsTimeLine"));
        // 相互关注
        groupList.add(newGroup("0", getString(R.string.timeline_bilateral), "statusesBilateralTimeLine"));
        // 发给我的
        groupList.add(newGroup("0", getString(R.string.timeline_tome), "statusesToMe"));
        // 好友分组
        Groups groups = AppContext.getGroups();
        if (groups != null) {
            for (Group group : groups.getLists()) {
                groupList.add(newGroup("1", group.getName(), group.getId()));
            }
        }

        return groupList;
    }

    public static AStripTabsFragment.StripTabItem newGroup(String group, String title, String type) {
        AStripTabsFragment.StripTabItem bean = new AStripTabsFragment.StripTabItem();

        bean.setTag(group);
        bean.setTitle(title);
        bean.setType(type);

        return bean;
    }

    @Override
    protected ABaseFragment newFragment(AStripTabsFragment.StripTabItem bean) {
        // 默认分组
        if ("0".equals(bean.getTag()))
            return TimelineDefaultFragment.newInstance(bean);
        // 好友分组
        return TimelineGroupsFragment.newInstance(bean);
    }

//    private void replaceSelfInActivity() {
//        getActivity().getFragmentManager().beginTransaction()
//                .replace(R.id.content_frame, newInstance(), "MainFragment")
//                .commit();
//    }

}
