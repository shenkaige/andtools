package com.phodev.andtools.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 半圆进度条
 * 
 * @author skg
 */
public class SemicircleProgressBar extends View {
	private float mProgress;
	private String mApexStartTitle;
	private String mApexEndTitle;
	private String mDisableBlockStartTitle;
	private String mDisableBlockEndTitle;
	private float mDisableStart;
	private float mDisableEnd;
	private float mBlockTitleMargin;
	private float mApexTitleMargin;
	private int mIndicatorColor;
	private int mIndicatorBgColor;
	private float mIndicatorRadius;
	private float mIndicatorBgRadius;
	private float mProgressRadius;
	private float mProgressStorkeWidth;
	private float mDisableStartAngle;
	private float mDisableSweepAngle;
	//
	private final TextPaint mPaintTitle = new TextPaint();
	private final TextPaint mPaintSubTitle = new TextPaint();
	private final Paint mPaintProgressBg = new Paint();
	private final Paint mPaintCircleEnd = new Paint();
	private final Paint mPaintIndicator = new Paint();
	private final Paint mPaintDisableBlock = new Paint();
	private final RectF mCircleRect = new RectF();

	public SemicircleProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SemicircleProgressBar(Context context) {
		super(context);
		init();
	}

	private void init() {
		mPaintIndicator.setAntiAlias(true);
		//
		mPaintDisableBlock.setAntiAlias(true);
		mPaintDisableBlock.setStyle(Style.STROKE);
		//
		mPaintProgressBg.setAntiAlias(true);
		mPaintProgressBg.setStyle(Style.STROKE);
		//
		mPaintCircleEnd.setAntiAlias(true);
		mPaintCircleEnd.setStyle(Style.FILL);
		//
		mPaintTitle.setAntiAlias(true);
		mPaintSubTitle.setAntiAlias(true);
		//
		setProgressWidth(40);
		setProgressColor(0x44FFFFFF);
		setTitleColor(0xFFB1ADBE);
		setTitleTextSize(50);
		setSubTitleColor(0xFFB1ADBE);
		setSubTitleTextSize(20);
		setProgressTitle("0", "100");
		setDisableBlock(0.1f, 0.2f);
		setDisableBlockTitle("10%", "20%");
		setDisableBlockColor(0xFF37295e);
		setProgressTitleMargin(10);
		setDisableBlockTitleMargin(30);
		setProgressIndicator(0xFFff9c00, 35, 0x22FFFFFF, 50);
	}

	private int mProgressColor;
	private int mProgressAlpha;

	public void setProgressIndicator(int color, float radius, int bgColor, float bgRadius) {
		mIndicatorColor = color;
		mIndicatorBgColor = bgColor;
		mIndicatorRadius = radius;
		mIndicatorBgRadius = bgRadius;
	}

	public void setProgressWidth(float with) {
		mProgressStorkeWidth = with;
		mPaintDisableBlock.setStrokeWidth(mProgressStorkeWidth);
		mPaintProgressBg.setStrokeWidth(mProgressStorkeWidth);
	}

	public void setProgressColor(int color) {
		mProgressColor = color | 0xFF000000;
		mProgressAlpha = color >>> 24;
		//
		mPaintProgressBg.setColor(mProgressColor);
		mPaintCircleEnd.setColor(mProgressColor);
	}

	public void setTitleTextSize(float size) {
		mPaintTitle.setTextSize(size);
	}

	public void setSubTitleTextSize(float size) {
		mPaintSubTitle.setTextSize(size);
	}

	public void setTitleColor(int color) {
		mPaintTitle.setColor(color);
	}

	public void setSubTitleColor(int color) {
		mPaintSubTitle.setColor(color);
	}

	public void setProgressTitleMargin(float margin) {
		mApexTitleMargin = margin;
	}

	public void setProgressTitle(String startTitle, String endTitle) {
		mApexStartTitle = startTitle;
		mApexEndTitle = endTitle;
	}

	public void setDisableBlockColor(int color) {
		mPaintDisableBlock.setColor(color);
	}

	/**
	 * @param start
	 *            -1 will ignore block limit
	 * @param end
	 *            -1 will ignore block limit
	 */
	public void setDisableBlock(float start, float end) {
		mDisableStart = start;
		mDisableEnd = end;
	}

	public float getDisableBlockStart() {
		return mDisableStart;
	}

	public float getDisableBlockEnd() {
		return mDisableEnd;
	}

	public void setDisableBlockTitle(String startTitle, String endTitle) {
		mDisableBlockStartTitle = startTitle;
		mDisableBlockEndTitle = endTitle;
	}

	public void setDisableBlockTitleMargin(float margin) {
		mBlockTitleMargin = margin;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int w = getMeasuredWidth();
		final int h = getMeasuredHeight();
		final float indicatorMinOffset = Math.max(getIndicatorRadius() * 2, mProgressStorkeWidth);
		final float titleMinHeightOffset = getApexTitleHeight();
		float heightMinOffset;
		if (titleMinHeightOffset > indicatorMinOffset / 2.0f) {
			heightMinOffset = titleMinHeightOffset - indicatorMinOffset / 2.0f;
		} else {
			heightMinOffset = indicatorMinOffset;
		}
		float titleWidthOffset = 0;
		if (mApexEndTitle != null) {
			titleWidthOffset = mPaintTitle.measureText(mApexEndTitle);
		}
		if (mApexStartTitle != null) {
			titleWidthOffset = Math.max(titleWidthOffset, mPaintTitle.measureText(mApexStartTitle));
		}
		float maxH = h - heightMinOffset;
		float maxW = w - Math.max(titleWidthOffset, indicatorMinOffset);
		float r = maxW / 2.0f;
		if (maxH < r) {
			r = maxH;
		}
		mProgressRadius = r;
		//
		mCircleRect.left = (w - (r * 2)) / 2.0f;
		mCircleRect.top = (h - r - heightMinOffset) / 2.0f;
		mCircleRect.right = mCircleRect.left + (r * 2);
		mCircleRect.bottom = mCircleRect.top + (r * 2);
		//
		mDisableStartAngle = 180.0f + mDisableStart * 180.0f;
		mDisableSweepAngle = (mDisableEnd - mDisableStart) * 180.0f;
	}

	private float mIndicatorCenterX;
	private float mIndicatorCenterY;

	private final Paint paintAlphaLayer = new Paint();

	@Override
	public void draw(Canvas canvas) {
		// draw progress bar
		paintAlphaLayer.setAlpha(mProgressAlpha);
		final int alphaSave = canvas.saveLayer(0, 0, getWidth(), getHeight(), paintAlphaLayer, Canvas.ALL_SAVE_FLAG);
		canvas.drawArc(mCircleRect, 180, 180, false, mPaintProgressBg);
		canvas.drawArc(mCircleRect, mDisableStartAngle, mDisableSweepAngle, false, mPaintDisableBlock);
		// draw end circle
		float apexCircleR = mProgressStorkeWidth / 2.0f;
		float apexCircleCenterY = mCircleRect.height() / 2 + mCircleRect.top;
		canvas.drawCircle(mCircleRect.left, apexCircleCenterY, apexCircleR, mPaintCircleEnd);
		canvas.drawCircle(mCircleRect.right, apexCircleCenterY, apexCircleR, mPaintCircleEnd);
		canvas.restoreToCount(alphaSave);
		// draw progress indicator
		final float progress = getProgress();
		mIndicatorCenterX = getXByDegress(progress, mProgressRadius) + mCircleRect.left;
		mIndicatorCenterY = getYByDegress(progress, mProgressRadius) + mCircleRect.top;
		mPaintIndicator.setColor(mIndicatorBgColor);
		canvas.drawCircle(mIndicatorCenterX, mIndicatorCenterY, getIndicatorRadius(), mPaintIndicator);
		mPaintIndicator.setColor(mIndicatorColor);
		canvas.drawCircle(mIndicatorCenterX, mIndicatorCenterY, mIndicatorRadius, mPaintIndicator);
		// draw start and end title
		float apexTitleY = apexCircleCenterY + getApexTitleHeight();
		mPaintTitle.setTextAlign(Align.CENTER);
		canvas.drawText(mApexStartTitle, mCircleRect.left, apexTitleY, mPaintTitle);
		canvas.drawText(mApexEndTitle, mCircleRect.right, apexTitleY, mPaintTitle);
		// draw disable block title
		final float titleMeasureRadius = mProgressRadius - apexCircleR - mBlockTitleMargin - mPaintTitle.getTextSize();
		final float titleXOffset = mCircleRect.left + apexCircleR + mBlockTitleMargin + mPaintTitle.getTextSize();
		final float titleYOffset = mCircleRect.top;
		float sectionTitleX = getXByDegress(mDisableStart, titleMeasureRadius) + titleXOffset;
		float sectionTitleY = getYByDegress(mDisableStart, titleMeasureRadius) + titleYOffset;
		mPaintTitle.setTextAlign(Align.CENTER);
		canvas.drawText(mDisableBlockStartTitle, sectionTitleX, sectionTitleY, mPaintTitle);
		sectionTitleX = getXByDegress(mDisableEnd, titleMeasureRadius) + titleXOffset;
		sectionTitleY = getYByDegress(mDisableEnd, titleMeasureRadius) + titleYOffset;
		canvas.drawText(mDisableBlockEndTitle, sectionTitleX, sectionTitleY, mPaintTitle);
		// --debug-start
		// for (int i = 0; i < 120; i++) {
		// sectionTitleX = getXByDegress(1.0f / 60.0f * i, titleMeasureRadius) +
		// titleXOffset;
		// sectionTitleY = getYByDegress(1.0f / 60.0f * i, titleMeasureRadius) +
		// titleYOffset;
		// canvas.drawCircle(sectionTitleX, sectionTitleY, 5, paintProgress);
		// }
		// end
	}

	private float getApexTitleHeight() {
		return (mProgressStorkeWidth / 2.0f) + mApexTitleMargin + mPaintTitle.getTextSize();
	}

	private float getIndicatorRadius() {
		return Math.max(mIndicatorBgRadius, mIndicatorRadius);
	}

	private float getXByDegress(float progress, float radius) {
		float progressCX;
		if (progress > 0.5f) {
			double radians = Math.toRadians((progress - 0.5f) * 180.0f);
			progressCX = (float) (Math.sin(radians) * radius) + radius;
		} else {
			double radians = Math.toRadians(progress * 180.0f);
			progressCX = (float) (radius - Math.cos(radians) * radius);
		}
		return progressCX;
	}

	private float getYByDegress(float progress, float radius) {
		float progressCY;
		if (progress > 0.5f) {
			double radians = Math.toRadians((progress - 0.5f) * 180.0f);
			progressCY = (float) (mCircleRect.height() / 2.0f - Math.cos(radians) * radius);
		} else {
			double radians = Math.toRadians(progress * 180.0f);
			progressCY = (float) (mCircleRect.height() / 2.0f - Math.sin(radians) * radius);
		}
		return progressCY;
	}

	public float getProgress() {
		return mProgress;
	}

	/**
	 * @param progress
	 *            progress>=0.0 progress <=1.0
	 */
	public void setProgress(float progress) {
		if (mProgress == progress) {
			return;
		}
		if (progress < 0) {
			progress = 0;
		} else if (progress > 1) {
			progress = 1;
		}
		setProgressWithoutCheck(fixProgress(progress));
	}

	private void setProgressWithoutCheck(float progress) {
		mProgress = progress;
		invalidate();
		if (mProgressListener != null) {
			mProgressListener.onProgressChanged(this, mProgress);
		}
	}

	private boolean isDrag = false;

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isDrag = getDistByTowPoint(x, y, mIndicatorCenterX, mIndicatorCenterY) <= getIndicatorRadius();
			break;
		case MotionEvent.ACTION_MOVE:
			float degress = getDegressByPoint(x, y);
			setProgressWithoutCheck(degress / 180.0f);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			isDrag = false;
			checkFixProgress();
			break;
		}
		if (isDrag) {
			return true;
		} else {
			return super.dispatchTouchEvent(event);
		}
	}

	private void checkFixProgress() {
		float d = fixProgress(mProgress);
		if (d == mProgress) {
			return;
		} else {
			setProgressWithoutCheck(d);
		}
	}

	private float fixProgress(float progress) {
		if (progress > mDisableStart && progress < mDisableEnd) {
			if (progress - mDisableStart > mDisableEnd - progress) {
				return mDisableEnd;
			} else {
				return mDisableStart;
			}
		}
		return progress;
	}

	private float getDegressByPoint(float x, float y) {
		final float centerX = mCircleRect.width() / 2.0f + mCircleRect.left;
		final float centerY = mCircleRect.height() / 2.0f + mCircleRect.top;
		if (y > centerY) {
			y = centerY;
		}
		float dx = Math.abs(x - centerX);
		float dy = Math.abs(y - centerY);
		if (x == centerX) {
			return 90.0f;
		} else if (x < centerX) {
			return (float) Math.toDegrees(Math.atan(dy / dx));
		} else {
			return 180.0f - (float) Math.toDegrees(Math.atan(dy / dx));
		}
	}

	private float getDistByTowPoint(float x, float y, float x2, float y2) {
		float dx = x - x2;
		float dy = y - y2;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	public interface OnProgressChangeListener {
		public void onProgressChanged(SemicircleProgressBar progressBar, float progress);
	}

	private OnProgressChangeListener mProgressListener;

	public void setOnProgressChangeListener(OnProgressChangeListener l) {
		mProgressListener = l;
	}
}
