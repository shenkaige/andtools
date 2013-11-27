package com.phodev.andtools.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.phodev.andtools.R;
import com.phodev.andtools.common.CommonParam;

/**
 * 从右边出现的SlidingMenu
 * 
 * @author sky
 * 
 */
public class RightSlidingMenu extends ViewGroup {
	public static final String TAG = "RightSlidingMenu";
	//
	private View menuView;
	private View contentView;
	private boolean mIsMenuShow;

	public RightSlidingMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RightSlidingMenu(Context context) {
		super(context);
		init();
	}

	private void init() {
		setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		menuView = findViewById(R.id.right_sliding_menu);
		contentView = findViewById(R.id.right_sliding_content);
	}

	public void setContentView(View contentView, View menuView) {
		this.contentView = contentView;
		this.menuView = menuView;
		invalidate();
	}

	private int followOffsetLeft;
	private int downX;
	private int downY;
	private boolean lockSlid = false;
	private boolean needCheckLockSlid;
	private final static float lock_slid_threshold = 1.1f;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		//
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX = x;
			downY = y;
			followOffsetLeft = getScrollX() + x;
			needCheckLockSlid = true;
			break;
		case MotionEvent.ACTION_MOVE:
			if (needCheckLockSlid) {
				int xDelta = Math.abs(x - downX);
				int yDelta = Math.abs(y - downY);
				if (yDelta <= 0) {
					lockSlid = true;
				} else {
					lockSlid = (float) xDelta / yDelta >= lock_slid_threshold;
				}
				needCheckLockSlid = false;
			}
			if (lockSlid) {
				follow(x);
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			finishFollow();
			lockSlid = false;
			break;
		}
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		int l = 0;
		int r = 0;
		if (contentView != null) {
			r = contentView.getMeasuredWidth() + l;
			contentView.layout(l, 0, r, contentView.getMeasuredHeight());
		}
		if (menuView != null) {
			l = r;
			r = l + menuView.getMeasuredWidth();
			menuView.layout(l, 0, r, menuView.getMeasuredHeight());
		}
	}

	private int dismessScrollX;
	private int showScrollX;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (menuView == null || contentView == null) {
			throw new IllegalArgumentException(
					"menuView or contentView can not be null in:"
							+ getClass().getName());
		}
		dismessScrollX = 0;
		measureChild(menuView, widthMeasureSpec, heightMeasureSpec);
		showScrollX = menuView.getMeasuredWidth();
		//
		int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
		int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
		if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
			widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth,
					MeasureSpec.EXACTLY);
		}
		if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight,
					MeasureSpec.EXACTLY);
		}
		//
		contentView.measure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(maxWidth, maxHeight);
	}

	private void refreshScroll(boolean smooth) {
		if (smooth) {
			if (isMenuShow()) {
				smoothScroll(getScrollX(), showScrollX);
			} else {
				smoothScroll(getScrollX(), dismessScrollX);
			}
		} else {
			if (isMenuShow()) {
				scrollTo(showScrollX, getScrollY());
			} else {
				scrollTo(dismessScrollX, getScrollY());
			}
		}
	}

	public void follow(int touchX) {
		innerFollow(-touchX + followOffsetLeft);
	}

	/** menu显示的宽度超过REQUEST_SHOW_FACTOR*menu width则同意显示，否则RollBack */
	private final float REQUEST_SHOW_FACTOR = 0.15f;
	/** menu显示的宽度小于REQUEST_DISMISS_FACTOR*menu width则同意隐藏，否则RollBack */
	private final float REQUEST_DISMISS_FACTOR = 0.85f;

	private void finishFollow() {
		if (CommonParam.DEBUG) {
			log("finishFollow is menu show:" + isMenuShow());
		}
		int menuWidth = menuView.getWidth();
		int fromX = getScrollX();
		int toX = dismessScrollX;
		int menuShowLength = getScrollX();
		float factor;
		if (isMenuShow()) {
			// request dismiss
			factor = REQUEST_DISMISS_FACTOR;
		} else {
			// request show
			factor = REQUEST_SHOW_FACTOR;
		}
		// Log.e("ttt", "show length:" + menuShowLength + ",f s l:"
		// + (menuWidth * factor) + ",menuWidth:" + menuWidth + ",factor:"
		// + factor + ",isShow:" + isMenuShow());
		if (menuShowLength > (menuWidth * factor)) {
			// show
			toX = showScrollX;
			setMenuShowFlag(true);
		} else {
			// dismiss
			toX = dismessScrollX;
			setMenuShowFlag(false);
		}
		smoothScroll(fromX, toX);
	}

	private void smoothScroll(int fromX, int toX) {
		if (mScrollRunnable != null) {
			mScrollRunnable.stop();
		}
		mScrollRunnable = new ScrollRunnable(this, fromX, toX);
		post(mScrollRunnable);
	}

	private void innerFollow(int x) {
		scrollTo(checkScrollX(x), getScrollY());
	}

	protected int checkScrollX(int newScrollX) {
		if (newScrollX < dismessScrollX) {
			newScrollX = dismessScrollX;
		} else if (newScrollX > showScrollX) {
			newScrollX = showScrollX;
		}
		return newScrollX;
	}

	private ScrollRunnable mScrollRunnable;
	//
	private static final int ANIMATION_DURATION_MS = 190;// 动画持续时间
	private static final int ANIMATION_FPS = 1000 / 60;// Frame Per second

	final class ScrollRunnable implements Runnable {
		private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
		private final int mScrollToX;
		private final int mScrollFromX;
		private final View mTargetView;

		private boolean mContinueRunning = true;
		private long mStartTime = -1;
		private int mCurrentX = -1;

		public ScrollRunnable(View targetView, int fromX, int toX) {
			mTargetView = targetView;
			mScrollFromX = fromX;
			mScrollToX = toX;
		}

		@Override
		public void run() {
			if (mStartTime == -1) {
				mStartTime = System.currentTimeMillis();
			} else {
				long normalizedTime = (1000 * (System.currentTimeMillis() - mStartTime))
						/ ANIMATION_DURATION_MS;
				normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

				final int deltaY = Math.round((mScrollFromX - mScrollToX)
						* mInterpolator
								.getInterpolation(normalizedTime / 1000f));
				mCurrentX = mScrollFromX - deltaY;
				mTargetView.scrollTo(mCurrentX, 0);
			}
			if (mContinueRunning) {
				if (mScrollToX == mCurrentX) {
					onScrollEnd();
					stop();
				} else {
					mTargetView.postDelayed(this, ANIMATION_FPS);
				}
			}
		}

		public void stop() {
			mContinueRunning = false;
			mTargetView.removeCallbacks(this);
		}
	}

	/**
	 * 判断Menu是否实现
	 * 
	 * @return
	 */
	public boolean isMenuShow() {
		return mIsMenuShow;
	}

	/**
	 * 隐藏Menu
	 * 
	 * @param smooth
	 */
	public void hideMenu(boolean smooth) {
		if (isMenuShow()) {
			setMenuShowFlag(false);
			refreshScroll(smooth);
		}
	}

	/**
	 * 显示Menu
	 * 
	 * @param smooth
	 */
	public void showMenu(boolean smooth) {
		if (!isMenuShow()) {
			setMenuShowFlag(true);
			refreshScroll(smooth);
		}
	}

	private void setMenuShowFlag(boolean showState) {
		if (showState == mIsMenuShow) {
			return;
		}
		mIsMenuShow = showState;
		notifyMenuShowChanged();
	}

	private void onScrollEnd() {
		if (mActionListener != null) {
			mActionListener.onAnimationEnd(this, isShown());
		}
	}

	private void notifyMenuShowChanged() {
		if (mActionListener != null) {
			mActionListener.onMenuShowChanged(this, isMenuShow());
		}
	}

	private MenuListener mActionListener;

	public void setMenuListener(MenuListener listener) {
		this.mActionListener = listener;
	}

	public interface MenuListener {
		public void onAnimationEnd(RightSlidingMenu v, boolean isMenuShow);

		public void onMenuShowChanged(RightSlidingMenu v, boolean isMenuShow);
	}

	void log(Object msg) {
		Log.d(TAG, "" + msg);
	}
}
