package org.aisen.weibo.sina.ui.fragment.settings;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.aisen.android.common.md.MDHelper;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.ui.widget.ColorPicker;

/**
 * 自定义主题颜色
 * 
 * @author wangdan
 *
 */
public class CustomThemeColorFragment extends DialogFragment {

	public static void launch(Activity context) {
		Fragment fragment = context.getFragmentManager().findFragmentByTag("DialogFragment");
    	if (fragment != null) {
    		context.getFragmentManager().beginTransaction().remove(fragment).commit();
    	}
    	
    	CustomThemeColorFragment dialogFragment = new CustomThemeColorFragment();
    	dialogFragment.show(context.getFragmentManager(), "DialogFragment");
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		setCancelable(true);
		
		View view = View.inflate(getActivity(), R.layout.ui_custom_themecolor, null);
		final ColorPicker mColorPicker = (ColorPicker) view.findViewById(R.id.colorPicker);
        int callback = getResources().getColor(R.color.material_deep_teal_500);
        mColorPicker.setColor(MDHelper.resolveColor(getActivity(), R.attr.colorPrimary, callback));

		return new MaterialDialog.Builder(getActivity())
				        .customView(view, false)
				        .negativeText(R.string.cancel)
				        .positiveText(R.string.title_settings)
						.onPositive(new MaterialDialog.SingleButtonCallback() {

							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
								int selected = mColorPicker.getColor();
								String color = String.format("#%X", selected);

//                                        AppSettings.setThemeColor(color);

								dialog.dismiss();

								getActivity().getFragmentManager().beginTransaction().remove(CustomThemeColorFragment.this)
										.commit();
							}

						})
				        .build();
	}

}
