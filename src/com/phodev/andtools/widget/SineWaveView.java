package com.phodev.andtools.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * sin 波形图
 * 
 * @author kaige
 * 
 */
public class SineWaveView extends View {
	private Paint mPaint = null;
	private float amplifier = 100.0f;

	public SineWaveView(Context context) {
		super(context);
		init();
	}

	public SineWaveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(5);
	}

	Paint saveLayerPaint = new Paint();
	{
		saveLayerPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
	}
	Paint mPaint2 = new Paint();
	{
		mPaint2.setAntiAlias(true);
		mPaint2.setColor(Color.RED & 0x00ffffff | 0x18000000);
		mPaint2.setStrokeWidth(5);
	}
	private int hope_wave_with = 400;
	private float frequency2 = getFrequencyByWaveWith(hope_wave_with);
	private float dlStartX, dlStartY, dlStopX, dlStopY;// draw line xy
	private float mXCoordinateStart = 100;// x坐标系开始位置
	private float mYCoordinateStart = 600;// y坐标系开始位置

	public float getFrequencyByWaveWith(int with) {
		// float p = (float) (2 * Math.PI / width);//method 1
		// float p = (float) (Math.PI / hope_wave_with);//method 2
		return (float) (Math.PI / with);
	}

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.BLACK);
		// amplifier = (amplifier * 2 > height) ? (height / 2) : amplifier;
		canvas.translate(mXCoordinateStart, mYCoordinateStart);
		for (int i = 0; i < hope_wave_with; i++) {
			dlStartX = i;
			// y=sin(初相+频率*x);
			dlStartY = amplifier * (float) (Math.sin(frequency2 * dlStartX));
			//
			dlStopX = (float) (i + 1);
			// y=sin(初相+频率*x);
			dlStopY = amplifier * (float) (Math.sin(frequency2 * dlStopX));
			//
			canvas.drawLine(dlStartX, dlStartY, dlStopX, dlStopY, mPaint);
			canvas.drawLine(dlStartX, dlStartY, dlStartX, 0, mPaint2);
		}
		if (add) {
			amplifier += 8f;
		} else {
			amplifier -= 8f;
		}
		if (isRunning) {
			postInvalidateDelayed(1000 / 80);
		}
	}

	private boolean isRunning = false;
	boolean add = true;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isRunning) {
			isRunning = false;
		} else {
			isRunning = true;
			add = !add;
			invalidate();
		}
		return super.onTouchEvent(event);
	}

}
