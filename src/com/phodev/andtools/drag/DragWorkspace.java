package com.phodev.andtools.drag;

import java.util.ArrayList;
import java.util.List;

import com.phodev.andtools.drag.support.PageContainerImpl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 容纳PageHost,DragLayer
 * 
 * @author skg
 * 
 */
public class DragWorkspace extends FrameLayout implements DragLayerHost {
	private PageContainerImpl mPageContainer;
	private DragLayer mDragLayer;
	private GestureDetector mGestureDetector;

	public DragWorkspace(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DragWorkspace(Context context) {
		super(context);
		init();
	}

	private void init() {
		mPageContainer = new PageContainerImpl(getContext());
		addView(mPageContainer);
		mDragLayer = new DragLayer(getContext(), this);
		mGestureDetector = new GestureDetector(getContext(), mGestureListener);
	}

	@Override
	public void attach(DragLayer layer) {
		changeDragStatus(CellModel.CELL_STATUS_EDIT);
		ViewGroup.LayoutParams lp = mDragLayer.getLayoutParams();
		if (lp == null) {
			lp = generateDefaultLayoutParams();
			mDragLayer.setLayoutParams(lp);
			lp.width = LayoutParams.MATCH_PARENT;
			lp.height = LayoutParams.MATCH_PARENT;
		}
		if (mDragLayer.getParent() == this) {
			return;
		}
		addView(mDragLayer);
	}

	@Override
	public void unattach(DragLayer layer) {
		removeView(mDragLayer);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean status = mGestureDetector.onTouchEvent(ev);
		if (mDragLayer.isDragging()) {
			status |= mDragLayer.dispatchTouchEvent(ev);
		}
		if (status) {
			return status;
		}
		return super.dispatchTouchEvent(ev);
	}

	private SimpleOnGestureListener mGestureListener = new SimpleOnGestureListener() {

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (DragConfig.CELL_STATUS == CellModel.CELL_STATUS_EDIT) {
				changeDragStatus(CellModel.CELL_STATUS_NORMAL);
				invalidate();
				requestLayout();
				return true;
			}
			return super.onDoubleTap(e);
		}

	};

	@Override
	public PageContainer getPageContainer() {
		return mPageContainer;
	}

	public DragLayer getDragLayer() {
		return mDragLayer;
	}

	/**
	 * 是否是在编辑状态
	 * 
	 * @return
	 */
	public boolean isEditting() {
		return DragConfig.CELL_STATUS == CellModel.CELL_STATUS_EDIT;
	}

	public enum DragStatus {
		editting, finished;
	}

	public DragStatus getDragStatus() {
		if (DragConfig.CELL_STATUS == CellModel.CELL_STATUS_EDIT) {
			return DragStatus.editting;
		} else {
			return DragStatus.finished;
		}
	}

	private void changeDragStatus(int status) {
		if (DragConfig.CELL_STATUS == status) {
			return;
		}
		if (status == CellModel.CELL_STATUS_EDIT) {
			DragConfig.CELL_STATUS = status;
			notifyDragStatusChanged(DragStatus.editting);
		} else if (status == CellModel.CELL_STATUS_NORMAL) {
			DragConfig.CELL_STATUS = status;
			notifyDragStatusChanged(DragStatus.finished);
		}
	}

	public interface DragStatusObserver {
		public void onDragStatusChange(DragStatus status);
	}

	private List<DragStatusObserver> dragStatusObserver = new ArrayList<DragStatusObserver>();

	public void registerDragStatusObserver(DragStatusObserver observer) {
		if (observer != null && !dragStatusObserver.contains(observer)) {
			dragStatusObserver.add(observer);
		}
	}

	public void unregisterDragStatusObserver(DragStatusObserver observer) {
		if (observer != null) {
			dragStatusObserver.remove(observer);
		}
	}

	private void notifyDragStatusChanged(DragStatus status) {
		for (DragStatusObserver dso : dragStatusObserver) {
			dso.onDragStatusChange(status);
		}
	}

}
