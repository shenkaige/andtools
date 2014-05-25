package com.phodev.andtools.wifianalyse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.phodev.andtools.wifianalyse.WifiChannels.Channel;
import com.phodev.andtools.wifianalyse.WifiChannels.ChannelList;

/**
 * sin 波形图
 * 
 * @author sky
 * 
 */
public class WifiChannelView extends View {
	private final static float SINE_WAVE_ACCURACY = 0.6f;
	private final static float HIGHLIGHT_SINE_WAVE_MULTIPLE = 1.8f;// 高亮sin先的宽度倍数
	// 正弦波paint
	private final Paint mSineWavePaint = new Paint();
	{
		mSineWavePaint.setAntiAlias(true);
		mSineWavePaint.setColor(Color.RED);
		mSineWavePaint.setStrokeWidth(5);
	}
	// 正弦波填充paint
	private final Paint mSineFillPaint = new Paint();
	{
		mSineFillPaint.setAntiAlias(true);
		mSineFillPaint.setColor(toFillColor(Color.RED));
		mSineFillPaint.setStrokeWidth(1);
	}
	// 表格线paint
	private final Paint mTablePaint = new Paint();
	{
		mTablePaint.setColor(Color.WHITE);
		mTablePaint.setStrokeWidth(1);
	}
	// 表格文字
	private final Paint mTableTextPaint = new Paint();
	{
		mTableTextPaint.setTextSize(getRawTextSizeFromSp(12));
		mTableTextPaint.setColor(Color.WHITE);
		mTableTextPaint.setAntiAlias(true);
		mTableTextPaint.setTextAlign(Align.CENTER);
	}
	// 表格行paint
	private final Paint mRowsPaint = new Paint();
	{
		mRowsPaint.setStyle(Paint.Style.STROKE);
		mRowsPaint.setStrokeWidth(1);
		mRowsPaint.setColor(Color.WHITE);
		int itv = (int) (this.getResources().getDisplayMetrics().density * 2f);
		PathEffect effects = new DashPathEffect(new float[] { itv, itv }, 0);
		mRowsPaint.setPathEffect(effects);
	}
	private final static float ssid_text_size = 14f;
	private final TextPaint ssidPaint = new TextPaint();
	{
		ssidPaint.setTextSize(getRawTextSizeFromSp(ssid_text_size));
		ssidPaint.setAntiAlias(true);
	}

	public WifiChannelView(Context context) {
		super(context);
		init();
	}

	public WifiChannelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private String signalStrengthName;
	private String signalName;

	private void init() {
		signalStrengthName = WifiChannels.getSignalStrengthName(getContext());
		signalName = WifiChannels.getSignalName(getContext());
	}

	private float getRawTextSizeFromSp(float spSize) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spSize,
				getResources().getDisplayMetrics());
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int l = 120;
		int t = 20;
		int r = getWidth() - 20;
		int b = getHeight() - 80;
		sin_table_rect.set(l, t, r, b);
		// 确定表格边界，确定sin波形宽度
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//
		canvas.drawColor(Color.BLACK);
		drawXYCoordinate(canvas);
		int count = canvas.save();
		float lineWidthOffset = mTablePaint.getStrokeWidth();
		if (lineWidthOffset <= 0) {
			lineWidthOffset = 1;
		}
		lineWidthOffset /= 2;
		canvas.clipRect(//
				sin_table_rect.left,//
				sin_table_rect.top,//
				sin_table_rect.right,//
				sin_table_rect.bottom);
		canvas.translate(sin_table_rect.left + lineWidthOffset,
				sin_table_rect.bottom - lineWidthOffset);
		drawWifiChannels(canvas);
		canvas.restoreToCount(count);
	}

	private final Path table_row_temp_path = new Path();
	private final Rect sin_table_rect = new Rect();
	private final static int wave_hz_width = 20;// 单位hz

	private void drawXYCoordinate(Canvas canvas) {
		int w = sin_table_rect.width();
		//
		int tl = sin_table_rect.left;
		int tt = sin_table_rect.top;
		int tr = sin_table_rect.right;
		int tb = sin_table_rect.bottom;
		//
		canvas.drawLine(tl, tb, tl, tt, mTablePaint);// left
		canvas.drawLine(tl, tt, tr, tt, mTablePaint);// top
		canvas.drawLine(tr, tt, tr, tb, mTablePaint);// right
		canvas.drawLine(tr, tb, tl, tb, mTablePaint);// bottom
		//
		drawTableRows(canvas, sin_table_rect);
		//
		canvas.drawText(signalName, canvas.getWidth() / 2f, tb
				+ mTableTextPaint.getTextSize() * 2 + 5, mTableTextPaint);
		//
		int c_count = canvas.save();
		canvas.rotate(-90, canvas.getWidth() / 2f, canvas.getHeight() / 2f);
		canvas.drawText(signalStrengthName, canvas.getWidth() / 2f,
				canvas.getHeight() / 2f - canvas.getWidth() / 2f
						+ mTableTextPaint.getTextSize(), mTableTextPaint);
		canvas.restoreToCount(c_count);
		//
		ChannelList list = WifiChannels.GHz_2400;
		Channel channelFirst = list.get(0);
		Channel channelEnd = list.get(list.getSize() - 1);
		currentMinHz = channelFirst.hz;
		int delta = channelEnd.hz - channelFirst.hz;
		//
		delta += wave_hz_width;
		hz_to_px_unit = (float) (w) / delta;
		hope_wave_width = (int) (hz_to_px_unit * wave_hz_width);
		frequency = getFrequencyByWaveWith(hope_wave_width);
		firstChannelLeftInTable = wave_hz_width / 2f * hz_to_px_unit;
		float offsetLeft = tl + firstChannelLeftInTable;
		int lastHz = channelFirst.hz;
		for (int i = 0; i < list.getSize(); i++) {
			Channel c = list.get(i);
			offsetLeft += (c.hz - lastHz) * hz_to_px_unit;
			canvas.drawText(String.valueOf(c.channel), offsetLeft,
					sin_table_rect.bottom + mTableTextPaint.getTextSize(),
					mTableTextPaint);
			lastHz = c.hz;
		}
	}

	private void drawTableRows(Canvas canvas, final Rect tableRect) {
		int h = tableRect.height();
		//
		int tl = tableRect.left;
		int tt = tableRect.top;
		int tr = tableRect.right;
		// int tb = tableRect.bottom;
		//
		int signalCount = WifiChannels.CHANNEL_SIGNAL.getSize();
		float step = h / (signalCount + 1f);
		Align oldAlign = mTableTextPaint.getTextAlign();
		mTableTextPaint.setTextAlign(Align.RIGHT);
		float textLeftOffset = -mTableTextPaint.getTextSize() * 0.25f;
		float textCenterY = (mTableTextPaint.getTextSize() - mTableTextPaint
				.descent()) / 2f;
		Log.i("ttt", "mTableTextPaint size:" + mTableTextPaint.getTextSize()
				+ ",descent:" + mTableTextPaint.descent() + ",ascent:"
				+ mTableTextPaint.ascent());
		for (int i = 1; i <= signalCount; i++) {
			// canvas.drawLine(10, 10 + step * i, w - 10, 10 + step * i, paint);
			table_row_temp_path.reset();
			table_row_temp_path.moveTo(tl, tt + step * i);
			table_row_temp_path.lineTo(tr, tt + step * i);
			canvas.drawPath(table_row_temp_path, mRowsPaint);
			String singal = String.valueOf(WifiChannels.CHANNEL_SIGNAL
					.get(i - 1));
			canvas.drawText(singal, textLeftOffset + tl, tt + step * i
					+ textCenterY, mTableTextPaint);
		}
		//
		mTableTextPaint.setTextAlign(oldAlign);
	}

	private void drawWifiChannels(Canvas canvas) {
		if (wifiEntitys == null || wifiEntitys.isEmpty()) {
			return;
		}
		// amplifier = (amplifier * 2 > height) ? (height / 2) : amplifier;

		int size = wifiEntitys.size();
		for (int i = 0; i < size; i++) {
			int c_count = canvas.save();
			WifiDrawEntity en = wifiEntitys.get(i);
			canvas.translate((en.frequency - currentMinHz) * hz_to_px_unit, 0);
			drawChannel(canvas, en);
			canvas.restoreToCount(c_count);
		}
	}

	private int hope_wave_width;
	private float hz_to_px_unit;// hz 转换到px的单位
	private float currentMinHz;// 第一个hz也就是最小的一个
	private float firstChannelLeftInTable;
	// 决定sin波形的宽度
	private float frequency;
	//
	private static final int SINE_WAVE_FILL_ALPHA_MASK = 0x18000000;

	// public boolean isFiveG_ZH_mode = false;
	//
	// public void setGZHMode(boolean fiveGZH_Mode) {
	// isFiveG_ZH_mode = fiveGZH_Mode;
	// invalidate();
	// }

	/**
	 * 转换填充颜色
	 */
	private int toFillColor(int color) {
		return color & 0x00ffffff | SINE_WAVE_FILL_ALPHA_MASK;
	}

	private void drawChannel(Canvas canvas, WifiDrawEntity e) {
		int color = getWifiWaveColor(e);
		float lineWidth = e.getDrawLineWidth();
		// height range[0-table height]
		float perUnit = sin_table_rect.height() / 80f;
		float amplifier = -(100 + e.curLevel) * perUnit;
		Log.e("ttt", "level:" + e.curLevel + ",ssid:" + e.SSID);
		mSineWavePaint.setColor(color);
		if (e.drawIsLight) {
			mSineWavePaint.setStrokeWidth(lineWidth
					* HIGHLIGHT_SINE_WAVE_MULTIPLE);
		} else {
			mSineWavePaint.setStrokeWidth(lineWidth);
		}
		//
		mSineFillPaint.setColor(toFillColor(color));
		//
		float startX, startY, stopX, stopY;// draw line xy
		final float end = hope_wave_width - SINE_WAVE_ACCURACY;
		for (float i = 0; i < end; i += SINE_WAVE_ACCURACY) {
			startX = i;
			// y=sin(初相+频率*x);
			startY = amplifier * (float) (Math.sin(frequency * startX));
			//
			stopX = (float) (i + 1);
			// y=sin(初相+频率*x);
			stopY = amplifier * (float) (Math.sin(frequency * stopX));
			//
			canvas.drawLine(startX, startY, stopX, stopY, mSineWavePaint);
			canvas.drawLine(startX, startY, startX, 0, mSineFillPaint);
		}
		ssidPaint.setColor(mSineWavePaint.getColor());
		//
		float maxHightX = hope_wave_width / 2;
		float maxHightY = amplifier * (float) (Math.sin(frequency * maxHightX));
		String text = e.SSID;
		if (text == null) {
			text = "*";
		}
		float textWidth = ssidPaint.measureText(text);
		canvas.drawText(text, maxHightX - textWidth / 2,//
				maxHightY - lineWidth / 2 - ssidPaint.descent(), //
				ssidPaint);
		// Log.e("ttt", "text paint,ascent:" + ssidPaint.ascent() + ",descent:"
		// + ssidPaint.descent() + ",text density:" + ssidPaint.density);
	}

	public float getFrequencyByWaveWith(int with) {
		// float p = (float) (2 * Math.PI / width);//method 1
		// float p = (float) (Math.PI / hope_wave_with);//method 2
		return (float) (Math.PI / with);
	}

	private final List<WifiDrawEntity> wifiEntitys = new ArrayList<WifiDrawEntity>();
	private HashMap<String, Integer> wifiColorMap = new HashMap<String, Integer>();
	private int colorCursor = 0;
	private final int colors[] = new int[] {
			//
			0xFFFF0000,//
			0xFF0000FF,//
			0xFF00FFFF,//
			0xFF00FF00,//
			0xFFFFFF00,//
			0xFF888888,//
			0xFFFF80FF,//
			0xFF8000FF,//
			0xFFFF7F00,//
			0xFFFF7F7F,//
			0xFFF319A7,//
			0xFF0024FF,//
			0xFF97B200 };

	private int getWifiWaveColor(WifiDrawEntity e) {
		if (e == null) {
			return Color.TRANSPARENT;
		}
		if (e.drawColor > 0) {
			return e.drawColor;
		} else {
			Integer color = wifiColorMap.get(e.BSSID);
			if (color == null) {
				if (colorCursor >= colors.length || colorCursor < 0) {
					colorCursor = 0;
				}
				color = colors[colorCursor];
				colorCursor++;
				wifiColorMap.put(e.BSSID, color);
			}
			return color;
		}
	}

	public void refreshScanResult(List<WifiDrawEntity> results) {
		//暂时不考虑动画，所以要先清除所有记录
		wifiEntitys.clear();
		//
		wifiEntitys.addAll(results);
		for (WifiDrawEntity wde : results) {
			if (wde == null)
				continue;
			WifiDrawEntity old = findEntity(wde.BSSID);
			if (old == null) {
				wifiEntitys.add(wde);
			} else {
				int oldLevel = old.curLevel;
				old.reset(wde);
				old.levelBeforLastChnaged = oldLevel;
			}
		}
		Collections.sort(wifiEntitys, wifiEntitysDrawComparator);
		invalidate();
	}

	private WifiDrawEntity findEntity(String bssid) {
		if (bssid == null) {
			return null;
		}
		if (wifiEntitys == null || wifiEntitys.isEmpty()) {
			return null;
		}
		for (WifiDrawEntity wde : wifiEntitys) {
			if (wde == null) {
				continue;
			}
			if (bssid.equals(wde.BSSID)) {
				return wde;
			}
		}
		return null;
	}

	private Comparator<WifiDrawEntity> wifiEntitysDrawComparator = new Comparator<WifiDrawEntity>() {

		@Override
		public int compare(WifiDrawEntity lhs, WifiDrawEntity rhs) {
			return rhs.curLevel - lhs.curLevel;
		}

	};
}
