package com.phodev.andtools.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

/**
 * 退场切换View效果
 * 
 * @author sky
 *
 */
public class CircleLogoutBox extends FrameLayout {
	private final static int FPS = 1000 / 40;
	private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

	private long mDuration = 1000;
	private long mStartTime;

	public CircleLogoutBox(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CircleLogoutBox(Context context) {
		super(context);
	}

	private float centerX;
	private float centerY;
	private float width;
	private float height;

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		width = getWidth();
		height = getHeight();
		centerX = width / 2;
		centerY = height / 2;
		if (centerX > 0 && centerY > 0) {
			maxMaskLayerRadius = (float) Math.sqrt(centerX * centerX + centerY
					* centerY);
		} else {
			maxMaskLayerRadius = -1;
		}
	}

	private float maxMaskLayerRadius;

	private float maskLayerRadius;
	private Paint shapePaint = new Paint();
	private Paint maskPaint = new Paint();
	{
		shapePaint.setAntiAlias(true);
		shapePaint.setColor(Color.BLUE);
		//
		maskPaint.setAntiAlias(true);
		maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
	}

	@Override
	public void draw(Canvas canvas) {
		if (!isBoxVisible) {
			return;
		}
		if (!isPlaying) {
			super.draw(canvas);
			return;
		}
		//
		if (maxMaskLayerRadius <= 0) {
			makeFinish(true);
			if (isBoxVisible) {
				super.draw(canvas);
			}
			return;
		}
		double timeDelta = (double) (System.currentTimeMillis() - mStartTime);
		float p = (float) (timeDelta / mDuration);
		maskLayerRadius = mInterpolator.getInterpolation(p)
				* maxMaskLayerRadius;
		// maskPaint.setAlpha(Math.max((int) (255f * (1f - p)), 5));
		if (timeDelta > mDuration) {
			makeFinish(true);
			if (isBoxVisible) {
				super.draw(canvas);
			}
			return;
		}
		int c_count = canvas.saveLayerAlpha(0, 0, width, height,
				(int) (255f * (1f - p)), Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
		canvas.saveLayer(0, 0, width, height, shapePaint, Canvas.ALL_SAVE_FLAG);
		canvas.drawCircle(centerX, centerY, maskLayerRadius, shapePaint);
		canvas.saveLayer(0, 0, width, height, maskPaint, Canvas.ALL_SAVE_FLAG);
		super.draw(canvas);
		canvas.restoreToCount(c_count);
		postInvalidateDelayed(FPS);
	}

	private void makeFinish(boolean notifyStop) {
		isPlaying = false;
		isBoxVisible = false;
		if (notifyStop && mCircleLogoutBoxListener != null) {
			mCircleLogoutBoxListener.onLogoutFinish(this);
		}
	}

	private boolean isBoxVisible = true;
	private boolean isPlaying = false;

	/**
	 * 启动退出效果
	 */
	public void startLogout() {
		mStartTime = System.currentTimeMillis();
		isPlaying = true;
		postInvalidate();
	}

	/**
	 * 重置效果
	 */
	public void reset() {
		isPlaying = false;
		isBoxVisible = true;
		maskLayerRadius = -1;
		postInvalidate();
		if (mCircleLogoutBoxListener != null) {
			mCircleLogoutBoxListener.onReset(this);
		}
	}

	public interface CircleLogoutBoxListener {
		public void onLogoutFinish(CircleLogoutBox c);

		public void onReset(CircleLogoutBox c);
	}

	private CircleLogoutBoxListener mCircleLogoutBoxListener;

	/**
	 * 设置周期监听
	 * 
	 * @param l
	 */
	public void setCircleLogoutBoxListener(CircleLogoutBoxListener l) {
		mCircleLogoutBoxListener = l;
	}

	public void setDuration(int millisecond) {
		mDuration = millisecond;
	}

	/**
	 * 设置加速度
	 * 
	 * @param interpolator
	 */
	public void setInterpolator(Interpolator interpolator) {
		if (interpolator == null) {
			throw new IllegalArgumentException(getClass().getCanonicalName()
					+ " Interpolator can not be null");
		}
		mInterpolator = interpolator;
	}
}
