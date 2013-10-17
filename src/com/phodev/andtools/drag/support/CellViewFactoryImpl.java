package com.phodev.andtools.drag.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.TextView;

import com.phodev.andtools.drag.CellLayout;
import com.phodev.andtools.drag.CellModel;
import com.phodev.andtools.drag.CellViewFactory;
import com.phodev.andtools.drag.DragConfig;

public class CellViewFactoryImpl implements CellViewFactory {
	public static final String TAG = "CellViewFactoryImpl";
	private SparseArray<List<CellLayout>> cellsCache = new SparseArray<List<CellLayout>>();
	Random random = new Random();

	@Override
	public CellLayout readerCell(CellModel cell, ViewGroup parent) {
		if (cell == null) {
			return null;
		}
		Context context = parent.getContext();
		//
		// Source source = (Source) cell.getData();
		//
		CellLayout cellLayout = obtainCellLayout(cell.getType());
		//
		if (cellLayout == null) {
			cellLayout = new CellLayout(context);
		}
		cellLayout.removeAllViews();
		TextView tc = new TextView(context);
		tc.setText("id:" + cell.hashCode());
		tc.setTextColor(0xFFFF0000);
		tc.setBackgroundColor((Integer) cell.getData());
		cellLayout.setContentView(tc);
		return cellLayout;
	}

	@Override
	public boolean recycleCellView(CellLayout cellLayout) {
		if (cellLayout == null) {
			return true;
		}
		CellModel cell = cellLayout.getCellModel();
		//
		if (cellLayout == null || cellLayout.getParent() != null) {
			if (DragConfig.DEBUG) {
				Log.d(TAG, "recycleCell failed:"
						+ cellLayout
						+ " haveParent:"
						+ (cellLayout == null ? false
								: cellLayout.getParent() != null));
			}
			return true;
		}
		int type = cell.getType();
		List<CellLayout> cellList = cellsCache.get(type);
		if (cellList == null) {
			cellList = new ArrayList<CellLayout>();
			cellsCache.put(type, cellList);
		}
		cellList.add(cellLayout);
		return true;
	}

	@Override
	public boolean clearAllCache() {
		int size = cellsCache.size();
		for (int i = 0; i < size; i++) {
			cellsCache.get(cellsCache.keyAt(i)).clear();
		}
		cellsCache.clear();
		return true;
	}

	private CellLayout obtainCellLayout(int cellType) {
		List<CellLayout> cellList = cellsCache.get(cellType);
		if (cellList != null && cellList.size() > 0) {
			return cellList.remove(0);
		}
		return null;
	}

}
