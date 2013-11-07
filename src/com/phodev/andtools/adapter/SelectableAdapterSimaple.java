package com.phodev.andtools.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

public class SelectableAdapterSimaple extends
		SelectableAdapter<Integer, Object> {

	@Override
	public int getCount() {
		// return super.getCount();
		return 20;// test
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CheckBox cb = null;
		if (convertView instanceof CheckBox) {
			cb = (CheckBox) convertView;
		} else {
			cb = new CheckBox(parent.getContext());
		}
		cb.setChecked(isCheckedData(getData(position)));
		return cb;
	}

	@Override
	protected Integer getItemCheckRecordKey(Object t) {
		return t.hashCode();
	}

}
