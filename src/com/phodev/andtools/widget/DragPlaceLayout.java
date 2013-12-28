package com.phodev.andtools.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * child可以拖动的Layout
 * 
 * @author sky
 * 
 */
public class DragPlaceLayout extends ViewGroup {
	public interface DragHolder {
		public void onStartDrag(DragPlaceLayout drag, View view,
				int dragOffsetX, int dragOffsetY);

		public void onDragFinish(View view);
	}

	public DragPlaceLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DragPlaceLayout(Context context) {
		super(context);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
		int cl, ct, cr, cb;
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			cl = lp.x;
			ct = lp.y;
			cr = cl + child.getMeasuredWidth();
			cb = ct + child.getMeasuredHeight();
			child.layout(cl, ct, cr, cb);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				measureChild(child, widthMeasureSpec, heightMeasureSpec);
			}
		}
		int maxW = MeasureSpec.getSize(widthMeasureSpec);
		int maxH = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(maxW, maxH);
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return generateDefaultLayoutParams();
	}

	@Override
	protected LayoutParams generateLayoutParams(
			android.view.ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	public static class LayoutParams extends ViewGroup.LayoutParams {

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(android.view.ViewGroup.LayoutParams source) {
			super(source);
		}

		public int x;

		public int y;

	}

	private View dragingView;
	private int dragOffsetX;
	private int dragOffsetY;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean result = super.dispatchTouchEvent(ev);
		if (dragingView != null) {
			int x = (int) ev.getX();
			int y = (int) ev.getY();
			switch (ev.getAction()) {
			case MotionEvent.ACTION_MOVE:
				refreshXY(dragingView, x + dragOffsetX, y + dragOffsetY);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_OUTSIDE:
				onDragFinish(dragingView);
				dragingView = null;
				dragOffsetX = 0;
				dragOffsetY = 0;
				break;
			}
		}
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			int x = (int) ev.getX();
			int y = (int) ev.getY();
			dragingView = findMappingView(x, y);
			if (dragingView != null) {
				result = true;
				dragOffsetX = -x + dragingView.getLeft();
				dragOffsetY = -y + dragingView.getTop();
				onStartDrag(dragingView);
			}
		}
		return result;
	}

	private void onStartDrag(View dragView) {
		if (dragView instanceof DragHolder) {
			DragHolder dh = (DragHolder) dragView;
			dh.onStartDrag(this, dragView, dragOffsetX, dragOffsetY);
		}
	}

	private void onDragFinish(View dragView) {
		if (dragView instanceof DragHolder) {
			DragHolder dh = (DragHolder) dragView;
			dh.onDragFinish(dragView);
		}
	}

	public View getDragView() {
		return dragingView;
	}

	public int getDragOffsetX() {
		return dragOffsetX;
	}

	public int getDragOffsetY() {
		return dragOffsetY;
	}

	public void refreshXY(View view, int newX, int newY) {
		if (view == null) {
			return;
		}
		LayoutParams lp = (LayoutParams) view.getLayoutParams();
		int cw = view.getMeasuredWidth();
		int ch = view.getMeasuredHeight();
		int pw = getWidth();
		int ph = getHeight();
		int l = newX;
		int t = newY;
		if (l < 0) {
			l = 0;
		}
		if (t < 0) {
			t = 0;
		}
		int r = l + cw;
		int b = t + ch;
		if (r > pw) {
			l = pw - cw;
			r = pw;
		}
		if (b > ph) {
			t = ph - ch;
			b = ph;
		}
		lp.x = l;
		lp.y = t;
		//
		view.forceLayout();
		view.layout(l, t, r, b);
	}

	private View findMappingView(int x, int y) {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View c = getChildAt(i);
			if (c.getLeft() < x && c.getTop() < y && c.getRight() > x
					&& c.getBottom() > y) {
				return c;
			}
		}
		return null;
	}
}
