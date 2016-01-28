package org.aisen.weibo.sina.ui.fragment.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class BasePreferenceFragment extends PreferenceFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		
		ListView listView = (ListView) rootView.findViewById(android.R.id.list);
//		listView.setClipToPadding(false);

		return rootView;
	}
	
	protected void setListSetting(int value, int hintId, ListPreference listPreference) {
		String[] valueTitleArr = getResources().getStringArray(hintId);
		
		listPreference.setSummary(valueTitleArr[value]);
	}

}
