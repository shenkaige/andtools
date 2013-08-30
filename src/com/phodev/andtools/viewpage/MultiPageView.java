package com.phodev.andtools.viewpage;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.phodev.andtools.common.CommonParam;

/**
 * 多页面切换显示组件，相对ViewPager添加了Page的View重用，简化了Adapter
 * 
 * @author skg
 * 
 */
public class MultiPageView extends ViewPager {
	private final static String TAG = "MultiPageView";
	private final static boolean DEBUG = CommonParam.DEBUG;
	private PagerAdapter pagerAdapter;
	private Object primaryItemObject;// 被选中的Object，在这里是一个View

	public MultiPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MultiPageView(Context context) {
		super(context);
		init();
	}

	private void init() {
		pagerAdapter = new PagerAdapterImpl();
		setAdapter(pagerAdapter);
	}

	public void setAdapter(MultiPageAdapter adapter) {
		if (mAdapter != null) {
			mAdapter.setDataSetObserverMapping(null);
		}
		mAdapter = adapter;
		if (mAdapter != null) {
			mAdapter.setDataSetObserverMapping(pagerAdapter);
			viewRecycle.setTypeCount(mAdapter.getViewTypeCount());
		}
		pagerAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		viewRecycle.clear();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		int pageWidth = right - left;
		int pageHeight = bottom - top;
		if (mOnFetchSizeListener != null) {
			mOnFetchSizeListener.onFetchSize(pageWidth, pageHeight);
		}
		if (mAdapter != null) {
			mAdapter.onFetchSize(pageWidth, pageHeight);
		}
		super.onLayout(changed, left, top, right, bottom);
	}

	private OnFetchSizeListener mOnFetchSizeListener;

	public interface OnFetchSizeListener {
		public void onFetchSize(int pageWidth, int pageHeight);
	}

	/**
	 * 设置获取Size的监听
	 * 
	 * @param l
	 */
	public void setOnFecthSizeListener(OnFetchSizeListener l) {
		mOnFetchSizeListener = l;
	}

	private MultiPageAdapter mAdapter = null;

	class PagerAdapterImpl extends PagerAdapter {
		@Override
		public int getCount() {
			if (mAdapter == null) {
				return 0;
			} else {
				return mAdapter.getCount();
			}
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View item = (View) object;
			container.removeView(item);
			item = mAdapter.onUnwrapPage(item, position);
			viewRecycle.addScrapView(item, mAdapter.getItemViewType(position));
			mAdapter.onPageRecyle(item);
		}

		@Override
		public void finishUpdate(ViewGroup container) {
			super.finishUpdate(container);
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			int type = mAdapter.getItemViewType(position);
			View convertView = viewRecycle.getScrapView(type);
			View item = mAdapter.getPageView(position, convertView, container);
			item = mAdapter.onWrapPage(item, position);
			container.addView(item);
			return item;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		private int mPrimaryItemPosition = -1;

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			if (mPrimaryItemPosition != position) {// 相等的跳过
				mPrimaryItemPosition = position;
				primaryItemObject = object;
				if (mListener != null) {
					mListener.onPrimaryItemConfirm(container, position, object);
				}
			}
			if (DEBUG) {
				log("setPrimaryItem:" + position);
			}
		}

		@Override
		public void startUpdate(ViewGroup container) {
			super.startUpdate(container);
		}

		@Override
		public void notifyDataSetChanged() {
			mPrimaryItemPosition = -1;
			super.notifyDataSetChanged();
		}

	}

	private PrimaryItemListener mListener;

	public void setPrimaryItemChangeListener(PrimaryItemListener listener) {
		mListener = listener;
	}

	public interface PrimaryItemListener {
		public void onPrimaryItemConfirm(ViewGroup container, int position,
				Object object);
	}

	/**
	 * 目前该方法返回的对象是瞬时的，并不能保证下一个时间单位会变成哪一个
	 * 
	 * @deprecated
	 */
	public View getCurrentPage() {
		return (View) primaryItemObject;
	}

	ViewRecycle viewRecycle = new ViewRecycle();

	/**
	 * 循环使用Item的View
	 * 
	 * @author skg
	 * 
	 */
	class ViewRecycle {
		ArrayList<View>[] scrapList;
		ArrayList<View> currentScraps;
		int mViewTypeCount;

		@SuppressWarnings("unchecked")
		void setTypeCount(int count) {
			if (count <= 0) {
				count = 1;
			}
			scrapList = new ArrayList[count];
			for (int i = 0; i < count; i++) {
				scrapList[i] = new ArrayList<View>();
			}
			currentScraps = scrapList[0];
			mViewTypeCount = count;
		}

		void addScrapView(View view, int type) {
			if (type < scrapList.length) {
				currentScraps = scrapList[type];
				currentScraps.add(view);
				if (DEBUG) {
					log("add type:" + type + " size:" + currentScraps.size());
				}
			}
		}

		View getScrapView(int type) {
			if (type < scrapList.length) {
				currentScraps = scrapList[type];
				if (currentScraps.size() > 0) {
					View view = currentScraps.remove(0);
					if (DEBUG) {
						log("get--> type:" + type + " size:"
								+ currentScraps.size());
					}
					return view;
				}
			}
			return null;
		}

		/**
		 * Clears the scrap heap.
		 */
		void clear() {
			if (mViewTypeCount == 1) {
				final ArrayList<View> scrap = currentScraps;
				final int scrapCount = scrap.size();
				for (int i = 0; i < scrapCount; i++) {
					removeDetachedView(scrap.remove(scrapCount - 1 - i), false);
					if (DEBUG) {
						log("clear record");
					}
				}
			} else {
				final int typeCount = mViewTypeCount;
				for (int i = 0; i < typeCount; i++) {
					final ArrayList<View> scrap = scrapList[i];
					final int scrapCount = scrap.size();
					for (int j = 0; j < scrapCount; j++) {
						removeDetachedView(scrap.remove(scrapCount - 1 - j),
								false);
						if (DEBUG) {
							log("clear record");
						}
					}
				}
			}
		}
	}

	void log(String msg) {
		Log.d(TAG, "-->" + msg);
	}
}
