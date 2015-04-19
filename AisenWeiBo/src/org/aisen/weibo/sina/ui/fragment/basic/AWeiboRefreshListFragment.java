package org.aisen.weibo.sina.ui.fragment.basic;

import com.m.ui.fragment.ASwipeRefreshListFragment;

import java.io.Serializable;

/**
 * 设置一个刷新列表中间层，更换刷新控件修改这里的父类即可
 *
 * Created by wangdan on 15/4/14.
 */
public abstract class AWeiboRefreshListFragment<T extends Serializable, Ts extends Serializable>
                            extends ASwipeRefreshListFragment<T, Ts> {
}
