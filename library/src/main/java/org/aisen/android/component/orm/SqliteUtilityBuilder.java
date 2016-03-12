package org.aisen.android.component.orm;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.aisen.android.common.utils.Logger;

import java.io.File;
import java.io.IOException;

public class SqliteUtilityBuilder {
	
	public static final String TAG = SqliteUtility.TAG;
	
	static final String DEFAULT_DB = "com_m_default_db";// 默认DB名称
	
	private String path;// DB的SD卡路径

	private String dbName = DEFAULT_DB;
	
	private int version = 1;// DB的Version，每次升级DB都默认先清库
	
	private boolean sdcardDb = false;
	
	public SqliteUtilityBuilder configDBName(String dbName) {
		this.dbName = dbName;
		
		return this;
	}
	
	public SqliteUtilityBuilder configVersion(int version) {
		this.version = version;
		
		return this;
	}
	
	public SqliteUtilityBuilder configSdcardPath(String path) {
		this.path = path;
		sdcardDb = true;
		
		return this;
	}
	
	public SqliteUtility build(Context context) {
		SQLiteDatabase db = null;
		
		if (sdcardDb) {
			db = openSdcardDb(path, dbName, version);

            Logger.d(TAG, String.format(String.format("打开app库 %s, version = %d", dbName, db.getVersion())));
		}
		else {
			db = new SqliteDbHelper(context, dbName, version).getWritableDatabase();

            Logger.d(TAG, String.format(String.format("打开sdcard库 %s, version = %d", dbName, db.getVersion())));
		}

		return new SqliteUtility(dbName, db);
	}

	static SQLiteDatabase openSdcardDb(String path, String dbName, int version) {
		SQLiteDatabase db = null;
		File dbf = new File(path + File.separator + dbName + ".db");
		
		if (dbf.exists()) {
			Logger.d(TAG, "打开库 %s", dbName);
			db = SQLiteDatabase.openOrCreateDatabase(dbf, null);
		} else {
			dbf.getParentFile().mkdirs();
			
			try {
				if (dbf.createNewFile()) {
					Logger.d(TAG, "新建一个库在sd卡, 库名 = %s, 路径 = %s", dbName, dbf.getAbsolutePath());
					db = SQLiteDatabase.openOrCreateDatabase(dbf, null);
				}
			} catch (IOException ioex) {
				ioex.printStackTrace();
				
				throw new RuntimeException("新建库失败, 库名 = " + dbName + ", 路径 = " + path, ioex);
			}
		}
		
		if (db != null) {
			int dbVersion = db.getVersion();
			Logger.d(TAG, "表 %s 的version = %d, newVersion = %d", dbName, dbVersion, version);
			
			if (dbVersion < version) {
				dropDb(db);

                // 更新DB的版本信息
                db.beginTransaction();
                try {
                    db.setVersion(version);
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                } finally {
                    db.endTransaction();
                }
			}

            return db;
		}
		
		throw new RuntimeException("打开库失败, 库名 = " + dbName + ", 路径 = " + path);
	}
	
	static class SqliteDbHelper extends SQLiteOpenHelper {

		SqliteDbHelper(Context context, String dbName, int dbVersion) {
			super(context, dbName, null, dbVersion);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			dropDb(db);
			onCreate(db);
		}
		
	}
	
	static void dropDb(SQLiteDatabase db) {
		Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type ='table' AND name != 'sqlite_sequence'", null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				db.execSQL("DROP TABLE " + cursor.getString(0));

                Logger.d(TAG, "删除表 = " + cursor.getString(0));
			}
		}
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
	}
	
}
