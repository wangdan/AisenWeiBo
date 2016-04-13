package org.aisen.weibo.sina.support.permissions;

import android.Manifest;

import org.aisen.android.support.action.IAction;
import org.aisen.android.support.permissions.APermissionsAction;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;

/**
 * Created by wangdan on 16/3/28.
 */
public class SdcardPermissionAction extends APermissionsAction {

    public SdcardPermissionAction(BaseActivity context, IAction parent) {
        super(context, parent, context.getActivityHelper(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void onPermissionDenied(boolean alwaysDenied) {
        if (alwaysDenied) {
            ((BaseActivity) getContext()).showMessage(R.string.alwaysdenied_sdcard_permission);
        }
        else {
            ((BaseActivity) getContext()).showMessage(R.string.cancel_sdcard_permission);
        }
    }

}
