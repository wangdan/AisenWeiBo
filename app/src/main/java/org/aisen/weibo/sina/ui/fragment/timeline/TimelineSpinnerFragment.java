package org.aisen.weibo.sina.ui.fragment.timeline;

import android.view.View;
import android.widget.AdapterView;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.ui.activity.base.MainActivity;

import java.util.List;

/**
 * 微博的Spinner选择
 *
 * Created by wangdan on 16/1/2.
 */
public class TimelineSpinnerFragment extends ABaseFragment implements MainActivity.MainSpinnerNavigation {

    public static TimelineSpinnerFragment newInstance() {
        return new TimelineSpinnerFragment();
    }

    ABaseFragment mFragment;

    @Override
    protected int inflateContentView() {
        return R.layout.ui_timeline_spinner;
    }

    @Override
    public String[] generateItems() {
        String[] items = new String[AppContext.getAccount().getGroups().getLists().size() + 3];
        items[0] = GlobalContext.getInstance().getResources().getString(R.string.timeline_all);
        items[1] = GlobalContext.getInstance().getResources().getString(R.string.timeline_bilateral);
        items[2] = GlobalContext.getInstance().getResources().getString(R.string.timeline_tome);

        for (int i = 0; i < AppContext.getAccount().getGroups().getLists().size(); i++) {
            items[3 + i] = AppContext.getAccount().getGroups().getLists().get(i).getName();
        }

        return items;
    }

    @Override
    public int initPosition() {
        return ActivityHelper.getIntShareData("Timeline_position" + AppContext.getAccount().getUid(), 0);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ActivityHelper.putIntShareData("Timeline_position" + AppContext.getAccount().getUid(), position);

        if (position <= 2) {
            mFragment = TimelineDefFragment.newInstance(position);
        }
        else {
            List<Group> groupList = AppContext.getAccount().getGroups().getLists();

            mFragment = TimelineGroupsFragment.newInstance(groupList.get(position - 3));
        }

        getActivity().getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.layTimelineContainer, mFragment, "TimelineFragment")
                        .commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mFragment != null && isActivityRunning() && !((BaseActivity) getActivity()).isDestory())
            getFragmentManager().beginTransaction().remove(mFragment).commit();
    }

}
