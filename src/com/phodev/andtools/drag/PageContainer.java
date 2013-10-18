package com.phodev.andtools.drag;

import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;

/**
 * 指定一套管理页面的接口
 * 
 * @author skg
 * 
 */
public interface PageContainer {
	public int getCurrentPageIndex();

	public View getCurrentPage();

	public void setAutoScroll(boolean autoScroll);

	public boolean isAutoScrollEnable();

	public void nextPage();

	public void previousPage();

	public boolean haveNextPage();

	public boolean havePreviousPage();

	public void setOnPageStatusListener(OnPageStatusListener listener);

	public void refreshCellState(int cellStatus);

	public interface OnPageStatusListener extends OnPageChangeListener {

	}

}
