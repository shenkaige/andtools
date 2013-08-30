package com.phodev.andtools.download.impl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * 下载记录
 * 
 * @author skg
 * 
 */
public class DownloadDatabase extends SQLiteOpenHelper {
	public static final String TAG = "DatabaseOpenHelper";
	public static final String DATABASE_NAME = "download.db";
	public static final int DATABASE_VERSION = 1;
	private static DownloadDatabase instance;

	private DownloadDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static final class TABLE_DownloadFile implements BaseColumns {
		public static final String _table_name = "download_file";
		public static final String file_source_url = "file_source_url";
		public static final String file_name = "file_name";
		public static final String file_size = "file_size";
		public static final String loaded_size = "loaded_size";
		public static final String status = "status";

		public static final String _table_create_sql = "CREATE TABLE "
				+ _table_name + //
				" ("//
				+ _ID + " INTEGER PRIMARY KEY,"//
				+ file_source_url + " TEXT,"//
				+ file_name + " TEXT,"//
				+ file_size + " LONG,"//
				+ loaded_size + " LONG,"//
				+ status + " INTEGER"//
				+ ");";
	}

	public static final class TABLE_DownloadBlock implements BaseColumns {
		public static final String _table_name = "download_block";
		public static final String file_source_url = "file_source_url";
		public static final String block_start = "block_start";
		public static final String block_end = "block_end";
		public static final String block_loaded_size = "block_loaded_size";

		public static final String _table_create_sql = "CREATE TABLE "
				+ _table_name + //
				" ("//
				+ _ID + " TEXT PRIMARY KEY,"//
				+ file_source_url + " TEXT,"//
				+ block_start + " LONG,"//
				+ block_end + " LONG,"//
				+ block_loaded_size + " INTEGER"//
				+ ");";
	}

	public static synchronized DownloadDatabase getInstance(Context context) {
		if (instance == null) {
			instance = new DownloadDatabase(context);
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_DownloadFile._table_create_sql);
		db.execSQL(TABLE_DownloadBlock._table_create_sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}