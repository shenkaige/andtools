package com.phodev.andtools.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridLayout;

/**
 * 修正GridLayout的焦点顺序问题
 * 
 * @author sky
 *
 */
public class FixFocusOrderGridLayout extends GridLayout {

	public FixFocusOrderGridLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FixFocusOrderGridLayout(Context context) {
		super(context);
	}

	private View nextFocusView;

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (nextFocusView == null) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (event.getAction() == KeyEvent.ACTION_DOWN)
					nextFocusView = searchNearestChild(getFocusedChild(), View.FOCUS_LEFT);
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				if (event.getAction() == KeyEvent.ACTION_DOWN)
					nextFocusView = searchNearestChild(getFocusedChild(), View.FOCUS_UP);
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (event.getAction() == KeyEvent.ACTION_DOWN)
					nextFocusView = searchNearestChild(getFocusedChild(), View.FOCUS_RIGHT);
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if (event.getAction() == KeyEvent.ACTION_DOWN)
					nextFocusView = searchNearestChild(getFocusedChild(), View.FOCUS_DOWN);
				break;
			}
		}
		if (event.getAction() == KeyEvent.ACTION_UP && nextFocusView != null) {
			nextFocusView.requestFocus();
			nextFocusView = null;
			return true;
		}
		if (nextFocusView == null) {
			return super.dispatchKeyEvent(event);
		} else {
			return true;
		}

	}

	/**
	 * 根据direction需找下一个可以获取焦点的View并请求焦点
	 * 
	 * @param ref
	 * @param direction
	 */
	private View searchNearestChild(View ref, int direction) {
		int childCount = getChildCount();
		if (childCount <= 0) {
			return null;
		}
		View nextFocusView = null;
		int minXDist = Integer.MAX_VALUE;
		int minYDist = Integer.MAX_VALUE;
		switch (direction) {
		case View.FOCUS_LEFT:
			for (int i = 0; i < childCount; i++) {
				View v = getChildAt(i);
				if (v == ref) {
					continue;
				}
				// distX is different with focus right
				int distX = ref.getLeft() - v.getRight();
				if (distX < 0) {
					continue;
				}
				int distY = Math.abs(v.getTop() - ref.getTop());
				if (distX < minXDist || (distX == minXDist && distY < minYDist)) {
					minYDist = distY;
					minXDist = distX;
					nextFocusView = v;
				}
			}
			break;
		case View.FOCUS_UP:
			for (int i = 0; i < childCount; i++) {
				View v = getChildAt(i);
				if (v == ref) {
					continue;
				}
				// distY is different with focus down
				int distY = ref.getTop() - v.getBottom();
				if (distY < 0) {
					continue;
				}
				int distX = Math.abs(v.getLeft() - ref.getLeft());
				if (distY < minYDist || (distY == minYDist && distX < minXDist)) {
					minYDist = distY;
					minXDist = distX;
					nextFocusView = v;
				}
			}
			break;
		case View.FOCUS_RIGHT:
			for (int i = 0; i < childCount; i++) {
				View v = getChildAt(i);
				if (v == ref) {
					continue;
				}
				// distX is different with focus left
				int distX = v.getLeft() - ref.getRight();
				if (distX < 0) {
					continue;
				}
				int distY = Math.abs(v.getTop() - ref.getTop());
				if (distX < minXDist || (distX == minXDist && distY < minYDist)) {
					minYDist = distY;
					minXDist = distX;
					nextFocusView = v;
				}
			}
			break;
		case View.FOCUS_DOWN:
			for (int i = 0; i < childCount; i++) {
				View v = getChildAt(i);
				if (v == ref) {
					continue;
				}
				// distY is different with focus top
				int distY = v.getTop() - ref.getBottom();
				if (distY < 0) {
					continue;
				}
				int distX = Math.abs(v.getLeft() - ref.getLeft());
				if (distY < minYDist || (distY == minYDist && distX < minXDist)) {
					minYDist = distY;
					minXDist = distX;
					nextFocusView = v;
				}
			}
			break;
		}
		return nextFocusView;
	}
}
