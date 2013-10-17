package com.phodev.andtools.drag;

import android.view.ViewGroup;

/**
 * Cell中View的适配器
 * 
 * @author skg
 * 
 */
public interface CellViewFactory {

	public CellLayout readerCell(CellModel cell, ViewGroup parent);

	public boolean recycleCellView(CellLayout view);

	public boolean clearAllCache();
}
