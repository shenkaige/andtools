package com.phodev.andtools.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author sky
 */
public class RoundProgressBar extends View {
	private float progress_boder_width = 8;// dp

	public RoundProgressBar(Context context) {
		super(context);
		init();
	}

	public RoundProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		progress_boder_width = getResources().getDisplayMetrics().density
				* progress_boder_width;
	}

	private float centerX;
	private float centerY;
	private float innerRadius;
	private float secondRadius;
	private SweepGradient progressBarGradient;
	//
	private Paint innerPaint = new Paint();
	private Paint secondPaint = new Paint();
	private Paint progressPaint = new Paint();

	{
		innerPaint.setAntiAlias(true);
		secondPaint.setAntiAlias(true);
		progressPaint.setAntiAlias(true);
	}
	private final RectF progressBarRect = new RectF();

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		float w = getWidth();
		float h = getHeight();
		centerX = w / 2;
		centerY = h / 2;
		float maxD = Math.min(w, h);
		progressBarRect.set(0, 0, maxD, maxD);
		//
		innerRadius = maxD / 2f - progress_boder_width;
		secondRadius = innerRadius + progress_boder_width / 2f;
	}

	private static final float d_360 = 360f;
	private static final int fps = 1000 / 40;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		innerPaint.setColor(innserColor);
		secondPaint.setColor(secondInnerColor);
		progressPaint.setColor(progressColor);
		canvas.drawCircle(centerX, centerY, secondRadius, secondPaint);
		checkPrgressBarPaint(isIndeterminate);
		if (isIndeterminate) {
			indeterminateProgressDegress += 2.8f;
			int c_count = canvas.save();
			canvas.rotate(indeterminateProgressDegress, centerX, centerY);
			canvas.drawArc(progressBarRect, 0f, d_360, true, progressPaint);
			canvas.restoreToCount(c_count);
			postInvalidateDelayed(fps);
		} else {
			float degress = Math.max(1, d_360 * curProgress);
			canvas.drawArc(progressBarRect, 45, degress, true, progressPaint);
		}
		canvas.drawCircle(centerX, centerY, innerRadius, innerPaint);
	}

	private void checkPrgressBarPaint(boolean isIndeterminate) {
		Shader shader = progressPaint.getShader();
		if (isIndeterminate) {
			Shader newShader = getProgressBarGradient();
			if (shader != newShader) {
				progressPaint.setShader(newShader);
			}
		} else {
			if (progressPaint.getShader() != null) {
				progressPaint.setShader(null);
			}
		}
	}

	private int[] gradientColors;
	private float[] gradientPositions;

	/**
	 * 必须在已经计算出center x y的时候才是有效的
	 * 
	 * @return
	 */
	private SweepGradient getProgressBarGradient() {
		if (progressBarGradient == null) {
			if (gradientColors == null) {
				return null;
			}
			progressBarGradient = new SweepGradient(centerX, centerY,
					gradientColors, gradientPositions);
		}
		return progressBarGradient;
	}

	private boolean isIndeterminate = Boolean.FALSE;
	private final static float def_indeterminateProgressDegress = 45f;
	private float indeterminateProgressDegress = def_indeterminateProgressDegress;

	/**
	 * 不确定百分比的的进度,一直转圈圈
	 * 
	 * @param indeterminate
	 */
	public void setIndeterminate(boolean indeterminate) {
		isIndeterminate = indeterminate;
		indeterminateProgressDegress = def_indeterminateProgressDegress;
		invalidate();
	}

	public boolean isIndeterminater() {
		return isIndeterminate;
	}

	private float curProgress;

	/**
	 * 设置进度0-1f
	 * 
	 * @param progress
	 */
	public void setProgress(float progress) {
		curProgress = progress;
	}

	private int innserColor = 0xff51c0de;
	private int secondInnerColor = Color.WHITE;
	private int progressColor = 0xff70d2ee;

	public void setInnerColor(int color) {
		innserColor = color;
		invalidate();
	}

	public void setSecondInnerColor(int color) {
		secondInnerColor = color;
		invalidate();
	}

	public void setProgressColor(int color) {
		progressColor = color;
		invalidate();
	}

	public void setIndeterminaterProgressGradient(int[] colors,
			float[] colorsPosition) {
		// new int[] { 0xff5bd9fc, 0xff5bd9fc, 0xff5df6ff },
		// new float[] { 0.0f, 0.9f, 1f }
		gradientColors = colors;
		gradientPositions = colorsPosition;
		progressBarGradient = null;
		invalidate();
	}
}
