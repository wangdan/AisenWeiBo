package org.aisen.android.support.permissions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdan on 16/2/26.
 */
public class DefPermissionsSubject implements IPermissionsSubject {

    private List<IPermissionsObserver> observers;

    public DefPermissionsSubject() {
        observers = new ArrayList<>();
    }

    @Override
    public void attach(IPermissionsObserver observer) {
        if (observer != null && !observers.contains(observer))
            observers.add(observer);
    }

    @Override
    public void detach(IPermissionsObserver observer) {
        if (observer != null && !observers.contains(observer))
            observers.remove(observer);
    }

    @Override
    public void notifyActivityResult(int requestCode, String[] permissions, int[] grantResults) {
        for (IPermissionsObserver observer : observers) {
            observer.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
