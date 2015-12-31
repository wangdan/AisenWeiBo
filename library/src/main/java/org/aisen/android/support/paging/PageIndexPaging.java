package org.aisen.android.support.paging;

import android.text.TextUtils;

import org.aisen.android.network.biz.IResult;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * 始终自增，但是有最大分页页码，根据配置的属性获取
 * 
 * @author Jeff.Wang
 *
 * @date 2014年9月22日
 */
public class PageIndexPaging<T extends Serializable, Ts extends Serializable> implements IPaging<T, Ts> {

	private static final long serialVersionUID = 8485595687197548908L;

	int pageTotal = -1;
	int pageIndex = 1;

	String pageTotalField;

	public PageIndexPaging(String pageTotalField) {
		this.pageTotalField = pageTotalField;
	}

	public PageIndexPaging() {
	}

	@Override
    public void processData(Ts newDatas, T firstData, T lastData) {
		pageIndex++;
		if (newDatas instanceof IResult) {
			IResult iResult = (IResult) newDatas;
			if (iResult.fromCache() && iResult.pagingIndex() != null) {
				pageIndex = Integer.parseInt(iResult.pagingIndex()[1]);
			}
		}
		if (!TextUtils.isEmpty(pageTotalField)) {
			Class clazz = newDatas.getClass();
			while (clazz != Object.class) {
				try {
					Field field = clazz.getDeclaredField(pageTotalField);
					field.setAccessible(true);
					pageTotal = Integer.parseInt(field.get(newDatas).toString());
					break;
				} catch (Exception e) {
					clazz = clazz.getSuperclass();
				}
			}
		}
	}

	@Override
    public String getPreviousPage() {
		return String.valueOf(pageIndex - 1);
	}

	@Override
    public String getNextPage() {
		return String.valueOf(pageIndex);
	}

}
