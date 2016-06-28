package org.aisen.weibo.sina.ui.fragment.account;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ARecycleViewFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.android.ui.widget.CircleImageView;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.MyApplication;
import org.aisen.weibo.sina.service.PublishService;
import org.aisen.weibo.sina.service.UnreadService;
import org.aisen.weibo.sina.service.notifier.UnreadCountNotifier;
import org.aisen.weibo.sina.service.publisher.PublishNotifier;
import org.aisen.weibo.sina.sinasdk.bean.UnreadCount;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.utils.AccountUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.activity.base.MainActivity;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号列表，显示在左侧的菜单里面
 *
 * Created by wangdan on 16/1/21.
 */
public class AccountFragment extends ARecycleViewFragment<AccountBean, ArrayList<AccountBean>> {

    public static final String TAG = "Account";

    public static void launch(Activity from) {
        SinaCommonActivity.launch(from, AccountFragment.class, null);
    }

    // 登录账号
    public static void login(AccountBean accountBean, boolean toMain) {
        if (AppContext.isLoggedIn()) {
            // 1、清理定时发布
            MyApplication.removeAllPublishAlarm();
            // 2、清理正在发布的数据
            PublishService.stopPublish();
            // 3、重新开始读取未读消息
            UnreadService.stopService();
            // 4、清理未读消息
            UnreadCountNotifier.mCount = new UnreadCount();
            // 5、清理通知栏
            PublishNotifier.cancelAll();
        }

        // 登录该账号
        AppContext.login(accountBean);
        AccountUtils.setLogedinAccount(accountBean);

        // 进入首页
        if (toMain) {
            MainActivity.login();

            MainActivity.runCheckAccountTask(accountBean);
        }
    }

    @ViewInject(id = R.id.btnAccountAdd, click = "addAccount")
    View btnAccountAdd;

    @Override
    public int inflateContentView() {
        return R.layout.ui_account;
    }

    @Override
    protected void setupRefreshConfig(RefreshConfig config) {
        super.setupRefreshConfig(config);

        config.footerMoreEnable = false;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(R.string.title_acount);

        setHasOptionsMenu(true);
    }

    @Override
    public IItemViewCreator<AccountBean> configItemViewCreator() {
        return new IItemViewCreator<AccountBean>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(R.layout.item_account, parent, false);
            }

            @Override
            public IITemView<AccountBean> newItemView(View convertView, int viewType) {
                return new AccountItemView(convertView);
            }

        };
    }

    @Override
    public void requestData(RefreshMode mode) {
        new AccountTask().execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        final AccountBean account = getAdapterItems().get(position);
        // 重新授权Aisen
        if (account.getAccessToken() == null || account.getAccessToken().isExpired()) {
            new AlertDialogWrapper.Builder(getActivity())
                    .setTitle(R.string.remind)
                    .setMessage(R.string.account_expired)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WebLoginFragment.launch(AccountFragment.this, WebLoginFragment.Client.aisen, account.getAccount(), account.getPassword(), BizFragment.REQUEST_CODE_AUTH);
                        }

                    })
                    .show();

            return;
        }

        login(account, true);

        getActivity().finish();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_account, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 新增授权
        if (item.getItemId() == R.id.add) {
            addAccount(null);
        }

        return super.onOptionsItemSelected(item);
    }

    void addAccount(View v) {
        WebLoginFragment.launch(this, WebLoginFragment.Client.aisen, null, null, BizFragment.REQUEST_CODE_AUTH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BizFragment.REQUEST_CODE_AUTH) {
            if (resultCode == Activity.RESULT_CANCELED) {

            }
            else if (resultCode == Activity.RESULT_OK) {
                AccountBean accountBean = (AccountBean) data.getSerializableExtra("account");

                AccountUtils.newAccount(accountBean);

                requestData(RefreshMode.reset);
            }
        }
    }

    class AccountItemView extends ARecycleViewItemView<AccountBean> {

        @ViewInject(id = R.id.txtName)
        TextView txtName;
        @ViewInject(id = R.id.txtDesc)
        TextView txtDesc;
        @ViewInject(id = R.id.imgPhoto)
        ImageView imgPhoto;
        @ViewInject(id = R.id.viewCover)
        CircleImageView viewCover;
        @ViewInject(id = R.id.txtTokenInfo)
        TextView txtTokenInfo;
        @ViewInject(id = R.id.btnRight, click = "deleteAccount")
        View btnRight;
        @ViewInject(id = R.id.divider)
        View divider;

        ColorDrawable grayDrawable;

        public AccountItemView(View itemView) {
            super(getActivity(), itemView);
        }

        @Override
        public void onBindData(View convertView, AccountBean data, int position) {
            if (grayDrawable == null)
                grayDrawable = new ColorDrawable(Color.parseColor("#99000000"));

            WeiBoUser user = data.getUser();

            BitmapLoader.getInstance().display(AccountFragment.this,
                    data.getUser().getAvatar_large(), imgPhoto, ImageConfigUtils.getPhotoConfig());

            txtName.setText(user.getScreen_name());
            txtDesc.setText(user.getDescription() + "");
            txtTokenInfo.setText(R.string.account_relogin_remind);
            if (data.getAccessToken() == null || data.getAccessToken().isExpired()) {
                txtTokenInfo.setVisibility(View.VISIBLE);
            } else {
                txtTokenInfo.setVisibility(View.GONE);
            }

            if (AppContext.isLoggedIn())
                viewCover.setVisibility(data.getUser().getIdstr().equals(AppContext.getAccount().getUser().getIdstr()) ? View.GONE : View.VISIBLE);
            else
                viewCover.setVisibility(View.VISIBLE);

            viewCover.setImageDrawable(grayDrawable);
            btnRight.setTag(data);

            divider.setVisibility(itemPosition() == itemSize() - 1 ? View.GONE : View.VISIBLE);
        }

        void deleteAccount(View v) {
            final AccountBean account = (AccountBean) v.getTag();

            new AlertDialogWrapper.Builder(getActivity())
                    .setTitle(R.string.remind)
                    .setMessage(R.string.account_destory_account_remind)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            new WorkTask<Void, Void, Boolean>() {

                                @Override
                                protected void onPrepare() {
                                    super.onPrepare();

                                    ViewUtils.createProgressDialog(getActivity(), getString(R.string.account_delete_account_loading), ThemeUtils.getThemeColor()).show();
                                }

                                @Override
                                public Boolean workInBackground(Void... params) throws TaskException {
                                    Logger.w(TAG, "删除账号 uid = " + account.getUid());
                                    AccountUtils.remove(account.getUid());

                                    // 如果是登录账号，退出登录
                                    if (AppContext.isLoggedIn() && account.getUid().equals(AppContext.getAccount().getUser().getIdstr()))
                                        AppContext.logout();

                                    try {
                                        Thread.sleep(300);
                                    } catch (Exception e) {
                                    }
                                    return true;
                                }

                                @Override
                                protected void onSuccess(Boolean result) {
                                    super.onSuccess(result);

                                    ViewUtils.dismissProgressDialog();

                                    requestData(RefreshMode.reset);
                                }

                            }.execute();
                        }
                    })
                    .show();
        }

    }

    class AccountTask extends APagingTask<Void, Void, ArrayList<AccountBean>> {

        public AccountTask() {
            super(RefreshMode.reset);
        }

        @Override
        protected List<AccountBean> parseResult(ArrayList<AccountBean> result) {
            return result;
        }

        @Override
        protected ArrayList<AccountBean> workInBackground(RefreshMode mode, String previousPage,
                                                          String nextPage, Void... params) throws TaskException {
            return (ArrayList<AccountBean>) AccountUtils.queryAccount();
        }

    }

    @Override
    public boolean onBackClick() {
        if (!AppContext.isLoggedIn()) {
            new AlertDialogWrapper.Builder(getActivity()).setTitle(R.string.remind)
                    .setMessage(R.string.account_account_exit_remind)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // android.os.Process.killProcess(android.os.Process.myPid());
                            getActivity().finish();
                        }
                    })
                    .show();
            return true;
        }

        return super.onBackClick();
    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(getActivity(), getString(R.string.title_acount) + "页");
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), getString(R.string.title_acount) + "页");
    }

}
