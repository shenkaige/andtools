package com.phodev.andtools.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * 选择类别的view
 * 
 * <pre>
 * </pre>
 * 
 * @author sky
 *
 */
public class SlideSelectView extends TextView {
	private float lineSpace;

	public SlideSelectView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setSlideSelectListener(new SlideSelectListener() {
			int lastIndex;

			@Override
			public void onSlideSelectedChanged(String content, int index) {
				if (lastIndex != index) {
					Log.e("ttt", "selected content:" + content);
				}
				lastIndex = index;
			}

			@Override
			public void onSlideSelectStateChanged(boolean isSelecting) {

			}
		});
	}

	public SlideSelectView(Context context) {
		super(context);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (itemStrs != null && itemStrs.length > 0) {
			int lineCount = itemStrs.length;
			float sizeW = getWidth() - getPaddingLeft() - getPaddingRight();
			float validH = getHeight() - getPaddingTop() - getPaddingBottom();
			float sizeH = (float) validH / (float) lineCount;
			setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.min(sizeW, sizeH));
			if (sizeW < sizeH && lineCount > 1) {
				lineSpace = (validH - sizeW * lineCount) / (lineCount - 1);
			} else {
				lineSpace = 0;
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (itemStrs == null || itemStrs.length == 0) {
			return;
		}
		Paint tp = getPaint();
		if (tp.getTextAlign() != Align.CENTER) {
			tp.setTextAlign(Align.CENTER);
		}
		if (tp.getTypeface() != getTypeface()) {
			tp.setTypeface(getTypeface());
		}
		tp.setColor(getTextColors().getDefaultColor());
		float textSize = getTextSize();
		float centerX = getWidth() / 2f;
		float topOffset = textSize + getPaddingTop();
		for (int i = 0; i < itemStrs.length; i++) {
			canvas.drawText(itemStrs[i], centerX, topOffset, tp);
			topOffset += textSize + lineSpace;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
			throw new IllegalArgumentException(this.getClass()
					.getCanonicalName()
					+ " view must declare exactly width like 30dp or 60dp...");
		}
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
				getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			checkSelectingState(true);
		case MotionEvent.ACTION_MOVE:
			checkMapingIndex(event.getX(), event.getY());
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			checkSelectingState(false);
			break;
		}
		super.dispatchTouchEvent(event);
		return true;
	}

	private void checkMapingIndex(float x, float y) {
		if (mSelectListener == null || itemStrs == null || itemStrs.length <= 0) {
			return;
		}
		float topOffset = getTextSize() + getPaddingTop();
		int lineIndex = 0;
		if (y < topOffset) {
			lineIndex = 0;
		} else {
			lineIndex = (int) Math.ceil((y - topOffset)
					/ (getTextSize() + lineSpace));
		}
		if (lineIndex < 0) {
			lineIndex = 0;
		} else if (lineIndex >= itemStrs.length) {
			lineIndex = itemStrs.length - 1;
		}
		mSelectListener.onSlideSelectedChanged(itemStrs[lineIndex], lineIndex);
	}

	private void checkSelectingState(boolean isSelecting) {
		if (mIsSelecting == isSelecting) {
			return;
		}
		setPressed(isSelecting);
		mIsSelecting = isSelecting;
		if (mSelectListener != null) {
			mSelectListener.onSlideSelectStateChanged(mIsSelecting);
		}
	}

	private String[] itemStrs;

	public static final String ITEM_SPLITOR = "\n";

	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
		itemStrs = null;
		if (text != null) {
			String str;
			if (text instanceof String) {
				str = (String) text;
			} else {
				str = text.toString();
			}
			if (str != null && str.length() > 0) {
				if (str.indexOf(ITEM_SPLITOR) > -1) {
					itemStrs = str.split(ITEM_SPLITOR);
				} else {
					itemStrs = new String[str.length()];
					for (int i = 0; i < str.length(); i++) {
						itemStrs[i] = String.valueOf(str.charAt(i));
					}
				}
			}
		}
	}

	private boolean mIsSelecting;

	/**
	 * 是否正在选择
	 * 
	 * @return
	 */
	public boolean isSelecting() {
		return mIsSelecting;
	}

	/**
	 * 选择监听器
	 * 
	 * @author sky
	 *
	 */
	public interface SlideSelectListener {

		/**
		 * 滑动选的内容变化
		 * 
		 * @param content
		 */
		public void onSlideSelectedChanged(String content, int index);

		/**
		 * 选择器状态发生变化
		 * 
		 * @param isSelecting
		 */
		public void onSlideSelectStateChanged(boolean isSelecting);
	}

	private SlideSelectListener mSelectListener;

	public void setSlideSelectListener(SlideSelectListener l) {
		mSelectListener = l;
	}
}
