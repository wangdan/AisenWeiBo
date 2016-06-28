package org.aisen.weibo.sina.ui.activity.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.BitmapUtil;
import org.aisen.android.common.utils.FileUtils;
import org.aisen.android.common.utils.ImagePickerUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.PhotoChoice;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.component.bitmaploader.core.BitmapDecoder;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.utils.AccountUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayOutputStream;
import java.io.File;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * Created by wangdan on 15-3-5.
 */
public class WeiboClientActivity extends BaseActivity implements PhotoChoice.PhotoChoiceListener {

    public static final String TAG = "WeiboClient";


    public static final String HOT_STATUSES = "http://m.weibo.cn/p/index?containerid=102803";

    public static final String HOT_TOPICS = "http://m.weibo.cn/p/index?containerid=100803";

    public static final String DM_URL = "http://m.weibo.cn";

    public static final String WEIBO_TOP = "http://m.weibo.cn/pubs/weibo/feed";

    public static final String CHART = "http://m.weibo.cn/msg/chat";

    public static final String PROFILE = "http://m.weibo.cn/users/%s?";

    public static final String LOGIN = "https://passport.weibo.cn/signin/login";

    @ViewInject(id = R.id.webview)
    WebView mWebView;
    @ViewInject(id = R.id.progress)
    SmoothProgressBar progressbar;

    private ValueCallback<Uri> uploadMsg;

    private PhotoChoice photoChoice;

    private boolean askForAuth = false;

    public static void launchForAuth(Fragment from, int requestCode) {
        Intent intent = new Intent(from.getActivity(), WeiboClientActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra("url", LOGIN);
        intent.putExtra("askForAuth", true);
        from.startActivityForResult(intent, requestCode);
    }

    public static void launchDM(Activity from) {
        launch(from, DM_URL);
    }

    public static void launchHotStatuses(Activity from) {
        launch(from, HOT_STATUSES);
    }

    public static void launchHotTopics(Activity from) {
        launch(from, HOT_TOPICS);
    }

    public static void launchWeiTop(Activity from) {
        launch(from, WEIBO_TOP);
    }

    public static void launchProfile(Activity from, String uid) {
        launch(from, String.format(PROFILE, uid));
    }

    public static void launchChat(Activity from, String uid) {
        launch(from, String.format("%s?uid=%s", CHART, uid));
//		Intent intent = new Intent(from, TestActivity.class);
//		intent.putExtra("url", String.format("%s?uid=%s", CHART, uid));
//		from.startActivity(intent);
    }

    public static void launch(Activity from, String url) {
        Intent intent = new Intent(from, WeiboClientActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra("url", url);
        from.startActivity(intent);
    }

    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_weibo_client);

        if (!Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            finish();
            return;
        }

        askForAuth = savedInstanceState != null ? savedInstanceState.getBoolean("askForAuth")
                                                : getIntent().getBooleanExtra("askForAuth", false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle("");

        progressbar.setIndeterminate(true);
        WebSettings setting = mWebView.getSettings();
        setting.setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WeiboWebChromeClient());
        mWebView.addJavascriptInterface(new LoginJavaScriptInterface(), "loginjs");
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!TextUtils.isEmpty(url) && isLoginUrl(mUrl) && url.toLowerCase().startsWith("http://m.weibo.cn")) {
                    Logger.d(TAG, "登录成功");

                    mWebView.loadUrl("javascript:getAccount()");

                    String cookie = CookieManager.getInstance().getCookie(DM_URL);
                    String accountCookie = AppContext.getAccount().getCookie();
                    if (TextUtils.isEmpty(accountCookie) || (!TextUtils.isEmpty(cookie) && !cookie.equalsIgnoreCase(accountCookie))) {
                        saveCookie(AppContext.getAccount().getAccount(), AppContext.getAccount().getPassword());
                        Logger.d(TAG, "手动记录cookie");
                    }
                }

                mUrl = url;

                view.loadUrl(url);

                Logger.d(TAG, "overriderUrlLoading = " + url);

                return true;
            }

        });
        setting.setJavaScriptCanOpenWindowsAutomatically(true);

        if (savedInstanceState == null) {
            if (getIntent().getData() != null)
                mUrl = getIntent().getData().toString();

            if (TextUtils.isEmpty(mUrl))
                mUrl = getIntent().getStringExtra("url");

            if (mUrl.startsWith("aisen://"))
                mUrl = mUrl.replace("aisen://", "");
        } else {
            mUrl = savedInstanceState.getString("url");
        }

        // 设置cookie
//        String cookie = AppContext.getAccount().getCookie();
//        if (TextUtils.isEmpty(cookie))
//            cookie = "";
//        CookieSyncManager.createInstance(this);
//        CookieManager cookieManager = CookieManager.getInstance();
//        cookieManager.setAcceptCookie(true);
//        cookieManager.setCookie(DM_URL, cookie);
//        CookieSyncManager.getInstance().sync();

        if (askForAuth) {
            fillAccount();
        }
        else {
            mWebView.loadUrl(mUrl);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("url", mUrl);
        outState.putBoolean("askForAuth", askForAuth);
    }

    class WeiboWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress < 100) {
                progressbar.setVisibility(View.VISIBLE);
            } else if (newProgress == 100) {
                progressbar.setVisibility(View.GONE);

                invalidateOptionsMenu();
            }

            super.onProgressChanged(view, newProgress);
        }

        public void openFileChooser(final ValueCallback<Uri> uploadMsg) {
            AlertDialog.Builder builder = new AlertDialog.Builder(WeiboClientActivity.this)
                    .setItems(getResources().getStringArray(R.array.chatPicChoose), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WeiboClientActivity.this.uploadMsg = uploadMsg;

                            if (photoChoice == null) {
                                String albumPath = SystemUtils.getSdcardPath() + File.separator + "/DCIM/Camera/";
                                File albumFile = new File(albumPath);
                                if (!albumFile.exists())
                                    albumFile.mkdirs();
                                photoChoice = new PhotoChoice(WeiboClientActivity.this, WeiboClientActivity.this, albumPath);
                                photoChoice.setFileName(String.format("%s.jpg", String.valueOf(System.currentTimeMillis() / 1000)));
                            }

                            photoChoice.setMode(PhotoChoice.PhotoChoiceMode.uriType);
                            switch (which) {
                                // 相册
                                case 0:
                                    photoChoice.start(null, 0);
                                    break;
                                // 拍照
                                case 1:
                                    photoChoice.start(null, 1);
                                    break;
                                default:
                                    break;
                            }
                        }
                    })
                    .setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WeiboClientActivity.this.uploadMsg = null;
                            uploadMsg.onReceiveValue(null);
                        }
                    });

            builder.show();
        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooser(uploadMsg);
        }

        // For Android > 4.1.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            openFileChooser(uploadMsg, acceptType);
        }

    }

    final class LoginJavaScriptInterface {

        public LoginJavaScriptInterface() {

        }

        @JavascriptInterface
        public void setAccount(String account, String password) {
            Logger.d(TAG, "自动记录cookie");
            saveCookie(account, password);
        }

    }

    private void saveCookie(String account, String password) {
        String cookie = CookieManager.getInstance().getCookie(DM_URL);
//            Logger.d(TAG, cookie);

        // 获取到cookie后，保存到账号
        AppContext.getAccount().setCookie(cookie);
        AppContext.getAccount().setAccount(account);
        AppContext.getAccount().setPassword(password);

        // 刷新到DB
        AccountUtils.updateAccount(AppContext.getAccount());
        AccountUtils.setLogedinAccount(AppContext.getAccount());

        if (askForAuth) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (photoChoice != null)
            photoChoice.onActivityResult(requestCode, resultCode, data);
    }

    public static String getFilePathFromUri(Context context, Uri uri,
                                            String[] projection, String selection, String[] selectionArgs,
                                            String sortOrder) {
        String path = ImagePickerUtils.getPath(context, uri);
        return path;
    }

    @Override
    public void choiceByte(byte[] datas) {

    }

    @Override
    public void choiceBitmap(Bitmap bitmap) {

    }

    @Override
    public void choieUri(Uri uri, int requestCode) {
        // 当拍摄照片时，提示是否设置旋转90度
        if (!AppSettings.isRotatePic() && !ActivityHelper.getBooleanShareData(GlobalContext.getInstance(), "RotatePicNoRemind", false)) {
            new AlertDialogWrapper.Builder(this).setTitle(R.string.remind)
                    .setMessage(R.string.publish_rotate_remind)
                    .setNegativeButton(R.string.donnot_remind, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityHelper.putBooleanShareData(GlobalContext.getInstance(), "RotatePicNoRemind", true);
                        }
                    })
                    .setPositiveButton(R.string.i_know, null)
                    .show();
        }

        // 拍摄照片时，顺时针旋转90度
        if (requestCode == PhotoChoice.CAMERA_IMAGE_REQUEST_CODE && AppSettings.isRotatePic()) {
            final String path = uri.toString().replace("file://", "");

            new WorkTask<Void, Void, String>() {

                @Override
                public String workInBackground(Void... params) throws TaskException {
                    try {
                        Bitmap bitmap = BitmapDecoder.decodeSampledBitmapFromFile(path, SystemUtils.getScreenHeight(WeiboClientActivity.this), SystemUtils.getScreenHeight(WeiboClientActivity.this));
                        bitmap = BitmapUtil.rotateBitmap(bitmap, 90);

                        ByteArrayOutputStream outArray = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outArray);

                        FileUtils.writeFile(new File(path), outArray.toByteArray());
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                    }
                    return path;
                }

                protected void onSuccess(String result) {
                    uploadPic(result);
                };

            }.execute();
        }
        else {
            uploadPic(uri.toString());
        }
    }

    @Override
    public void unChoice() {

    }

    private void uploadPic(String path) {
        if (uploadMsg != null) {
            if (path.toString().startsWith("content://")) {
                path = getFilePathFromUri(this, Uri.parse(path), null, null, null, null);

                Logger.v(TAG, "相册图片地址, path = " + path);
            }
            else {
                path = path.toString().replace("file://", "");
                Logger.v(TAG, "拍照图片地址, path = " + path);

                // 扫描文件
                SystemUtils.scanPhoto(WeiboClientActivity.this, new File(path));
            }
            File file = new File(path);
            if (file.exists())
                uploadMsg.onReceiveValue(Uri.fromFile(file));
            else {
                uploadMsg.onReceiveValue(null);

                showMessage(R.string.dm_pic_notexist);
            }
            Logger.w(TAG, String.format("上传文件是否存在 = %s, 文件路径 = %s", String.valueOf(file.exists()), file.getAbsoluteFile()));
        }

        uploadMsg = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_weiboclient, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.fillAccount).setVisible(!TextUtils.isEmpty(mUrl) && isLoginUrl(mUrl));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.fillAccount) {
            fillAccount();
        }
        else if (item.getItemId() == R.id.refresh) {
            mWebView.reload();
        }

        return super.onOptionsItemSelected(item);
    }

    private void fillAccount() {
        final String url = "http://passport.weibo.cn/signin/login?";
        new WorkTask<Void, Void, String>() {

            @Override
            protected void onPrepare() {
                super.onPrepare();

                ViewUtils.createProgressDialog(WeiboClientActivity.this, getString(R.string.account_fillaccount_loading), ThemeUtils.getThemeColor()).show();
            }

            @Override
            public String workInBackground(Void... p) throws TaskException {
                try {
                    AccountBean accountBean = AccountUtils.getLogedinAccount();
                    if (TextUtils.isEmpty(accountBean.getAccount()) || TextUtils.isEmpty(accountBean.getPassword()))
                        throw new TaskException("", getString(R.string.account_fillaccount_faild));

                    String js = FileUtils.readAssetsFile("mobile.js", GlobalContext.getInstance());
                    js = js.replace("%username%", accountBean.getAccount());
                    js = js.replace("%password%", accountBean.getPassword());

                    Document dom = Jsoup.connect(url).get();
                    String html = dom.toString();
                    html = html.replace("</head>", js + "</head>");
                    return html;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                throw new TaskException("", getString(R.string.account_fillaccount_faild));
            }

            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                Logger.e(s);
                mWebView.loadDataWithBaseURL("http://passport.weibo.cn", s, "text/html", "UTF-8", "");
                GlobalContext.getInstance().getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:fillAccount()");

                        ViewUtils.dismissProgressDialog();
                    }
                }, 1500);
            }

            @Override
            protected void onFailure(TaskException exception) {
                super.onFailure(exception);

                showMessage(exception.getMessage());

                ViewUtils.dismissProgressDialog();
            }

        }.execute();
    }

    @Override
    public boolean onHomeClick() {
        return super.onBackClick();
    }

    @Override
    public boolean onBackClick() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();

            return true;
        }

        return super.onBackClick();
    }

    private boolean isLoginUrl(String url) {
        return url.toLowerCase().startsWith("https://passport.weibo.cn/signin/login");
    }

}
