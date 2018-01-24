package org.aisen.android.support.permissions;

/**
 * Created by wangdan on 16/2/26.
 */
public interface IPermissionsSubject {

    void attach(IPermissionsObserver observer);

    void detach(IPermissionsObserver observer);

    void notifyActivityResult(int requestCode, String[] permissions, int[] grantResults);

}
