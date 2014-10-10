package org.aisen.weibo.sina.ui.activity.topics;

import org.aisen.weibo.sina.ui.activity.common.WeiboBaseActivity;
import org.aisen.weibo.sina.ui.fragment.topics.TopicsFragment;

import android.net.Uri;
import android.os.Bundle;

import com.m.R;
import com.m.ui.fragment.ABaseFragment;

/**
 * 话题
 * 
 * @author wangdan
 *
 */
public class TopicsActivity extends WeiboBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_fragment_container);

		if (savedInstanceState == null) {
			Uri data = getIntent().getData();
			if (data != null) {
				String d = data.toString();
				int index = d.lastIndexOf("/");
				String topicsName = d.substring(index + 1);
				topicsName = topicsName.substring(1, topicsName.length() - 1);

				ABaseFragment fragment = TopicsFragment.newInstance(topicsName);
				getFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment, "TopicsTimelint").commit();
			} 
			else {
				finish();
				return;
			}
		}

	}
	
}
