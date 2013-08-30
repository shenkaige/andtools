package com.phodev.andtools.db.sample;

import com.phodev.andtools.db.DataSource;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * DataSource impl
 * 
 * @author skg
 * 
 */
public class PhodevDB implements DataSource {

	/** Note */
	public static final class Table_Note {
		private Table_Note() {
		}

		public static final String _table_name = "notes";
		public static final String ID = "_id";
		public static final String GRAMMAR_ID = "_grammar_id";
		public static final String TYPE = "_type";
		public static final String CONTENT = "_content";
		public static final String VERSION = "_version";
		public static final String LAST_OPERATE = "_last_operate";
		public static final String NOTE_CREATE_TIME = "_note_create_time";
		public static final String LOCAL_STATE = "_local_state";
		public static final String UID = "_uid";
		//
		public static final String create_table_sql = "CREATE TABLE "
				+ _table_name + " ("//
				+ ID + " INTEGER,"//
				+ GRAMMAR_ID + " INTEGER,"//
				+ TYPE + " INTEGER,"//
				+ CONTENT + " TEXT,"//
				+ VERSION + " INTEGER,"//
				+ LAST_OPERATE + " TEXT,"//
				+ NOTE_CREATE_TIME + " LONG PRIMARY KEY NOT NULL,"//
				+ LOCAL_STATE + " INTEGER,"//
				+ UID + " TEXT"//
				+ ");";
		public static final String update_from1to2_SQL = "ALTER TABLE "
				+ _table_name + " ADD " + UID + " TEXT;";
	}

	private static PhodevDB instance = new PhodevDB();

	private PhodevDB() {
	}

	public static PhodevDB getInstance() {
		return instance;
	}

	private SQLiteDatabase mSQLiteDatabase;

	public SQLiteDatabase openSQLiteDatabase(Context context) {
		if (mSQLiteDatabase == null || !mSQLiteDatabase.isOpen()) {
			mSQLiteDatabase = PhodevDatabaseHelper.getInstance(context)
					.getWritableDatabase();
		}
		return mSQLiteDatabase;
	}

	@Override
	public void askCloseDatabase(SQLiteDatabase db) {
		// do not close db,we hope use global
	}

	private NoteDao noteDao;

	public NoteDao getNoteDao() {
		if (noteDao == null) {
			noteDao = new NoteDao(this);
		}
		return noteDao;
	}
}
