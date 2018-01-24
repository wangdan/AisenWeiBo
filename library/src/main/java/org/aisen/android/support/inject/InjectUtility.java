package org.aisen.android.support.inject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;

import java.lang.reflect.Field;

public class InjectUtility {

	static final String TAG = "InjectUtility";

    public static void initInjectedView(Activity sourceActivity) {
		initInjectedView(sourceActivity, sourceActivity, sourceActivity.getWindow().getDecorView());
	}

    public static void initInjectedView(Context context, Object injectedSource, View sourceView) {
		Class<?> clazz = injectedSource.getClass();
		for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
			Field[] fields = clazz.getDeclaredFields();
			if (fields != null && fields.length > 0) {
				for (Field field : fields) {
					ViewInject viewInject = field.getAnnotation(ViewInject.class);
					if (viewInject != null) {
						// ViewId可以是id配置，也可以是IdStr配置
						int viewId = viewInject.id();
						if (viewId == 0) {
							String idStr = viewInject.idStr();
							if (!TextUtils.isEmpty(idStr)) {
								try {
									String packageName = context.getPackageName();
									Resources resources = context.getPackageManager().getResourcesForApplication(packageName);

									viewId = resources.getIdentifier(idStr, "id", packageName);

									if (viewId == 0)
										throw new RuntimeException(String.format("%s 的属性%s关联了id=%s，但是这个id是无效的", clazz.getSimpleName(),
												field.getName(), idStr));
								} catch (Exception e) {
//									e.printStackTrace();
								}
							}
						}
						if (viewId != 0) {
							try {
								field.setAccessible(true);
								/*
								 * 当已经被赋值时，不在重复赋值，用于include，inflate情景下的viewinject组合
								 */
								if (field.get(injectedSource) == null) {
									field.set(injectedSource, sourceView.findViewById(viewId));

									if (Logger.DEBUG) {
										Logger.v(TAG, "id = %d, view = %s", viewId, field.get(injectedSource) + "");
									}
								} else {
									continue;
								}
							} catch (Exception e) {
								Logger.printExc(InjectUtility.class, e);
							}
						}

						String clickMethod = viewInject.click();
						if (!TextUtils.isEmpty(clickMethod))
							setViewClickListener(injectedSource, field, clickMethod);

						String longClickMethod = viewInject.longClick();
						if (!TextUtils.isEmpty(longClickMethod))
							setViewLongClickListener(injectedSource, field, longClickMethod);

						String itemClickMethod = viewInject.itemClick();
						if (!TextUtils.isEmpty(itemClickMethod))
							setItemClickListener(injectedSource, field, itemClickMethod);

						String itemLongClickMethod = viewInject.itemLongClick();
						if (!TextUtils.isEmpty(itemLongClickMethod))
							setItemLongClickListener(injectedSource, field, itemLongClickMethod);

						Select select = viewInject.select();
						if (!TextUtils.isEmpty(select.selected()))
							setViewSelectListener(injectedSource, field, select.selected(), select.noSelected());

					}
				}
			}
		}
	}

    public static void setViewClickListener(Object injectedSource, Field field, String clickMethod) {
		try {
			Object obj = field.get(injectedSource);
			if (obj instanceof View) {
				((View) obj).setOnClickListener(new EventListener(injectedSource).click(clickMethod));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static void setViewLongClickListener(Object injectedSource, Field field, String clickMethod) {
		try {
			Object obj = field.get(injectedSource);
			if (obj instanceof View) {
				((View) obj).setOnLongClickListener(new EventListener(injectedSource).longClick(clickMethod));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static void setItemClickListener(Object injectedSource, Field field, String itemClickMethod) {
		try {
			Object obj = field.get(injectedSource);
			if (obj instanceof AbsListView) {
				((AbsListView) obj).setOnItemClickListener(new EventListener(injectedSource).itemClick(itemClickMethod));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static void setItemLongClickListener(Object injectedSource, Field field, String itemClickMethod) {
		try {
			Object obj = field.get(injectedSource);
			if (obj instanceof AbsListView) {
				((AbsListView) obj).setOnItemLongClickListener(new EventListener(injectedSource).itemLongClick(itemClickMethod));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static void setViewSelectListener(Object injectedSource, Field field, String select, String noSelect) {
		try {
			Object obj = field.get(injectedSource);
			if (obj instanceof View) {
				((AbsListView) obj).setOnItemSelectedListener(new EventListener(injectedSource).select(select).noSelect(noSelect));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
