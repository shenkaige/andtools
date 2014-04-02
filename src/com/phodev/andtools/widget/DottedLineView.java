package com.phodev.andtools.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

import com.phodev.andtools.R;

/**
 * 虚线View
 * 
 * @author skg
 * 
 */
public class DottedLineView extends View {
	private Paint paint;
	private Path path;
	private int mLineStrokeWidth;
	private int mLineColor;

	public DottedLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.DottedLineView);
		mLineColor = a.getColor(R.styleable.DottedLineView_line_color,
				Color.BLACK);
		mLineStrokeWidth = a.getDimensionPixelSize(
				R.styleable.DottedLineView_line_stroke_width, 1);
		a.recycle();
		init();
	}

	public DottedLineView(Context context) {
		super(context);
		init();
	}

	private void init() {
		paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(mLineColor);
		path = new Path();
		paint.setStrokeWidth(mLineStrokeWidth);
		int interval = (int)(getResources().getDisplayMetrics().density*2f);
		PathEffect effects = new DashPathEffect(new float[] { interval, interval }, 1);
		paint.setPathEffect(effects);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int startX = getLeft() + getPaddingLeft();
		int y = getPaddingTop();
		int endX = getMeasuredWidth() - getPaddingRight() - getPaddingLeft();
		path.reset();
		path.moveTo(startX, y);
		path.lineTo(endX, y);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawPath(path, paint);
	}

	/**
	 * Color值,并不是Color的ResID
	 * 
	 * @param color
	 */
	public void setColor(int color) {
		mLineColor = color;
		paint.setColor(mLineColor);
		invalidate();
	}

}
