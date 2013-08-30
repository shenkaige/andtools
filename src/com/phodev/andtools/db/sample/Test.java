package com.phodev.andtools.db.sample;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class Test {

	public void test(Context context) {
		final int grammarId = 10;
		final String uid = "test_user_id";
		NoteDao dao = PhodevDB.getInstance().getNoteDao();
		Note note = new Note();
		note.setContent("test content");
		note.setOwnerUid(uid);
		note.setGrammarId(grammarId);
		note.setLocalState(Note.LOCAL_STATE_NORMAL);
		note.setType(Note.CONTENT_TYPE_TEXT);
		//
		dao.addIfNotExist(context, note);
		//
		List<Note> out = new ArrayList<Note>();
		dao.getAll(context, out);
		//
	}
}
