package com.phodev.andtools.drag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

/**
 * Cell中View的适配器
 * 
 * @author skg
 * 
 */
public abstract class CellViewFactory {
	public static final String TAG = "CellViewFactory";
	private SparseArray<List<CellLayout>> cellsCache = new SparseArray<List<CellLayout>>();
	// 正在服役的Cell
	private Map<CellModel, CellLayout> serviceCellMapping = new HashMap<CellModel, CellLayout>();

	public final CellLayout readerCell(CellModel cell, ViewGroup parent) {
		if (cell == null) {
			return null;
		}
		CellLayout cellLayout = obtainCellLayout(cell.getType());
		//
		cellLayout = getCellLayout(cell, cellLayout, parent);
		serviceCellMapping.put(cell, cellLayout);
		return cellLayout;
	}

	public final boolean recycleCellView(CellLayout cellLayout) {
		if (cellLayout == null) {
			return true;
		}
		CellModel cell = cellLayout.getCellModel();
		serviceCellMapping.remove(cell);
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

	public final boolean clearAllCache() {
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

	public final CellLayout findCellLayout(CellModel cell) {
		return serviceCellMapping.get(cell);
	}

	/**
	 * 删除对该Cell对应的CellLayout的管理
	 * 
	 * @param cell
	 */
	public final CellLayout removeCell(CellModel cell) {
		return serviceCellMapping.remove(cell);
	}

	/**
	 * 获取CellLayout
	 * 
	 * @param cellModel
	 * @param convertView
	 * @return
	 */
	protected abstract CellLayout getCellLayout(CellModel cellModel,
			CellLayout convertView, ViewGroup parent);
}
