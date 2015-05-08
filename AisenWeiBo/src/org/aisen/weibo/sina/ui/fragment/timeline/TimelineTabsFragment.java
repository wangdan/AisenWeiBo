package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.m.common.utils.ActivityHelper;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.AStripTabsFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.activity.basic.MainActivity;
import org.aisen.weibo.sina.ui.fragment.basic.AMainStripTabsFragment;
import org.aisen.weibo.sina.ui.fragment.basic.MenuGenerator;
import org.sina.android.bean.Group;
import org.sina.android.bean.Groups;
import org.sina.android.bean.WeiBoUser;

import java.util.ArrayList;

/**
 * 时间线的Tab页
 *
 * Created by wangdan on 15/4/14.
 */
public class TimelineTabsFragment extends AMainStripTabsFragment {

    public static ABaseFragment newInstance() {
        return new TimelineTabsFragment();
    }

    private WeiBoUser loggedIn;

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        loggedIn = AppContext.getUser();

        // 2014-8-30 解决因为状态保存而导致的耗时阻塞
//        if (savedInstanceSate != null) {
//            ArrayList<AStripTabsFragment.StripTabItem> mChanneList = generateTabs();
//            for (int i = 0; i < mChanneList.size(); i++) {
//                ABaseFragment fragment = (ABaseFragment) getActivity().getFragmentManager()
//                        .findFragmentByTag(makeFragmentName(i));
//                if (fragment != null)
//                    getActivity().getFragmentManager().beginTransaction().remove(fragment).commit();
//            }
//        }

        super.layoutInit(inflater, null);

        setHasOptionsMenu(false);
    }

    @Override
    protected int delayGenerateTabs() {
        return 230;// 230
    }

    @Override
    protected String configLastPositionKey() {
        return AisenUtils.getUserKey("Timeline", loggedIn);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
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

    @Override
    public void onResume() {
        super.onResume();

        if (isGroupsChanged()) {
            setGroupChanged(false);

            ((MainActivity) getActivity()).onMenuSelected(MenuGenerator.generateMenu("1"), true, null);
        }

//        IntentFilter filter = new IntentFilter();
//        filter.addAction(OfflineService.ACTION_STATUS_OFFLINE);
//        getActivity().registerReceiver(mReceiver, filter);
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//
//        getActivity().unregisterReceiver(mReceiver);
//    }

    public static void setGroupChanged(boolean changed) {
        ActivityHelper.putBooleanShareData("TimelineGroupsChanged", changed);
    }

    public static boolean isGroupsChanged() {
        return ActivityHelper.getBooleanShareData("TimelineGroupsChanged", false);
    }

//    OfflineService.OfflineBroadcastReceiver mReceiver = new OfflineService.OfflineBroadcastReceiver() {
//
//        @Override
//        protected void onReceiveStatusOfflined(String groupId) {
//            ArrayList<AStripTabsFragment.StripTabItem> items = generateTabs();
//            for (int i = 0; i < items.size(); i++) {
//                if (items.get(i).getType().equalsIgnoreCase(groupId)) {
//                    ABaseFragment fragment = (ABaseFragment) getActivity().getFragmentManager()
//                            .findFragmentByTag(makeFragmentName(i));
//                    if (fragment != null && fragment instanceof TimelineGroupsFragment) {
//                        ((TimelineGroupsFragment) fragment).resetDatas();
//                        Logger.d("重置分组" + items.get(i).getTitle() + "的数据");
//                    }
//                    break;
//                }
//            }
//        }
//
//    };

}
