package com.phodev.andtools.drag;

//TO DO:
//- improve timer performance (especially on Eee Pad)
//- improve child rearranging
import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.phodev.andtools.drag.CellLayout.CellActionListener;
import com.phodev.andtools.drag.DataLine.Locations;
import com.phodev.andtools.drag.DataLine.OnDataDeleteListener;
import com.phodev.andtools.drag.DataLine.SegmentListener;
import com.phodev.andtools.drag.DragLayer.DraggingObject;

@SuppressLint("WrongCall")
public class DraggableGridView extends ViewGroup implements DropTarget,
		OnDataDeleteListener {
	private String TAG = "DraggableGridView";
	//
	protected int colCount;
	protected int childSize;
	//
	private int borderPaddingLeft;
	private int borderPaddingTop;
	private int borderPaddingRight;
	private int borderPaddingBottom;
	//
	private int mVerticalSpace = -1;
	private int mHorizontalSpace = -1;
	//
	protected int draggedViewPos = -1;
	protected int lastX = -1;
	protected int lastY = -1;
	protected int lastTarget = -1;
	//
	private final static int animT = DragConfig.CELL_MOVE_ANIMATION_DURATION;
	private OnCellClickListener onCellClickListener;
	private DragLayer mDragLayer;
	private CellViewFactory cellViewFactory;

	public DraggableGridView(CellViewFactory cellViewFactory,
			DragLayer dragLayer, int columnsCount, int childSize) {
		super(dragLayer.getContext());
		this.cellViewFactory = cellViewFactory;
		mDragLayer = dragLayer;
		if (columnsCount > 0) {
			colCount = columnsCount;
		} else {
			colCount = DragConfig.GRID_DEFAULT_COLUMNS;
		}
		this.childSize = childSize;
		init();
	}

	private void init() {
		setChildrenDrawingOrderEnabled(true);
		// must use super to setup this listener
		super.setOnClickListener(innerOnClickListener);
		// must use super to setup this listener
		super.setOnLongClickListener(innerOnLongClickListener);
	}

	private boolean mDataIsChanged = false;
	private int mPageIndex;
	private DataLine<CellModel> mDataLine;

	public void setData(int pageIndex, DataLine<CellModel> dataLine) {
		if (mDataLine != null) {
			mDataLine.setSegmentListener(null, mPageIndex);
		}
		mPageIndex = pageIndex;
		mDataLine = dataLine;
		if (mDataLine != null) {
			mDataLine.setSegmentListener(dataSetObserver, mPageIndex);
		}
		mDataIsChanged = true;
		requestLayout();
	}

	public int getPageIndex() {
		return mPageIndex;
	}

	public int getItemCount() {
		if (mDataLine == null) {
			return 0;
		}
		return mDataLine.getSegmentLength(getPageIndex());
	}

	private SegmentListener dataSetObserver = new SegmentListener() {

		@Override
		public void onSegmentDestroy(int segmentIndex) {
			onDataSetChanged();
		}

		@Override
		public void onDataMove(int fromGlobalPos, int moveToGlobalPos) {
			if (!interceptDataSetChangeFromPosition) {
				draggedViewPos = -1;
				mDataIsChanged = true;
				onLayout(false, getLeft(), getTop(), getRight(), getBottom());
			}
			if (DragConfig.DEBUG) {
				log("onDataSetChange pageIndex:" + getPageIndex()
						+ " interceptDataSetChangeFromPosition:"
						+ interceptDataSetChangeFromPosition);
			}
		}

		@Override
		public void onDeleteAffected(int fromSegment, int delGlobalPos,
				Locations mapping) {
			if (getChildCount() <= 0) {
				// 这种情况下，我们认为，数据的删除并没有影响到自己，因为本来就没人任何view显示，那还让他继续空着好了
				return;
			}
			// 如果是被删除的Cell所在的页面，我们先不忙处理，等待，删除的Animation完毕后，在处理UI
			if (getPageIndex() != fromSegment) {
				popFirstChild();
			}
		}

		@Override
		public void onDataSetChanged() {
			mDataIsChanged = true;
			requestLayout();
		}
	};

	private void popFirstChild() {
		int curPageIndex = getPageIndex();
		// 移除第一个位置的Cell
		CellLayout child = (CellLayout) getChildAt(0);
		child.clearAnimation();
		removeViewInLayout(child);
		detachCell(child);
		// cellViewFactory.recycleCellView(child);//依然还会再次使用，不需要回收
		// 尝试补充最后空缺的位置
		CellModel c = mDataLine.getData(curPageIndex, getItemCount() - 1);
		if (c != null) {
			CellLayout celllLayout = readerNewCell(c);
			View ncv = celllLayout;
			if (ncv != null) {
				LayoutParams lp = ncv.getLayoutParams();
				if (lp == null) {
					lp = generateDefaultLayoutParams();
				}
				addViewInLayout(ncv, -1, lp);// +
				attachCell(ncv);
				if (isNeedMeasure(ncv, childSize, childSize)) {
					int spec = exactlySizeMeasureSpec(childSize);
					ncv.measure(spec, spec);
				}
			}
		}
		// TODO 优化，针对但个数据的变化，减少不需要的更新
		mDataIsChanged = true;
		requestLayout();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// int mw = r - l;
		// determine childSize
		// int offset = (colCount + 1) * padding;
		// childSize = (mw - offset) / colCount;
		//
		if (mHorizontalSpace < 0 || mVerticalSpace < 0) {
			mVerticalSpace = (r - l) / colCount;
			mHorizontalSpace = mVerticalSpace;
		}
		boolean needMeasureChild = changed || mDataIsChanged;
		// -----------------------------------------------------
		if (mDataIsChanged) {
			removeAllViews(false, false);
			int count = getItemCount();
			if (DragConfig.DEBUG) {
				log("=page item count" + count);
			}
			int pageIndex = getPageIndex();
			for (int i = 0; i < count; i++) {
				LayoutParams params = generateDefaultLayoutParams();
				CellModel cell = mDataLine.getData(pageIndex, i);
				if (DragConfig.DEBUG) {
					int ig = mDataLine.indexInGlobal(pageIndex, i);
					log("=pageIndex:" + pageIndex + " index:" + i + " gi:" + ig);
				}
				CellLayout cellLayout = readerNewCell(cell);
				addViewInLayout(cellLayout, i, params, true);
				attachCell(cellLayout);
			}
			mDataIsChanged = false;
		}
		// check measure------------------------------------
		int childCount = getChildCount();
		if (needMeasureChild) {
			int childMeasureSpec = exactlySizeMeasureSpec(childSize);
			// check measure child
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				if (isNeedMeasure(child, childSize, childSize)) {
					child.forceLayout();
					child.measure(childMeasureSpec, childMeasureSpec);
				}
			}
		}
		final Point xy = layoutPoint;
		xy.set(0, 0);
		// layout all child--------------------------------
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (newComeCellViewIndex == i) {
				xy.set(newComeCellRequestX, newComeCellRequestY);
			} else {
				xy.set(0, 0);
				getCoorFromIndex(i, xy);
			}
			child.forceLayout();
			child.layout(xy.x, xy.y, xy.x + childSize, xy.y + childSize);
		}
	}

	private Point layoutPoint = new Point();

	protected CellLayout readerNewCell(CellModel cellModel) {
		CellLayout cellLayout = cellViewFactory.readerCell(cellModel, this);
		boundCellLayoutAndModel(cellLayout, cellModel);
		return cellLayout;
	}

	private void boundCellLayoutAndModel(CellLayout cellLayout, CellModel model) {
		if (cellLayout != null) {
			cellLayout.setCellModel(model);
			cellLayout.setCellActionListener(cellActionListener);
		}
	}

	private int exactlySizeMeasureSpec(int exactlySize) {
		return MeasureSpec.makeMeasureSpec(exactlySize, MeasureSpec.EXACTLY);
	}

	private boolean isNeedMeasure(View child, int hopeWidth, int hopeHeight) {
		if (child == null) {
			return false;
		}
		if (child.getMeasuredWidth() != hopeWidth
				|| child.getMeasuredHeight() != hopeHeight) {
			return true;
		}
		return false;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int childMeasureSpec = MeasureSpec.makeMeasureSpec(childSize,
				MeasureSpec.EXACTLY);
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			getChildAt(i).measure(childMeasureSpec, childMeasureSpec);
		}
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		if (draggedViewPos == -1)
			return i;
		else if (i == childCount - 1)
			return draggedViewPos;
		else if (i >= draggedViewPos)
			return i + 1;
		return i;
	}

	/**
	 * 计算网格的高度
	 * 
	 * @param rows
	 * @return
	 */
	public int calculateGridHeight(int rows) {
		int size = rows * childSize;
		int spaceCount = rows - 1;
		if (spaceCount > 0) {
			size += spaceCount * getHorizontalSpace();
		}
		return size;
	}

	/**
	 * 根据坐标获取对应的Child
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getChildFromCoor(int x, int y) {
		int col = getColumnFromX(x);
		int row = getRowFromY(y);
		if (col == -1 || row == -1) // touch is between columns or rows
			return -1;
		int index = row * colCount + col;
		if (index >= getChildCount())
			return -1;
		return index;
	}

	/**
	 * 根据x坐标获取列(header view算是同一列)
	 * 
	 * @param x
	 * @return
	 */
	protected int getColumnFromX(int x) {
		x -= getBorderPaddingLeft();
		for (int i = 0; x > 0; i++) {
			if (x < childSize)
				return i;
			x -= (childSize + getHorizontalSpace());
		}
		return -1;
	}

	/**
	 * 根据y获取行（header view也算作单独一行）
	 * 
	 * @param y
	 * @return
	 */
	protected int getRowFromY(int y) {
		y -= getBorderPaddingTop();
		for (int i = 0; y > 0; i++) {
			if (y < childSize)
				return i;
			y -= (childSize + getVerticalSpace());
		}
		return -1;
	}

	protected void getCoorFromIndex(int index, Point out) {
		if (out != null) {
			int col = index % colCount;
			int row = index / colCount;
			int x = calculateItemLeft(col);
			int y = calculateItemTop(row);
			out.set(x, y);
		}
	}

	private int calculateItemLeft(int columnIndex) {
		int size = columnIndex * childSize;
		int spaceCount = columnIndex;
		if (spaceCount > 0) {
			size += spaceCount * getHorizontalSpace();
		}
		return size + getBorderPaddingLeft();
	}

	private int calculateItemTop(int rowIndex) {
		int size = rowIndex * childSize;
		int spaceCount = rowIndex;
		if (spaceCount > 0) {
			size += spaceCount * getVerticalSpace();
		}
		return size + getBorderPaddingTop();
	}

	public void setBorderPadding(int left, int top, int right, int bottom) {
		borderPaddingLeft = left;
		borderPaddingTop = top;
		borderPaddingRight = right;
		borderPaddingBottom = bottom;
		requestLayout();
	}

	public int getBorderPaddingLeft() {
		return borderPaddingLeft;
	}

	public int getBorderPaddingTop() {
		return borderPaddingTop;
	}

	public int getBorderPaddingRight() {
		return borderPaddingRight;
	}

	public int getBorderPaddingBottom() {
		return borderPaddingBottom;
	}

	public int getVerticalSpace() {
		return mVerticalSpace;
	}

	/**
	 * 获取竖直Item之间的space
	 * 
	 * @param itemVerticalSpace
	 */
	public void setVerticalSpace(int itemVerticalSpace) {
		mVerticalSpace = itemVerticalSpace;
	}

	/**
	 * 获取水平Item之间的space
	 * 
	 * @return
	 */
	public int getHorizontalSpace() {
		return mHorizontalSpace;
	}

	public void setHorizontalSpace(int itemHorizontalSpace) {
		mHorizontalSpace = itemHorizontalSpace;
	}

	private OnClickListener innerOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (secondaryOnClickListener != null) {
				secondaryOnClickListener.onClick(v);
			}
			if (DragConfig.CELL_STATUS == CellModel.CELL_STATUS_EDIT) {
				if (DragConfig.DEBUG) {
					log("abort item onclick listener,because current cell is edit status");
				}
				return;
			}
			if (onCellClickListener != null) {
				int lastIndex = getLastIndex();
				if (lastIndex >= 0) {
					View cellView = getChildAt(lastIndex);
					if (cellView instanceof CellLayout) {
						onCellClickListener.onItemClick((CellLayout) cellView);
					}
				}
			}
		}
	};
	private OnLongClickListener innerOnLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			int index = getLastIndex();
			if (index != -1) {
				CellLayout cellLayout = findCellLayoutFromParent(index);
				postDrag(cellLayout, index);
				return true;
			}
			if (secondaryOnLongClickListener != null) {
				return secondaryOnLongClickListener.onLongClick(v);
			}
			return false;
		}
	};

	private void postDrag(CellLayout cellLayout, int viewPosition) {
		if (mDragLayer != null) {
			if (cellLayout == null) {
				return;
			}
			int oX = cellLayout.getLeft();
			int oY = cellLayout.getTop();
			int page = getPageIndex();
			int posInPage = viewPosition;
			if (mDragLayer.postDrag(cellLayout, this, oX, oY, page, posInPage)) {
				draggedViewPos = viewPosition;
			}
			if (DragConfig.DEBUG) {
				log("post drag page:" + page + " posInPage:" + posInPage
						+ ",orignX:" + oX + ",orignY:" + oY);
			}
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			lastX = (int) ev.getX();
			lastY = (int) ev.getY();
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onStartCheckDrop(DraggingObject drag) {
		if (drag != null) {
			if (drag.pageIndex == getPageIndex()) {
				draggedViewPos = drag.positionInPage;
				lastTarget = draggedViewPos;
				layoutViewGone(getChildAt(draggedViewPos));
			}
		}
	}

	private void layoutViewGone(View view) {
		if (view != null) {
			view.layout(0, 0, 0, 0);
		}
	}

	private boolean haveOtherPageCellIn;

	@Override
	public void onCheckDrop(DraggingObject obj, int x, int y) {
		if (lastAnimationIsRunning) {
			return;
		}
		lastX = x;
		lastY = y;
		//
		if (mDragLayer == null || !mDragLayer.isDragging()) {
			return;
		}
		int target = getChildFromCoor(x, y);
		if (target == -1) {
			return;
		}
		CellLayout cellLayout = (CellLayout) getChildAt(target);
		if (cellLayout != null && !cellLayout.isMoveable()) {
			return;
		}
		//
		DraggingObject drag = mDragLayer.getDraggingObject();
		if (drag == null) {
			return;
		}
		int curPageIndex = getPageIndex();
		if (curPageIndex != drag.pageIndex) {
			// 来自其他页面的Drag
			draggedViewPos = target;
			haveOtherPageCellIn = true;
		}
		if (draggedViewPos == -1) {
			return;
		}
		if (!haveOtherPageCellIn) {
			getChildAt(draggedViewPos).layout(0, 0, 0, 0);// 在跨页拖动的时候要特别注意
		}
		if (lastTarget == target) {
			return;
		}
		lastTarget = target;
		pressInItem(drag, target);
	}

	@Override
	public void onDropFinish(DraggingObject obj) {
		if (draggedViewPos != -1) {
			View v = getChildAt(draggedViewPos);
			if (lastTarget != -1) {
				reorderChildren();
			} else {
				Point xy = new Point();
				getCoorFromIndex(draggedViewPos, xy);
				v.layout(xy.x, xy.y, xy.x + childSize, xy.y + childSize);
			}
			v.clearAnimation();
			lastTarget = -1;
			draggedViewPos = -1;
		}
	}

	private int invalidPosition = -1;

	/**
	 * 向当前页面压入一个Item到指定的位置,并做出合理的过度动画
	 * 
	 * @param drag
	 * @param targetPosition
	 */
	private void pressInItem(DraggingObject drag, int targetPosition) {
		if (drag == null) {
			return;
		}
		int gapPos;
		int endPos;
		invalidPosition = -1;
		// 1,确定将被排挤出去的Item
		int curPageIndex = getPageIndex();
		int moveOutO;// 移出方向
		gapPos = targetPosition;
		if (drag.pageIndex > curPageIndex) {// 从后面一页跨页移动过来的,排挤当前页面内的最后一个到下一页
			invalidPosition = getMoveableChildFromEnd(-1, null);
			endPos = invalidPosition;
			moveOutO = MOVE_HORIZONTAL_RIGHT;
		} else if (drag.pageIndex < curPageIndex) {// 从前一页跨页移动过来的,排挤当前页面的第一个到上一个页
			invalidPosition = getMoveableChildFromHeader(-1, null);
			endPos = invalidPosition;
			moveOutO = MOVE_HORIZONTAL_LEFT;
		} else {// 相等的情况是同一页内的移动,不排挤任何Item,只做移动动画
			endPos = drag.positionInPage;
			moveOutO = -1;
		}
		animateGap(gapPos, endPos, invalidPosition, moveOutO);
	}

	static final int MOVE_HORIZONTAL_LEFT = 1;
	static final int MOVE_HORIZONTAL_RIGHT = 2;

	/**
	 * 做移动动画
	 * 
	 * @param gapPos
	 *            指定要占用的位置
	 * @param endPos
	 *            从站定位置到最后一个手影响的位置(如果是同一个页面内的移动，那么就是空着的那个位置)
	 * @param moveOut
	 *            需要做动画移动到屏幕之外的，并且消失掉的位置
	 * @param outHO
	 *            out 的水平方向 @{@link #MOVE_HORIZONTAL_LEFT}@
	 *            {@link #MOVE_HORIZONTAL_RIGHT}
	 */
	private void animateGap(int gapPos, int endPos, int moveOut, int outHO) {
		if (gapPos < 0 || endPos < 0) {
			return;
		}
		Point oldXY = new Point();
		if (DragConfig.DEBUG) {
			log("-#---->gapPos:" + gapPos + " emptyPos:" + endPos);
		}
		if (moveOut != -1) {
			oldXY.set(0, 0);
			getCoorFromIndex(moveOut, oldXY);
			getChildAt(outHO);
			CellLayout v = findCellLayoutFromParent(moveOut);
			//
			int oldX = 0;
			int oldY = 0;
			int newX;
			int newY;
			if (outHO == MOVE_HORIZONTAL_RIGHT) {
				newX = getMeasuredWidth() - oldXY.x;
				newY = -(v.getMeasuredHeight() + getVerticalSpace()) * 2// 无唯一值，不同的值动画效果不一样
						- getBorderPaddingTop();
			} else {// left
				newX = -oldXY.x - getBorderPaddingLeft() - v.getMeasuredWidth();
				newY = (v.getMeasuredHeight() + getVerticalSpace()) * 2// 无唯一值，不同的值动画效果不一样
						+ getBorderPaddingTop();
			}
			// -------------------------------------------------------
			AnimationListener listener = null;
			if (gapPos == endPos) {
				lastAnimationIsRunning = true;
				listener = lastAnimListenerForGap;
			}
			startMoveAnimation(v, oldX, oldY, newX, newY, true, listener);
			oldXY.set(0, 0);
			if (listener != null || gapPos == endPos) {
				// 已经是最后一个需要做动画的任务
				return;
			}
		}
		//
		dispatchAnimation(endPos, gapPos, lastAnimListenerForGap);
	}

	private void dispatchAnimation(int endPos, int gapPos,
			AnimationListener lastAnimationListener) {
		Point oldXY = new Point();
		Point newXY = new Point();
		boolean increase = endPos < gapPos;
		int lastNewPos = -1;
		int pageIndex = getPageIndex();
		while (gapPos != endPos) {
			int oldPos;
			int newPos = endPos;
			if (increase) {
				oldPos = ++endPos;
			} else {
				oldPos = --endPos;
			}
			CellModel cell = mDataLine.getData(pageIndex, oldPos);
			if (cell == null) {
				continue;
			}
			CellLayout cellLayout = cellViewFactory.findCellLayout(cell);
			if (cellLayout == null) {
				continue;
			}
			if (!cell.isMoveable()) {
				lastNewPos = newPos;
				continue;
			} else if (lastNewPos > 0) {
				newPos = lastNewPos;
				lastNewPos = -1;
			}
			//
			oldXY.set(0, 0);
			newXY.set(0, 0);
			getCoorFromIndex(oldPos, oldXY);
			getCoorFromIndex(newPos, newXY);
			View v = cellLayout;
			int oX = oldXY.x - v.getLeft();
			int oY = oldXY.y - v.getTop();
			int nX = newXY.x - v.getLeft();
			int nY = newXY.y - v.getTop();
			//
			AnimationListener listener = null;
			if (gapPos == endPos && lastAnimationListener != null) {
				lastAnimationIsRunning = true;
				listener = lastAnimationListener;
				if (DragConfig.DEBUG) {
					log("-#setup animation listener when the last animation start");
				}
			}
			startMoveAnimation(v, oX, oY, nX, nY, true, listener);
			if (DragConfig.DEBUG) {
				log("-#newXY:" + nX + "," + nY);
			}
		}
	}

	private Animation startMoveAnimation(View v, int oldX, int oldY, int newX,
			int newY, boolean fillAfter, AnimationListener l) {
		return startMoveAnimation(v, oldX, oldY, newX, newY,
				Animation.ABSOLUTE, fillAfter, l);
	}

	private Animation startMoveAnimation(View v, int oldX, int oldY, int newX,
			int newY, int unitType, boolean fillAfter, AnimationListener l) {
		setupMoveAnimation(v, oldX, oldY, newX, newY, unitType, fillAfter, l);
		Animation an = v.getAnimation();
		v.startAnimation(an);
		return an;
	}

	private void setupMoveAnimation(View v, int oldX, int oldY, int newX,
			int newY, boolean fillAfter, AnimationListener l) {
		setupMoveAnimation(v, oldX, oldY, newX, newY, Animation.ABSOLUTE,
				fillAfter, l);
	}

	private void setupMoveAnimation(View v, int oldX, int oldY, int newX,
			int newY, int unitType, boolean fillAfter, AnimationListener l) {
		TranslateAnimation translate = new TranslateAnimation(unitType, oldX,
				unitType, newX, unitType, oldY, unitType, newY);
		translate.setDuration(animT);
		translate.setFillEnabled(fillAfter);
		translate.setFillAfter(fillAfter);
		translate.setAnimationListener(l);
		v.clearAnimation();
		v.setAnimation(translate);
	}

	private void setupScaleAnimation(View view, AnimationListener listener) {
		ScaleAnimation anim = new ScaleAnimation(1, 0, 1, 0,
				view.getWidth() / 2, view.getHeight() / 2);
		anim.setAnimationListener(listener);
		anim.setDuration(animT);
		anim.setFillEnabled(true);
		anim.setFillAfter(true);
		view.clearAnimation();
		view.setAnimation(anim);
	}

	private boolean lastAnimationIsRunning = false;
	private AnimationListener lastAnimListenerForGap = new SimpleAnimationListener() {
		@Override
		public void onAnimationEnd(Animation animation) {
			DraggingObject drag = mDragLayer.getDraggingObject();
			if (drag != null) {
				int newPage = getPageIndex();
				int newPos = lastTarget;
				int oldPage = drag.pageIndex;
				int oldPos = drag.positionInPage;
				View v = getChildAt(lastTarget);
				int newLeft = v.getLeft();
				int newTop = v.getTop();
				//
				moveViewAndData(invalidPosition, drag.cellLayout, oldPage,
						oldPos, newPage, newPos);
				invalidPosition = -1;
				//
				drag.positionInPage = lastTarget;
				drag.pageIndex = newPage;
				drag.originalX = newLeft;
				drag.originalY = newTop;

			}
			haveOtherPageCellIn = false;
			lastAnimationIsRunning = false;
		}

	};
	private CellActionListener cellActionListener = new CellActionListener() {
		@Override
		public void onCellRequestRemove(CellLayout cellLayout) {
			if (lastAnimationIsRunning == true) {
				if (DragConfig.DEBUG) {
					log("abort dell cell,because haveAnimationIsRunning");
				}
				return;
			}
			if (DragConfig.DEBUG) {
				DragConfig.debugCellLayout(cellLayout, TAG,
						"item dell on click page index:" + getPageIndex());
			}
			int[] out = new int[2];
			CellModel cell = cellLayout.getCellModel();
			int delCellGlobalIndex = mDataLine.indexOf(cell);
			mDataLine.indexInSegment(delCellGlobalIndex, out);
			int delViewIndex = out[1];
			//
			int childCount = getChildCount();
			if (delViewIndex < 0 || delViewIndex >= childCount) {
				return;
			}
			invalidPositionOnDeleteChild = delViewIndex;
			//
			mDataLine.registerLocationChangedListener(DraggableGridView.this);
			mDataLine.remove(cell);
		}
	};

	private void moveViewAndData(int removeChild, CellLayout cell, int oldPage,
			int oldPos, int newPage, int newPos) {
		if (removeChild >= 0) {
			int ri = lastTarget;
			View oc = getChildAt(removeChild);
			if (DragConfig.DEBUG) {
				DragConfig.debugCellLayout(oc, TAG, "removed view name:");
			}
			oc.clearAnimation();
			removeViewInLayout(oc);
			//
			moveData(oldPage, oldPos, newPage, newPos);
			//
			View child = cell;
			child.clearAnimation();
			LayoutParams lp = oc == null ? null : oc.getLayoutParams();
			if (lp == null) {
				lp = generateDefaultLayoutParams();
			}
			addViewInLayout(child, ri, lp, true);
			layoutViewGone(child);
		} else {
			moveData(oldPage, oldPos, newPage, newPos);
		}
	}

	private boolean interceptDataSetChangeFromPosition = false;

	private void moveData(int fromPageA, int itemAPos, int toPageB, int imteBPos) {
		if (mDataLine != null) {
			interceptDataSetChangeFromPosition = true;
			mDataLine.moveFromTo(fromPageA, itemAPos, toPageB, imteBPos);
			interceptDataSetChangeFromPosition = false;
		}
	}

	private int invalidPositionOnDeleteChild = -1;
	private Set<Animation> deleteItemAnimTree = new HashSet<Animation>();

	private void prepareDeleteAnim(Animation animation) {
		if (animation == null) {
			return;
		}
		deleteItemAnimTree.add(animation);
		animation.setAnimationListener(deleteImteAnimListener);
	}

	private AnimationListener deleteImteAnimListener = new SimpleAnimationListener() {

		@Override
		public void onAnimationEnd(Animation animation) {
			deleteItemAnimTree.remove(animation);
			if (deleteItemAnimTree.size() <= 0) {
				deleletInvalideChild();
				lastAnimationIsRunning = false;
			}
		}

	};

	@Override
	public void onLocationChanged(int delGlobalIndex, Locations indexMapping) {
		// 在需要的时候在明确的注册，每次注册只生效一次
		mDataLine.unregisterLocationChangedListener(this);
		// 数据已经移动完毕，当前页面的view还是原始的，这个时候开始做所有的动画，最后一个动画结束后，刷新当前页面的View
		//
		// 让被删除的View小消失的动画
		View view = getChildAt(invalidPositionOnDeleteChild);
		setupScaleAnimation(view, null);
		prepareDeleteAnim(view.getAnimation());
		setupAnimationForDelete(indexMapping);
		//
		lastAnimationIsRunning = true;
		//
		newComeCellRequestX = -1;
		newComeCellRequestY = -1;
		newComeCellViewIndex = -1;
		invalidate();
	}

	private int newComeCellRequestX = -1;
	private int newComeCellRequestY = -1;
	private int newComeCellViewIndex = -1;

	private void setupAnimationForDelete(Locations loccations) {
		int changeSize = loccations.size();
		int pageStart = mDataLine.getSegmentStart(getPageIndex());
		int pageEnd = mDataLine.getSegmentEnd(getPageIndex());
		if (pageEnd < 0 || pageStart < 0) {
			return;
		}
		Point oldXY = new Point();
		Point newXY = new Point();
		for (int i = 0; i < changeSize; i++) {
			int oldGIndex = loccations.getOldLocation(i);
			int newGIndex = loccations.getNewLocation(i);
			if (oldGIndex == newGIndex || newGIndex < pageStart
					|| newGIndex > pageEnd) {
				continue;
			}
			int oldPos = oldGIndex - pageStart;
			int newPos = newGIndex - pageStart;
			oldXY.set(0, 0);
			newXY.set(0, 0);
			getCoorFromIndex(oldPos, oldXY);
			getCoorFromIndex(newPos, newXY);
			//
			if (oldGIndex > pageEnd) {
				// 来自右边的页面
				// 布局到当前页面
				CellModel cell = mDataLine.getData(pageEnd);
				if (cell == null) {
					continue;
				}
				// 数据已经改变了，那么当前页面的最后一个数据，就是后面一页中的第一个数据
				CellLayout newCellView = cellViewFactory.findCellLayout(cell);
				if (newCellView == null) {
					continue;
				}
				//
				newCellView.setCellActionListener(cellActionListener);
				// measure,layout
				int x = getWidth();
				int y = getBorderPaddingTop();
				//
				newComeCellViewIndex = getChildCount();
				newComeCellRequestX = x;
				newComeCellRequestY = y;
				innerAddNewCellToLayout(newCellView, newComeCellViewIndex, true);
				//
				int oX = 150;
				int oY = 150;
				int nX = -newXY.x - x;
				int nY = newXY.y - y;
				setupMoveAnimation(newCellView, oX, oY, nX, nY, true, null);
				prepareDeleteAnim(newCellView.getAnimation());
			} else if (oldGIndex < pageStart) {
				// 来自左边页面
				// 在删除的时候是个特殊的操作，当前页面如果数据不够的话，那么只能从右边页面过来，从左边过来是不可能，所以忽略这种情况
			} else {
				// 自己的页面
				View cellView = findCellLayoutFromParent(oldPos);
				//
				int oX = oldXY.x - cellView.getLeft();
				int oY = oldXY.y - cellView.getTop();
				int nX = newXY.x - cellView.getLeft();
				int nY = newXY.y - cellView.getTop();
				setupMoveAnimation(cellView, oX, oY, nX, nY, true, null);
				prepareDeleteAnim(cellView.getAnimation());
			}
		}
	}

	private void innerAddNewCellToLayout(CellLayout cll, int index,
			boolean checkMeasure) {
		if (cll == null) {
			return;
		}
		LayoutParams lp = cll.getLayoutParams();
		if (lp == null) {
			lp = generateDefaultLayoutParams();
		}
		addViewInLayout(cll, index, lp, true);
		//
		if (checkMeasure) {
			if (cll.getMeasuredWidth() != childSize
					|| cll.getMeasuredHeight() != childSize) {
				int childMeasureSpec = MeasureSpec.makeMeasureSpec(childSize,
						MeasureSpec.EXACTLY);
				cll.forceLayout();
				cll.measure(childMeasureSpec, childMeasureSpec);
			}
		}
	}

	private void deleletInvalideChild() {
		if (invalidPositionOnDeleteChild != -1) {
			if (invalidPositionOnDeleteChild < getChildCount()) {
				CellLayout view = (CellLayout) getChildAt(invalidPositionOnDeleteChild);
				view.clearAnimation();
				removeViewInLayout(view);
				detachCell(view);
				cellViewFactory.recycleCellView(view);
				// TODO is ready need reorder
				reorderChildren();
			}
		}
		invalidPositionOnDeleteChild = -1;
	}

	/**
	 * 从child的头部开始循环，返回第一个可以move的child
	 * 
	 * @return
	 */
	private int getMoveableChildFromHeader(int endPos, CellModel ignoreCell) {
		int childCount = getChildCount();
		if (childCount <= 0) {
			return -1;
		}
		if (endPos < 0 || endPos >= childCount) {
			endPos = childCount - 1;
		}
		for (int i = 0; i <= endPos; i++) {
			CellLayout cl = (CellLayout) getChildAt(i);
			CellModel cell = cl.getCellModel();
			if (cell != null && cell != ignoreCell && cell.isMoveable()) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 从child的尾部开始循环返回第一个可以move的child
	 * 
	 * @return
	 */
	private int getMoveableChildFromEnd(int endPos, CellModel ignoreCell) {
		int childCount = getChildCount();
		if (childCount <= 0) {
			return -1;
		}
		if (endPos < 0) {
			endPos = 0;
		} else if (endPos >= childCount) {
			endPos = childCount - 1;
		}
		for (int i = childCount - 1; i >= endPos; i--) {
			CellLayout cl = (CellLayout) getChildAt(i);
			CellModel cell = cl.getCellModel();
			if (cell != null && cell != ignoreCell && cell.isMoveable()) {
				return i;
			}
		}
		return -1;
	}

	protected void reorderChildren() {
		// FIGURE OUT HOW TO REORDER CHILDREN WITHOUT REMOVING THEM ALL AND
		// RECONSTRUCTING THE LIST!!!
		for (int i = 0; i < getChildCount(); i++) {
			// 该方法很邪门，不能在下面的for循环中调用
			getChildAt(i).clearAnimation();
		}
		removeAllViewsInLayout();
		// TODO-----------------重写ReorderChildren
		int size = getItemCount();
		int pageIndex = getPageIndex();
		for (int i = 0; i < size; i++) {
			CellModel cell = mDataLine.getData(pageIndex, i);
			CellLayout child = cellViewFactory.findCellLayout(cell);
			boundCellLayoutAndModel(child, cell);
			addViewInLayout(child, i, child.getLayoutParams(), true);
		}
		onLayout(false, getLeft(), getTop(), getRight(), getBottom());
	}

	private void removeAllViews(boolean relayoutNow, boolean recycle) {
		int childCount = getChildCount();
		if (childCount <= 0) {
			return;
		}
		View[] needRecycleChildren = null;
		if (recycle) {
			needRecycleChildren = new View[childCount];
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				child.clearAnimation();
				needRecycleChildren[i] = child;
			}
		}
		// remove all old view
		if (relayoutNow) {
			removeAllViews();
		} else {
			removeViewsInLayout(0, childCount);
		}
		if (needRecycleChildren != null && needRecycleChildren.length > 0) {
			for (int i = 0; i < childCount; i++) {
				CellLayout child = (CellLayout) needRecycleChildren[i];
				detachCell(child);
			}
		}
	}

	private void attachCell(View view) {
		if (view instanceof CellLayout) {
			CellLayout cellLayout = ((CellLayout) view);
			cellLayout.onAttach(this);
			onCellAttach(cellLayout);
		}
	}

	private void detachCell(CellLayout cellLayout) {
		if (cellLayout == null) {
			return;
		}
		cellLayout.onDetach(this);
		onCellDetach(cellLayout);
	}

	protected void onCellAttach(CellLayout cell) {

	}

	protected void onCellDetach(CellLayout cell) {
	}

	public int getLastIndex() {
		return getChildFromCoor(lastX, lastY);
	}

	protected OnClickListener secondaryOnClickListener;

	@Override
	public void setOnClickListener(OnClickListener l) {
		secondaryOnClickListener = l;
	}

	protected OnLongClickListener secondaryOnLongClickListener;

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		secondaryOnLongClickListener = l;
	}

	public void setOnCellClickListener(OnCellClickListener l) {
		this.onCellClickListener = l;
	}

	public interface OnCellClickListener {
		public void onItemClick(CellLayout cell);
	}

	public void releaseAll() {
		removeAllViews(true, true);
		draggedViewPos = -1;
		lastTarget = -1;
	}

	protected CellLayout findCellLayoutFromParent(int index) {
		View v = getChildAt(index);
		if (v instanceof CellLayout) {
			return (CellLayout) v;
		}
		return null;
	}

	void log(String msg) {
		Log.d(TAG, "" + msg);
	}
}