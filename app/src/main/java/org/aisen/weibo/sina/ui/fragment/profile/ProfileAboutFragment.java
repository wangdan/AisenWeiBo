package org.aisen.weibo.sina.ui.fragment.profile;

import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.FriendshipShow;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.GroupListed;
import org.aisen.weibo.sina.sinasdk.bean.GroupMemberListed;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.activity.profile.WeiboClientActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;

import java.util.ArrayList;

/**
 * Created by wangdan on 16/1/12.
 */
public class ProfileAboutFragment extends ABaseFragment 
                                    implements BizFragment.OnModifyUserRemarkCallback,
                                                View.OnClickListener,
                                                ProfilePagerFragment.IUserProfileRefresh,
                                                BizFragment.OnCreateFriendshipCallback,
                                                BizFragment.OnDestoryFriendshipCallback,
                                                BizFragment.OnDestoryFollowerCallback {

    public static ProfileAboutFragment newInstance(WeiBoUser user) {
        Bundle args = new Bundle();
        args.putSerializable("mUser", user);

        ProfileAboutFragment fragment = new ProfileAboutFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @ViewInject(id = R.id.item01)
    View item01;
    @ViewInject(id = R.id.item02)
    View item02;
    @ViewInject(id = R.id.item03)
    View item03;
    @ViewInject(id = R.id.item04)
    View item04;
    @ViewInject(id = R.id.item05)
    View item05;
    @ViewInject(id = R.id.scrollView)
    ScrollView scrollView;
    @ViewInject(id = R.id.btnMention, click = "onClick")
    TextView btnMention;
    @ViewInject(id = R.id.btnDM, click = "onClick")
    TextView btnDM;
    @ViewInject(id = R.id.btnDetail, click = "onClick")
    TextView btnDetail;

    private WeiBoUser mUser;
    private FriendshipShow mFriendship;
    private GroupMemberListed mGroupMemberListed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = savedInstanceState == null ? (WeiBoUser) getArguments().getSerializable("mUser")
                                          : (WeiBoUser) savedInstanceState.getSerializable("mUser");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("mUser", mUser);
        outState.putSerializable("friendship", mFriendship);
        outState.putSerializable("groupListed", mGroupMemberListed);
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_profile_about;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        if (savedInstanceSate == null) {
            loadFriendship();
        }
    }

    void setProfile() {
        View reasonView = item03;
        View locationView = item04;
        View descView = item05;
        View remarkView = item01;
        View groupsView = item02;

        // 认证原因
        if (!TextUtils.isEmpty(mUser.getVerified_reason())) {
            reasonView.setVisibility(View.VISIBLE);
            setItemTitle(reasonView, getString(R.string.profile_ver_reason_hint));
            setItemBody(reasonView, mUser.getVerified_reason() + "");
        } else {
            reasonView.setVisibility(View.GONE);
        }
        // 所在地
        setItemTitle(locationView, getString(R.string.profile_location_hint));
        setItemBody(locationView, mUser.getLocation() + "");
        // 简介
        setItemTitle(descView, getString(R.string.profile_des_hint02));
        if (!TextUtils.isEmpty(mUser.getDescription())) {
            setItemBody(descView, mUser.getDescription());
        } else {
            setItemBody(descView, getString(R.string.profile_des_none));
        }
        // 备注
        setItemTitle(remarkView, getString(R.string.profile_remark_hint));
        remarkView.setVisibility(mFriendship == null || !mFriendship.getSource().getFollowing() ? View.GONE : View.VISIBLE);
        if (mFriendship != null) {
            TextView txtRemark = (TextView) remarkView.findViewById(R.id.txtBody);
            try {
                BizFragment.createBizFragment(this).modifyUserMark(remarkView.findViewById(R.id.btnItem), mUser, this);
            } catch (Exception e) {
            }
            if (!TextUtils.isEmpty(mUser.getRemark()))
                txtRemark.setText(mUser.getRemark());
            else
                txtRemark.setText(R.string.profile_remark_none02);
        }
        // 分组
        TextView txtGroups = (TextView) groupsView.findViewById(R.id.txtBody);
        groupsView.setVisibility(mGroupMemberListed == null ? View.GONE : View.VISIBLE);
        groupsView.findViewById(R.id.btnItem).setTag("groups");
        groupsView.findViewById(R.id.btnItem).setOnClickListener(this);
        if (mGroupMemberListed != null) {
            if (mGroupMemberListed.getLists().size() == 0) {
                txtGroups.setText(R.string.profile_group_none);
            } else {
                StringBuffer sb = new StringBuffer();
                for (GroupListed groupListed : mGroupMemberListed.getLists())
                    sb.append(groupListed.getName()).append(" ");

                txtGroups.setText(sb.toString().trim());
            }
        }

        // 最后一个Divider不显示
        if (item05.getVisibility() == View.VISIBLE) {
            item05.findViewById(R.id.viewDivider).setVisibility(View.GONE);
        } else if (item04.getVisibility() == View.VISIBLE) {
            if (item04.getVisibility() == View.VISIBLE) {
                item04.findViewById(R.id.viewDivider).setVisibility(View.GONE);
            }
        }
    }

    private void setItemTitle(View view, String text) {
        ((TextView) view.findViewById(R.id.txtTitle)).setText(text);
    }

    private void setItemBody(View view, String text) {
        ((TextView) view.findViewById(R.id.txtBody)).setText(text);
    }

    private void loadFriendship() {
        // 加载用户关系
        if (mUser != null && !mUser.getIdstr().equals(AppContext.getAccount().getUser().getIdstr())) {
            new FriendshipTask().execute();
        }
    }

    public View getScrollView() {
        return scrollView;
    }

    @Override
    public void onModifyUserRemark(String remark) {
        mUser.setRemark(remark);

        setProfile();
    }

    @Override
    public void onFriendshipCreated(WeiBoUser targetUser) {
        if (mFriendship != null)
            mFriendship.getSource().setFollowing(true);

        mUser.setRecentStatusId(targetUser.getRecentStatusId());

        mGroupMemberListed = new GroupMemberListed();
        mGroupMemberListed.setLists(new ArrayList<GroupListed>());
        mGroupMemberListed.setUid(mUser.getIdstr());

        setProfile();
        setGroupList();

        // 设置Pager的菜单显示
        Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(SinaCommonActivity.FRAGMENT_TAG);
        ((ProfilePagerFragment) fragment).setFriendshipShow(mFriendship);
    }

    @Override
    public void onFriendshipDestoryed(WeiBoUser targetUser) {
        if (mFriendship != null)
            mFriendship.getSource().setFollowing(false);

        mGroupMemberListed = null;

        mUser.setRemark("");
        setProfile();

        // 设置Pager的菜单显示
        Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(SinaCommonActivity.FRAGMENT_TAG);
        ((ProfilePagerFragment) fragment).setFriendshipShow(mFriendship);
    }

    @Override
    public void onDestoryFollower(WeiBoUser user) {
        if (mFriendship != null)
            mFriendship.getTarget().setFollowing(false);

        // 设置Pager的菜单显示
        Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(SinaCommonActivity.FRAGMENT_TAG);
        ((ProfilePagerFragment) fragment).setFriendshipShow(mFriendship);
    }

    @Override
    public void onClick(View v) {
        String tag = v.getTag() == null ? null : v.getTag().toString();
        // 分组管理
        if ("groups".equalsIgnoreCase(tag)) {
            setGroupList();
        }
        // @Ta
        else if (v == btnMention) {
            MobclickAgent.onEvent(getActivity(), "mention_ta");

            BizFragment.createBizFragment(this).mentionUser(getActivity(), mUser);
        }
        // 私信
        else if (v == btnDM) {
            MobclickAgent.onEvent(getActivity(), "dm_ta");

            WeiboClientActivity.launchChat(getActivity(), mUser.getIdstr());
        }
        // 详细信息
        else if (v == btnDetail) {
            MobclickAgent.onEvent(getActivity(), "more_ta");

            WeiboClientActivity.launchProfile(getActivity(), mUser.getIdstr());
        }
    }

    /**
     * 设置分组信息
     */
    Dialog groupDialog;

    public void setGroupList() {
        if (mGroupMemberListed == null)
            return;

        if (groupDialog != null && groupDialog.isShowing())
            groupDialog.dismiss();

        String[] items = new String[AppContext.getAccount().getGroups().getLists().size()];
        final boolean[] checkedItems = new boolean[AppContext.getAccount().getGroups().getLists().size()];
        final boolean[] editCheckedItems = new boolean[AppContext.getAccount().getGroups().getLists().size()];

        for (int i = 0; i < AppContext.getAccount().getGroups().getLists().size(); i++) {
            Group group = AppContext.getAccount().getGroups().getLists().get(i);

            items[i] = group.getName();
            checkedItems[i] = false;
            editCheckedItems[i] = false;
            for (GroupListed groupListed : mGroupMemberListed.getLists()) {
                if (groupListed.getIdstr().equals(group.getIdstr())) {
                    checkedItems[i] = true;
                    editCheckedItems[i] = true;
                    break;
                }
            }
        }

//        View customTitle = View.inflate(getActivity(), R.layout.lay_group_dialogtitle, null);
//        customTitle.findViewById(R.id.btnSettings).setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // 设置分组
//                GroupSortFragment.lanuch(getActivity());
//            }
//        });
        AlertDialogWrapper.Builder dialogBuilder = new AlertDialogWrapper.Builder(getActivity())
                .setTitle(R.string.profile_group_setting)
                .setMultiChoiceItems(items, editCheckedItems, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        editCheckedItems[which] = isChecked;
                    }
                });
        try {
            try {
                // 解决有些设备版本较低的BUG，没查这个方法的最低版本要求
                dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        groupDialog = null;
                    }
                });
            } catch (NoSuchMethodError e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
        }
        groupDialog = dialogBuilder.setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new SetGroupTask().execute(checkedItems, editCheckedItems);
                    }
                })
                .show();
    }

    public void setUser(WeiBoUser user) {
        this.mUser = user;
        setProfile();
    }

    // 设置分组
    class SetGroupTask extends WorkTask<boolean[], String, Boolean> {

        @Override
        protected void onPrepare() {
            super.onPrepare();

            ViewUtils.createProgressDialog(getActivity(), getString(R.string.profile_group_update_loading), ThemeUtils.getThemeColor()).show();
        }

        @Override
        public Boolean workInBackground(boolean[]... params) throws TaskException {
            final boolean[] checkedItems = params[0];
            final boolean[] editCheckedItems = params[1];
            mGroupMemberListed = new GroupMemberListed();
            mGroupMemberListed.setLists(new ArrayList<GroupListed>());
            for (int i = 0; i < editCheckedItems.length; i++) {
                Group group = AppContext.getAccount().getGroups().getLists().get(i);
                GroupListed groupListed = new GroupListed();
                groupListed.setIdstr(group.getIdstr());
                groupListed.setName(group.getName());

                // 这个分组没有选中
                if (editCheckedItems[i]) {
                    // 如果这个分组原来也是选中的，则不需要编辑
                    if (checkedItems[i]) {
                        mGroupMemberListed.getLists().add(groupListed);
                    }
                    // 如果这个分组原来没有选中，则添加
                    else {
                        // 添加好友到分组
                        SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).friendshipsGroupdMembersAdd(mUser.getIdstr(), group.getIdstr());
                        publishProgress(String.format(getString(R.string.profile_add_to_group), group.getName()));

                        mGroupMemberListed.getLists().add(groupListed);
                    }
                } else {
                    // 如果原来有这个分组，现在没有勾选，则删除
                    if (checkedItems[i]) {
                        // 从分组中删除好友
                        SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).friendshipsGroupdMembersDestory(mUser.getIdstr(), group.getIdstr());
                        publishProgress(String.format(getString(R.string.profile_group_remvoe_friend), group.getName()));
                    }
                    // 原来没有勾选，现在也没有勾选，不做处理
                }
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (getActivity() != null && values != null && values.length > 0)
                ViewUtils.updateProgressDialog(values[0]);
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            ViewUtils.dismissProgressDialog();

            setProfile();
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            showMessage(R.string.profile_edit_group_faild);
        }

        @Override
        protected void onSuccess(Boolean result) {
            super.onSuccess(result);

            showMessage(R.string.profile_edit_group_success);
        }

    }

    // 获取用户关系
    class FriendshipTask extends WorkTask<Void, Void, FriendshipShow> {

        @Override
        public FriendshipShow workInBackground(Void... params) throws TaskException {
            FriendshipShow friendshipShow = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).friendshipsShow(AppContext.getAccount().getUser().getIdstr(), mUser.getIdstr());

            if (friendshipShow.getSource().getFollowing()) {
                GroupMemberListed[] result = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).friendshipGroupsListed(mUser.getIdstr());
                if (result != null && result.length > 0) {
                    mGroupMemberListed = result[0];
                } else {
                    mGroupMemberListed = new GroupMemberListed();
                    mGroupMemberListed.setLists(new ArrayList<GroupListed>());
                    mGroupMemberListed.setUid(mUser.getId());
                }
            }

            return friendshipShow;
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            // 如果界面没有被销毁，就加载
            if (getActivity() != null) {
                BaseActivity baseActivity = (BaseActivity) getActivity();
                if (!baseActivity.isDestory()) {
                    new FriendshipTask().execute();
                }
            }
        }

        @Override
        protected void onSuccess(FriendshipShow result) {
            super.onSuccess(result);

            if (getActivity() != null) {
                mFriendship = result;

                // 设置Pager的菜单显示
                Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(SinaCommonActivity.FRAGMENT_TAG);
                ((ProfilePagerFragment) fragment).setFriendshipShow(mFriendship);

                getActivity().invalidateOptionsMenu();

                setProfile();
            }
        }

    }

    @Override
    public void refreshProfile() {
        loadFriendship();
    }

}
