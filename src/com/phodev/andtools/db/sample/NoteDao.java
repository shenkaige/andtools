package com.phodev.andtools.db.sample;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.phodev.andtools.common.CommonParam;
import com.phodev.andtools.db.BaseDao;
import com.phodev.andtools.db.DataSource;
import com.phodev.andtools.db.BaseDao.QueryBuider;
import com.phodev.andtools.db.sample.PhodevDB.Table_Note;

public class NoteDao extends BaseDao<Note> {

	public NoteDao(DataSource db) {
		super(db, Table_Note._table_name);
	}

	@Override
	protected void setValues(Note data, ContentValues cv) {
		cv.put(Table_Note.ID, data.getId());
		cv.put(Table_Note.GRAMMAR_ID, data.getGrammarId());
		cv.put(Table_Note.TYPE, data.getType());
		cv.put(Table_Note.CONTENT, data.getContent());
		cv.put(Table_Note.VERSION, data.getVersion());
		cv.put(Table_Note.LAST_OPERATE, data.getLastOperate());
		cv.put(Table_Note.NOTE_CREATE_TIME, data.getNoteCreateTime());
		cv.put(Table_Note.LOCAL_STATE, data.getLocalState());
		cv.put(Table_Note.UID, data.getOwnerUid());
	}

	@Override
	protected String makeDeleteWhere(Note data) {
		if (data == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(Table_Note.NOTE_CREATE_TIME);
		sb.append("=");
		sb.append(data.getNoteCreateTime());
		return sb.toString();
	}

	@Override
	protected String makeUpdateWhere(Note data) {
		return makeDeleteWhere(data);
	}

	public static final int index_ID = 0;
	public static final int index_GRAMMAR_ID = 1;
	public static final int index_TYPE = 2;
	public static final int index_CONTENT = 3;
	public static final int index_VERSION = 4;
	public static final int index_LAST_OPERATE = 5;
	public static final int index_NOTE_CREATE_TIME = 6;
	public static final int index_LOCAL_STATE = 7;
	public static final int index_UID = 8;

	@Override
	protected Note readFullColumn(Cursor c) {
		Note note = new Note();
		note.setId(c.getInt(index_ID));
		note.setGrammarId(c.getInt(index_GRAMMAR_ID));
		note.setType(c.getInt(index_TYPE));
		note.setContent(c.getString(index_CONTENT));
		note.setVersion(c.getInt(index_VERSION));
		note.setLastOperate(c.getString(index_LAST_OPERATE));
		note.setNoteCreateTime(c.getLong(index_NOTE_CREATE_TIME));
		note.setLocalState(c.getInt(index_LOCAL_STATE));
		note.setOwnerUid(c.getString(index_UID));
		return note;
	}

	@Override
	protected void onAfterDelete(SQLiteDatabase db, Note d) {
		if (d == null) {
			return;
		}
		// select count(_grammar_id) as c from notes where _grammar_id=1
		// StringBuilder sb = new StringBuilder();
		// sb.append("select count(");
		// sb.append(Table_Note.GRAMMAR_ID);
		// sb.append(") ");
		// sb.append("as c from ");
		// sb.append(Table_Note._table_name);
		// sb.append(" where ");
		// sb.append(Table_Note.GRAMMAR_ID);
		// sb.append("=");
		// sb.append(d.getGrammarId());
		// sb.append(" and ");
		// sb.append(Table_Note.LOCAL_STATE);
		// sb.append("!=");
		// sb.append(Note.LOCAL_STATE_DELETE);
		//
		// String sql = sb.toString();
		// if (CommonParam.DEBUG) {
		// reportSQL(sql, "检查笔记关联的StudyPlan数据");
		// }
		// Cursor c = db.rawQuery(sql, null);
		// if (!c.moveToFirst() || c.getInt(0) <= 0) {
		// // 需要删除StudyPlan
		// StringBuilder tempSb = new StringBuilder();
		// tempSb.append(Table_StudyPlan.GRAMMAR_ID);
		// tempSb.append("=");
		// tempSb.append(d.getGrammarId());
		// db.delete(Table_StudyPlan._table_name, tempSb.toString(), null);
		// if (CommonParam.DEBUG) {
		// log("发现删除的Note是最后一个，所以把关联的StudyPlan也删除");
		// }
		// }
		// safeClose(c);
		// askCloseDatabase(db);
	}

	@Override
	protected void onAfterPerInsert(SQLiteDatabase db, Note d) {
		super.onAfterPerInsert(db, d);
		if (d.getLocalState() == Note.LOCAL_STATE_DELETE) {
			onAfterDelete(db, d);
		}
	}

	public void query(Context context, String uid, boolean includeEmptyUID,
			int grammarId, List<Note> out, int localState,
			boolean ignoreLoclaState) {
		query(context, new QueryByGrammarId(uid, includeEmptyUID, grammarId,
				true, localState, ignoreLoclaState), out);
	}

	public void queryNeedSyncNote(Context context, List<Note> out, String uid,
			boolean includeEmptyUID) {
		query(context, new QueryByGrammarId(uid, includeEmptyUID, 0, false,
				Note.LOCAL_STATE_NORMAL, true), out);
	}

	public class QueryByGrammarId implements QueryBuider<Note> {
		private final int grammarId;
		private final int localSate;
		private final boolean usreGrammarId;
		private final boolean ignoreLocalState;
		private final String uid;
		private final boolean includeNullUidData;

		public QueryByGrammarId(String uid, boolean includeNullUidData,
				int grammarId, boolean userGrammarId, int localState,
				boolean ignoreLocalState) {
			this.uid = uid;
			this.includeNullUidData = includeNullUidData;
			this.grammarId = grammarId;
			this.usreGrammarId = userGrammarId;
			this.localSate = localState;
			this.ignoreLocalState = ignoreLocalState;
		}

		@Override
		public String getQuerySQL() {
			StringBuilder sb = new StringBuilder();
			sb.append("select * from ");
			sb.append(Table_Note._table_name);
			sb.append(" where ");
			if (usreGrammarId) {
				sb.append(Table_Note.GRAMMAR_ID);
				sb.append("=");
				sb.append(grammarId);
				sb.append(" and ");
			}
			sb.append(Table_Note.LOCAL_STATE);
			if (ignoreLocalState) {
				sb.append("!=");
			} else {
				sb.append("=");
			}
			sb.append(localSate);
			//
			// (s._uid=4 or s._uid isNull or length(_uid)<=0 )
			// 添加UID过滤
			sb.append(" and (");
			if (uid == null) {
				sb.append(Table_Note.UID);
				sb.append(uid);
				sb.append(" isNull ");
				if (includeNullUidData) {
					sb.append(" or length(");
					sb.append(Table_Note.UID);
					sb.append(")<=0 ");
				}
			} else {
				sb.append(Table_Note.UID);
				sb.append("=");
				sb.append(uid);
				if (includeNullUidData) {
					sb.append(" or ");
					sb.append(Table_Note.UID);
					sb.append(" isNull or length(");
					sb.append(Table_Note.UID);
					sb.append(")<=0 ");
				}
			}
			sb.append(")");
			//
			sb.append(" order by ");
			sb.append(Table_Note.NOTE_CREATE_TIME);
			sb.append(" DESC");
			String sql = sb.toString();
			if (CommonParam.DEBUG) {
				reportSQL(sql, "Query note ByGrammarId:" + grammarId);
			}
			return sql;
		}

		@Override
		public Note readFullColumn(Cursor c) {
			return NoteDao.this.readFullColumn(c);
		}

	}

	//

	public int updateNoteLocalStateByGrammarId(Context context, int grammarId,
			int noteLocalState) {
		SQLiteDatabase db = openSQLiteDatabase(context);
		if (!isDatabaseValid(db)) {
			return 0;
		}
		ContentValues cv = new ContentValues();
		cv.put(Table_Note.LOCAL_STATE, noteLocalState);
		StringBuilder sb = new StringBuilder();
		sb.append(Table_Note.GRAMMAR_ID);
		sb.append("=");
		sb.append(grammarId);
		String whereClaus = sb.toString();
		if (CommonParam.DEBUG) {
			reportSQL(whereClaus, "updateNoteLocalStateByGrammarId");
		}
		int affectCount = db.update(Table_Note._table_name, cv, whereClaus,
				null);
		askCloseDatabase(db);
		return affectCount;
	}
}
