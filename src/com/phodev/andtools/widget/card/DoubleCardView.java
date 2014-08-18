package com.phodev.andtools.widget.card;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Scroller;

/**
 * 卡片组
 * 
 * <pre>
 * 该类管理一组卡片，通过默认的规则管理卡片的布局，和移动
 * 1,布局默认规则：
 * 			每张card按照顺数覆盖在前面的一张card之上，覆盖的时候要空留出前一张card的Title,如果前一张card没有title，则全部覆盖
 * 2,移动规则       :
 * 			第一card是不可以手动移动的，其余的card在移动的时候会联动前面一张card
 * </pre>
 * 
 * @author skg
 * 
 */
public class DoubleCardView extends ViewGroup {
	private final static int card_speed_factor = 4;

	public DoubleCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DoubleCardView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setOnClickListener(selfOnClickListener);
	}

	private CardView moveLockedCard = null;

	private float lastTouchX;
	private float lastTouchY;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		View v = getChildAt(getShowCardIndex());
		// boolean result = super.dispatchTouchEvent(ev);
		boolean result = v.dispatchTouchEvent(ev);
		lastTouchX = ev.getX();
		lastTouchY = ev.getY();
		//
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:// 任何情况我都需要判断是否是可拖动的
			moveLockedCard = lockMoveCard((int) ev.getX(), (int) ev.getY());
			if (moveLockedCard != null) {
				result = true;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (ignoreUnlockMoveCard) {
				ignoreUnlockMoveCard = false;
			} else {
				unlockMoveCard(moveLockedCard);
			}
			moveLockedCard = null;
			break;
		case MotionEvent.ACTION_MOVE:
			if (moveLockedCard != null) {
				int y = (int) ev.getY();
				y -= moveLockedCard.getMoveOffsetY();
				moveCard(moveLockedCard, (int) ev.getX(), y);
				return true;
			}
			break;
		}
		return result;
	}

	private boolean ignoreUnlockMoveCard = false;
	public OnClickListener selfOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int x = (int) lastTouchX;
			int y = (int) lastTouchY;
			CardView cv = (CardView) getChildAt(1);
			if (isInTitle(cv, x, y) && cv.isShowable()) {
				ignoreUnlockMoveCard = true;
				if (isTopCardShow()) {
					pushTopCardDown();
				} else {
					pushTopCardUp();
				}
			}
		}
	};

	private CardView lockMoveCard(int x, int y) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			CardView cv = (CardView) getChildAt(i);
			if (isInTitle(cv, x, y) && cv.isShowable()) {
				return cv;
			}
		}
		return null;
	}

	private boolean isInTitle(CardView cv, int x, int y) {
		if (cv == null) {
			return false;
		}
		View tv = cv.getTitleView();
		if (tv != null) {
			int l = tv.getLeft() + cv.getLeft();
			int t = tv.getTop() + cv.getTop();
			int r = tv.getRight();
			int b = tv.getBottom() + t;
			if (x > l && x < r && y > t && y < b) {
				cv.setMoveOffset(x - l, y - t);
				return true;
			}
		}
		return false;
	}

	private void unlockMoveCard(CardView moveLockedCard) {
		if (moveLockedCard != null) {
			int cardHeight = moveLockedCard.getHeight();
			int startY = moveLockedCard.getTop();
			float factor;
			if (moveLockedCard.isExpanding()) {
				factor = 0.1f;
				moveLockedCard.setExpanding(false);
			} else {
				factor = 0.9f;
				moveLockedCard.setExpanding(true);
			}
			boolean show = startY > (cardHeight * factor);
			if (show) {
				currentShowCardIndex = 1;
			} else {
				currentShowCardIndex = 0;
			}
			actionWithAnim(moveLockedCard, show);
		}
	}

	private void actionWithAnim(CardView cv, boolean show) {
		if (cv == null) {
			return;
		}
		int cardHeight = cv.getMeasuredHeight();
		int startY = cv.getTop();
		//
		int titleHeight;
		if (cv.getTitleView() == null) {
			titleHeight = 0;
		} else {
			titleHeight = cv.getTitleView().getMeasuredHeight();
		}
		//
		int distY;
		if (show) {
			distY = -startY;
		} else {
			distY = cardHeight - startY - titleHeight;
		}
		scroller.start(cv, startY, distY);
	}

	/**
	 * 移动card
	 * 
	 * @param card
	 * @param newX
	 * @param newY
	 */
	private void moveCard(CardView card, int newX, int newY) {
		if (card == null) {
			return;
		}
		int l = card.getLeft();
		int t = newY;
		int r = card.getRight();
		int b = card.getMeasuredHeight() + t;
		card.layout(l, t, r, b);
		// move 关联的card
		CardView c2 = (CardView) getChildAt(0);
		if (c2 != null) {
			l = c2.getLeft();
			t = (card.getTop() - c2.getMeasuredHeight()) / card_speed_factor;
			r = c2.getRight();
			b = t + c2.getMeasuredHeight();
			c2.layout(l, t, r, b);
		}
		invalidate();
	}

	private ScrollerRunnable scroller = new ScrollerRunnable();

	private final static int fps = 1000 / 60;
	private final static int scroll_duration = 300;

	class ScrollerRunnable implements Runnable {
		private Scroller mScroller;
		private CardView cardView;

		public ScrollerRunnable() {
			mScroller = new Scroller(getContext(),
					new AccelerateDecelerateInterpolator());
		}

		public void start(CardView cardView, int startY, int distY) {
			if (!mScroller.isFinished()) {
				return;
			}
			this.cardView = cardView;
			mScroller.startScroll(0, startY, 0, distY, scroll_duration);
			post(this);
		}

		public boolean isRunning() {
			return !mScroller.isFinished();
		}

		@Override
		public void run() {
			if (cardView == null) {
				mScroller.forceFinished(true);
				return;
			}
			if (mScroller.computeScrollOffset()) {
				moveCard(cardView, cardView.getLeft(), mScroller.getCurrY());
				postDelayed(this, fps);
			} else {
				onCardShowChanged();
			}
		}
	}

	private void onCardShowChanged() {
		if (mCardListener != null) {
			if (isTopCardShow()) {
				mCardListener.onPullUp();
			} else {
				mCardListener.onPullDown();
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
		if (childCount <= 0) {
			return;
		}
		if (childCount > 2) {
			throw new RuntimeException("we support max 2 cards now");
		}
		int maxWidht = getMeasuredWidth();
		int topOffste = 0;
		int bottom;
		for (int i = 0; i < childCount; i++) {
			CardView cv = (CardView) getChildAt(i);
			bottom = cv.getMeasuredHeight() + topOffste;
			cv.layout(0, topOffste, maxWidht, bottom);
			View title = cv.getTitleView();
			if (i == currentShowCardIndex) {
				cv.setExpanding(true);
				topOffste += bottom;
			} else {
				cv.setExpanding(false);
				if (title != null) {
					// 我们认为title一定是在CardView的最上面
					topOffste += title.getMeasuredHeight();
				}
			}
		}
	}

	private int currentShowCardIndex = 0;// 默认显示第一个

	public int getShowCardIndex() {
		return currentShowCardIndex;
	}

	public boolean isTopCardShow() {
		return currentShowCardIndex == 1;
	}

	public void pushTopCardUp() {
		currentShowCardIndex = 1;
		actionWithAnim((CardView) getChildAt(1), true);
	}

	public void pushTopCardDown() {
		currentShowCardIndex = 0;
		actionWithAnim((CardView) getChildAt(1), false);
	}

	public static final int first_card = 0;
	public static final int second_card = 1;

	/**
	 * @param index
	 *            {@link #first_card} {@link #second_card}
	 */
	public void setShowcard(int index) {
		if (index == currentShowCardIndex) {
			return;
		}
		int childCount = getChildCount();
		if (index >= childCount) {
			index = childCount - 1;
		} else if (index < 0) {
			index = 0;
		}
		currentShowCardIndex = index;
		requestLayout();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
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
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// do not support margin
		int childCount = getChildCount();
		int perCardViewTitleHeight = 0;
		int paddingW = getPaddingLeft() + getPaddingRight();
		int paddingH = getPaddingTop() + getPaddingBottom();
		//
		for (int i = childCount - 1; i > -1; i--) {
			CardView cv = (CardView) getChildAt(i);
			if (cv == null) {
				continue;
			}
			LayoutParams lp = cv.getLayoutParams();
			int fixH_MeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight
					- perCardViewTitleHeight, MeasureSpec.EXACTLY);
			//
			int ws = getChildMeasureSpec(widthMeasureSpec, paddingW, lp.width);
			int hs = getChildMeasureSpec(fixH_MeasureSpec, paddingH, lp.height);
			// measure
			cv.measure(ws, hs);
			View cvt = cv.getTitleView();
			if (cvt == null) {
				perCardViewTitleHeight = 0;
			} else {
				perCardViewTitleHeight = cvt.getMeasuredHeight();
			}
		}
	}

	public interface CardListener {

		public void onPullUp();

		public void onPullDown();

	}

	private CardListener mCardListener;

	public void setCardListener(CardListener l) {
		mCardListener = l;
	}
}
