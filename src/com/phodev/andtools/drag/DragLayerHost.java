package com.phodev.andtools.drag;

/**
 * 规定容纳DragLayer的接口
 * 
 * @author skg
 * 
 */
public interface DragLayerHost {
	public void attach(DragLayer layer);

	public void unattach(DragLayer layer);

	public PageContainer getPageContainer();
	//
	// public void setOnPageChangedListener(OnPageChangedListener l);
	//
	// public interface OnPageChangedListener {
	// public void onPageChanged(int newPageIndex, View pageView);
	// }

	// public interface DragListener {
	//
	// public void onDragStarted(DraggingObject dragObject);
	//
	// public void onEndDrag(DraggingObject dragObject);
	// }
}
