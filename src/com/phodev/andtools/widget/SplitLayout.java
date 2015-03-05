package com.phodev.andtools.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 可分割的布局
 * 
 * @author sky
 * 
 */
public class SplitLayout extends FrameLayout {

	public SplitLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SplitLayout(Context context) {
		super(context);
		init();
	}

	private void init() {
		setWillNotDraw(false);
	}

	private Path dropPath = new Path();
	private Paint mMaskPaint = new Paint();
	{
		mMaskPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_OUT));
		mMaskPaint.setAntiAlias(true);
	}
	private Paint mPaint = new Paint();
	{
		mPaint.setColor(Color.RED);
		mPaint.setAntiAlias(true);
	}

	public void setDropArea(Path path) {
		dropPath.reset();
		dropPath.addPath(path);
		invalidate();
	}

	public void setDropArea(float... points) {
		drapAreaRelativePoints = null;
		drapAreaRelativeChanged = false;
		dropPath.reset();
		if (points != null && points.length / 2 > 1) {
			int pCount = points.length / 2;
			dropPath.moveTo(points[0], points[1]);
			for (int i = 1; i < pCount; i++) {
				dropPath.lineTo(points[i * 2], points[i * 2 + 1]);
			}
			dropPath.close();
		}
		invalidate();
	}

	/**
	 * <pre>
	 * <li>View center:(0.5,0.5)</li>
	 * <li>View upper left:(0,0)</li>
	 * <li>View lower right:(1,1)</li>
	 * </pre>
	 * 
	 * @param points
	 */
	public void setDropAreaByPercentage(float... points) {
		drapAreaRelativePoints = points;
		drapAreaRelativeChanged = true;
		invalidate();
	}

	private void convertPercentagePointsToLine(Path path, float[] points) {
		int vw = getWidth();
		int vh = getHeight();
		if (points == null) {
			return;
		}
		int pCount = points.length / 2;
		if (pCount < 1) {
			return;
		}
		path.moveTo(vw * points[0], vh * points[1]);
		for (int i = 1; i < pCount; i++) {
			path.lineTo(vw * points[i * 2], vh * points[i * 2 + 1]);
		}
	}

	private float[] drapAreaRelativePoints;
	private boolean drapAreaRelativeChanged = false;

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (drapAreaRelativePoints != null && !drapAreaRelativeChanged
				&& changed) {
			drapAreaRelativeChanged = changed;
		}
	}

	private final int layerFlag = Canvas.ALL_SAVE_FLAG;

	@Override
	public void draw(Canvas canvas) {
		if (drapAreaRelativeChanged && drapAreaRelativePoints != null) {
			dropPath.reset();
			convertPercentagePointsToLine(dropPath, drapAreaRelativePoints);
			dropPath.close();
			drapAreaRelativeChanged = false;
		}
		//
		if (dropPath.isEmpty()) {
			super.draw(canvas);
			return;
		}
		//
		canvas.saveLayer(0, 0, getWidth(), getHeight(), mPaint, layerFlag);
		canvas.drawPath(dropPath, mPaint);
		canvas.saveLayer(0, 0, getWidth(), getHeight(), mMaskPaint, layerFlag);
		super.draw(canvas);
		canvas.restore();
		canvas.restore();
	}
}
