package com.phodev.andtools.viewpage;

import java.util.List;
import java.util.Map;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.phodev.andtools.common.CommonParam;

/**
 * 分页显示的Adapter
 * 
 * @author skg
 * 
 */
public class PagingAdapter<DataType> extends MultiPageAdapter implements
		PageDataProvider<DataType> {
	private final String TAG = "PagingAdapter";
	private final boolean DEBUG = CommonParam.DEBUG;
	private PageCreater<DataType> mPageCreater;
	private List<DataType> mData;
	private int mPageItemCount;
	private int mPageCount;
	private int mFirstPageSpecificItemCount = -1;
	private boolean mUseSpecificFirstPageCount = false;
	private boolean mPageCreaterConfiged = false;

	public PagingAdapter(int pageItemCount, PageCreater<DataType> creater) {
		mPageItemCount = pageItemCount;
		mPageCreater = creater;
	}

	@Override
	public void onFetchSize(int pageWidth, int pageHeight) {
		if (mPageCreater != null && !mPageCreaterConfiged) {
			mPageCreater.configPage(pageWidth, pageHeight, mPageItemCount);
			mPageItemCount = mPageCreater.getPageCapacity();
			mPageCreaterConfiged = true;
			updateDataInfo();
		}
	}

	@Override
	public void notifyDataSetChanged() {
		updateDataInfo();
		super.notifyDataSetChanged();
	}

	/**
	 * 设置数据
	 * 
	 * @param data
	 */
	public void setData(List<DataType> data) {
		this.mData = data;
		updateDataInfo();
	}

	public List<DataType> getData() {
		return this.mData;
	}

	private int updateDataInfo() {
		mPageCount = 0;
		if (mData != null && mPageItemCount > 0) {
			int dataSize = mData.size();
			int remainSize = dataSize;
			if (mFirstPageSpecificItemCount >= 0) {
				mUseSpecificFirstPageCount = true;
				remainSize = dataSize - mFirstPageSpecificItemCount;
			} else {
				mUseSpecificFirstPageCount = false;
			}
			if (remainSize > 0) {
				mPageCount = (int) Math.ceil(remainSize
						/ (float) mPageItemCount);
			}
			if (mUseSpecificFirstPageCount) {
				mPageCount++;
			}
			if (DEBUG) {
				log("mData.size():" + dataSize + "  mPageItemCount:"
						+ mPageItemCount + "  mPageCount:" + mPageCount
						+ "  FirstPageSpecificItemCount"
						+ mFirstPageSpecificItemCount);
			}
		} else {
			mPageCount = 0;
		}
		if (mPageListener != null) {
			mPageListener.onPagesCountChanged(mPageCount);
		}
		if (mPageCreater != null) {
			mPageCreater.onDataChanged(this);
		}
		return mPageCount;
	}

	@Override
	public int getCount() {
		if (mPageCreaterConfiged) {
			return mPageCount;
		} else {
			// 如果页面没有配置好，则等待配置，并然会0
			return 0;
		}
	}

	@Override
	public View getPageView(int position, View convertView, ViewGroup parent) {
		if (mPageCreater == null) {
			return null;
		}
		return mPageCreater.createrPage(position, convertView, parent, this);
	}

	@Override
	public int getDataStart(int pageIndex) {
		// 如果是第一页，那么不管是否是指定了特殊数量，都是从0开始
		int start = 0;
		if (mUseSpecificFirstPageCount) {
			if (pageIndex == 0) {
				start = 0;
			} else {
				start = (pageIndex - 1) * mPageItemCount;
				start += mFirstPageSpecificItemCount;
			}
		} else {
			start = pageIndex * mPageItemCount;
		}
		if (start >= mData.size()) {
			start = mData.size() - 1;
		}
		return start;
	}

	@Override
	public int getDataEnd(int pageIndex) {
		int end = 0;
		if (mUseSpecificFirstPageCount) {
			if (pageIndex == 0) {// 第一页
				end = mFirstPageSpecificItemCount - 1;
			} else {
				end = pageIndex * mPageItemCount - 1;
				end += mFirstPageSpecificItemCount;
			}
		} else {
			end = (pageIndex + 1) * mPageItemCount - 1;
		}
		if (end >= mData.size()) {
			end = mData.size() - 1;
		}
		return end;
	}

	@Override
	public DataType getData(int globalPosition) {
		if (mData == null || globalPosition >= mData.size()) {
			return null;
		}
		return mData.get(globalPosition);
	}

	@Override
	public DataType getData(int pageIndex, int position) {
		int gloablePosition = getDataStart(pageIndex) + position;
		if (mData == null || gloablePosition >= mData.size()
				|| gloablePosition < 0) {
			return null;
		}
		return mData.get(gloablePosition);
	}

	@Override
	public List<DataType> getAllData() {
		return mData;
	}

	@Override
	public int copyData(List<DataType> array, int start, int end) {
		if (DEBUG) {
			log("copydata start:" + start + " end:" + end);
		}
		if (array != null && mData != null && mData.size() > 0) {
			array.clear();
			if (end >= mData.size()) {
				end = mData.size() - 1;
			}
			if (start < 0)
				start = 0;
			if (end < 0)
				end = 0;
			if (end < start) {
				return 0;
			}
			for (int i = start; i <= end; i++) {
				array.add(mData.get(i));
			}
			return end - start;
		} else {
			return 0;
		}
	}

	@Override
	public int copyPageData(List<DataType> array, int pageIndex) {
		return copyData(array, getDataStart(pageIndex), getDataEnd(pageIndex));
	}

	@Override
	public int getTotalDataSize() {
		if (mData != null) {
			return mData.size();
		}
		return 0;
	}

	@Override
	public int getDefaultPerPageItemCount() {
		return mPageItemCount;
	}

	@Override
	public int getPageItemCount(int pageIndex) {
		int ps = getDataStart(pageIndex);
		int pe = getDataEnd(pageIndex);
		if (ps < 0 || pe < 0 || pe < ps) {
			return 0;
		}
		int pageItemCount = pe - ps;
		pageItemCount++;
		return pageItemCount;
	}

	@Override
	public int getPageCount() {
		return mPageCount;
	}

	@Deprecated
	@Override
	public void setSpecificPerPageItemCount(Map<Integer, Integer> refer) {
		// not implemented
		Log.w(TAG, "setSpecificPerPageItemCount not implemented");
	}

	/**
	 * @param count
	 *            小于0则按照没有指定计算
	 */
	@Override
	public void setFirstPageSpecificItemCount(int count) {
		int old = mFirstPageSpecificItemCount;
		mFirstPageSpecificItemCount = count;
		if (old != count) {
			// 重新计算PageCount
			updateDataInfo();
		}
	}

	@Override
	protected void onPageRecyle(View page) {
		if (mPageCreater != null) {
			mPageCreater.onPageRecycle(page);
		}
		super.onPageRecyle(page);
	}

	private PagingListener mPageListener;

	public void setPagingListener(PagingListener pageListener) {
		mPageListener = pageListener;
	}

	public interface PagingListener {
		public void onPagesCountChanged(int totalCount);
	}

	void log(String msg) {
		Log.d(TAG, "" + msg);
	}
}
