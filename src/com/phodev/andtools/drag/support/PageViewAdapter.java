package com.phodev.andtools.drag.support;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.phodev.andtools.R;
import com.phodev.andtools.drag.CacheManager;
import com.phodev.andtools.drag.CellLayout;
import com.phodev.andtools.drag.CellLayout.OnAttachChangedListener;
import com.phodev.andtools.drag.CellModel;
import com.phodev.andtools.drag.CellViewFactory;
import com.phodev.andtools.drag.DataLine.OnSegmentDestroyListener;
import com.phodev.andtools.drag.DataLine.SegmentCountListener;
import com.phodev.andtools.drag.DragConfig;
import com.phodev.andtools.drag.DragLayer;
import com.phodev.andtools.drag.DraggableGridView;
import com.phodev.andtools.drag.DraggableGridView.OnCellClickListener;
import com.phodev.andtools.drag.GridSpaceCalculator;
import com.phodev.andtools.utils.ImageUtility;
import com.phodev.andtools.viewpage.MultiPageAdapter;

/**
 * 页面Adapter
 * 
 * @author skg
 * 
 */
public class PageViewAdapter extends MultiPageAdapter {
	private List<CellModel> mSourceList;
	private DragLayer mDragLayer;
	private Map<Integer, Integer> mPageLenRefer;
	private DataLineCellsImpl mDataLine;
	private CellViewFactory cellViewFactory = new CellViewFactoryImpl();
	private CacheManager<CellModel> mCacheManager;
	private final int COLUMN_COUNT = 3;
	private final int HOPE_ITEM_SPACE = 2;
	private int defPageLength = 12;// 3*4
	private int mItemHorizontalSpace;
	private int mItemVerticalSpace;
	private int mBorderPaddingLeft;
	private int mBorderPaddingTop;
	private int mBorderPaddingRight;
	private int mBorderPaddingBottom;
	private int mChildSize;
	private boolean isConfiged = false;
	private OnCellClickListener mOnCellClickListener;

	public PageViewAdapter(DragLayer dragLayer,
			Map<Integer, Integer> pageLenRefer,
			CacheManager<CellModel> cacheManager, OnCellClickListener listner) {
		mPageLenRefer = pageLenRefer;
		mCacheManager = cacheManager;
		mDragLayer = dragLayer;
		mOnCellClickListener = listner;
	}

	private boolean setDataOnCreateDataLine = false;

	public void setData(List<CellModel> data) {
		if (data != mSourceList) {
			mSourceList = data;
			if (mDataLine != null) {
				mDataLine.setDataSource(data);
			} else {
				setDataOnCreateDataLine = true;
			}
		}
	}

	private SegmentCountListener mSegmentCountListener;

	public void setSegementCountListener(SegmentCountListener listener) {
		mSegmentCountListener = listener;
		if (mDataLine != null) {
			mDataLine.setSegmentCountListener(mSegmentCountListener);
		}
	}

	private OnSegmentDestroyListener mOnSegmentDestroyListener;

	public void setOnSegmentDestroyListener(OnSegmentDestroyListener listener) {
		mOnSegmentDestroyListener = listener;
		if (mDataLine != null) {
			mDataLine.setOnSegmentDestroyListener(mOnSegmentDestroyListener);
		}
	}

	private DataLineCreateListener mDataLineCreateListener;

	public void setDataLineCreateListener(DataLineCreateListener l) {
		mDataLineCreateListener = l;
	}

	public interface DataLineCreateListener {
		public void onDataLineCreate(DataLineCellsImpl dataLine);
	}

	@Override
	public int getCount() {
		if (!isConfiged || mDataLine == null) {
			return 0;
		} else {
			return mDataLine.getSegmentCount();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getPageView(int position, View convertView, ViewGroup parent) {
		DraggableGridView v;
		if (convertView instanceof DraggableGridView) {
			v = (DraggableGridView) convertView;
		} else {
			v = new DraggableGridView(cellViewFactory, mDragLayer,
					COLUMN_COUNT, mChildSize);
			v.setOnCellClickListener(mOnCellClickListener);
			v.setBorderPadding(mBorderPaddingLeft, mBorderPaddingTop,
					mBorderPaddingRight, mBorderPaddingBottom);
			v.setHorizontalSpace(mItemHorizontalSpace);
			v.setVerticalSpace(mItemVerticalSpace);
		}
		v.setData(position, mDataLine);
		return v;
	}

	@Override
	protected void onPageRecyle(View page) {
		super.onPageRecyle(page);
		if (page instanceof DraggableGridView) {
			((DraggableGridView) page).releaseAll();
		}
	}

	public CacheManager<CellModel> getCacheManager() {
		return mCacheManager;
	}

	@Override
	public void onFetchSize(int pageWidth, int pageHeight) {
		super.onFetchSize(pageWidth, pageHeight);
		if (pageWidth <= 0 || pageHeight <= 0) {
			return;
		}
		if (isConfiged && mDataLine != null) {
			return;
		}
		GridSpaceCalculator gc = new GridSpaceCalculator();
		gc.setBound(pageWidth, pageHeight, COLUMN_COUNT, HOPE_ITEM_SPACE, true,
				false);
		int fitRows = gc.getFitRows();
		mChildSize = gc.getColumnWidth();
		defPageLength = fitRows * COLUMN_COUNT;
		mItemHorizontalSpace = gc.getItemHorizontalSpace();
		mItemVerticalSpace = gc.getItemVerticleSpace();
		mBorderPaddingLeft = mItemHorizontalSpace + gc.getCastoffWidth() / 2;
		mBorderPaddingRight = mBorderPaddingLeft;
		mBorderPaddingTop = mItemVerticalSpace + gc.getCastoffHeight() / 2;
		mBorderPaddingBottom = mBorderPaddingTop;
		if (DragConfig.DEBUG) {
			log("fit rows:" + fitRows + " defPageLength:" + defPageLength
					+ " castoffWidth:" + gc.getCastoffWidth()
					+ " castoffHeight:" + gc.getCastoffHeight());
		}
		mDataLine = new DataLineCellsImpl(defPageLength, mPageLenRefer);
		if (setDataOnCreateDataLine) {
			setDataOnCreateDataLine = false;
			mDataLine.setDataSource(mSourceList);
		}
		mDataLine.setSegmentCountListener(mSegmentCountListener);
		mDataLine.setOnSegmentDestroyListener(mOnSegmentDestroyListener);
		mDataLine.boundCacheManager(mCacheManager);
		//
		isConfiged = true;
		if (mDataLineCreateListener != null) {
			mDataLineCreateListener.onDataLineCreate(mDataLine);
		}
		notifyDataSetChanged();
	}

	private OnAttachChangedListener onCellAttachListener = new OnAttachChangedListener() {

		@Override
		public void onAttachChanged(CellLayout cell, ViewGroup parent,
				boolean status) {
			if (status) {
				runningAddSourceCell = cell;
				checkNewSourceTag();
			} else {
				checkNewSourceTag();
				runningAddSourceCell = null;
			}
		}
	};

	private void checkNewSourceTag() {
		if (runningAddSourceCell == null || !newSouceTagConfigChanged) {
			return;
		}
		// haveNewSource = true;//debug
		if (haveNewSource) {
			View tagView = getNewTagView(runningAddSourceCell.getContext());
			if (tagView == runningAddSourceCell.getTagView()) {
				return;
			} else {
				runningAddSourceCell.setTagView(tagView);
			}
		} else {
			runningAddSourceCell.removeTagView();
		}
		checkNeSourceTagImage();
		//
		newSouceTagConfigChanged = false;
	}

	private void checkNeSourceTagImage() {
		if (runningAddSourceCell != null) {
			ImageView img = (ImageView) runningAddSourceCell
					.findViewById(R.id.templet_source_icon);
			if (img != null && addSourceIconUrl != null
					&& addSourceIconUrl.length() > 0) {
				ImageUtility.loadImage(img, addSourceIconUrl, 0,
						R.drawable.btn_source_add, false);
			}
		}
	}

	private boolean haveNewSource = false;
	private String addSourceIconUrl;
	private CellLayout runningAddSourceCell;
	private boolean newSouceTagConfigChanged = false;

	public void updateHaveNewStatus(boolean haveNew) {
		haveNewSource = haveNew;
		newSouceTagConfigChanged = true;
		checkNewSourceTag();
	}

	public void updateAddSourceIconUrl(String iconUrl) {
		addSourceIconUrl = iconUrl;
		newSouceTagConfigChanged = true;
		checkNeSourceTagImage();
	}

	public int getItemHorizontalSpace() {
		return mBorderPaddingRight + DragConfig.getCellContentMargging();
	}

	private ImageView newTagView;

	private View getNewTagView(Context context) {
		if (newTagView == null) {
			newTagView = new ImageView(context);
			newTagView.setImageResource(R.drawable.ic_new_souce);
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lp.gravity = Gravity.RIGHT | Gravity.TOP;
			newTagView.setLayoutParams(lp);
		}
		return newTagView;
	}

	public static final String TAG = "PageViewAdapter";

	public void log(Object msg) {
		Log.d(TAG, "" + msg);
	}
}
