package com.phodev.andtools.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * 修改原有setScrollIndicators（up,down)方法，添加精确的可滚动判断
 * 
 * @author sky
 * 
 */
public class ListViewFixIndicators extends ListView {

	public ListViewFixIndicators(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ListViewFixIndicators(Context context) {
		super(context);
		init();
	}

	private void init() {
		super.setOnScrollListener(innerScrollListener);
	}

	private View upScrollHintView;
	private View downScrollHintView;
	private final int[] rootXY = new int[2];
	private int rootWindowBottom;

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		post(sysnScrollableTagViewRunnable);
		getLocationInWindow(rootXY);
		rootWindowBottom = rootXY[1] + getHeight();
	}

	@Override
	public void setScrollIndicators(View up, View down) {
		upScrollHintView = up;
		downScrollHintView = down;
	}

	private OnScrollListener mOnScrollListener;

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mOnScrollListener = l;
	}

	private OnScrollListener innerScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (mOnScrollListener != null) {
				mOnScrollListener.onScrollStateChanged(view, scrollState);
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			syncScrollableTag(firstVisibleItem, visibleItemCount,
					totalItemCount);
			if (mOnScrollListener != null) {
				mOnScrollListener.onScroll(view, firstVisibleItem,
						visibleItemCount, totalItemCount);
			}
		}
	};
	private int[] itemXY = new int[2];

	private void syncScrollableTag(int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		if (upScrollHintView == null || upScrollHintView == null) {
			return;
		}
		if (totalItemCount <= visibleItemCount || totalItemCount <= 0) {
			checkVisibility(upScrollHintView, View.INVISIBLE);
			checkVisibility(downScrollHintView, View.INVISIBLE);
		} else {
			if (firstVisibleItem > 0) {
				checkVisibility(upScrollHintView, View.VISIBLE);
			} else {// check scroll y
				getChildAt(0).getLocationOnScreen(itemXY);
				if (itemXY[1] < rootXY[1] - getPaddingTop()) {
					checkVisibility(upScrollHintView, View.VISIBLE);
				} else {
					checkVisibility(upScrollHintView, View.INVISIBLE);
				}
			}
			if (firstVisibleItem + visibleItemCount < totalItemCount) {
				checkVisibility(downScrollHintView, View.VISIBLE);
			} else {// check scroll y
				View child = getChildAt(getChildCount() - 1);
				child.getLocationOnScreen(itemXY);
				//
				int childBottom = itemXY[1] + child.getHeight();
				//
				if (childBottom > rootWindowBottom - getPaddingBottom()) {
					checkVisibility(downScrollHintView, View.VISIBLE);
				} else {
					checkVisibility(downScrollHintView, View.INVISIBLE);
				}
			}
		}
	}

	private Runnable sysnScrollableTagViewRunnable = new Runnable() {

		@Override
		public void run() {
			int firstVisibleItem;
			int visibleItemCount;
			int totalItemCount;
			ListAdapter ad = getAdapter();
			if (ad == null || ad.getCount() <= 0) {
				firstVisibleItem = 0;
				visibleItemCount = 0;
				totalItemCount = 0;
			} else {
				firstVisibleItem = getFirstVisiblePosition();
				visibleItemCount = getLastVisiblePosition() - firstVisibleItem
						+ 1;
				totalItemCount = ad.getCount();
			}
			syncScrollableTag(firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
	};

	private void checkVisibility(View v, int visibility) {
		if (v != null && v.getVisibility() != visibility) {
			v.setVisibility(visibility);
		}
	}
}
