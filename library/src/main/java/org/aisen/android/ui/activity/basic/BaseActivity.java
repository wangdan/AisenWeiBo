package org.aisen.android.ui.activity.basic;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import org.aisen.android.R;
import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.BitmapOwner;
import org.aisen.android.network.task.ITaskManager;
import org.aisen.android.network.task.TaskManager;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.InjectUtility;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.widget.AsToolbar;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by wangdan on 15-1-16.
 */
public class BaseActivity extends ActionBarActivity implements BitmapOwner, ITaskManager, AsToolbar.OnToolbarDoubleClick {

    static final String TAG = "Activity-Base";

    private static Class<? extends BaseActivityHelper> mHelperClass;
    private BaseActivityHelper mHelper;

    private int theme = 0;// 当前界面设置的主题

    private Locale language = null;// 当前界面的语言

    private TaskManager taskManager;

    private boolean isDestory;

    // 当有Fragment Attach到这个Activity的时候，就会保存
    private Map<String, WeakReference<ABaseFragment>> fragmentRefs;

    private static BaseActivity runningActivity;

    private View rootView;

    private Toolbar mToolbar;

    public static BaseActivity getRunningActivity() {
        return runningActivity;
    }

    public static void setRunningActivity(BaseActivity activity) {
        runningActivity = activity;
    }

    public static void setHelper(Class<? extends BaseActivityHelper> clazz) {
        mHelperClass = clazz;
    }

    protected int configTheme() {
        if (mHelper != null) {
            int theme = mHelper.configTheme();
            if (theme > 0)
                return theme;
        }

        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (mHelper == null) {
            try {
                if (mHelperClass != null) {
                    mHelper = mHelperClass.newInstance();
                    mHelper.bindActivity(this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mHelper != null)
            mHelper.onCreate(savedInstanceState);

        fragmentRefs = new HashMap<String, WeakReference<ABaseFragment>>();

        if (savedInstanceState == null) {
            theme = configTheme();

            language = new Locale(SettingUtility.getPermanentSettingAsStr("language", Locale.getDefault().getLanguage()),
                    SettingUtility.getPermanentSettingAsStr("language-country", Locale.getDefault().getCountry()));
        } else {
            theme = savedInstanceState.getInt("theme");

            language = new Locale(savedInstanceState.getString("language"), savedInstanceState.getString("language-country"));
        }
        // 设置主题
        if (theme > 0)
            setTheme(theme);
        // 设置语言
        setLanguage(language);

        taskManager = new TaskManager();

        // 如果设备有实体MENU按键，overflow菜单不会再显示
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this);
        if (viewConfiguration.hasPermanentMenuKey()) {
            try {
                Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(viewConfiguration, false);
            } catch (Exception e) {
            }
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mHelper != null)
            mHelper.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mHelper != null)
            mHelper.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (mHelper != null)
            mHelper.onRestart();
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(View.inflate(this, layoutResID, null));
    }

    public View getRootView() {
        return rootView;
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);

        rootView = view;

        InjectUtility.initInjectedView(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        rootView = view;

        InjectUtility.initInjectedView(this);
        
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null)
            setSupportActionBar(mToolbar);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mHelper != null)
            mHelper.onSaveInstanceState(outState);

        outState.putInt("theme", theme);
        outState.putString("language", language.getLanguage());
        outState.putString("language-country", language.getCountry());
    }

    public void addFragment(String tag, ABaseFragment fragment) {
        fragmentRefs.put(tag, new WeakReference<ABaseFragment>(fragment));
    }

    public void removeFragment(String tag) {
        fragmentRefs.remove(tag);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mHelper != null)
            mHelper.onResume();

        setRunningActivity(this);

        if (theme == configTheme()) {

        } else {
            Logger.i("theme changed, reload()");
            reload();

            return;
        }

        String languageStr = SettingUtility.getPermanentSettingAsStr("language", Locale.getDefault().getLanguage());
        String country = SettingUtility.getPermanentSettingAsStr("language-country", Locale.getDefault().getCountry());
        if (language != null && language.getLanguage().equals(languageStr) && country.equals(language.getCountry())) {

        }
        else {
            Logger.i("language changed, reload()");
            reload();

            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mHelper != null)
            mHelper.onPause();
    }

    public void setLanguage(Locale locale) {
        Resources resources = getResources();//获得res资源对象
        Configuration config = resources.getConfiguration();//获得设置对象
        config.locale = locale;
        DisplayMetrics dm = resources.getDisplayMetrics();//获得屏幕参数：主要是分辨率，像素等。
        resources.updateConfiguration(config, dm);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mHelper != null)
            mHelper.onStop();
    }

    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {

        isDestory = true;

        removeAllTask(true);

        if (BitmapLoader.getInstance() != null)
        	BitmapLoader.getInstance().cancelPotentialTask(this);

        super.onDestroy();

        if (mHelper != null)
            mHelper.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mHelper != null) {
            boolean handle = mHelper.onOptionsItemSelected(item);
            if (handle)
                return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                if (onHomeClick())
                    return true;
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected boolean onHomeClick() {
        if (mHelper != null) {
            boolean handle = mHelper.onHomeClick();
            if (handle)
                return true;
        }

        Set<String> keys = fragmentRefs.keySet();
        for (String key : keys) {
            WeakReference<ABaseFragment> fragmentRef = fragmentRefs.get(key);
            ABaseFragment fragment = fragmentRef.get();
            if (fragment != null && fragment.onHomeClick())
                return true;
        }

        return onBackClick();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mHelper != null) {
            boolean handle = mHelper.onKeyDown(keyCode, event);
            if (handle)
                return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (onBackClick())
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onBackClick() {
        if (mHelper != null) {
            boolean handle = mHelper.onBackClick();
            if (handle)
                return true;
        }

        Set<String> keys = fragmentRefs.keySet();
        for (String key : keys) {
            WeakReference<ABaseFragment> fragmentRef = fragmentRefs.get(key);
            ABaseFragment fragment = fragmentRef.get();
            if (fragment != null && fragment.onBackClick())
                return true;
        }

        finish();

        return true;
    }

    @Override
    final public void addTask(@SuppressWarnings("rawtypes") WorkTask task) {
        taskManager.addTask(task);
    }

    @Override
    final public void removeTask(String taskId, boolean cancelIfRunning) {
        taskManager.removeTask(taskId, cancelIfRunning);
    }

    @Override
    final public void removeAllTask(boolean cancelIfRunning) {
        taskManager.removeAllTask(cancelIfRunning);
    }

    @Override
    final public int getTaskCount(String taskId) {
        return taskManager.getTaskCount(taskId);
    }

    /**
     * 以Toast形式显示一个消息
     *
     * @param msg
     */
    public void showMessage(CharSequence msg) {
        ViewUtils.showMessage(this, msg.toString());
    }

    /**
     * @param msgId
     */
    public void showMessage(int msgId) {
        showMessage(getText(msgId));
    }

    @Override
    public void finish() {
        // 2014-09-12 解决ATabTitlePagerFragment的destoryFragments方法报错的BUG
        setDestory(true);

        super.finish();

        if (mHelper != null) {
            mHelper.finish();
        }
    }

    public boolean isDestory() {
        return isDestory;
    }

    public void setDestory(boolean destory) {
        this.isDestory = destory;
    }

    @Override
    public boolean canDisplay() {
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mHelper != null) {
            mHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onToolbarDoubleClick() {
        Set<String> keys = fragmentRefs.keySet();
        for (String key : keys) {
            WeakReference<ABaseFragment> fragmentRef = fragmentRefs.get(key);
            ABaseFragment fragment = fragmentRef.get();
            if (fragment != null && fragment instanceof AsToolbar.OnToolbarDoubleClick) {
                if (((AsToolbar.OnToolbarDoubleClick) fragment).onToolbarDoubleClick())
                    return true;
            }
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (mHelper != null) {
            mHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public BaseActivityHelper getActivityHelper() {
        return mHelper;
    }

}
