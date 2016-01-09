package org.aisen.android.ui.fragment.itemview;

import java.io.Serializable;

/**
 *
 * Created by wangdan on 16/1/9.
 */
public abstract class AHeaderItemViewCreator<T extends Serializable> implements IItemViewCreator<T> {

    @Override
    final public int[][] setLayoutRes() {
        return setHeaderLayoutRes();
    }

    abstract public int[][] setHeaderLayoutRes();

}
