package org.aisen.weibo.sina.ui.fragment.account;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.FileUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.component.container.FragmentArgs;
import org.aisen.android.component.container.FragmentContainerActivity;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.http.ParamsUtil;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.AccessToken;
import org.aisen.weibo.sina.sinasdk.bean.Groups;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * 登录页面
 *
 * Created by wangdan on 15-1-28.
 */
public class LoginFragment extends ABaseFragment {

    public static void launch(ABaseFragment from, String account, String password, int requestCode) {
        FragmentArgs args = new FragmentArgs();
        args.add("account", account);
        args.add("password", password);

        FragmentContainerActivity.launchForResult(from, LoginFragment.class, args, requestCode);
    }

    public static void launch(BaseActivity from, String account, String password, int requestCode) {
        FragmentArgs args = new FragmentArgs();
        args.add("account", account);
        args.add("password", password);

        FragmentContainerActivity.launchForResult(from, LoginFragment.class, args, requestCode);
    }

    public static void launch(ABaseFragment from, int requestCode) {
        FragmentContainerActivity.launchForResult(from, LoginFragment.class, null, requestCode);
    }

    public static final String TAG = "Login";

    @ViewInject(id = R.id.webview)
    WebView webView;
    @ViewInject(id = R.id.progress)
    SmoothProgressBar progressBar;
//    @ViewInject(id = R.id.editAccount)
    EditText editAccount;
//    @ViewInject(id = R.id.editPassword)
    EditText editPassword;
    @ViewInject(id = R.id.textinputAccount)
    TextInputLayout textinputAccount;
    @ViewInject(id = R.id.textinputPassword)
    TextInputLayout textinputPassword;
    @ViewInject(id = R.id.layWeb)
    View layWeb;
    @ViewInject(id = R.id.layInput)
    View layInput;

    private String mAccount;
    private String mPassword;

    private AccountTask accountTask;

    boolean accountFilled = false;

    @Override
    protected int inflateContentView() {
        return R.layout.as_ui_login;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(R.string.title_login);

        setHasOptionsMenu(true);

        progressBar.setIndeterminate(true);

        editAccount = textinputAccount.getEditText();
        editAccount.addTextChangedListener(new MTextWatcher(textinputAccount));
        editPassword = textinputPassword.getEditText();
        editPassword.addTextChangedListener(new MTextWatcher(textinputPassword));
        
        if (getArguments() != null) {
            editAccount.setText(getArguments().getString("account"));
            editPassword.setText(getArguments().getString("password"));
        }

        WebSettings setting = webView.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setDomStorageEnabled(true);
        setting.setAppCacheEnabled(true);
        setting.setDefaultTextEncodingName("utf-8") ;
        webView.addJavascriptInterface(new LoginJavaScriptInterface(), "loginjs");
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                if (url.startsWith(SettingUtility.getStringSetting("callback_url")))
                    webView.loadUrl("javascript:getAccount()");
                view.loadUrl(url);
                Logger.d(TAG, "load url = " + url);

                // 是否授权成功
                if (accountTask == null && url != null && url.startsWith(SettingUtility.getStringSetting("callback_url"))) {
                    Params params = ParamsUtil.deCodeUrl(url);
                    String code = params.getParameter("code");

                    Logger.d(TAG, "授权成功, code = " + code);

                    if (getActivity() == null)
                        return true;

                    accountTask = new AccountTask();
                    accountTask.execute(code);
                }

                return true;
            }

        });
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                } else if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }

                if (newProgress < 100) {

                } else if (newProgress == 100) {
                    Logger.d(TAG, String.format("progress = 100 , url = %s", view.getUrl()));
                    if (!accountFilled && !TextUtils.isEmpty(view.getUrl()) && view.getUrl().equalsIgnoreCase("about:blank")) {
                        webView.loadUrl("javascript:fillAccount()");
                        accountFilled = true;
                    }

                    // 是否授权成功
                    if (accountTask == null && webView.getUrl() != null && webView.getUrl().startsWith(SettingUtility.getStringSetting("callback_url"))) {
                        Params params = ParamsUtil.deCodeUrl(webView.getUrl());
                        String code = params.getParameter("code");

                        Logger.d(TAG, "授权成功, code = " + code);

                        if (getActivity() == null)
                            return;

                        accountTask = new AccountTask();
                        accountTask.execute(code);
                    }
                }

                if (webView.getUrl() != null && webView.getUrl().startsWith(SettingUtility.getStringSetting("callback_url"))) {
                    webView.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }

        });

        if (!TextUtils.isEmpty(editAccount.getText().toString()) &&
                !TextUtils.isEmpty(editPassword.getText().toString())) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doLogin();
                }

            }, 500);
        }
    }
    
    class MTextWatcher implements TextWatcher {

    	TextInputLayout textInputLayout;
    	
    	public MTextWatcher(TextInputLayout textInputLayout) {
    		this.textInputLayout = textInputLayout;
		}
    	
		@Override
		public void afterTextChanged(Editable arg0) {
			
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			textInputLayout.setErrorEnabled(false);
		}
    	
    }

    final class LoginJavaScriptInterface {

        public LoginJavaScriptInterface() {

        }

        @JavascriptInterface
        public void setAccount(String account, String password) {
            mAccount = account;
            mPassword = password;
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_login, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.login) {
            doLogin();
        }

        return super.onOptionsItemSelected(item);
    }

    private void doLogin() {
        mAccount = editAccount.getText().toString();
        mPassword = editPassword.getText().toString();

        if (TextUtils.isEmpty(mAccount)) {
        	textinputAccount.setError(getString(R.string.account_invalid_input_account));
        	textinputAccount.setErrorEnabled(true);
        }
        else if (TextUtils.isEmpty(mPassword)) {
        	textinputPassword.setError(getString(R.string.account_invalid_input_password));
        	textinputPassword.setErrorEnabled(true);
        }
        else {
            new LoadLoginHtmlTask().execute();
        }
    }

    class LoadLoginHtmlTask extends WorkTask<Void, Void, String> {

        @Override
        protected void onPrepare() {
            super.onPrepare();

            ViewUtils.createProgressDialog(getActivity(), getString(R.string.comm_request_loading), ThemeUtils.getThemeColor()).show();
        }

        @Override
        public String workInBackground(Void... params) throws TaskException {
            final String url = String
                    .format("https://api.weibo.com/oauth2/authorize?client_id=%s&scope=friendships_groups_read,friendships_groups_write,statuses_to_me_read,follow_app_official_microblog&redirect_uri=%s&display=mobile&forcelogin=true",
                            "2362431378", "http://boyqiang520.s8.csome.cn/oauth2/");
            int count = 3;
            while (count-- >= 0) {
                try {
                    String js = FileUtils.readAssetsFile("oauth.js", GlobalContext.getInstance());
                    js = js.replace("%username%", mAccount).replace("%password%", mPassword);

                    Document dom = Jsoup.connect(url).get();
                    String html = dom.toString();
                    html = html.replace("</head>", js + "</head>")
                            .replace("action-type=\"submit\"", "action-type=\"submit\" id=\"submit\"");
                    return html;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            throw new TaskException("", GlobalContext.getInstance().getResources().getString(R.string.account_get_html_faild));
        }

        @Override
        protected void onSuccess(String s) {
            super.onSuccess(s);

//            Logger.v(TAG, s);
            layInput.setVisibility(View.GONE);
            layWeb.setVisibility(View.VISIBLE);
            // 加载网页
            webView.loadDataWithBaseURL("https://api.weibo.com", s, "text/html", "UTF-8", "");
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            showMessage(exception.getMessage());
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            ViewUtils.dismissProgressDialog();
        }
    }

    /**
     * 1、加载授权信息
     * 2、加载用户信息
     *
     * @author wangdan
     *
     */
    class AccountTask extends WorkTask<String, Integer, WeiBoUser> {

        @Override
        protected void onPrepare() {
            super.onPrepare();

            ViewUtils.createProgressDialog(getActivity(), getString(R.string.account_load_auth), ThemeUtils.getThemeColor()).show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if (getActivity() != null)
                ViewUtils.updateProgressDialog(getString(values[0]));
        }

        @Override
        public WeiBoUser workInBackground(String... params) throws TaskException {
            // 1、请求授权
            AccessToken accessToken = SinaSDK.getInstance(null).getAccessToken(params[0]);
            AccountBean account = new AccountBean();
            account.setToken(accessToken);

            // 2、加载用户信息
            publishProgress(R.string.account_load_userinfo);
            WeiBoUser user = SinaSDK.getInstance(accessToken).userShow(accessToken.getUid(), null);

            // 3、加载分组信息
            publishProgress(R.string.account_load_groups);
            Groups groups = SinaSDK.getInstance(accessToken).friendshipGroups();

            // 4、更新新账户到DB
            account.setUser(user);
            account.setGroups(groups);
            account.setAccount(mAccount);
            account.setPassword(mPassword);
            account.setUserId(user.getIdstr());
            // 2014-09-18 移除了所有账户信息，包括LoggedIn，所以需要调用AccountFragment.login()
            AccountDB.remove(account.getUserId());
            AccountDB.newAccount(account);

            if (AppContext.isLogedin() &&
                    // 2014-09-18 仅更新登录用户数据
                    AppContext.getUser().getIdstr().equals(user.getIdstr())) {
                AccountFragment.login(account, false);
            }

            if (getActivity() != null) {
                Intent data = new Intent();
                data.putExtra("token", accessToken);
                getActivity().setResult(Activity.RESULT_OK, data);
            }


            return user;
        }

        @Override
        protected void onSuccess(WeiBoUser result) {
            super.onSuccess(result);

            Logger.d(TAG, "授权成功");

            showMessage(R.string.account_auth_success);

            if (getActivity() != null) {
                getActivity().finish();
            }
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            if ("21324".equals(exception.getCode()) && getActivity() != null) {
                new AlertDialogWrapper.Builder(getActivity()).setTitle(R.string.remind)
                        .setMessage(R.string.account_illegal_app)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().finish();
                            }
                        })
                        .show();
            }
            else {
                showMessage(exception.getMessage());

                if (getActivity() != null)
                    requestData();
            }
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            ViewUtils.dismissProgressDialog();

            accountTask = null;
        }

    }

}
