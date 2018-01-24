package org.aisen.android.component.orm.extra;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.orm.SqliteUtility;
import org.aisen.android.component.orm.annotation.AutoIncrementPrimaryKey;
import org.aisen.android.component.orm.annotation.PrimaryKey;
import org.aisen.android.component.orm.utils.TableInfoUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * 表信息<br/>
 * 包含表名，字段信息<br/>
 * 
 * @author wangdan
 *
 */
public class TableInfo {
	
	private Class<?> clazz;
	
	private TableColumn primaryKey;
	
	private String tableName;
	
	private final List<TableColumn> columns;
	
	public TableInfo(Class<?> clazz) {
		columns = new ArrayList<TableColumn>();

		this.clazz = clazz;
		
		setInit();
	}

	public TableInfo(String tableName, TableColumn primaryKey) {
		columns = new ArrayList<TableColumn>();

		this.tableName = tableName;
		this.primaryKey = primaryKey;
	}
	
	private <T> void setInit() {
		// 设置表名
		setTableName();
		// 获取所有的属性
		setColumns(clazz);
		// 如果主键为空，抛出异常
		if (primaryKey == null)
			throw new RuntimeException("类 " + clazz.getSimpleName() + " 没有设置主键，请使用标注主键");
		
		if (Logger.DEBUG) {
			Logger.v(SqliteUtility.TAG, String.format("类 %s 的主键是 %s", clazz.getSimpleName(), primaryKey.getColumn()));
			for (TableColumn column : columns) {
				Logger.v(SqliteUtility.TAG, String.format("[column = %s, datatype = %s]", column.getColumn(), column.getDataType()));
			}
		}
	}
	
	private void setTableName() {
		tableName = TableInfoUtils.getTableName(clazz);
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public TableColumn getPrimaryKey() {
		return primaryKey;
	}

	public String getTableName() {
		return tableName;
	}
	
	public List<TableColumn> getColumns() {
		return columns;
	}
	
	public void setColumns(Class<?> c) {
        if (c == null || "Object".equalsIgnoreCase(c.getSimpleName())) {
            return;
        }
		
		Field fields[] = c.getDeclaredFields();
		
		for (Field field : fields) {
			// 设置主键
			if (primaryKey == null) {
				PrimaryKey annotationField = field.getAnnotation(PrimaryKey.class);
				if (annotationField != null) {
					primaryKey = new TableColumn();
                    primaryKey.setColumn(annotationField.column());
                    setColumn(field, primaryKey);
					continue;
				} else {
					AutoIncrementPrimaryKey autoIncrementPrimaryKey = field.getAnnotation(AutoIncrementPrimaryKey.class);
					if (autoIncrementPrimaryKey != null) {
						primaryKey = new AutoIncrementTableColumn();
	                    primaryKey.setColumn(autoIncrementPrimaryKey.column());
	                    setColumn(field, primaryKey);
						continue;
					}
				}
			}
		
			// 不包括序列划字段
			if ("serialVersionUID".equals(field.getName()))
				continue;
			// Gradle编译自动产生的字段
			if ("$change".equals(field.getName()))
				continue;

			// 添加到字段集合
			TableColumn column = new TableColumn();
			column.setColumn(field.getName());
            setColumn(field, column);
			columns.add(column);
		}
		
		setColumns(c.getSuperclass());
	}
	
	private void setColumn(Field field, TableColumn column) {
        column.setField(field);

		if (field.getType().getName().equals("int") ||
				field.getType().getName().equals("java.lang.Integer")) {
	        column.setDataType("int");
            column.setColumnType("INTEGER");
		}
		else if (field.getType().getName().equals("long") ||
				field.getType().getName().equals("java.lang.Long")) {
			column.setDataType("long");
            column.setColumnType("INTEGER");
		}
		else if (field.getType().getName().equals("float") ||
				field.getType().getName().equals("java.lang.Float")) {
			column.setDataType("float");
            column.setColumnType("REAL");
		}
		else if (field.getType().getName().equals("double") ||
				field.getType().getName().equals("java.lang.Double")) {
            column.setDataType("double");
            column.setColumnType("REAL");
		}
		else if (field.getType().getName().equals("boolean") ||
				field.getType().getName().equals("java.lang.Boolean")) {
			column.setDataType("boolean");
            column.setColumnType("TEXT");
		}
		else if (field.getType().getName().equals("char") ||
				field.getType().getName().equals("java.lang.Character")) {
            column.setDataType("char");
            column.setColumnType("TEXT");
		}
		else if (field.getType().getName().equals("byte") ||
				field.getType().getName().equals("java.lang.Byte")) {
            column.setDataType("byte");
            column.setColumnType("INTEGER");
		}
		else if (field.getType().getName().equals("short") ||
				field.getType().getName().equals("java.lang.Short")) {
            column.setDataType("short");
            column.setColumnType("TEXT");
		}
		else if (field.getType().getName().equals("java.lang.String")) {
            column.setDataType("string");
            column.setColumnType("TEXT");
		}
        else if (field.getType().getName().equals("[B")) {
            column.setDataType("blob");
            column.setColumnType("BLOB");
        }
		else {
            column.setDataType("object");
            column.setColumnType("TEXT");
		}
	}
	
}
