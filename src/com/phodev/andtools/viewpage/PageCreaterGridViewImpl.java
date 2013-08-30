package com.phodev.andtools.viewpage;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.phodev.andtools.common.CommonParam;

/**
 * GridView分页页面创建Base实现
 * 
 * @author skg
 * 
 */
public abstract class PageCreaterGridViewImpl<T> implements PageCreater<T> {
	private final String TAG = "PageCreaterGridViewImpl";
	private final boolean DEBUG = CommonParam.DEBUG;
	private int perPageItemCount;
	private OnItemClickListener mItemClickListener;
	private OnItemLongClickListener mItemLongClickListener;
	//
	private int mColumns;
	private int mFitItemWith;
	private int mFitRows;
	private int mMaxW;
	private int mMaxH;
	private int mGridViewW;
	private int mGridViewH;
	private int mHopeItemSpace;// 希望的ItemSpace
	private boolean useHorizontalPadding;
	private boolean useVerticlePadding;
	// Horizontal Vertical Max Ratio
	private final float max_v_h_space_ratio = 1.8f;

	public PageCreaterGridViewImpl(int columns, int hopeItemSpace,
			boolean useHorizontalPadding, boolean useVerticlePadding) {
		mColumns = columns;
		mHopeItemSpace = hopeItemSpace;
		this.useHorizontalPadding = useHorizontalPadding;
		this.useVerticlePadding = useVerticlePadding;
	}

	@Override
	public void configPage(int maxW, int maxH, int perPageCount) {
		mMaxW = maxW;
		mMaxH = maxH;
		setBound(mMaxW, mMaxH, mColumns, mHopeItemSpace);
		if (mPageCreaterListener != null) {
			mPageCreaterListener.onPageConfiged(this);
		}
	}

	@Override
	public int getPageCapacity() {
		return mFitRows * mColumns;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View createrPage(int pageIndex, View convertView, ViewGroup parent,
			PageDataProvider<T> provider) {
		PageHolder ph = null;
		if (convertView == null) {
			ph = createPage(parent.getContext());
			convertView = ph.rootView;
			convertView.setTag(ph);
		} else {
			ph = (PageHolder) convertView.getTag();
		}
		ph.adapter.resetDataForReuse(ph.gridView, provider, pageIndex);
		ph.gridView.setAdapter(ph.adapter);
		perShowGridConfig(ph.gridView, pageIndex);// 用户可以自定义
		return convertView;
	}

	@Override
	public void onDataChanged(PageDataProvider<T> provider) {
		if (provider != null) {
			perPageItemCount = provider.getDefaultPerPageItemCount();
		}
	}

	@Override
	public void onPageRecycle(View page) {
		// if (page != null) {
		// PageHolder ph = null;
		// try {
		// ph = (PageHolder) page.getTag();
		// } catch (Exception e) {
		// ph = null;
		// }
		// if (ph != null) {
		// GridViewAdapter adapter = null;
		// try {
		// adapter = (GridViewAdapter) ph.gridView.getAdapter();
		// } catch (Exception e) {
		// ph = null;
		// }
		// if (adapter != null) {
		// adapter.clearDataNotify();
		// return;
		// }
		// }
		// }

	}

	class PageHolder {
		LinearLayout rootView;
		GridView gridView;
		GridViewAdapter adapter;
	}

	/**
	 * 创建一个页面
	 * 
	 * @return
	 */
	private PageHolder createPage(Context context) {
		PageHolder pageHolder = new PageHolder();
		pageHolder.adapter = new GridViewAdapter();
		// pageHolder.pageView = crateGridView(context);
		crateGridView(context, pageHolder);
		pageHolder.gridView.setAdapter(pageHolder.adapter);
		return pageHolder;
	}

	private GridView crateGridView(Context context, PageHolder ph) {
		LinearLayout box = new LinearLayout(context);
		ViewGroup.LayoutParams boxLp = new ViewGroup.LayoutParams(mMaxW, mMaxH);
		box.setLayoutParams(boxLp);
		//
		GridView g = new GridView(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				getGridWidth(), getGridHeight());
		g.setLayoutParams(lp);
		box.addView(g);
		//
		ph.rootView = box;
		ph.gridView = g;
		//
		g.setColumnWidth(getColumnWidth());
		g.setNumColumns(mColumns);
		g.setGravity(Gravity.CENTER);
		g.setVerticalSpacing(mVerticleSpace);
		g.setHorizontalSpacing(mHorizontalSpace);
		int left = 0;
		int top = 0;
		int right = 0;
		int bottom = 0;
		if (useHorizontalPadding) {
			left = mHorizontalSpace;
			right = mHorizontalSpace;
		}
		if (useVerticlePadding) {
			bottom = mVerticleSpace;
			top = mVerticleSpace;
		}
		g.setPadding(left, top, right, bottom);
		// g.setOnItemClickListener(innerListener);
		// g.setOnItemLongClickListener(innerLongClickListener);
		g.setSelector(android.R.color.transparent);
		// 由于4.0系统存在位置的bug，只能试试ItemView的OnClick和OnLongClick代替处理
		// g.setOnItemClickListener(mItemClickListener);
		// g.setOnItemLongClickListener(mItemLongClickListener);
		return g;
	}

	class GridViewAdapter extends BaseAdapter implements OnClickListener,
			OnLongClickListener {
		AdapterView<?> parent;
		PageDataProvider<T> provider;
		int pageIndex = -1;

		public void resetDataForReuse(AdapterView<?> parent,
				PageDataProvider<T> provider, int pageIndex) {
			this.parent = parent;
			this.provider = provider;
			this.pageIndex = pageIndex;
		}

		public void clearDataNotify() {
			pageIndex = -1;
			provider = null;
			if (DEBUG) {
				log("page recycle and notify data changed current getCount:"
						+ getCount() + " pageIndex:" + pageIndex
						+ "  bound AbsAdaptorView:" + parent);
			}
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (provider != null && pageIndex >= 0) {
				return provider.getPageItemCount(pageIndex);
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return getItemData(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (DEBUG) {
				log("getView()  position:" + position);
			}
			T t = getItemData(position);
			View view = getItemView(position, convertView, parent, t);
			if (view != null) {
				view.setOnClickListener(this);
				view.setOnLongClickListener(this);
				view.setId(position);
			}
			return view;
		}

		@Override
		public int getItemViewType(int position) {
			return getItemType(position, getItemData(position));
		}

		@Override
		public int getViewTypeCount() {
			return getItemTypeCount();
		}

		@Override
		public boolean onLongClick(View v) {
			if (CommonParam.DEBUG) {
				log("OnItemClick " + v.getId());
			}
			if (mItemLongClickListener == null || v == null) {
				return false;
			}
			int id = v.getId();
			return mItemLongClickListener.onItemLongClick(parent, v, id, id);
		}

		@Override
		public void onClick(View v) {
			if (CommonParam.DEBUG) {
				log("OnItemClick " + v.getId());
			}
			if (mItemClickListener != null && v != null) {
				int id = v.getId();
				mItemClickListener.onItemClick(parent, v, id, id);
			}
		}

		public T getItemData(int position) {
			if (provider != null && pageIndex >= 0) {
				return provider.getData(pageIndex, position);
			}
			return null;
		}

	}

	/*
	 * private OnItemClickListener innerListener = new OnItemClickListener() {
	 * 
	 * @Override public void onItemClick(AdapterView<?> parent, View view, int
	 * position, long id) { if (mItemClickListener != null) {
	 * mItemClickListener.onItemClick(parent, view, position, id); } }
	 * 
	 * }; private OnItemLongClickListener innerLongClickListener = new
	 * OnItemLongClickListener() {
	 * 
	 * @Override public boolean onItemLongClick(AdapterView<?> parent, View
	 * view, int position, long id) { if (mItemLongClickListener != null) {
	 * return mItemLongClickListener.onItemLongClick(parent, view, position,
	 * id); } return false; }
	 * 
	 * };
	 */

	/**
	 * 为了减少不必要的inner OnItemClickListener的创建，已经取消了Inner
	 * Listener对事件的转发。所带来的弊端就是，方法必须在Item的create之前设置才有效
	 */
	public void setItemClickListener(OnItemClickListener listener) {
		mItemClickListener = listener;
	}

	/**
	 * 为了减少不必要的inner OnItemClickListener的创建，已经取消了Inner
	 * Listener对事件的转发。所带来的弊端就是，方法必须在Item的create之前设置才有效
	 */
	public void setItemLoogClickListener(OnItemLongClickListener listener) {
		mItemLongClickListener = listener;
	}

	private PageCreaterListener<T> mPageCreaterListener;

	public interface PageCreaterListener<K> {
		public void onPageConfiged(PageCreaterGridViewImpl<K> pcgv);
	}

	/**
	 * 设置监听器
	 * 
	 * @param pcl
	 */
	public void setPageCreaterListener(PageCreaterListener<T> pcl) {
		mPageCreaterListener = pcl;
	}

	/**
	 * 获取GridView的宽度
	 * 
	 * @return
	 */
	public int getGridWidth() {
		return mGridViewW;
	}

	/**
	 * 获取GridView的高度
	 * 
	 * @return
	 */
	public int getGridHeight() {
		return mGridViewH;
	}

	/**
	 * 获取GridView的高度
	 * 
	 * @return
	 */
	public int calculateGridHeight(int rows) {
		if (rows <= 0) {
			rows = 1;
		}
		if (useVerticlePadding) {
			return getColumnWidth() * rows + (rows + 1) * mVerticleSpace;
		} else {
			return getColumnWidth() * rows + (rows - 1) * mVerticleSpace;
		}
	}

	/**
	 * 获取GridView的宽度
	 * 
	 * @return
	 */
	public int calculateGridWidth(int columns) {
		if (columns <= 0) {
			columns = 1;
		}
		if (useHorizontalPadding) {
			return getColumnWidth() * columns + (columns + 1) * mVerticleSpace;
		} else {
			return getColumnWidth() * columns + (columns - 1) * mVerticleSpace;
		}
	}

	/**
	 * 获取ColumnWidth
	 * 
	 * @return
	 */
	public int getColumnWidth() {
		return mFitItemWith;
	}

	/**
	 * 每页显示的数量
	 * 
	 * @return
	 */
	public int getPerPageItemCount() {
		return perPageItemCount;
	}

	/**
	 * 多少个列
	 * 
	 * @return
	 */
	public int getColumns() {
		return mColumns;
	}

	/**
	 * 最大宽度
	 * 
	 * @return
	 */
	public int getMaxWith() {
		return mMaxW;
	}

	/**
	 * Item的space
	 * 
	 * @return
	 */
	public int getHopeItemSpace() {
		return mHopeItemSpace;
	}

	private void setBound(int maxW, int maxH, int maxColumns, int itemSpace) {
		if (maxColumns <= 0) {
			throw new RuntimeException("maxColumns must > 0.maxColumns="
					+ maxColumns);
		}
		if (maxW <= 0 || maxH <= 0) {
			throw new RuntimeException("maxW and maxH must all > 0. maxW="
					+ maxW + " maxH=" + maxH);
		}
		int fitItemSize;
		int rawColumnW = maxW / maxColumns;
		int hopeRows = maxH / rawColumnW;
		int fitRows = getFitRows(rawColumnW, hopeRows, maxH);
		int rawRowHeight = 0;
		if (fitRows > 0) {
			rawRowHeight = maxH / fitRows;
		}
		if (rawRowHeight > rawColumnW) {
			fitItemSize = rawColumnW;
		} else {
			fitItemSize = rawRowHeight;
		}
		mHorizontalSpace = getSpace(maxW, fitItemSize, maxColumns,
				useHorizontalPadding);
		mVerticleSpace = getSpace(maxH, fitItemSize, fitRows,
				useVerticlePadding);
		if (mHorizontalSpace < itemSpace || mVerticleSpace < itemSpace) {
			fitItemSize -= itemSpace;
			mHorizontalSpace = getSpace(maxW, fitItemSize, maxColumns,
					useHorizontalPadding);
			mVerticleSpace = getSpace(maxH, fitItemSize, fitRows,
					useVerticlePadding);
		}
		float ratio = 0;
		if (mHorizontalSpace > mVerticleSpace) {
			ratio = mHorizontalSpace / (float) mVerticleSpace;
		} else {
			ratio = mVerticleSpace / (float) mHorizontalSpace;
		}
		if (ratio > max_v_h_space_ratio && ratio > 0) {
			// 需要对V H Space做调整
			if (mHorizontalSpace > mVerticleSpace) {
				mHorizontalSpace = (int) (mHorizontalSpace / ratio);
			} else {
				mVerticleSpace = (int) (mVerticleSpace / ratio);
			}
		}
		//
		mFitItemWith = fitItemSize;
		mFitRows = fitRows;
		//
		mGridViewH = calculateGridHeight(fitRows);
		mGridViewW = calculateGridWidth(maxColumns);
		mCastoffHeight = mMaxH - mGridViewH;
		mCastoffWidth = mMaxW - mGridViewW;
		if (DEBUG) {
			log("maxW:" + maxW + " maxH:" + maxH + " FitRows:" + mFitRows
					+ " fitItemSize:" + fitItemSize + " HorizontalSpace:"
					+ mHorizontalSpace + " VerticleSpace:" + mVerticleSpace
					+ " mGridViewH:" + mGridViewH + " mGridViewW:" + mGridViewW
					+ " mCastoffHeight:" + mCastoffHeight + " mCastoffWidth:"
					+ mCastoffWidth);
		}
	}

	private int getSpace(int totalSize, int itemSize, int count,
			boolean usePadding) {
		int spaceCount = count;
		if (usePadding) {
			spaceCount++;
		} else {
			spaceCount--;
		}
		if (count <= 0 || spaceCount <= 0) {
			return 0;
		}
		return (totalSize - itemSize * count) / spaceCount;
	}

	private int mHorizontalSpace;
	private int mVerticleSpace;

	public int getItemHorizontalSpace() {
		return mHorizontalSpace;
	}

	public int getItemVerticleSpace() {
		return mVerticleSpace;
	}

	private int mCastoffWidth;
	private int mCastoffHeight;

	/**
	 * 获取通过计算之后，并没有被使用的剩余的宽度
	 * 
	 * @return
	 */
	public int getCastoffWidth() {
		return mCastoffWidth;
	}

	/**
	 * 获取通过计算之后，并没有被使用的剩余的高度
	 * 
	 * @return
	 */
	public int getCastoffHeight() {
		return mCastoffHeight;
	}

	/**
	 * 是否使用了水平的Padding
	 * 
	 * <pre>
	 * GridView的
	 * paddingLeft,paddingRight
	 * </pre>
	 * 
	 * @return
	 */
	public boolean isUseHorizontalPadding() {
		return useHorizontalPadding;
	}

	/**
	 * 是否使用竖直的Padding *
	 * 
	 * <pre>
	 * GridView的
	 * paddingTop,paddingLeft
	 * </pre>
	 * 
	 * @return
	 */
	public boolean isUseVerticlePadding() {
		return useVerticlePadding;
	}

	/**
	 * 递归计算合适的行数
	 * 
	 * @param referW
	 * @param hopeRows
	 * @param maxH
	 * @return
	 */
	private int getFitRows(int referW, int hopeRows, int maxH) {
		int rows = hopeRows;
		while (true) {
			if (maxH - rows * referW > (referW / 2)) {
				// 加一行
				rows++;
			} else {
				return rows;
			}
		}
	}

	protected abstract View getItemView(int position, View convertView,
			ViewGroup parent, T t);

	protected int getItemType(int position, T t) {
		return 0;
	}

	protected int getItemTypeCount() {
		return 1;
	}

	/**
	 * 配置定义属性
	 * 
	 * @param gridView
	 */
	protected void perShowGridConfig(GridView gridView, int pageIndex) {

	}

	void log(String msg) {
		Log.d(TAG, "" + msg);
	}
}
