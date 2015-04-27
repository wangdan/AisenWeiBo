package org.aisen.weibo.sina.ui.fragment.settings;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.m.ui.widget.CircleImageView;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * 界面配色设置
 * 
 * @author wangdan
 *
 */
public class MDColorsDialogFragment extends DialogFragment implements OnItemClickListener {

	public static void launch(Activity context) {
		Fragment fragment = context.getFragmentManager().findFragmentByTag("DMColorsDialogFragment");
    	if (fragment != null) {
    		context.getFragmentManager().beginTransaction().remove(fragment).commit();
    	}
    	
    	MDColorsDialogFragment dialogFragment = new MDColorsDialogFragment();
    	dialogFragment.show(context.getFragmentManager(), "DMColorsDialogFragment");
	}
	
	private String[] colors;
	
	private Map<String, ColorDrawable> colorMap = new HashMap<String, ColorDrawable>();
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		setCancelable(true);
		
		View view = View.inflate(getActivity(), R.layout.as_ui_mdcolors_dialog, null);
		
		GridView gridView = (GridView) view.findViewById(R.id.grid);
		gridView.setAdapter(new MDColorsAdapter());
		gridView.setOnItemClickListener(this);
		
		return new AlertDialogWrapper.Builder(getActivity())
                                            .setView(view)
                                            .setPositiveButton(R.string.cancel, null)
                                            .create();
	}
	
	class MDColorsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return getColors().length;
		}

		@Override
		public Object getItem(int position) {
			return getColors()[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = View.inflate(getActivity(), R.layout.as_item_mdcolors, null);

			String color = getColors()[position];
			if (!colorMap.containsKey(color))
				colorMap.put(color, new ColorDrawable(Color.parseColor(color)));
			
			CircleImageView imgColor = (CircleImageView) convertView.findViewById(R.id.imgColor);
			ColorDrawable colorDrawable = colorMap.get(color);
			imgColor.setImageDrawable(colorDrawable);
			
			View imgSelected = convertView.findViewById(R.id.imgSelected);
			imgSelected.setVisibility(!TextUtils.isEmpty(AppSettings.getThemeColor()) &&
											AppSettings.getThemeColor().indexOf(getColors()[position]) != -1 ? View.VISIBLE : View.GONE);
			
			return convertView;
		}
		
	}
	
	String[] getColors() {
		if (colors == null)
			colors = getResources().getStringArray(R.array.metrail_design_colors);
		
		return colors;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String color = getColors()[position];

        AppSettings.setThemeColor(color);

    	dismiss();
	}
	
}
