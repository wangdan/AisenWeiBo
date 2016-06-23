package org.aisen.weibo.sina.ui.fragment.account;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.FileUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.http.ParamsUtil;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.AccessToken;
import org.aisen.weibo.sina.sinasdk.bean.Groups;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 网页授权页面
 *
 * Created by wangdan on 15/12/31.
 */
public class WebLoginFragment extends ABaseFragment {

    public static final String TAG = "Fragment-Login";

    public enum Client {
        aisen,
        weico
    }

    public static void launch(Activity from, Client client, String account, String password, int requestCode) {
        FragmentArgs args = new FragmentArgs();
        args.add("client", client.toString());

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password)) {
            args.add("account", account);
            args.add("password", password);
        }

        SinaCommonActivity.launchForResult(from, WebLoginFragment.class, args, requestCode);
    }

    public static void launch(Fragment from, Client client, String account, String password, int requestCode) {
        FragmentArgs args = new FragmentArgs();
        args.add("client", client.toString());

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password)) {
            args.add("account", account);
            args.add("password", password);
        }

        SinaCommonActivity.launchForResult(from, WebLoginFragment.class, args, requestCode);
    }

    @ViewInject(id = R.id.webview)
    WebView mWebView;
    @ViewInject(id = R.id.progressbar)
    ProgressBar mProgressBar;

    private Client mClient;
    private String mAccount = "";
    private String mPassword = "";
    private boolean mAccountFilled = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.login_title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mClient = Client.valueOf(getArguments().getString("client"));
        if (getArguments().containsKey("account")) {
            mAccount = getArguments().getString("account");
        }
        if (getArguments().containsKey("password")) {
            mPassword = getArguments().getString("password");
        }
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_login;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        WebSettings setting = mWebView.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setDomStorageEnabled(true);
        setting.setAppCacheEnabled(true);
        setting.setDefaultTextEncodingName("utf-8") ;
        mWebView.addJavascriptInterface(new LoginJavaScriptInterface(), "loginjs");
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                // 授权成功
                if (url != null && url.startsWith(getClientCallback())) {
                    Params params = ParamsUtil.deCodeUrl(url);
                    String code = params.getParameter("code");

                    if (isActivityRunning() && mLoadAccountTask == null) {

                        Logger.d(TAG, "授权成功, code = " + code);

                        // 把WebView隐藏，因为callback页面是个错误的页面
                        mWebView.setVisibility(View.INVISIBLE);

                        mLoadAccountTask = new LoadAccountTask();
                        mLoadAccountTask.execute(code);
                    }
                }

                if (!TextUtils.isEmpty(url) && !url.startsWith("sinaweibo://")) {
                    view.loadUrl(url);
                }

                Logger.d(TAG, "load url = %s", view.getUrl());

                return true;
            }

        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);

                    Logger.d(TAG, "progress = %d , url = %s", newProgress, view.getUrl());
                } else if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);

                    Logger.d(TAG, "progress = 100 , url = %s", view.getUrl());

                    // 填充账号
                    if (!TextUtils.isEmpty(mAccount) && !TextUtils.isEmpty(mPassword)) {
                        if (!mAccountFilled && !TextUtils.isEmpty(view.getUrl()) && view.getUrl().equalsIgnoreCase("about:blank")) {
                            Logger.d(TAG, "fillAccount(%s, %s)", mAccount, mPassword);

                            mWebView.loadUrl("javascript:fillAccount()");
                            mAccountFilled = true;
                        }
                    }
                }

                super.onProgressChanged(view, newProgress);
            }

        });
    }

    @Override
    public void requestData() {
        super.requestData();

        new LoadHtmlTask().execute();
    }

    final class LoginJavaScriptInterface {

        public LoginJavaScriptInterface() {
            Logger.d(TAG, "new LoginJavaScriptInterface()");
        }

        @JavascriptInterface
        public void setAccount(String account, String password) {
            Logger.d(TAG, "account = %s, password = %s", account, password);

            mAccount = account;
            mPassword = password;
        }

    }

    /**
     * 抓取网页，填充账户密码，且通过JS注入，获取正确的账户密码保存在手机备下次使用
     */
    class LoadHtmlTask extends ABaseTask<Void, Void, String> {

        public LoadHtmlTask() {
            super("LoadHtmlTask");
        }

        @Override
        public String workInBackground(Void... params) throws TaskException {
            int count = 3;
            while (count-- >= 0) {
                try {
                    String js = FileUtils.readAssetsFile("oauth.js", GlobalContext.getInstance());
                    js = js.replace("%username%", mAccount).replace("%password%", mPassword);

                    Document dom = Jsoup.connect(getLoginUrl()).get();
                    String html = dom.toString();
                    html = html.replace("<html>", "<html id='all' >").replace("</head>", js + "</head>")
                            .replace("action-type=\"submit\"", "action-type=\"submit\" id=\"submit\"");

                    // 通过监听input标签的oninput事件，来获取账户密码
                    // onchange是value改变，且焦点改变才触发
                    // oninput是value改变就触发
                    try {
                        dom = Jsoup.parse(html);
                        Element inputAccount = dom.select("input#userId").first();
                        inputAccount.attr("oninput", "getAccount()");

                        Element pwdAccount = dom.select("input#passwd").first();
                        pwdAccount.attr("oninput", "getAccount()");

                        Logger.d(TAG, inputAccount.toString());
                        Logger.d(TAG, pwdAccount.toString());

                        html = dom.toString();

                        Logger.d(TAG, "添加input监听事件");
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                    Logger.v(TAG, html);

                    return html;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            throw new TaskException("", GlobalContext.getInstance().getResources().getString(R.string.login_html_faild));
        }

        @Override
        protected void onSuccess(String s) {
            super.onSuccess(s);

            if (isActivityRunning()) {
                // 加载网页
                mWebView.loadDataWithBaseURL("https://api.weibo.com", s, "text/html", "UTF-8", "");
            }
        }

    }

    /**
     * 1、获取授权的TOKEN
     * 2、加载用户信息
     * 3、加载分组信息
     */
    LoadAccountTask mLoadAccountTask;
    class LoadAccountTask extends WorkTask<String, Integer, AccountBean> {

        LoadAccountTask() {
            mLoadAccountTask = this;
        }

        @Override
        protected void onPrepare() {
            super.onPrepare();

            ViewUtils.createProgressDialog(getActivity(), getString(R.string.login_loading_token), ThemeUtils.getThemeColor()).show();
        }

        @Override
        public AccountBean workInBackground(String... params) throws TaskException {
            AccountBean account = new AccountBean();
            account.setAccount(mAccount);
            account.setPassword(mPassword);

            // 1、请求授权
            AccessToken accessToken = null;
            if (mClient == Client.aisen) {
                accessToken = SinaSDK.getInstance(null).getAccessToken(params[0]);
            }
            else {
                accessToken = SinaSDK.getInstance(null).getWeicoAccessToken(params[0]);
            }
            accessToken.setAppKey(getClientKey());
            accessToken.setAppScreet(getClientScreet());
            account.setAccessToken(accessToken);
            Logger.d(TAG, "加载Token[%s]", accessToken.getToken());

            if (mClient == Client.aisen) {
                // 2、加载用户信息
                publishProgress(R.string.login_loading_userinfo);
                WeiBoUser user = SinaSDK.getInstance(accessToken).userShow(accessToken.getUid(), null);
                account.setUser(user);
                account.setUid(user.getIdstr());
                Logger.d(TAG, "加载用户[%s]", user.getScreen_name());

                // 3、加载分组信息
                publishProgress(R.string.login_loading_groups);
                Groups groups = SinaSDK.getInstance(accessToken).friendshipGroups();
                account.setGroups(groups);
                Logger.d(TAG, "加载分组[%d]个", groups.getLists().size());
            }

            return account;
        }

        @Override
        protected void onSuccess(AccountBean accountBean) {
            super.onSuccess(accountBean);

            if (isActivityRunning()) {
                showMessage(R.string.login_success);

                Logger.d(TAG, "授权成功");

                Intent data = new Intent();
                data.putExtra("account", accountBean);
                getActivity().setResult(Activity.RESULT_OK, data);
            }
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            if (isActivityRunning()) {
                showMessage(R.string.login_faild);

                Logger.d(TAG, "授权失败 " + exception.getMessage());

                getActivity().setResult(Activity.RESULT_CANCELED);
            }
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            ViewUtils.dismissProgressDialog();

            if (isActivityRunning()) {
                getActivity().finish();
            }
        }

    }

    private String getLoginUrl() {
        if (mClient == Client.weico) {
            return String
                    .format("https://api.weibo.com/oauth2/authorize?client_id=%s&redirect_uri=%s&display=mobile&forcelogin=true",
                            getClientKey(), getClientCallback());
        }

        return String
                .format("https://api.weibo.com/oauth2/authorize?client_id=%s&scope=friendships_groups_read,friendships_groups_write,statuses_to_me_read,follow_app_official_microblog&redirect_uri=%s&display=mobile&forcelogin=true",
                        getClientKey(), getClientCallback());
    }

    private String getClientKey() {
        if (mClient == Client.weico) {
            return SettingUtility.getStringSetting("weico_key");
        }

        return "2362431378";
    }

    public String getClientScreet() {
        if (mClient == Client.weico) {
            return SettingUtility.getStringSetting("weico_screet");
        }

        return "582ce3cdcdeb8a3b45087073d0dbcadf";
    }

    private String getClientCallback() {
        if (mClient == Client.weico) {
            return SettingUtility.getStringSetting("weico_callback");
        }

        return "http://boyqiang520.s8.csome.cn/oauth2/";
    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(getActivity(), String.format("%s授权页", mClient == Client.weico ? "Weico" : "Aisen"));
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), String.format("%s授权页", mClient == Client.weico ? "Weico" : "Aisen"));
    }

}
