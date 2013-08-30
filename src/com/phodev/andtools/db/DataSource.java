package com.phodev.andtools.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * 数据源
 * 
 * @author skg
 * 
 */
public interface DataSource {
	public SQLiteDatabase openSQLiteDatabase(Context context);

	public void askCloseDatabase(SQLiteDatabase db);
}
