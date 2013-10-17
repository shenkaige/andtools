package com.phodev.andtools.drag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;

import com.phodev.andtools.drag.PageContainer.OnPageStatusListener;

/**
 * 拖动时候的层，控制被拖动的View的显示和拖动的逻辑
 * 
 * @author skg
 * 
 */
public class DragLayer extends ViewGroup {
	static final String TAG = "DragLayer";
	private final float DRAG_DELEGATE_SIZE_RATIO = 1.05f;
	private DragDelegateView mDragDelegate;
	private DraggingObject mDraggingObj = new DraggingObject();
	private DropTarget mDropTarget;
	private DragLayerHost mDragLayerHost;
	private PageContainer mPageContainer;

	public DragLayer(Context context, DragLayerHost host) {
		super(context);
		mDragLayerHost = host;
		mPageContainer = host.getPageContainer();
		mPageContainer.setOnPageStatusListener(mOnPageStatusListener);
		init();
	}

	private void init() {
		mDragDelegate = new DragDelegateView(getContext());
		addView(mDragDelegate);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mDragDelegate.layout(dragDelLeft, dragDelTop, dragDelRight,
				dragDelBottom);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mDragDelegate.forceLayout();
		mDragDelegate.measure(widthMeasureSpec, heightMeasureSpec);
	}

	public boolean postDrag(CellLayout cellLayuot, ViewGroup parent,
			int originX, int originY, int pageIndex, int positionInPage) {
		CellModel cell = cellLayuot.getCellModel();
		mDraggingObj.recyle();
		mDraggingObj.cell = cell;
		mDraggingObj.cellLayout = cellLayuot;
		mDraggingObj.parent = parent;
		mDraggingObj.originalX = originX;
		mDraggingObj.originalY = originY;
		mDraggingObj.pageIndex = pageIndex;
		mDraggingObj.positionInPage = positionInPage;
		//
		// clear old position
		dragDelLeft = dragDelTop = dragDelRight = dragDelBottom = 0;
		mDragDelegate.clearAnimation();
		//
		if (mDraggingObj.isEffective()) {
			mDragLayerHost.getPageContainer().setAutoScroll(false);
			attachToLayerHost();
			if (!mDraggingObj.cell.isMoveable()) {
				mDraggingObj.recyle();
				invalidate();
				requestLayout();
				return false;
			}
			//
			View v = mDraggingObj.cellLayout;
			movePageMinBoundary = v.getMeasuredWidth() / 2;
			if (movePageMinBoundary <= 0) {
				movePageMinBoundary = DragConfig.getMovePageMinBoundary();
			}
			mDragDelegate.setBitmap(v);
			//
			View page = mDragLayerHost.getPageContainer().getCurrentPage();
			if (page instanceof DropTarget) {
				mDropTarget = (DropTarget) page;
			}
			isDragging = true;
			onStartCheckDrop();
			post(startDragViewRunnable);
			return true;
		} else {
			// 如果无效，reset，避免不完整的无效的引用
			mDraggingObj.recyle();
			return false;
		}
	}

	private Runnable startDragViewRunnable = new Runnable() {
		@Override
		public void run() {
			startDragViewShowAnimation(mDragDelegate);
		}
	};

	private void attachToLayerHost() {
		mDragLayerHost.attach(this);
	}

	private void dettachFromLayerHost() {
		mDragLayerHost.unattach(this);
	}

	private boolean isDragging = false;

	public boolean isDragging() {
		return isDragging;
	}

	/**
	 * 获取当前的滚动的目标
	 * 
	 * @return
	 */
	public DraggingObject getDraggingObject() {
		return mDraggingObj;
	}

	private boolean isReorderAnimationRunning = false;

	public boolean isReorderAnimationRunning() {
		return isReorderAnimationRunning;
	}

	public void setReorderAnimationReunning(boolean running) {
		isReorderAnimationRunning = running;
	}

	private int dragDelLeft = 0;
	private int dragDelTop = 0;
	private int dragDelRight = 0;
	private int dragDelBottom = 0;
	private final int MOVE_PAGE_CONFIRM_TIME = DragConfig.MOVE_PAGE_CONFIRM_TIME;
	private int movePageMinBoundary;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_MOVE:
			handlerMove((int) ev.getX(), (int) ev.getY());
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			isDragging = false;
			dettachFromLayerHost();
			onDropFinish();
			//
			mDragLayerHost.getPageContainer().setAutoScroll(true);
			break;
		}
		// 拦截所有事件,我们只希望drag层有事件交互
		return true;
	}

	@SuppressLint("WrongCall")
	private void handlerMove(int x, int y) {
		int childSize = mDragDelegate.getMeasuredWidth();
		dragDelLeft = x - childSize / 2;
		dragDelTop = y - childSize / 2;
		dragDelRight = dragDelLeft + childSize;
		dragDelBottom = dragDelTop + childSize;
		onLayout(true, getLeft(), getTop(), getRight(), getBottom());
		mDragDelegate.invalidate();
		//
		if (x < movePageMinBoundary) {
			if (!isRunningToPreviousPage && mPageContainer.havePreviousPage()) {
				isRunningToPreviousPage = true;
				postDelayed(previousPageRunable, MOVE_PAGE_CONFIRM_TIME);
			}
		} else if (x > (getWidth() - movePageMinBoundary)) {
			if (!isRunningToNextPage && mPageContainer.haveNextPage()) {
				isRunningToNextPage = true;
				postDelayed(nextPageRunable, MOVE_PAGE_CONFIRM_TIME);
			}
		} else {
			if (isRunningToPreviousPage) {
				isRunningToPreviousPage = false;
				removeCallbacks(previousPageRunable);
			}
			if (isRunningToNextPage) {
				isRunningToNextPage = false;
				removeCallbacks(nextPageRunable);
			}
			checkDrop(x, y);
		}
	}

	private void checkDrop(int x, int y) {
		if (DragConfig.DEBUG) {
			log("check drag x:" + x + "y:" + y);
		}
		if (mDropTarget != null) {
			mDropTarget.onCheckDrop(mDraggingObj, x, y);
		}
	}

	private void onStartCheckDrop() {
		if (mDropTarget != null && mLastDropTarget != mDropTarget) {
			if (mLastDropTarget != null) {
				mLastDropTarget.onDropFinish(mDraggingObj);
			}
			mLastDropTarget = mDropTarget;
			mDropTarget.onStartCheckDrop(mDraggingObj);
		}
	}

	private void onDropFinish() {
		if (mDropTarget != null) {
			mDropTarget.onDropFinish(mDraggingObj);
		}
		//
		mDraggingObj.recyle();
		mDragDelegate.recycleBitmap();
	}

	private boolean isRunningToPreviousPage = false;
	private Runnable previousPageRunable = new Runnable() {
		@Override
		public void run() {
			mDragLayerHost.getPageContainer().previousPage();
		}
	};
	private boolean isRunningToNextPage = false;
	private Runnable nextPageRunable = new Runnable() {
		@Override
		public void run() {
			mDragLayerHost.getPageContainer().nextPage();
		}
	};
	private DropTarget mLastDropTarget;
	private OnPageStatusListener mOnPageStatusListener = new OnPageStatusListener() {

		@Override
		public void onPageSelected(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			if (isDragging) {
				View pgv = mDragLayerHost.getPageContainer().getCurrentPage();
				if (pgv instanceof DropTarget) {
					mDropTarget = (DropTarget) pgv;
					onStartCheckDrop();
					if (DragConfig.DEBUG) {
						log("on page select changed mCurrentDropTarget:"
								+ mDropTarget.hashCode() + " pageView:"
								+ pgv.hashCode());
					}
				}
				// 尝试移除还存在的CallBack
				removeCallbacks(nextPageRunable);
				removeCallbacks(previousPageRunable);
				isRunningToPreviousPage = false;
				isRunningToNextPage = false;
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}
	};

	private Bitmap getViewBitmap(View v) {
		if (v == null) {
			return null;
		}
		Bitmap result = null;
		Bitmap cacheBitmap = v.getDrawingCache();
		if (cacheBitmap != null && cacheBitmap.isMutable()) {
			result = Bitmap.createBitmap(cacheBitmap);
		}
		if (result == null) {
			int w = v.getMeasuredWidth();
			int h = v.getMeasuredHeight();
			if (w <= 0 || h <= 0) {
				return null;
			}
			result = Bitmap.createBitmap(w, h, Config.ARGB_8888);
			Canvas c = new Canvas(result);
			if (v instanceof CellLayout) {
				((CellLayout) v).drawDraggingFace(c);
			} else {
				v.draw(c);
			}
		}
		return result;
	}

	private final int animT = 150;
	private final int dragDelegateAlpha = 180;
	private final float dragDelegateAnimationAlpha = 0.7f;

	private void startDragViewShowAnimation(DragDelegateView v) {
		int w = v.getMeasuredWidth();
		AnimationSet animSet = new AnimationSet(true);
		int pivot = w * 3 / 4;
		float startScale = 1 / DRAG_DELEGATE_SIZE_RATIO;
		ScaleAnimation scale = new ScaleAnimation(startScale, 1, startScale, 1,
				pivot, pivot);
		scale.setDuration(animT);
		AlphaAnimation alpha = new AlphaAnimation(1, dragDelegateAnimationAlpha);
		alpha.setDuration(animT);
		animSet.addAnimation(scale);
		animSet.addAnimation(alpha);
		// animSet.setFillEnabled(true);
		// animSet.setFillAfter(true);
		v.startAnimation(animSet);
	}

	/**
	 * 描述一个被拖动的Object的信息
	 */
	class DraggingObject {
		public CellModel cell;
		public CellLayout cellLayout;
		public ViewGroup parent;
		public int x = -1;
		public int y = -1;
		public int originalX;
		public int originalY;
		public boolean dragComplete = false;
		public int pageIndex = -1;
		public int positionInPage = -1;

		public void recyle() {
			pageIndex = -1;
			positionInPage = -1;
			cell = null;
			cellLayout = null;
			x = -1;
			y = -1;
			dragComplete = false;
		}

		public boolean isEffective() {
			return cell != null && pageIndex >= 0 && positionInPage >= 0;
		}
	}

	class DragDelegateView extends View {
		private Bitmap mBitmap;
		private Paint mPaint;

		public DragDelegateView(Context context) {
			super(context);
			mPaint = new Paint();
			mPaint.setAlpha(dragDelegateAlpha);
			mPaint.setAntiAlias(true);
		}

		public void setBitmap(View view) {
			setBitmap(getViewBitmap(view));
		}

		public void setBitmap(Bitmap bitmap) {
			if (bitmap != mBitmap) {
				recycleBitmap();
			}
			mBitmap = bitmap;
		}

		public void recycleBitmap() {
			if (mBitmap != null && !mBitmap.isRecycled()) {
				mBitmap.recycle();
			}
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if (DragConfig.DEBUG) {
				log("draglayer-draw--DragDelegateView-->onDraw");
			}
			if (mBitmap != null && !mBitmap.isRecycled()) {
				int c_count = canvas.save();
				canvas.scale(DRAG_DELEGATE_SIZE_RATIO, DRAG_DELEGATE_SIZE_RATIO);
				canvas.drawBitmap(mBitmap, 0, 0, mPaint);
				canvas.restoreToCount(c_count);
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int measuredWidth = 0;
			int measuredHeight = 0;
			if (mBitmap != null) {
				measuredWidth = (int) (mBitmap.getWidth() * DRAG_DELEGATE_SIZE_RATIO);
				measuredHeight = measuredWidth;
			}
			setMeasuredDimension(measuredWidth, measuredHeight);
			if (DragConfig.DEBUG) {
				log("on measuer-->measuredWidth:" + measuredWidth
						+ " measuredHeight:" + measuredHeight + " w:"
						+ getWidth() + " h:" + getHeight());
			}
		}

	}

	void log(String msg) {
		Log.d(TAG, "" + msg);
	}

}
