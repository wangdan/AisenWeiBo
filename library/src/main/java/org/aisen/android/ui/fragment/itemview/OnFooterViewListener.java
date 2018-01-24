package org.aisen.android.ui.fragment.itemview;

import org.aisen.android.network.task.TaskException;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.APagingFragment;

/**
 * Created by wangdan on 16/1/9.
 */
public interface OnFooterViewListener {

    void onTaskStateChanged(AFooterItemView<?> footerItemView, ABaseFragment.ABaseTaskState state, TaskException exception, APagingFragment.RefreshMode mode);

    void setFooterViewToRefreshing();

}
