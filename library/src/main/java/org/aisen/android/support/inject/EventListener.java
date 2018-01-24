package org.aisen.android.support.inject;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EventListener implements OnClickListener, OnLongClickListener, OnItemClickListener, OnItemSelectedListener, OnItemLongClickListener {

    private Object handler;

    private String clickMethod;
    private String longClickMethod;
    private String itemClickMethod;
    private String itemSelectMethod;
    private String nothingSelectedMethod;
    private String itemLongClickMehtod;

    public EventListener(Object handler) {
        this.handler = handler;
    }

    public EventListener click(String method) {
        this.clickMethod = method;
        return this;
    }

    public EventListener longClick(String method) {
        this.longClickMethod = method;
        return this;
    }

    public EventListener itemLongClick(String method) {
        this.itemLongClickMehtod = method;
        return this;
    }

    public EventListener itemClick(String method) {
        this.itemClickMethod = method;
        return this;
    }

    public EventListener select(String method) {
        this.itemSelectMethod = method;
        return this;
    }

    public EventListener noSelect(String method) {
        this.nothingSelectedMethod = method;
        return this;
    }

    public boolean onLongClick(View v) {
        return invokeLongClickMethod(handler, longClickMethod, v);
    }

    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        return invokeItemLongClickMethod(handler, itemLongClickMehtod, arg0, arg1, arg2, arg3);
    }

    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

        invokeItemSelectMethod(handler, itemSelectMethod, arg0, arg1, arg2, arg3);
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        invokeNoSelectMethod(handler, nothingSelectedMethod, arg0);
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

        invokeItemClickMethod(handler, itemClickMethod, arg0, arg1, arg2, arg3);
    }

    public void onClick(View v) {

        invokeClickMethod(handler, clickMethod, v);
    }

    public static Object invokeClickMethod(Object handler, String methodName, Object... params) {
        if (handler == null)
            return null;
        Method method = null;
        Class<?> clazz = handler.getClass();

        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            /*try {
                method = clazz.getDeclaredMethod(methodName, View.class);
				method.setAccessible(true);
//				method = clazz.getMethod(methodName, View.class);
				if (method != null)
					return method.invoke(handler, params);
			} catch (Exception e) {
				e.printStackTrace();
			}*/
            Method ms[] = clazz.getDeclaredMethods();
            boolean breakFlag = false;
            for (Method m : ms) {
                if (m.getName().equals(methodName)) {
                    method = m;
                    breakFlag = true;
                    break;
                }
            }
            if (breakFlag) break;
        }
        if (method != null) {
            try {
                method.setAccessible(true);
                return method.invoke(handler, params);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    public static boolean invokeLongClickMethod(Object handler, String methodName, Object... params) {
        if (handler == null)
            return false;
        Method method = null;
        try {
            // public boolean onLongClick(View v)
            method = handler.getClass().getDeclaredMethod(methodName, View.class);
            method.setAccessible(true);
            if (method != null) {
                Object obj = method.invoke(handler, params);
                return obj == null ? false : Boolean.valueOf(obj.toString());
            } else
                throw new RuntimeException("no such method:" + methodName);
        } catch (Exception e) {
//			e.printStackTrace();
        }

        return false;

    }

    public static Object invokeItemClickMethod(Object handler, String methodName, Object... params) {
        if (handler == null)
            return null;
        Method method = null;
        try {
            // /onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            method = handler.getClass().getDeclaredMethod(methodName, AdapterView.class, View.class, int.class, long.class);
            if (method != null)
                return method.invoke(handler, params);
            else
                throw new RuntimeException("no such method:" + methodName);
        } catch (Exception e) {
//			e.printStackTrace();
        }

        return null;
    }

    public static boolean invokeItemLongClickMethod(Object handler, String methodName, Object... params) {
        if (handler == null)
            throw new RuntimeException("invokeItemLongClickMethod: handler is null :");
        Method method = null;
        try {
            // /onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,long
            // arg3)
            method = handler.getClass().getDeclaredMethod(methodName, AdapterView.class, View.class, int.class, long.class);
            if (method != null) {
                Object obj = method.invoke(handler, params);
                return Boolean.valueOf(obj == null ? false : Boolean.valueOf(obj.toString()));
            } else
                throw new RuntimeException("no such method:" + methodName);
        } catch (Exception e) {
//			e.printStackTrace();
        }

        return false;
    }

    public static Object invokeItemSelectMethod(Object handler, String methodName, Object... params) {
        if (handler == null)
            return null;
        Method method = null;
        try {
            // /onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long
            // arg3)
            method = handler.getClass().getDeclaredMethod(methodName, AdapterView.class, View.class, int.class, long.class);
            if (method != null)
                return method.invoke(handler, params);
            else
                throw new RuntimeException("no such method:" + methodName);
        } catch (Exception e) {
//			e.printStackTrace();
        }

        return null;
    }

    public static Object invokeNoSelectMethod(Object handler, String methodName, Object... params) {
        if (handler == null)
            return null;
        Method method = null;
        try {
            // onNothingSelected(AdapterView<?> arg0)
            method = handler.getClass().getDeclaredMethod(methodName, AdapterView.class);
            if (method != null)
                return method.invoke(handler, params);
            else
                throw new RuntimeException("no such method:" + methodName);
        } catch (Exception e) {
//			e.printStackTrace();
        }

        return null;
    }

}
