package com.phodev.andtools.drag;

import com.phodev.andtools.drag.DragLayer.DraggingObject;

/**
 * 可以Drop的接口
 * 
 * @author skg
 * 
 */
public interface DropTarget {

	public void onStartCheckDrop(DraggingObject obj);

	public void onCheckDrop(DraggingObject obj, int x, int y);

	public void onDropFinish(DraggingObject obj);

}
