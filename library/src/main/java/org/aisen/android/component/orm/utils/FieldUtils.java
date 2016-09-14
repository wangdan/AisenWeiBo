package org.aisen.android.component.orm.utils;

import org.aisen.android.component.orm.extra.TableColumn;

public class FieldUtils {
	
	public static final String OWNER = "com_m_common_owner";

	public static final String KEY = "com_m_common_key";

	public static final String CREATEAT = "com_m_common_createat";

    public static TableColumn getOwnerColumn() {
        TableColumn column = new TableColumn();
        column.setColumn(OWNER);
        return column;
    }

    public static TableColumn getKeyColumn() {
        TableColumn column = new TableColumn();
        column.setColumn(KEY);
        return column;
    }

    public static TableColumn getCreateAtColumn() {
        TableColumn column = new TableColumn();
        column.setColumn(CREATEAT);
        return column;
    }

}
