package com.phodev.andtools.download.impl;

import java.util.List;
import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.phodev.andtools.download.Constants;
import com.phodev.andtools.download.DownloadFile;
import com.phodev.andtools.download.impl.DownloadDatabase.TABLE_DownloadBlock;
import com.phodev.andtools.download.impl.DownloadDatabase.TABLE_DownloadFile;

/**
 * 下载记录管理器
 * 
 * @author skg
 */
public class DownloadRecorder {
	public static final String TAG = "DownloadRecorder";
	private static DownloadRecorder instance = new DownloadRecorder();

	private DownloadRecorder() {
	}

	public static DownloadRecorder getInstance() {
		return instance;
	}

	/**
	 * 提那几Block记录
	 * 
	 * <pre>
	 * 1,我们认为你添加的Block数据库中都是不存在的，如果存在，我们将会把Block的对应的一组block全部删除
	 * ２,Block被添加后我们会设置好对应的id,该id可以用来检索到唯一对应的block
	 * 3,虽然blocks中带有SourceURL但是我们以传递竟来的sourceURL为准，因为这是同一组block
	 * </pre>
	 * 
	 * @param context
	 * @param blockGroup
	 * @param sourceUrl
	 */
	public void addBlocks(Context context, List<DownloadBlock> blockGroup,
			String sourceUrl) {
		if (blockGroup == null || blockGroup.isEmpty()) {
			return;
		}
		SQLiteDatabase db = openDatabase(context);
		if (db == null) {
			return;
		}
		db.beginTransaction();
		checkDeleteExistBlock(db, sourceUrl);
		ContentValues cv = new ContentValues();
		for (DownloadBlock b : blockGroup) {
			String uuid = UUID.randomUUID().toString();
			b.setId(uuid);

			copy(cv, b);
			db.insert(TABLE_DownloadBlock._table_name, null, cv);

			cv.clear();
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		askCloseDatabase(db);
	}

	/**
	 * 删除SourceURL对应的所有Block
	 * 
	 * @param context
	 * @param sourceUrl
	 */
	public void removeBlocks(Context context, String sourceUrl) {
		SQLiteDatabase db = openDatabase(context);
		if (db == null) {
			return;
		}
		checkDeleteExistBlock(db, sourceUrl);
		askCloseDatabase(db);
	}

	/**
	 * 删除SourceURL对应的所有Block
	 * 
	 * @param sourceUrl
	 */
	private void checkDeleteExistBlock(SQLiteDatabase db, String sourceUrl) {
		if (db != null && sourceUrl != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("delete from ");
			sb.append(TABLE_DownloadBlock._table_name);
			sb.append(" where ");
			sb.append(TABLE_DownloadBlock.file_source_url);
			sb.append(" = ");
			sb.append('\'');
			sb.append(sourceUrl);
			sb.append('\'');
			String sql = sb.toString();
			if (Constants.DEBUG) {
				reportSQL(sql, "checkDeleteExistBlock");
			}
			db.execSQL(sql);
		}
	}

	/**
	 * 根据Block的id更新信息
	 * 
	 * @param context
	 * @param blocks
	 */
	public void updateBlockProgress(Context context, List<DownloadBlock> blocks) {
		if (blocks == null || blocks.isEmpty()) {
			return;
		}
		SQLiteDatabase db = openDatabase(context);
		if (db == null) {
			return;
		}
		db.beginTransaction();
		ContentValues cv = new ContentValues();
		String whereClause = TABLE_DownloadBlock._ID + "=?";
		String[] args = new String[1];
		for (DownloadBlock b : blocks) {
			args[0] = b.getId();
			copy(cv, b);
			db.update(TABLE_DownloadBlock._table_name, cv, whereClause, args);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		askCloseDatabase(db);
	}

	public void updateBlockProgress(Context context, DownloadBlock block) {
		if (block == null) {
			return;
		}
		SQLiteDatabase db = openDatabase(context);
		if (db == null) {
			return;
		}
		ContentValues cv = new ContentValues();
		String whereClause = TABLE_DownloadBlock._ID + "=?";
		String[] args = new String[1];
		args[0] = block.getId();
		copy(cv, block);
		db.update(TABLE_DownloadBlock._table_name, cv, whereClause, args);
		askCloseDatabase(db);
	}

	public void getBocks(Context context, List<DownloadBlock> out,
			DownloadFile blockDownloadFile) {
		if (out == null || blockDownloadFile == null) {
			return;
		}
		String sourceUrl = blockDownloadFile.getSourceUrl();
		if (sourceUrl == null || TextUtils.isEmpty(sourceUrl)) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select * from ");
		sb.append(TABLE_DownloadBlock._table_name);
		sb.append(" where ");
		sb.append(TABLE_DownloadBlock.file_source_url);
		sb.append(" = ");
		sb.append('\'');
		sb.append(sourceUrl);
		sb.append('\'');
		String sql = sb.toString();
		if (Constants.DEBUG) {
			reportSQL(sql, "getBocks");
		}
		SQLiteDatabase db = openDatabase(context);
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst()) {
			int index_id = c.getColumnIndex(TABLE_DownloadBlock._ID);
			int index_start = c.getColumnIndex(TABLE_DownloadBlock.block_start);
			int index_end = c.getColumnIndex(TABLE_DownloadBlock.block_end);
			int index_loadSize = c
					.getColumnIndex(TABLE_DownloadBlock.block_loaded_size);
			do {
				DownloadBlock b = new DownloadBlock(//
						blockDownloadFile,//
						sourceUrl,//
						c.getLong(index_start),//
						c.getLong(index_end),//
						c.getLong(index_loadSize));
				b.setId(c.getString(index_id));
				out.add(b);
			} while (c.moveToNext());
		}
		safeClose(c);
		askCloseDatabase(db);
	}

	//
	public void addFile(Context context, DownloadFile file) {
		if (file == null) {
			return;
		}
		ContentValues cv = new ContentValues();
		copy(cv, file);
		cv.remove(TABLE_DownloadFile._ID);
		SQLiteDatabase db = openDatabase(context);
		db.beginTransaction();
		//
		checkDeleteFile(db, file.getSourceUrl());
		db.insert(TABLE_DownloadFile._table_name, null, cv);
		//
		db.setTransactionSuccessful();
		db.endTransaction();
		askCloseDatabase(db);
	}

	public void removeFile(Context context, String sourceUrl) {
		if (sourceUrl == null) {
			return;
		}
		SQLiteDatabase db = openDatabase(context);
		checkDeleteFile(db, sourceUrl);
		askCloseDatabase(db);
	}

	private void checkDeleteFile(SQLiteDatabase db, String sourceUrl) {
		String whereClause = TABLE_DownloadFile.file_source_url + "='"
				+ sourceUrl + "'";
		db.delete(TABLE_DownloadFile._table_name, whereClause, null);
	}

	public void updateFile(Context context, DownloadFile file) {
		if (file == null) {
			return;
		}
		SQLiteDatabase db = openDatabase(context);
		if (db == null) {
			return;
		}
		//
		String whereClause = TABLE_DownloadFile.file_source_url + "='"
				+ file.getSourceUrl() + "'";
		ContentValues cv = new ContentValues();
		copy(cv, file);
		db.update(TABLE_DownloadFile._table_name, cv, whereClause, null);
		askCloseDatabase(db);
	}

	public DownloadFile getFile(Context context, String sourceUrl) {
		if (sourceUrl == null || TextUtils.isEmpty(sourceUrl)) {
			return null;
		}
		SQLiteDatabase db = openDatabase(context);
		if (db == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select * from ");
		sb.append(TABLE_DownloadFile._table_name);
		sb.append(" where ");
		sb.append(TABLE_DownloadFile.file_source_url);
		sb.append("=");
		sb.append('\'');
		sb.append(sourceUrl);
		sb.append('\'');
		sb.append(" limit 1");
		String sql = sb.toString();
		if (Constants.DEBUG) {
			reportSQL(sql, "getFile");
		}
		DownloadFile file = null;
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst()) {
			file = new DownloadFile();
			//
			int index = c.getColumnIndex(TABLE_DownloadFile.file_source_url);
			file.setSourceUrl(c.getString(index));
			//
			index = c.getColumnIndex(TABLE_DownloadFile.file_name);
			file.setFileName(c.getString(index));
			//
			index = c.getColumnIndex(TABLE_DownloadFile.file_size);
			file.setFileSize(c.getLong(index));
			//
			index = c.getColumnIndex(TABLE_DownloadFile.loaded_size);
			file.setLoadedSize(c.getLong(index));
			//
			index = c.getColumnIndex(TABLE_DownloadFile.status);
			file.setStatus(c.getInt(index));
		}
		safeClose(c);
		askCloseDatabase(db);
		return file;
	}

	/**
	 * 获取列表
	 * 
	 * @param context
	 * @param out
	 * @param fileStatusFilter
	 *            只匹配指定status
	 */
	public void getFiles(Context context, List<DownloadFile> out,
			int... fileStatusFilter) {
		if (out == null) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select * from ");
		sb.append(TABLE_DownloadFile._table_name);
		if (fileStatusFilter != null && fileStatusFilter.length > 0) {
			sb.append(" where ");
			int len = fileStatusFilter.length;
			int lastOne = len - 1;
			for (int i = 0; i < len; i++) {
				sb.append(TABLE_DownloadFile.status);
				sb.append("=");
				sb.append(fileStatusFilter[i]);
				if (i != lastOne) {
					sb.append(" or ");
				}
			}
		}
		String sql = sb.toString();
		if (Constants.DEBUG) {
			reportSQL(sql, "getFile");
		}
		//
		SQLiteDatabase db = openDatabase(context);
		if (db == null) {
			return;
		}
		Cursor c = db.rawQuery(sql, null);
		//
		if (c.moveToFirst()) {
			//
			int indexSourceUrl = c
					.getColumnIndex(TABLE_DownloadFile.file_source_url);
			int iFileName = c.getColumnIndex(TABLE_DownloadFile.file_name);
			int iFileSize = c.getColumnIndex(TABLE_DownloadFile.file_size);
			int iLoadedSize = c.getColumnIndex(TABLE_DownloadFile.loaded_size);
			int iStatus = c.getColumnIndex(TABLE_DownloadFile.status);
			do {
				DownloadFile f = new DownloadFile();
				f.setSourceUrl(c.getString(indexSourceUrl));
				f.setFileName(c.getString(iFileName));
				f.setFileSize(c.getLong(iFileSize));
				f.setLoadedSize(c.getLong(iLoadedSize));
				f.setStatus(c.getInt(iStatus));
				out.add(f);
			} while (c.moveToNext());
		}
		//
		safeClose(c);
		askCloseDatabase(db);
	}

	private void copy(ContentValues cv, DownloadBlock b) {
		cv.put(TABLE_DownloadBlock._ID, b.getId());
		cv.put(TABLE_DownloadBlock.file_source_url, b.getSourceUrl());
		cv.put(TABLE_DownloadBlock.block_start, b.getStart());
		cv.put(TABLE_DownloadBlock.block_end, b.getEnd());
		cv.put(TABLE_DownloadBlock.block_loaded_size, b.getLoadedSize());
	}

	private void copy(ContentValues cv, DownloadFile file) {
		cv.put(TABLE_DownloadFile.file_source_url, file.getSourceUrl());
		cv.put(TABLE_DownloadFile.file_name, file.getFileName());
		cv.put(TABLE_DownloadFile.file_size, file.getFileSize());
		cv.put(TABLE_DownloadFile.loaded_size, file.getLoadedSize());
		cv.put(TABLE_DownloadFile.status, file.getStatus());
	}

	private SQLiteDatabase mSQLiteDatabase;

	private SQLiteDatabase openDatabase(Context context) {
		if (mSQLiteDatabase == null || !mSQLiteDatabase.isOpen()) {
			mSQLiteDatabase = null;
			if (context != null) {
				context = context.getApplicationContext();
			}
			DownloadDatabase db = DownloadDatabase.getInstance(context);
			if (db != null) {
				mSQLiteDatabase = db.getWritableDatabase();
			}
		}
		return mSQLiteDatabase;
	}

	@Override
	protected void finalize() throws Throwable {
		if (mSQLiteDatabase != null && mSQLiteDatabase.isOpen()) {
			mSQLiteDatabase.close();
		}
	}

	protected void askCloseDatabase(SQLiteDatabase db) {
		// if (db != null && db.isOpen()) {
		// db.close();
		// }
	}

	protected void safeClose(Cursor c) {
		if (c != null && !c.isClosed()) {
			c.close();
		}
	}

	protected String reportSQL(String sql, String actionTag) {
		return reportSQL(sql, actionTag, null);
	}

	protected String reportSQL(String sql, String actionTag, String desc) {
		Log.d(TAG, ">-----------SQL-----------");
		Log.d(TAG, "Action:" + actionTag);
		Log.d(TAG, "SQL   :" + sql);
		if (desc != null) {
			Log.d(TAG, "  Desc:" + desc);
		}
		Log.d(TAG, "<--");
		return sql;
	}

	protected void log(String sql) {
		Log.d(TAG, "-->" + sql);
	}
}
