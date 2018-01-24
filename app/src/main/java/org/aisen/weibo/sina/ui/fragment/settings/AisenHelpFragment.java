package org.aisen.weibo.sina.ui.fragment.settings;

import android.os.Bundle;

import org.aisen.weibo.sina.R;

/**
 * Created by wangdan on 16/8/12.
 */
public class AisenHelpFragment extends BasePreferenceFragment {

    public static AisenHelpFragment newInstance() {
        return new AisenHelpFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addPreferencesFromResource(R.xml.ui_aisen_help);

    }

}
