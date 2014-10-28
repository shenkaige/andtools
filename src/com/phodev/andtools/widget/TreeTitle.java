package com.phodev.andtools.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 属性title
 * 
 * @author sky
 *
 */
public class TreeTitle extends ViewGroup {

	public TreeTitle(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TreeTitle(Context context) {
		super(context);
		init();
	}

	private void init() {
		setChildrenDrawingOrderEnabled(true);
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		return childCount - 1 - i;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
		int leftOffset = 0;
		int bottom = getHeight();
		int left;
		int right;
		for (int i = 0; i < childCount; i++) {
			View v = getChildAt(i);
			left = leftOffset;
			right = left + v.getMeasuredWidth();
			v.layout(left, 0, right, bottom);
			leftOffset = right - v.getPaddingRight();
		}
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int childCount = getChildCount();
		int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
		int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
		int cWMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth,
				MeasureSpec.UNSPECIFIED);
		int cHMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight,
				MeasureSpec.EXACTLY);
		int totalWidth = 0;
		int lastPaddingRight = 0;
		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {
				totalWidth -= lastPaddingRight;
				View v = getChildAt(i);
				v.setPadding(lastPaddingRight, v.getPaddingTop(),
						v.getPaddingRight(), v.getPaddingBottom());
				v.measure(cWMeasureSpec, cHMeasureSpec);
				totalWidth += v.getMeasuredWidth();
				lastPaddingRight = v.getPaddingRight();
			}
		}
		setMeasuredDimension(totalWidth, maxHeight);
	}

	public void add(String msg) {
		TextView tv = createItem();
		if (getChildCount() % 2 == 0) {
		} else {
		}
		tv.setText(msg);
		// tv.setOnClickListener(onClickListener);
		addView(tv);
	}

	private TextView createItem() {
		TextView tv = new TextView(getContext());
		return tv;
	}

}
