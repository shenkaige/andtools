package com.phodev.andtools.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.phodev.andtools.R;

/**
 * 圆形头部View
 * 
 * @author sky
 */
public class CircleHeaderLayout extends FrameLayout {

	private float centerXoffset;
	private float circleRadius;
	private float circleCenterX;
	private float circleCenterY;
	private boolean circleAreaUseable;
	public static final int ACTION_DRAW = 0;
	public static final int ACTION_CUT = 1;

	public CircleHeaderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.CircleHeader);
		circleAreaUseable = a.getBoolean(
				R.styleable.CircleHeader_circle_area_usable, true);
		setCenterXoffset(a.getDimensionPixelSize(
				R.styleable.CircleHeader_circle_center_x_offset, 0));
		setCircleRadius(a.getDimensionPixelSize(
				R.styleable.CircleHeader_circle_radius, 0));
		init(a.getInt(R.styleable.CircleHeader_circle_action, ACTION_DRAW));

		a.recycle();

	}

	private final Paint maskPaint = new Paint();
	private final Paint drawPaint = new Paint();

	private void init(int action) {
		setWillNotDraw(false);
		maskPaint.setAntiAlias(true);
		maskPaint.setXfermode(new PorterDuffXfermode(
				action == ACTION_CUT ? PorterDuff.Mode.SRC_OUT
						: PorterDuff.Mode.SRC_IN));
		//
		drawPaint.setAntiAlias(true);
		drawPaint.setColor(Color.WHITE);
	}

	public void setCircleRadius(float radius) {
		this.circleRadius = radius;
		refreshMiniSize();
	}

	public void setCenterXoffset(float offset) {
		centerXoffset = offset;
		refreshMiniSize();
	}

	private void refreshMiniSize() {
		setMinimumWidth((int) (circleRadius + centerXoffset) + getPaddingLeft()
				+ getPaddingRight());
		setMinimumHeight((int) (circleRadius * 2) + getPaddingTop()
				+ getPaddingBottom());
		if (!circleAreaUseable) {
			setPadding((int) (circleRadius + centerXoffset), getPaddingTop(),
					getPaddingRight(), getPaddingBottom());
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		circleCenterY = getHeight() / 2f;
		circleCenterX = centerXoffset;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.saveLayer(0, 0, getWidth(), getHeight(), drawPaint,
				Canvas.ALL_SAVE_FLAG);
		canvas.drawCircle(circleCenterX, circleCenterY, circleRadius, drawPaint);
		//
		canvas.saveLayer(0, 0, getWidth(), getHeight(), maskPaint,
				Canvas.ALL_SAVE_FLAG);
		super.draw(canvas);
		canvas.restore();

	}

}
