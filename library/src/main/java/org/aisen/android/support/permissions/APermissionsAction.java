package org.aisen.android.support.permissions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.support.action.IAction;

/**
 * 申请一组权限
 *
 * Created by wangdan on 16/2/26.
 */
public abstract class APermissionsAction extends IAction implements IPermissionsObserver {

    public static final String TAG = "Permission";

    private IPermissionsSubject subject;
    private String permission;
    private int requestCode;

    public APermissionsAction(Activity context, IAction parent, IPermissionsSubject subject, String permission) {
        super(context, parent);

        this.subject = subject;
        this.permission = permission;
        requestCode = permission.hashCode();
    }



    @Override
    protected boolean interrupt() {
        boolean interrupt = super.interrupt();

        if (requestCode == 0) {

        }
        // 低于SDK23
        else if (Build.VERSION.SDK_INT < 23) {

        }
        // 授予了权限
        else if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getContext(), permission)) {
            Logger.d(TAG, "已经授予了权限, permission = %s", permission);
        }
        // 没有或者拒绝了权限
        else if (PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(getContext(), permission)) {
            interrupt = true;

            Logger.d(TAG, "%s permission = %s", "PERMISSION_DENIED", permission);

            doInterrupt();
        }

        return interrupt;
    }

    @Override
    public void doInterrupt() {
        // 对没有权限做出处理，默认申请权限
        if (!handlePermissionNone()) {
            Logger.d(TAG, "handlePermissionNone(false)");

            requestPermission();
        }
        else {
            Logger.d(TAG, "handlePermissionNone(true)");
        }
    }

    /**
     * 申请权限时，如果权限是已经被拒绝的，做出处理
     *
     * @return true:请求一次权限，调用requestPermission()
     */
    protected boolean handlePermissionNone() {
        return false;
    }

    /**
     * 权限被拒绝了
     *
     * @param alwaysDenied 勾选了不再提醒
     *
     * @return  true:
     */
    protected void onPermissionDenied(boolean alwaysDenied) {

    }

    /**
     * 申请权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    protected void requestPermission() {
        if (subject != null) {
            subject.attach(this);
        }

        Logger.d(TAG, "requestPermission(%s)", permission);

        // XT1562(Motorola) 这里会上报一个这样的错误，暂时搞不懂为什么
        // java.lang.IllegalArgumentException:Wake lock not active: android.os.Binder@3e69cbf from uid 1000
        try {
            getContext().requestPermissions(new String[]{ permission }, requestCode);
        } catch (IllegalArgumentException e) {
            Logger.printExc(APermissionsAction.class, e);
        }
    }

    /**
     * 处理授权结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Logger.d(TAG, "onRequestPermissionsResult , requestCode = " + requestCode);

        if (grantResults != null && grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                Logger.d(TAG, "requestCode = %d, permission = %s, grantResult = %d", requestCode, permissions[i], grantResults[i]);
            }
        }

        if (subject != null) {
            subject.detach(this);
        }

        if (requestCode == this.requestCode) {
            if (permissions != null && permissions.length > 0 && permission.equals(permissions[0])) {
                if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    run();

                    return;
                }
            }

            if (ActivityCompat.shouldShowRequestPermissionRationale(getContext(), permission)) {
                Logger.d(TAG, "onPermissionDenied(false)");

                onPermissionDenied(false);
            } else {
                Logger.d(TAG, "onPermissionDenied(true)");

                onPermissionDenied(true);
            }
        }
    }

}
