package com.phodev.andtools.drag.support;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.phodev.andtools.drag.PageContainer;
import com.phodev.andtools.viewpage.MultiPageView;

/**
 * 可滚动页面的实现
 * 
 * @author skg
 * 
 */
public class PageContainerImpl extends MultiPageView implements PageContainer {

	public PageContainerImpl(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PageContainerImpl(Context context) {
		super(context);
		init();
	}

	private void init() {
		// setOffscreenPageLimit(2);// 最多持有5个界面(2+1+2)
		super.setOnPageChangeListener(mInnerOnPageChangeListener);
	}

	@Override
	public int getCurrentPageIndex() {
		return getCurrentItem();
	}

	private boolean mAutoScroll = true;

	@Override
	public void setAutoScroll(boolean autoScroll) {
		this.mAutoScroll = autoScroll;
	}

	@Override
	public boolean isAutoScrollEnable() {
		return mAutoScroll;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (isAutoScrollEnable()) {
			return super.dispatchTouchEvent(ev);
		}
		return false;
	}

	@Override
	public void nextPage() {
		if (haveNextPage()) {
			setCurrentItem(getCurrentItem() + 1, true);
		}
	}

	@Override
	public void previousPage() {
		if (havePreviousPage()) {
			setCurrentItem(getCurrentItem() - 1, true);
		}
	}

	@Override
	public boolean haveNextPage() {
		PagerAdapter ad = getAdapter();
		if (ad == null) {
			return false;
		}
		return getCurrentItem() + 1 < ad.getCount();
	}

	@Override
	public boolean havePreviousPage() {
		return (getCurrentItem() - 1) >= 0;
	}

	private OnPageStatusListener mOnPageSelectedListener;

	@Override
	public void setOnPageStatusListener(OnPageStatusListener listener) {
		mOnPageSelectedListener = listener;
	}

	private OnPageChangeListener mOnPageChangeListenerOut;

	@Override
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		mOnPageChangeListenerOut = listener;
	}

	private OnPageChangeListener mInnerOnPageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int arg0) {
			if (mOnPageChangeListenerOut != null) {
				mOnPageChangeListenerOut.onPageSelected(arg0);
			}
			if (mOnPageSelectedListener != null) {
				mOnPageSelectedListener.onPageSelected(arg0);
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			if (mOnPageChangeListenerOut != null) {
				mOnPageChangeListenerOut.onPageScrolled(arg0, arg1, arg2);
			}
			if (mOnPageSelectedListener != null) {
				mOnPageSelectedListener.onPageScrolled(arg0, arg1, arg2);
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			if (mOnPageChangeListenerOut != null) {
				mOnPageChangeListenerOut.onPageScrollStateChanged(arg0);
			}
			if (mOnPageSelectedListener != null) {
				mOnPageSelectedListener.onPageScrollStateChanged(arg0);
			}
		}
	};

	@Override
	public void refreshCellState(int cellStatus) {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View v = getChildAt(i);
			v.forceLayout();
			v.requestLayout();
			v.invalidate();
		}
	}
}
