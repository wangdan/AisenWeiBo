package org.aisen.android.ui.fragment.itemview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

/**
 *
 * Created by wangdan on 16/1/9.
 */
public abstract class AHeaderItemViewCreator<T extends Serializable> implements IItemViewCreator<T> {

    @Override
    public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
        for (int[] headerLayoutRes : setHeaders()) {
            if (viewType == headerLayoutRes[1]) {
                return inflater.inflate(headerLayoutRes[0], parent, false);
            }
        }
        return null;
    }

    /**
     *
     * @return position 0:layoutRes,1:viewType
     */
    abstract public int[][] setHeaders();

}
