package com.phodev.andtools.drag.support;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import com.phodev.andtools.drag.CellLayout;
import com.phodev.andtools.drag.CellModel;
import com.phodev.andtools.drag.CellViewFactory;

public class CellViewFactoryImpl extends CellViewFactory {

	@Override
	protected CellLayout getCellLayout(CellModel cellModel,
			CellLayout convertView, ViewGroup parent) {
		Context context = parent.getContext();
		if (convertView == null) {
			convertView = new CellLayout(context);
		}
		TextView tc = new TextView(context);
		tc.setText("id:" + cellModel.i);
		tc.setTextColor(0xFFFF0000);
		tc.setBackgroundColor((Integer) cellModel.getData());
		convertView.setContentView(tc);
		return convertView;
	}

}
