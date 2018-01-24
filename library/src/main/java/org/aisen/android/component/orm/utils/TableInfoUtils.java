package org.aisen.android.component.orm.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.orm.SqliteUtility;
import org.aisen.android.component.orm.annotation.TableName;
import org.aisen.android.component.orm.extra.TableColumn;
import org.aisen.android.component.orm.extra.TableInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TableInfoUtils {

	public static final String TAG = SqliteUtility.TAG;
	
	private static final HashMap<String, TableInfo> tableInfoMap;
	
	static {
		tableInfoMap = new HashMap<String, TableInfo>();
	}

	public static <T> TableInfo exist(String dbName, Class<T> clazz) {
		return tableInfoMap.get(dbName + "-" + getTableName(clazz));
	}
	
	/**
	 * 当没有注解的时候默认用类的名称作为表名,并把点（.）替换为下划线(_)
	 * 
	 * @param clazz
	 * @return
	 */
	public static String getTableName(Class<?> clazz) {
		TableName table = clazz.getAnnotation(TableName.class);
		if (table == null || table.table().trim().length() == 0) {
			return clazz.getName().replace('.', '_');
		}
		return table.table();
	}
	
	public static <T> TableInfo newTable(String dbName, SQLiteDatabase db, Class<T> clazz) {
		Cursor cursor = null;
		
		TableInfo tableInfo = new TableInfo(clazz);
		tableInfoMap.put(dbName + "-" + getTableName(clazz), tableInfo);
		
		try {
			// 检查表是否存在
			String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='" + tableInfo.getTableName() + "' ";
			
			cursor = db.rawQuery(sql, null);
			if (cursor != null && cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					cursor.close();
					Logger.d(TAG, "表 %s 已存在", tableInfo.getTableName());

					cursor = db.rawQuery("PRAGMA table_info" + "(" + tableInfo.getTableName() + ")", null);
					// table的所有字段名称
					List<String> tableColumns = new ArrayList<String>();
					if (cursor != null && cursor.moveToNext()) {
						do {
							tableColumns.add(cursor.getString(cursor.getColumnIndex("name")));
						} while (cursor.moveToNext());
					}
					cursor.close();

					// 检查新对象的是否更新
					List<String> properList = new ArrayList<String>();
					for (TableColumn column : tableInfo.getColumns()) {
						properList.add(column.getColumn());
					}
					
					// 如果有新增字段，自动添加，暂时不能删除字段
					List<String> newFieldList = new ArrayList<String>();
					for (String field : properList) {
						if (tableInfo.getPrimaryKey().equals(field))
							continue;
						
						boolean isNew = true;
						
						for (String tableColumn : tableColumns) {
							if (tableColumn.equals(field)) {
								isNew = false;
								break;
							}
						}
						
						if (isNew)
							newFieldList.add(field);
					}
					
					for (String newField : newFieldList) {
						db.execSQL(String.format("ALTER TABLE %s ADD %s TEXT", tableInfo.getTableName(), newField));
						Logger.d(TAG, "表 %s 新增字段 %s", tableInfo.getTableName(), newField);
					}
					
					return tableInfo;
				}
			}
			
			// 创建一张新的表
			String createSql = SqlUtils.getTableSql(tableInfo);
			db.execSQL(createSql);
			Logger.d(TAG, "创建一张新表 %s", tableInfo.getTableName());
		} catch (Exception e) {
			e.printStackTrace();

            Logger.d(TAG, e.getMessage() + "");
		} finally {
			if (cursor != null)
				cursor.close();
			cursor = null;
		}

        return tableInfo;
	}
	
}
