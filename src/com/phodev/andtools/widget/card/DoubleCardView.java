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
		//
	}

	private CardView moveLockedCard = null;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			moveLockedCard = lockMoveCard((int) ev.getX(), (int) ev.getY());
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			unlockMoveCard();
			break;
		case MotionEvent.ACTION_MOVE:
			if (moveLockedCard != null) {
				moveCard(moveLockedCard, (int) ev.getX(), (int) ev.getY());
				return true;
			}
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private CardView lockMoveCard(int x, int y) {
		int count = getChildCount();
		int l, t, r, b;
		for (int i = 0; i < count; i++) {
			CardView cv = (CardView) getChildAt(i);
			if (cv != null) {
				View tv = cv.getTitleView();
				if (tv != null) {
					l = tv.getLeft() + cv.getLeft();
					t = tv.getTop() + cv.getTop();
					r = tv.getRight();
					b = tv.getBottom() + t;
					if (x > l && x < r && y > t && y < b) {
						return cv;
					}
				}
			}
		}
		return null;
	}

	private void unlockMoveCard() {
		if (moveLockedCard != null) {
			int cardHeight = moveLockedCard.getHeight();
			int startY = moveLockedCard.getTop();
			//
			int titleHeight;
			if (moveLockedCard.getTitleView() == null) {
				titleHeight = 0;
			} else {
				titleHeight = moveLockedCard.getTitleView().getHeight();
			}
			//
			float factor;
			if (moveLockedCard.isExpanding()) {
				factor = 0.2f;
			} else {
				factor = 0.8f;
			}
			//
			int distY;
			if (startY > (cardHeight * factor)) {
				distY = cardHeight - startY - titleHeight;
			} else {
				distY = -startY;
			}
			scroller.start(moveLockedCard, startY, distY);
		}
		moveLockedCard = null;
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
	}

	private ScrollerRunnable scroller = new ScrollerRunnable();

	class ScrollerRunnable implements Runnable {
		private Scroller mScroller;
		private CardView cardView;

		public ScrollerRunnable() {
			mScroller = new Scroller(getContext(),
					new AccelerateDecelerateInterpolator());
		}

		public void start(CardView cardView, int startY, int distY) {
			if (!mScroller.isFinished()) {
				mScroller.forceFinished(true);
				removeCallbacks(this);
			}
			this.cardView = cardView;
			mScroller.startScroll(0, startY, 0, distY, 300);
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
				postDelayed(this, 1000 / 60);
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

	/**
	 * 获取显示的Card的index
	 * 
	 * @return
	 */
	public int getShowCard() {
		return currentShowCardIndex;
	}

	/**
	 * 设置要显示的card
	 * 
	 * @param index
	 */
	public void setShowCard(int index) {
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
}
