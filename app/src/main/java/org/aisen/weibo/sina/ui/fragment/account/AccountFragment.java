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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.container.FragmentContainerActivity;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.adapter.ABaseAdapter;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.AListFragment;
import org.aisen.android.ui.widget.CircleImageView;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.MyApplication;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.AccessToken;
import org.aisen.weibo.sina.sinasdk.bean.TokenInfo;
import org.aisen.weibo.sina.sinasdk.bean.UnreadCount;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.notifier.UnreadCountNotifier;
import org.aisen.weibo.sina.support.publisher.PublishNotifier;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.sys.service.PublishService;
import org.aisen.weibo.sina.sys.service.UnreadService;
import org.aisen.weibo.sina.ui.activity.basic.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号管理页面
 *
 * Created by wangdan on 15/4/12.
 */
public class AccountFragment extends AListFragment<AccountBean, ArrayList<AccountBean>> {

    public static final String TAG = "Account";

    public static void launch(Activity from) {
        FragmentContainerActivity.launch(from, AccountFragment.class, null);
    }

    // 登录账号
    public static void login(AccountBean accountBean, boolean toMain) {
        if (AppContext.isLogedin()) {
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
            // 6、清理内存数据
//            TimelineMemoryCacheUtility.clear();
        }

        // 登录该账号
        AppContext.login(accountBean);
        AccountDB.setLogedinAccount(accountBean);

        // 进入首页
        if (toMain)
            MainActivity.login();
    }

    @ViewInject(id = R.id.btnAccountAdd, click = "addAccount")
    View btnAccountAdd;

    @Override
    protected int inflateContentView() {
        return R.layout.as_ui_account;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(R.string.title_acount);

        setHasOptionsMenu(true);

        new CheckTokenInfoTask().execute();
    }

    @Override
    protected ABaseAdapter.AbstractItemView<AccountBean> newItemView() {
        return new AccountItemView();
    }

    @Override
    protected void requestData(RefreshMode mode) {
        new AccountTask().execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        final AccountBean account = getAdapterItems().get(position);
        // 重新授权Aisen
        if (account.getToken().isExpired()) {
            new AlertDialogWrapper.Builder(getActivity())
                    .setTitle(R.string.remind)
                    .setMessage(R.string.account_expired)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LoginFragment.launch(AccountFragment.this, account.getAccount(), account.getPassword(), 1000);
                        }

                    })
                    .show();

            return;
        }
        // 重新授权Weico
        else if (account.getAdvancedToken() == null || account.getAdvancedToken().isExpired()) {
            new AlertDialogWrapper.Builder(getActivity())
                    .setTitle(R.string.remind)
                    .setMessage(R.string.account_ad_expired)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WeicoLoginFragment.launch(AccountFragment.this, account.getAccount(), account.getPassword(), 1001);
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
        LoginFragment.launch(this, 1000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            AccessToken token = (AccessToken) data.getSerializableExtra("token");
//            String days = String.valueOf(TimeUnit.SECONDS.toDays(token.getExpires_in()));
//            new AlertDialogWrapper.Builder(getActivity())
//                                    .setTitle(R.string.remind)
//                                    .setMessage(String.format(getString(R.string.account_newaccount_remind), days))
//                                    .setPositiveButton(R.string.i_know, null)
//                                    .show();

            requestData(RefreshMode.reset);
        }
        else if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            AccessToken token = (AccessToken) data.getSerializableExtra("token");
            Logger.e(token);

            for (AccountBean dbAccount : AccountDB.query()) {
                if (dbAccount.getUserId().equals(token.getUid())) {
                    dbAccount.setAdvancedToken(token);
                    AccountDB.newAccount(dbAccount);
                    if (AppContext.isLogedin() && AppContext.getUser().getIdstr().equals(dbAccount.getUserId())) {
                        AppContext.getAccount().setAdvancedToken(token);
                        AccountDB.setLogedinAccount(AppContext.getAccount());
                    }
                    break;
                }
            }

            requestData(RefreshMode.reset);
        }
    }

    class AccountItemView extends ABaseAdapter.AbstractItemView<AccountBean> {

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

        private ColorDrawable grayDrawable;

        @Override
        public int inflateViewId() {
            return R.layout.as_item_account;
        }

        @Override
        public void bindingData(View convertView, AccountBean data) {
            if (grayDrawable == null)
                grayDrawable = new ColorDrawable(Color.parseColor("#99000000"));

            WeiBoUser user = data.getUser();

            BitmapLoader.getInstance().display(AccountFragment.this,
                    data.getUser().getAvatar_large(), imgPhoto, ImageConfigUtils.getPhotoConfig());

            txtName.setText(user.getScreen_name());
            txtDesc.setText(user.getDescription() + "");
            txtTokenInfo.setText(R.string.account_relogin_remind);
            if (data.getToken().isExpired() || data.getAdvancedToken() == null || data.getAdvancedToken().isExpired()) {
                txtTokenInfo.setVisibility(View.VISIBLE);
            } else {
                txtTokenInfo.setVisibility(View.GONE);
            }

            if (AppContext.isLogedin())
                viewCover.setVisibility(data.getUser().getIdstr().equals(AppContext.getUser().getIdstr()) ? View.GONE : View.VISIBLE);
            else
                viewCover.setVisibility(View.VISIBLE);

            viewCover.setImageDrawable(grayDrawable);
            btnRight.setTag(data);

            divider.setVisibility(getPosition() == getSize() - 1 ? View.GONE : View.VISIBLE);
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
                                    Logger.w(TAG, "删除账号 uid = " + account.getUserId());
                                    AccountDB.remove(account.getUserId());

                                    // 如果是登录账号，退出登录
                                    if (AppContext.isLogedin() && account.getUserId().equals(AppContext.getUser().getIdstr()))
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

    class AccountTask extends PagingTask<Void, Void, ArrayList<AccountBean>> {

        public AccountTask() {
            super("AccountTask", RefreshMode.reset);
        }

        @Override
        protected List<AccountBean> parseResult(ArrayList<AccountBean> result) {
            return result;
        }

        @Override
        protected ArrayList<AccountBean> workInBackground(RefreshMode mode, String previousPage,
                                                          String nextPage, Void... params) throws TaskException {
            return (ArrayList<AccountBean>) AccountDB.query();
        }

    }

    @Override
    public boolean onBackClick() {
        if (!AppContext.isLogedin()) {
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

    class CheckTokenInfoTask extends WorkTask<Void, Void, Boolean> {

        @Override
        public Boolean workInBackground(Void... params) throws TaskException {
            for (AccountBean account : AccountDB.query()) {
                TokenInfo tokenInfo = null;
                // Aisen授权
                try {
                    tokenInfo = SinaSDK.getInstance(account.getToken()).getTokenInfo(account.getToken().getToken());
                } catch (TaskException e) {
                    e.printStackTrace();
                    if ("21327".equals(e.getCode()) ||
                            "21317".equals(e.getCode())) {
                        tokenInfo = new TokenInfo();
                        tokenInfo.setExpire_in(0);
                    }
                    else {
                        return false;
                    }
                }
                account.getToken().setExpires_in(tokenInfo.getExpire_in());
                // Weico授权
                try {
                    if (account.getAdvancedToken() != null)
                        tokenInfo = SinaSDK.getInstance(account.getAdvancedToken()).getTokenInfo(account.getAdvancedToken().getToken());
                    else {
                        tokenInfo = new TokenInfo();
                        tokenInfo.setExpire_in(0);
                    }
                } catch (TaskException e) {
                    e.printStackTrace();
                    if ("21327".equals(e.getCode()) ||
                            "21317".equals(e.getCode())) {
                        tokenInfo = new TokenInfo();
                        tokenInfo.setExpire_in(0);
                    }
                    else {
                        return false;
                    }
                }
                if (account.getAdvancedToken() != null)
                    account.getAdvancedToken().setExpires_in(tokenInfo.getExpire_in());

                // 刷新用户信息
                AccountDB.newAccount(account);
                if (AppContext.getAccount() != null && AppContext.getAccount().getUserId().equals(account.getUserId())) {
                    AppContext.getAccount().setToken(account.getToken());
                    AppContext.setAdvancedToken(account.getAdvancedToken());
                    AccountDB.setLogedinAccount(AppContext.getAccount());

                    if (account.getToken().isExpired() ||
                            account.getAdvancedToken() == null || account.getAdvancedToken().isExpired()) {
                        AppContext.logout();
                    }
                }

                publishProgress();
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            requestData(RefreshMode.reset);
        }
    }

}
