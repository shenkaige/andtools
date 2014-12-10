package com.phodev.andtools.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;

/**
 * 水波效果
 * 
 * @author sky
 *
 */
public class WaterWaveView extends View {
	private final static int FPS = 1000 / 40;
	private float mMaxWaveAreaRadius;
	private float mWaveIntervalSize;
	private float mStirStep;// 波移动的步幅
	private float mWaveStartWidth;// px
	private float mWaveEndWidth;// px
	private int mWaveColor;
	/** 颜色渐变控制器 */
	private Interpolator interpolator = new CycleInterpolator(0.5f);
	private float mViewCenterX;
	private float mViewCenterY;
	private final Paint mWavePaint = new Paint();
	{
		mWavePaint.setAntiAlias(true);
		mWavePaint.setStyle(Style.STROKE);
	}
	private final Paint mWaveCenterShapePaint = new Paint();
	{
		mWaveCenterShapePaint.setAntiAlias(true);
		mWaveCenterShapePaint.setStyle(Style.FILL);
	}
	private boolean mFillAllView = false;
	private float mFillWaveSourceShapeRadius;
	private final List<Wave> mWaves = new ArrayList<Wave>();

	public WaterWaveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public WaterWaveView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setWaveInfo(60f, 2f, 2f, 15f, Color.BLUE);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mViewCenterX = getWidth() / 2;
		mViewCenterY = getHeight() / 2;
		float waveAreaRadius = mMaxWaveAreaRadius;
		if (mFillAllView) {
			waveAreaRadius = (float) Math.sqrt(mViewCenterX * mViewCenterX
					+ mViewCenterY * mViewCenterY);
		} else {
			waveAreaRadius = Math.min(mViewCenterX, mViewCenterY);
		}
		if (mMaxWaveAreaRadius != waveAreaRadius) {
			mMaxWaveAreaRadius = waveAreaRadius;
			resetWave();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		stir();
		for (Wave w : mWaves) {
			mWavePaint.setColor(w.color);
			mWavePaint.setStrokeWidth(w.width);
			canvas.drawCircle(mViewCenterX, mViewCenterY, w.radius, mWavePaint);
		}
		if (mFillWaveSourceShapeRadius > 0f) {
			canvas.drawCircle(mViewCenterX, mViewCenterY,
					mFillWaveSourceShapeRadius, mWaveCenterShapePaint);
		}
		postInvalidateDelayed(FPS);
	}

	/**
	 * 波
	 * 
	 * @author sky
	 */
	class Wave {
		public float radius;
		public float width;
		public int color;

		public Wave() {
			reset();
		}

		public void reset() {
			radius = 0;
			width = mWaveStartWidth;
			color = mWaveColor;
		}

		@Override
		public String toString() {
			return "Wave [radius=" + radius + ", width=" + width + ", color="
					+ color + "]";
		}

	}

	private Wave mLastRmoveWave;

	/**
	 * 触发涌动传播
	 */
	private void stir() {
		Wave nearestWave = mWaves.isEmpty() ? null : mWaves.get(0);
		if (nearestWave == null || nearestWave.radius >= mWaveIntervalSize) {
			Wave w = null;
			if (mLastRmoveWave != null) {
				w = mLastRmoveWave;
				mLastRmoveWave = null;
				w.reset();
			} else {
				w = new Wave();
			}
			mWaves.add(0, w);
		}
		float waveWidthIncrease = mWaveEndWidth - mWaveStartWidth;
		int size = mWaves.size();
		for (int i = 0; i < size; i++) {
			Wave w = mWaves.get(i);
			float rP = w.radius / mMaxWaveAreaRadius;
			if (rP > 1f) {
				rP = 1f;
			}
			w.width = mWaveStartWidth + rP * waveWidthIncrease;
			w.radius += mStirStep;
			float factor = interpolator.getInterpolation(rP);
			w.color = mWaveColor & 0x00FFFFFF | ((int) (255 * factor) << 24);
		}
		Wave farthestWave = mWaves.get(size - 1);
		if (farthestWave.radius > mMaxWaveAreaRadius + farthestWave.width / 2) {
			mWaves.remove(size - 1);
		}
	}

	/**
	 * 如果true会选择view的最大的对角线作为活动半径
	 * 
	 * @param fillAllView
	 */
	public void setFillAllView(boolean fillAllView) {
		mFillAllView = fillAllView;
		resetWave();
	}

	public void resetWave() {
		mWaves.clear();
		postInvalidate();
	}

	/**
	 * 填充波形起源的中心点
	 * 
	 * @param radius
	 *            半径大小
	 */
	public void setFillWaveSourceShapeRadius(float radius) {
		mFillWaveSourceShapeRadius = radius;
	}

	/**
	 * 设置波形属性
	 * 
	 * @param intervalSize
	 *            两个波形之间的间距
	 * @param stireStep
	 *            波形移动速度
	 * @param startWidth
	 *            起始波形宽度
	 * @param endWidth
	 *            终止波形宽度
	 * @param color
	 *            波形颜色
	 */
	public void setWaveInfo(float intervalSize, float stireStep,
			float startWidth, float endWidth, int color) {
		mWaveIntervalSize = intervalSize;
		mStirStep = stireStep;
		mWaveStartWidth = startWidth;
		mWaveEndWidth = endWidth;
		setWaveColor(color);
		resetWave();
	}

	/**
	 * 设置波形颜色
	 * 
	 * @param color
	 */
	public void setWaveColor(int color) {
		mWaveColor = color;
		mWaveCenterShapePaint.setColor(mWaveColor);
	}

}
