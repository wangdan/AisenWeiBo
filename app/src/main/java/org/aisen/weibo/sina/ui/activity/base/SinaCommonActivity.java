package org.aisen.weibo.sina.ui.activity.base;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;

import java.lang.reflect.Method;

/**
 * Created by wangdan on 15/12/21.
 */
public class SinaCommonActivity extends BaseActivity implements AisenActivityHelper.EnableSwipeback {

    private int overrideTheme = -1;

    public static final String FRAGMENT_TAG = "FRAGMENT_CONTAINER";

    /**
     * 启动一个界面
     *
     * @param activity
     * @param clazz
     * @param args
     */
    public static void launch(Activity activity, Class<? extends Fragment> clazz, FragmentArgs args) {
        Intent intent = new Intent(activity, SinaCommonActivity.class);
        intent.putExtra("className", clazz.getName());
        if (args != null)
            intent.putExtra("args", args);
        activity.startActivity(intent);
    }

    public static void launchForResult(Fragment fragment, Class<? extends Fragment> clazz, FragmentArgs args, int requestCode) {
        if (fragment.getActivity() == null)
            return;
        Activity activity = fragment.getActivity();

        Intent intent = new Intent(activity, SinaCommonActivity.class);
        intent.putExtra("className", clazz.getName());
        if (args != null)
            intent.putExtra("args", args);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void launchForResult(Activity from, Class<? extends Fragment> clazz, FragmentArgs args, int requestCode) {
        Intent intent = new Intent(from, SinaCommonActivity.class);
        intent.putExtra("className", clazz.getName());
        if (args != null)
            intent.putExtra("args", args);
        from.startActivityForResult(intent, requestCode);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String className = getIntent().getStringExtra("className");
        if (TextUtils.isEmpty(className)) {
            finish();
            return;
        }

        int contentId = org.aisen.android.R.layout.comm_ui_fragment_container;

        FragmentArgs values = (FragmentArgs) getIntent().getSerializableExtra("args");

        Fragment fragment = null;
        if (savedInstanceState == null) {
            try {
                Class clazz = Class.forName(className);
                fragment = (Fragment) clazz.newInstance();
                // 设置参数给Fragment
                if (values != null) {
                    try {
                        Method method = clazz.getMethod("setArguments", new Class[] { Bundle.class });
                        method.invoke(fragment, FragmentArgs.transToBundle(values));
                    } catch (Exception e) {
                    }
                }
                // 重写Activity的主题
                try {
                    Method method = clazz.getMethod("setActivityTheme");
                    if (method != null) {
                        int theme = Integer.parseInt(method.invoke(fragment).toString());
                        if (theme > 0) {
                            overrideTheme = theme;
                        }
                    }
                } catch (Exception e) {
                }
                // 重写Activity的contentView
                try {
                    Method method = clazz.getMethod("inflateActivityContentView");
                    if (method != null) {
                        int fragmentConfigId = Integer.parseInt(method.invoke(fragment).toString());
                        if (fragmentConfigId > 0) {
                            contentId = fragmentConfigId;
                        }
                    }
                } catch (Exception e) {
                }
            } catch (Exception e) {
                e.printStackTrace();
                finish();
                return;
            }
        }
//        ABaseFragment fragment = null;
//        if (savedInstanceState == null) {
//            try {
//                Class clazz = Class.forName(className);
//                Object object = clazz.newInstance();
//                if (object instanceof ABaseFragment) {
//
//                    fragment = (ABaseFragment) clazz.newInstance();
//
//                    // 设置参数给Fragment
//                    if (values != null) {
//                        fragment.setArguments(FragmentArgs.transToBundle(values));
//                    }
//                    // 重写Activity的主题
//                    if (fragment.setActivityTheme() > -1) {
//                        overrideTheme = fragment.setActivityTheme();
//                    }
//                    // 重写Activity的contentView
//                    if (fragment.inflateActivityContentView() > 0) {
//                        contentId = fragment.inflateActivityContentView();
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                finish();
//                return;
//            }

        super.onCreate(savedInstanceState);
        setContentView(contentId);

        if (fragment != null) {
            if (!(fragment instanceof ABaseFragment) || ((ABaseFragment) fragment).inflateContentView() > 0) {
                getFragmentManager().beginTransaction().add(org.aisen.android.R.id.fragmentContainer, fragment, FRAGMENT_TAG).commit();
            }
            else {
                getFragmentManager().beginTransaction().add(fragment, FRAGMENT_TAG).commit();
            }
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowHomeEnabled(false);

        BizFragment.createBizFragment(this);
    }

    @Override
    protected int configTheme() {
        if (overrideTheme > 0)
            return overrideTheme;

        return super.configTheme();
    }

    @Override
    public boolean canSwipe() {
        return true;
    }

}
