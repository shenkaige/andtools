package com.phodev.andtools.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * 垂直滚动的TextView
 * 
 * @author sky
 * 
 */
public class VerticalMarqueeTextview extends TextView {

	public VerticalMarqueeTextview(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VerticalMarqueeTextview(Context context) {
		super(context);
	}

	private int maxScrollY;

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		initMaxScrollAndCheckFrezon();
	}

	private void checkEnoughToMarquee() {
		isEnoughToMarquee = maxScrollY > getHeight();
		// Log.e("ttt", "isEnoughToMarquee:" + isEnoughToMarquee +
		// ",maxScrollY:"
		// + maxScrollY + ",vh:" + getHeight() + ",line count:"
		// + getLineCount() + ",line height:" + getLineHeight());
	}

	private boolean isFrozenFromWindowFocusChanged = Boolean.FALSE;
	private boolean isFrozenFromVisible = Boolean.FALSE;
	//
	private boolean hasAttachedToWindow;
	private boolean isEnoughToMarquee = false;

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		// 解决锁屏，但是visible依然是可见的问题
		if (hasWindowFocus) {
			if (isFrozenFromWindowFocusChanged) {
				isFrozenFromWindowFocusChanged = false;
				checkFrozen();
			}
		} else {// 锁屏的时候，我們也希望凍結
			isFrozen = true;
			isFrozenFromWindowFocusChanged = true;
		}
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (visibility == View.VISIBLE) {
			if (isFrozenFromVisible) {
				isFrozenFromVisible = false;
				checkFrozen();
			}
		} else {
			isFrozen = true;
			isFrozenFromVisible = true;
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		hasAttachedToWindow = true;
		checkFrozen();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		hasAttachedToWindow = false;
		checkFrozen();
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
		initMaxScrollAndCheckFrezon();
	}

	private void checkFrozen() {
		boolean oldIsFronze = isFrozen;
		isFrozen = !(isEnoughToMarquee && hasAttachedToWindow
				&& !isFrozenFromVisible && !isFrozenFromWindowFocusChanged);
		// Log.e("ttt", "on checkFrozen,isFrozen:" + isFrozen + ",oldIsFronze:"
		// + oldIsFronze + ",hasAttachedToWindow:" + hasAttachedToWindow
		// + ",viewIsVisible:" + isFrozenFromVisible);
		if (oldIsFronze) {// 需要还原
			if (marqueeModel == MarqueeModel.AUTO_ON_VISIBLE) {
				if (isMarquee) {
					checkScroll();
				} else {
					startMarquee();
				}
			} else if (isMarquee) {
				checkScroll();
			}
		}
	}

	private final int FPS = 1000 / 50;
	private final int SPEED = 1;// per step move PX
	private boolean isFrozen = true;

	private void checkScroll() {
		if (isMarquee && !isFrozen) {
			postDelayed(scrollTextRunnable, FPS);
		}
	}

	private void initMaxScrollAndCheckFrezon() {
		post(makeMaxScrollAndCheckFrezon);
	}

	final Runnable makeMaxScrollAndCheckFrezon = new Runnable() {

		@Override
		public void run() {
			maxScrollY = getLineCount() * getLineHeight();
			checkEnoughToMarquee();
			checkFrozen();
		}
	};
	final Runnable scrollTextRunnable = new Runnable() {
		@Override
		public void run() {
			if (isMarquee && !isFrozen) {
				scrollBy(0, SPEED);
				int scrollY = getScrollY();
				if (scrollY > maxScrollY) {
					scrollTo(0, -getHeight());
				}
				checkScroll();
			}
		}
	};

	private boolean isMarquee = false;

	public void startMarquee() {
		if (isMarquee) {
			return;
		}
		isMarquee = true;
		checkScroll();
	}

	/**
	 * 重置滚动位置，但是并不会停止Marquee
	 */
	public void resetMarqueeLocation() {
		scrollTo(0, 0);
	}

	public void stopMarquee() {
		isMarquee = false;
		removeCallbacks(scrollTextRunnable);
	}

	/** 运行中 */
	public static int STATE_MARQUEE_RUNNING = 1;
	/** 没有运行 */
	public static int STATE_MARQUEE_STOPED = 2;
	/** 因为不可见所以冻结了,该状态会自动还原 */
	public static int STATE_FROZEN = 3;
	/** 没有足够的长度可以跑马灯 */
	public static int STATE_NO_ENOUGH_LENGTH = 4;

	public int getMarqueeState() {
		if (isFrozen) {
			return STATE_FROZEN;
		}
		if (!isEnoughToMarquee) {
			return STATE_NO_ENOUGH_LENGTH;
		}
		if (isMarquee) {
			return STATE_MARQUEE_RUNNING;
		} else {
			return STATE_MARQUEE_STOPED;
		}
	}

	private MarqueeModel marqueeModel = MarqueeModel.AUTO_ON_VISIBLE;

	/**
	 * 设置滚动模式
	 * 
	 * @param model
	 */
	public void setMarqueeModel(MarqueeModel model) {
		marqueeModel = model;
		if (marqueeModel == null) {
			marqueeModel = MarqueeModel.AUTO_ON_VISIBLE;
		}
		switch (marqueeModel) {
		case AUTO_ON_VISIBLE:
			if (getVisibility() == View.VISIBLE) {
				startMarquee();
			} else {
				stopMarquee();
			}
			break;
		case MANUAL:
			break;
		}
	}

	public enum MarqueeModel {
		/** 自动控制 */
		AUTO_ON_VISIBLE,
		/** 手动 */
		MANUAL
	}
}
