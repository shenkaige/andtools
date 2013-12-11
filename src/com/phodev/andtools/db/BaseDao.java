package com.phodev.andtools.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.phodev.andtools.common.CommonParam;

/**
 * 控制数据的基本操作
 * 
 * @author skg
 */
public abstract class BaseDao<T> {
	private final String TAG;
	private DataSource dataSource;
	private final String Table_Name;

	public BaseDao(DataSource dataSource, String tableName) {
		TAG = getClass().getCanonicalName();
		this.dataSource = dataSource;
		Table_Name = tableName;
	}

	/**
	 * 如果约束的主键已经了就不添加
	 * 
	 * @param context
	 * @param data
	 */
	public void addIfNotExist(Context context, T data) {
		if (data == null) {
			if (CommonParam.DEBUG) {
				log("addOrUpdate--data is null");
			}
			return;
		}
		SQLiteDatabase db = openSQLiteDatabase(context);
		if (!isDatabaseValid(db)) {
			if (CommonParam.DEBUG) {
				log("addOrUpdate--isDatabaseValid=false");
			}
			return;
		}
		ContentValues cv = new ContentValues();
		setValues(data, cv);
		try {
			db.insertOrThrow(Table_Name, null, cv);
			onAfterPerInsert(db, data);
		} catch (SQLException e) {
			// e.printStackTrace();//ignore
		}
		askCloseDatabase(db);
	}

	/**
	 * 添加或更新一条记录
	 * 
	 * @param context
	 * @param data
	 */
	public void addOrUpdate(Context context, T data) {
		if (data == null) {
			if (CommonParam.DEBUG) {
				log("addOrUpdate--data is null");
			}
			return;
		}
		SQLiteDatabase db = openSQLiteDatabase(context);
		if (!isDatabaseValid(db)) {
			if (CommonParam.DEBUG) {
				log("addOrUpdate--isDatabaseValid=false");
			}
			return;
		}
		innserAddOrUpdate(db, data, null);
		askCloseDatabase(db);
	}

	private void innserAddOrUpdate(SQLiteDatabase db, T data,
			ContentValues reuseCV) {
		ContentValues cv = reuseCV;
		if (cv == null) {
			cv = new ContentValues();
		} else {
			cv.clear();
		}
		setValues(data, cv);
		try {
			db.insertOrThrow(Table_Name, null, cv);
		} catch (SQLiteConstraintException e) {
			String whereClause = makeUpdateWhere(data);
			db.update(Table_Name, cv, whereClause, null);
			if (CommonParam.DEBUG) {
				log("try insert the exist row,so we update the row only");
			}
		}
		onAfterPerInsert(db, data);
		cv.clear();
	}

	/**
	 * 添加或者更新
	 * 
	 * @param context
	 * @param dataList
	 */
	public void addOrUpdate(Context context, List<T> dataList) {
		if (isEmptyList(dataList)) {
			if (CommonParam.DEBUG) {
				log("addOrUpdate--empty data list");
			}
			return;
		}
		SQLiteDatabase db = openSQLiteDatabase(context);
		if (!isDatabaseValid(db)) {
			if (CommonParam.DEBUG) {
				log("addOrUpdate--isDatabaseValid=false");
			}
			return;
		}
		try {
			db.beginTransaction();
			//
			ContentValues cv = new ContentValues();
			for (T d : dataList) {
				if (d == null) {
					continue;
				}
				innserAddOrUpdate(db, d, cv);
			}
			//
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		askCloseDatabase(db);
		if (CommonParam.DEBUG) {
			log("addOrUpdate succes, data list size:" + dataList.size());
		}
	}

	/**
	 * 删除记录
	 * 
	 * @param context
	 * @param dataList
	 */
	public void delete(Context context, List<T> dataList) {
		if (isEmptyList(dataList)) {
			if (CommonParam.DEBUG) {
				log("delete--empty data list");
			}
			return;
		}
		SQLiteDatabase db = openSQLiteDatabase(context);
		if (!isDatabaseValid(db)) {
			if (CommonParam.DEBUG) {
				log("delete--isDatabaseValid=false");
			}
			return;
		}
		try {
			db.beginTransaction();
			for (T d : dataList) {
				if (d != null) {
					db.delete(Table_Name, makeDeleteWhere(d), null);
					onAfterDelete(db, d);
				}
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		askCloseDatabase(db);
		if (CommonParam.DEBUG) {
			log("delete succes, data list size:" + dataList.size());
		}
	}

	public void delete(Context context, T data) {
		if (data == null) {
			if (CommonParam.DEBUG) {
				log("delete--empty data:" + data);
			}
			return;
		}
		SQLiteDatabase db = openSQLiteDatabase(context);
		if (!isDatabaseValid(db)) {
			if (CommonParam.DEBUG) {
				log("delete--isDatabaseValid=false");
			}
			return;
		}
		db.delete(Table_Name, makeDeleteWhere(data), null);
		onAfterDelete(db, data);
		askCloseDatabase(db);
		if (CommonParam.DEBUG) {
			log("delete succes");
		}
	}

	/**
	 * 获取所有
	 * 
	 * @param context
	 * @param out
	 */
	public void getAll(Context context, List<T> out) {
		if (out == null) {
			if (CommonParam.DEBUG) {
				log("getAll--out list is null,so finish query");
			}
			return;
		}
		SQLiteDatabase db = openSQLiteDatabase(context);
		if (!isDatabaseValid(db)) {
			if (CommonParam.DEBUG) {
				log("getAll--isDatabaseValid=false");
			}
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select * from ");
		sb.append(Table_Name);
		String sql = sb.toString();
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst()) {
			do {
				T d = readFullColumn(c);
				if (d != null) {
					onAfterPerQuery(db, d);
					out.add(d);
				}
			} while (c.moveToNext());
		}
		safeClose(c);
		askCloseDatabase(db);
		if (CommonParam.DEBUG) {
			log("getAll succes, data list size:" + out.size());
		}
	}

	public T queryFirstMatch(Context context, QueryBuider<T> queryBuider) {
		if (queryBuider == null) {
			if (CommonParam.DEBUG) {
				log("queryFirstMatch--queryBuider is null,so finish query");
			}
		}
		SQLiteDatabase db = openSQLiteDatabase(context);
		if (!isDatabaseValid(db)) {
			return null;
		}
		String sql = queryBuider.getQuerySQL();
		Cursor c = db.rawQuery(sql, null);
		T d = null;
		if (c.moveToFirst()) {
			d = queryBuider.readFullColumn(c);
			onAfterPerQuery(db, d);
		}
		safeClose(c);
		askCloseDatabase(db);
		if (CommonParam.DEBUG) {
			log("query succes data:" + d);
		}
		return d;
	}

	/**
	 * 查询
	 * 
	 * @param context
	 * @param queryBuider
	 * @param out
	 */
	public void query(Context context, QueryBuider<T> queryBuider, List<T> out) {
		if (out == null) {
			if (CommonParam.DEBUG) {
				log("query--out list is null,so finish query");
			}
			return;
		}
		if (queryBuider == null) {
			if (CommonParam.DEBUG) {
				log("query--queryBuider is null,so finish query");
			}
		}
		SQLiteDatabase db = openSQLiteDatabase(context);
		if (!isDatabaseValid(db)) {
			return;
		}
		String sql = queryBuider.getQuerySQL();
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst()) {
			do {
				T d = queryBuider.readFullColumn(c);
				if (d != null) {
					out.add(d);
					onAfterPerQuery(db, d);
				}
			} while (c.moveToNext());
		}
		safeClose(c);
		askCloseDatabase(db);
		if (CommonParam.DEBUG) {
			log("query succes, data list size:" + out.size());
		}
	}

	protected abstract void setValues(T data, ContentValues cv);

	protected abstract String makeDeleteWhere(T data);

	protected abstract String makeUpdateWhere(T data);

	protected abstract T readFullColumn(Cursor c);

	protected void onAfterPerInsert(SQLiteDatabase db, T d) {

	}

	/**
	 * 当查询到结果后
	 * 
	 * @param db
	 * @param d
	 */
	protected void onAfterPerQuery(SQLiteDatabase db, T d) {

	}

	protected void onAfterDelete(SQLiteDatabase db, T d) {

	}

	public interface QueryBuider<T> {

		public String getQuerySQL();

		public T readFullColumn(Cursor c);
	}

	// --------------------------------------------------------------------------
	protected final SQLiteDatabase openSQLiteDatabase(Context context) {
		return dataSource.openSQLiteDatabase(context);
	}

	protected void askCloseDatabase(SQLiteDatabase db) {
		dataSource.askCloseDatabase(db);
	}

	protected void safeClose(Cursor c) {
		if (c != null && !c.isClosed()) {
			c.close();
		}
	}

	protected boolean isDatabaseValid(SQLiteDatabase db) {
		return db != null && db.isOpen();
	}

	protected String reportSQL(String sql, String actionTag) {
		return reportSQL(sql, actionTag, null);
	}

	public boolean isEmptyList(List<?> list) {
		return list == null || list.isEmpty();
	}

	public boolean isClosed() {
		// 暂时不考虑close
		return false;
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

	protected void log(String msg) {
		Log.d(TAG, "-->" + msg);
	}
}
