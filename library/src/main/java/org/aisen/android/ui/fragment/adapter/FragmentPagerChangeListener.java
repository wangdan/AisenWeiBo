package org.aisen.android.ui.fragment.adapter;

public interface FragmentPagerChangeListener {

	void instantiate(String fragmentName);
	
	void destroy(String fragmentName);
	
}
