package org.aisen.android.ui.activity.basic;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import org.aisen.android.support.permissions.IPermissionsObserver;
import org.aisen.android.support.permissions.IPermissionsSubject;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户注册回调BaseActivity的生命周期及相关的方法，自行添加
 *
 * Created by wangdan on 15/4/14.
 */
public class BaseActivityHelper implements IPermissionsSubject {

    private List<IPermissionsObserver> observers;

    private BaseActivity mActivity;

    protected void bindActivity(BaseActivity activity) {
        this.mActivity = activity;
    }

    protected BaseActivity getActivity() {
        return mActivity;
    }

    protected void onCreate(Bundle savedInstanceState) {
        observers = new ArrayList<>();
    }

    public void onPostCreate(Bundle savedInstanceState) {

    }

    public View findViewById(int id) {
        return mActivity.findViewById(id);
    }

    protected void onStart() {

    }

    protected void onRestart() {

    }

    protected void onResume() {

    }

    protected void onPause() {

    }

    protected void onStop() {

    }

    public void onDestroy() {

    }

    public void finish() {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    public void onSaveInstanceState(Bundle outState) {

    }

    // 这三个方法暂不支持
//    public void setContentView(int layoutResID) {
//
//    }
//
//    public void setContentView(View view) {
//
//    }
//
//    public void setContentView(View view, ViewGroup.LayoutParams params) {
//
//    }

    protected boolean onHomeClick() {
        return false;
    }

    public boolean onBackClick() {
        return false;
    }

    protected int configTheme() {
        return 0;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
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

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        notifyActivityResult(requestCode, permissions, grantResults);
    }

}
