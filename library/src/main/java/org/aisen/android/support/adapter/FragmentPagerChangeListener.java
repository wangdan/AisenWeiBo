package org.aisen.android.support.adapter;

public interface FragmentPagerChangeListener {

	public void instantiate(String fragmentName);
	
	public void destroy(String fragmentName);
	
}
