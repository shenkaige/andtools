package com.phodev.andtools.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 正方形布局
 * 
 * @author sky
 */
public class SquareLayout extends FrameLayout {

	public SquareLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquareLayout(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int w = getMeasuredWidth();
		int h = getMeasuredHeight();
		if (w == h) {
			return;
		}
		int measureSize = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
		super.onMeasure(measureSize, measureSize);
	}
}
