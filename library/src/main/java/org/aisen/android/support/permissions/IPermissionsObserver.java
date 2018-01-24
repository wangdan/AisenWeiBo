package org.aisen.android.support.permissions;

/**
 * Created by wangdan on 16/2/26.
 */
public interface IPermissionsObserver {

    void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

}
