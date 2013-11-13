package com.phodev.andtools.db.sample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.phodev.andtools.common.CommonParam;
import com.phodev.andtools.db.sample.PhodevDB.Table_Note;

public class PhodevDatabaseHelper extends SQLiteOpenHelper {
	private static final String TAG = "PhodevDatabaseHelper";
	public static final String DATABASE_NAME = "phodev_demo_db_module.db";

	public static final int DATABASE_VERSION = 1;
	private static PhodevDatabaseHelper instance = null;

	public static synchronized PhodevDatabaseHelper getInstance(Context context) {
		if (instance == null) {
			instance = new PhodevDatabaseHelper(context);
		}
		return instance;
	}

	PhodevDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		SQLiteDatabase db = super.getWritableDatabase();
		return db;
	}

	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		SQLiteDatabase db = super.getReadableDatabase();
		return db;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Table_Note.create_table_sql);
		if (CommonParam.DEBUG) {
			log("first init database ,so create all tables,current version:"
					+ DATABASE_VERSION);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	void log(String msg) {
		Log.d(TAG, "" + msg);
	}
}
