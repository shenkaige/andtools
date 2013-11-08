package com.phodev.andtools.adapter;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SelectableAdapterSimaple extends
		SelectableAdapter<Integer, String> {
	private static final int POSITION_ID = 0xf213129;

	public SelectableAdapterSimaple() {
		mData = new ArrayList<String>();
		for (int i = 0; i < 20; i++) {
			mData.add("item" + i);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CheckBox cb = null;
		if (convertView instanceof CheckBox) {
			cb = (CheckBox) convertView;
		} else {
			cb = new CheckBox(parent.getContext());
			cb.setOnCheckedChangeListener(onCheckedChangeListener);
		}
		//
		cb.setTag(POSITION_ID, position);
		cb.setChecked(isCheckedData(getData(position)));
		return cb;
	}

	private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Object obj = buttonView.getTag(POSITION_ID);
			if (obj instanceof Integer) {
				int position = (Integer) obj;
				setChecked(position, isChecked);
			}
		}
	};

	@Override
	protected Integer getItemCheckRecordKey(String t) {
		return t.hashCode();
	}

}
