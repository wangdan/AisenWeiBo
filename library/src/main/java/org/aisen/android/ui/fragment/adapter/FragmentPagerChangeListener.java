package org.aisen.android.ui.fragment.adapter;

public interface FragmentPagerChangeListener {

	public void instantiate(String fragmentName);
	
	public void destroy(String fragmentName);
	
}
