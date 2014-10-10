package org.aisen.weibo.sina.ui.activity.common;

import java.lang.reflect.Method;

import org.aisen.weibo.sina.ui.fragment.base.BizFragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.m.R;
import com.m.ui.utils.FragmentArgs;

/**
 * 公共界面
 * 
 * @author Jeff.Wang
 *
 */
public class FragmentContainerActivity extends WeiboBaseActivity {

	/**
	 * 启动一个界面
	 * 
	 * @param activity
	 * @param clazz
	 * @param values
	 */
	public static void launch(Activity activity, Class<? extends Fragment> clazz, FragmentArgs args) {
		Intent intent = new Intent(activity, FragmentContainerActivity.class);
		intent.putExtra("className", clazz.getName());
		if (args != null)
			intent.putExtra("args", args);
		activity.startActivity(intent);
	}
	
	public static void launchForResult(Fragment fragment, Class<? extends Fragment> clazz, FragmentArgs args,
								int requestCode) {
		if(fragment.getActivity() == null)
			return;
		Activity activity = fragment.getActivity();
		
		Intent intent = new Intent(activity, FragmentContainerActivity.class);
		intent.putExtra("className", clazz.getName());
		if (args != null)
			intent.putExtra("args", args);
		fragment.startActivityForResult(intent, requestCode);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String className = getIntent().getStringExtra("className");
		if (TextUtils.isEmpty(className)) {
			finish();
			return;
		}

		FragmentArgs values = (FragmentArgs) getIntent().getSerializableExtra("args");

		Fragment fragment = null;
		if (savedInstanceState == null) {
			try {
				Class clazz = Class.forName(className);
				fragment = (Fragment) clazz.newInstance();
				if (values != null) {
					try {
						Method method = clazz.getMethod("setArguments", new Class[] { Bundle.class });
						method.invoke(fragment, FragmentArgs.transToBundle(values));
					} catch (Exception e) {
//						e.printStackTrace();
					}
				}
				try {
					Method method = clazz.getMethod("setTheme");
					if(method != null) {
						int themeRes = Integer.parseInt(method.invoke(fragment).toString());
						setTheme(themeRes);
					}
				} catch (Exception e) {
//					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
				finish();
				return;
			}
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_fragment_container);
		
		BizFragment.getBizFragment(this);
		
		if(fragment != null) {
			getFragmentManager().beginTransaction().add(R.id.fragmentContainer, fragment, className).commit();
		}
	}
	
}
