package org.aisen.android.component.orm.utils;

import android.text.TextUtils;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.orm.SqliteUtility;
import org.aisen.android.component.orm.extra.AutoIncrementTableColumn;
import org.aisen.android.component.orm.extra.Extra;
import org.aisen.android.component.orm.extra.TableColumn;
import org.aisen.android.component.orm.extra.TableInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdan on 15-3-17.
 */
public class SqlUtils {

    public static String getTableSql(TableInfo table) {
        TableColumn primaryKey = table.getPrimaryKey();
        StringBuffer strSQL = new StringBuffer();
        strSQL.append("CREATE TABLE IF NOT EXISTS ");
        strSQL.append(table.getTableName());
        strSQL.append(" ( ");

        // 自增主键
        if (primaryKey instanceof AutoIncrementTableColumn) {
        	strSQL.append(" ")
        		  .append(primaryKey.getColumn()).append(" ")
        		  .append(" INTEGER PRIMARY KEY AUTOINCREMENT ,");
        }
        else {
        	strSQL.append(" ").append(primaryKey.getColumn())
	              .append(" ").append(primaryKey.getColumnType())
	              .append(" NOT NULL ,");
        }

        for (TableColumn column : table.getColumns()) {
            strSQL.append(" ").append(column.getColumn());
            strSQL.append(" ").append(column.getColumnType());
            strSQL.append(" ,");
        }

        strSQL.append(" ").append(FieldUtils.OWNER).append(" text NOT NULL, ");

        strSQL.append(" ").append(FieldUtils.KEY).append(" text NOT NULL, ");

        strSQL.append(" ").append(FieldUtils.CREATEAT).append(" INTEGER NOT NULL ");

        // 自动增加的主键，只有id一个
        if (primaryKey instanceof AutoIncrementTableColumn) {
        }
        else {
            strSQL.append(", PRIMARY KEY ( ").append(primaryKey.getColumn()).append(" , ")
                    .append(FieldUtils.KEY).append(" , ")
                    .append(FieldUtils.OWNER).append(" )");
        }

//        strSQL.append(" PRIMARY KEY ( ").append(primaryKey.getColumn()).append(" ) ");

//        strSQL.append(" PRIMARY KEY ( ").append(primaryKey.getColumn()).append(" ), ");
//        strSQL.append(" UNIQUE KEY (").append(primaryKey.getColumn()).append(", ")
//                                      .append(FieldUtils.OWNER).append(", ")
//                                      .append(FieldUtils.KEY).append(" )");

//        strSQL.deleteCharAt(strSQL.length() - 1);
        strSQL.append(" )");

        String tableStr = strSQL.toString();
        Logger.d(SqliteUtility.TAG, "create table = " + tableStr);
        return tableStr;
    }

    public static String createSqlInsert(String insertInto, TableInfo tableInfo) {
        List<TableColumn> columns = new ArrayList<TableColumn>();
        // 如果是自增主键，不设置值
        if (tableInfo.getPrimaryKey() instanceof AutoIncrementTableColumn)
        	;
        else
        	columns.add(tableInfo.getPrimaryKey());
        columns.addAll(tableInfo.getColumns());
        columns.add(FieldUtils.getOwnerColumn());
        columns.add(FieldUtils.getKeyColumn());
        columns.add(FieldUtils.getCreateAtColumn());

        StringBuilder builder = new StringBuilder(insertInto);
        builder.append(tableInfo.getTableName()).append(" (");
        appendColumns(builder, columns);
        builder.append(") VALUES (");
        appendPlaceholders(builder, columns.size());
        builder.append(')');
        return builder.toString();
    }

    public static StringBuilder appendColumns(StringBuilder builder, List<TableColumn> columns) {
        int length = columns.size();
        for (int i = 0; i < length; i++) {
            builder.append('\'').append(columns.get(i).getColumn()).append('\'');
            if (i < length - 1) {
                builder.append(',');
            }
        }
        return builder;
    }

    public static StringBuilder appendPlaceholders(StringBuilder builder, int count) {
        for (int i = 0; i < count; i++) {
            if (i < count - 1) {
                builder.append("?,");
            } else {
                builder.append('?');
            }
        }
        return builder;
    }

    public static String appendExtraWhereClauseSql(Extra extra) {
        StringBuffer sb = new StringBuffer();

        if (extra == null || (TextUtils.isEmpty(extra.getKey()) && TextUtils.isEmpty(extra.getOwner()))) {
            sb.append("");
        }
        else if (!TextUtils.isEmpty(extra.getKey()) && !TextUtils.isEmpty(extra.getOwner())) {
            sb.append(" ").append(FieldUtils.OWNER).append(" = '").append(extra.getOwner()).append("' ")
                          .append(" and ")
                          .append(FieldUtils.KEY).append(" = '").append(extra.getKey()).append("' ");
        }
        else if (!TextUtils.isEmpty(extra.getKey())) {
            sb.append(FieldUtils.KEY).append(" = '").append(extra.getKey()).append("' ");
        }
        else if (!TextUtils.isEmpty(extra.getOwner())) {
            sb.append(" ").append(FieldUtils.OWNER).append(" = '").append(extra.getOwner()).append("' ");
        }

        return sb.toString();
    }

    public static String appendExtraWhereClause(Extra extra) {
        StringBuffer sb = new StringBuffer();

        if (extra == null || (TextUtils.isEmpty(extra.getKey()) && TextUtils.isEmpty(extra.getOwner()))) {
            sb.append("");
        }
        else if (!TextUtils.isEmpty(extra.getKey()) && !TextUtils.isEmpty(extra.getOwner())) {
            sb.append(" ").append(FieldUtils.KEY).append(" = ? ")
                    .append(" and ")
                    .append(FieldUtils.OWNER).append(" = ? ");
        }
        else if (!TextUtils.isEmpty(extra.getKey())) {
            sb.append(FieldUtils.KEY).append(" = ? ");
        }
        else if (!TextUtils.isEmpty(extra.getOwner())) {
            sb.append(" ").append(FieldUtils.OWNER).append(" = ? ");
        }

        return sb.toString();
    }

    public static String[] appendExtraWhereArgs(Extra extra) {
        List<String> argList = new ArrayList<String>();

        if (extra != null) {
            if (!TextUtils.isEmpty(extra.getKey()))
                argList.add(extra.getKey());
            if (!TextUtils.isEmpty(extra.getOwner()))
                argList.add(extra.getOwner());
        }

        return argList.toArray(new String[0]);
    }

}
