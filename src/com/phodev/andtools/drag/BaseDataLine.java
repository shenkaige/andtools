package com.phodev.andtools.drag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.SparseArray;

import com.phodev.andtools.drag.DataLine.Data;

@SuppressLint("UseSparseArrays")
public class BaseDataLine<T extends Data> implements DataLine<T> {
	static final boolean DEBUG = Boolean.TRUE;
	static final String TAG = "BaseDataLine";
	private List<T> mData;
	private int mDefaultSegmentLength;
	private Map<Integer, Integer> mSegmentLengthInfo;// 指定具体每个段落的容量
	private Map<Integer, Segment> mSegmentList = new HashMap<Integer, Segment>();

	public BaseDataLine(int defSegmentLen, Map<Integer, Integer> segmentLenInfo) {
		mDefaultSegmentLength = defSegmentLen;
		mSegmentLengthInfo = segmentLenInfo;
	}

	public void setDataSource(List<T> data) {
		if (mData == data) {
			return;
		}
		clear();
		mData = data;
		updateDataLineSource();
	}

	protected void clear() {
		// 清除原有数据的状态
		mSegmentList.clear();
	}

	protected void updateDataLineSource() {
		int dataSize = mData == null ? 0 : mData.size();
		if (dataSize > 0) {
			int dataEnd = dataSize - 1;
			int segmentIndex = 0;
			int dataCursor = 0;
			while (dataCursor < dataEnd) {
				int segLength = calculateSegmentLength(segmentIndex);
				int segStart = dataCursor;
				int segEnd;
				if (segLength <= 0) {
					segStart = -1;
					segEnd = -1;
				} else {
					// 从0开始计数,所以要-1,并且包括start和end位置的数据
					segEnd = dataCursor + segLength - 1;
					if (segEnd >= dataEnd) {
						segLength = segLength - (segEnd - dataEnd);
						segEnd = dataEnd;
						dataCursor = segEnd;
					} else {
						dataCursor += segLength;
					}
				}
				Segment s = new Segment();
				s.index = segmentIndex;
				s.start = segStart;
				s.end = segEnd;
				s.length = segLength;
				mSegmentList.put(segmentIndex, s);
				//
				segmentIndex++;
			}
			notifySegementCountChanged();
		}
	}

	private int calculateSegmentLength(int segmentIndex) {
		if (mSegmentLengthInfo == null) {
			return mDefaultSegmentLength;
		}
		Integer len = mSegmentLengthInfo.get(segmentIndex);
		if (len == null) {
			return mDefaultSegmentLength;
		}
		return len;
	}

	@Override
	public int getDataLenght() {
		if (mData != null) {
			return mData.size();
		}
		return 0;
	}

	@Override
	public int getSegmentLength(int segmentIndex) {
		Segment s = mSegmentList.get(segmentIndex);
		if (s == null) {
			return -1;
		}
		return s.length;
	}

	@Override
	public int getSegmentStart(int segmentIndex) {
		Segment s = mSegmentList.get(segmentIndex);
		if (s == null) {
			return -1;
		}
		return s.start;
	}

	@Override
	public int getSegmentEnd(int segmentIndex) {
		Segment s = mSegmentList.get(segmentIndex);
		if (s == null) {
			return -1;
		}
		return s.end;
	}

	@Override
	public int getSegmentCount() {
		return mSegmentList.size();
	}

	@Override
	public T getData(int index) {
		int size = mData == null ? -1 : mData.size();
		if (index < 0 || index >= size) {
			return null;
		}
		return mData.get(index);
	}

	@Override
	public T getData(int segmentIndex, int index) {
		return getData(getDataStart(segmentIndex) + index);
	}

	@Override
	public T getData(int index, int fliterFlag, boolean desc) {
		int size = mData == null ? -1 : mData.size();
		if (index < 0 || index >= size) {
			return null;
		}
		if (desc) {
			for (int i = index; i >= 0; i--) {
				T d = mData.get(i);
				if (isValideFlag(d, fliterFlag)) {
					return d;
				}
			}
		} else {
			for (int i = index; i < size; i++) {
				T d = mData.get(i);
				if (isValideFlag(d, fliterFlag)) {
					return d;
				}
			}
		}
		return null;
	}

	@Override
	public T getData(int segmentIndex, int index, int fliterFlag, boolean desc) {
		return getData(getDataStart(segmentIndex) + index, fliterFlag, desc);
	}

	private boolean isValideFlag(T d, int filterFlag) {
		if (d == null) {
			return false;
		}
		switch (filterFlag) {
		case FLITER_FLAG_DELETABLE:
			if (d.isDeletable()) {
				return true;
			}
			break;
		case FLITER_FLAG_MOVEABLE:
			if (d.isMoveable()) {
				return true;
			}
			break;
		case FLITER_FLAG_DELETABLE_AND_MOVEABLE:
			if (d.isDeletable() && d.isMoveable()) {
				return true;
			}
			break;
		}
		return false;
	}

	@Override
	public int getDataStart(int segmentIndex) {
		Segment s = mSegmentList.get(segmentIndex);
		if (s == null) {
			return -1;
		}
		return s.start;
	}

	@Override
	public int getDataEnd(int segmentIndex) {
		Segment s = mSegmentList.get(segmentIndex);
		if (s == null) {
			return -1;
		}
		return s.end;
	}

	@Override
	public int copyData(List<T> array, int start, int end) {
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
	public int copySegmentData(List<T> array, int segmentIndex) {
		return copyData(array, getDataStart(segmentIndex),
				getDataEnd(segmentIndex));
	}

	private SimpleReadOnlyList simpleReadOnlyList;

	@Override
	public ReadOnlyList<T> getDataSource() {
		if (simpleReadOnlyList == null) {
			simpleReadOnlyList = new SimpleReadOnlyList();
		}
		return simpleReadOnlyList;
	}

	@Override
	public boolean moveFromTo(int fromIndex, int toIndex) {
		int size = getDataLenght();
		if (fromIndex < 0 || toIndex < 0) {
			return false;
		}
		if (fromIndex >= size || toIndex >= size) {
			return false;
		}
		T d = mData.remove(fromIndex);
		mData.add(toIndex, d);
		// Log.e("ttt", "---0-move-from index:" + fromIndex + " to index:"
		// + toIndex);
		if (toIndex < fromIndex) {
			int start = toIndex;
			int end = fromIndex + 1;
			for (int i = start; i < end; i++) {
				// Log.e("ttt", "---0-check pos:" + i);
				T t = mData.get(i);
				if (!t.isMoveable()) {
					mData.remove(i);
					mData.add(i - 1, t);
				}
			}
		} else {
			int start = toIndex;
			int end = fromIndex - 1;
			for (int i = start; i > end; i--) {
				// Log.e("ttt", "---0-check pos:" + i);
				T t = mData.get(i);
				if (!t.isMoveable()) {
					mData.remove(i);
					mData.add(i + 1, t);
				}
			}
		}
		innerOnDataMove(fromIndex, toIndex);
		return true;
	}

	@Override
	public boolean moveFromTo(int fromSegment, int fromIndex, int toSegment,
			int toIndex) {
		return moveFromTo(getDataStart(fromSegment) + fromIndex,
				getDataStart(toSegment) + toIndex);
	}

	@Override
	public boolean remove(T d) {
		if (mData == null) {
			return false;
		}
		return removeAt(mData.indexOf(d)) != null;
	}

	private LocationsImpl locations = new LocationsImpl();

	@Override
	public T removeAt(int dataPosition) {
		if (mData == null) {
			return null;
		}
		if (dataPosition < 0 || dataPosition > getDataLenght()) {
			return null;
		}
		T d = mData.get(dataPosition);
		if (d == null) {
			return null;
		} else if (!d.isDeletable()) {
			return null;
		}
		//
		if (removeWithMoveableSortFilter(dataPosition, locations) == null) {
			return null;
		}
		// update segment and notify changed
		int[] out = new int[2];
		indexInSegment(dataPosition, out);
		int dellFromSegment = out[0];
		int segmentCount = getSegmentCount();
		if (DragConfig.DEBUG) {
			log("removeAt:" + dataPosition + " affect sgement from:"
					+ dellFromSegment + " to " + (segmentCount - 1));
		}
		int lastSegment = segmentCount - 1;
		while (mSegmentList.size() > 0) {
			Segment s = mSegmentList.get(lastSegment);
			if (s.length > 1) {
				s.length--;
				if (DragConfig.DEBUG) {
					log("modify segment " + lastSegment + " item length:"
							+ s.length);
				}
				break;
			} else if (s.length == 1) {
				s.length = 0;
				// notify destroy
				SegmentListener sl = mSegmentListener.get(lastSegment);
				mSegmentList.remove(lastSegment);
				if (sl != null) {
					sl.onSegmentDestroy(lastSegment);
				}
				notifySegementCountChanged();
				if (mOnSegmentDestroyListener != null) {
					mOnSegmentDestroyListener.onSegmentDestroy(lastSegment);
				}
				if (DragConfig.DEBUG) {
					log("destory segment position:" + lastSegment);
				}
				break;
			} else {
				// 继续需找下一个
				lastSegment--;
			}
		}
		for (int i = dellFromSegment; i < segmentCount; i++) {
			notifyDataDelete(i, dellFromSegment, dataPosition, locations);
		}
		notifyDataSortChangedAfterDelete(dataPosition, locations);
		notifyDataObserverDelete(d, dataPosition, locations);
		locations.clear();
		return d;
	}

	@Override
	public T removeAt(int segment, int posInSegment) {
		return removeAt(indexInGlobal(segment, posInSegment));
	}

	private final static int TAG_KEY_DELETE_POSITION = 0xFFF00F;

	private T removeWithMoveableSortFilter(int removePos,
			LocationsImpl locationsHolder) {
		RemoveSortFilter<T> filter = getRemoveSortFilter();
		if (filter == null) {
			return null;
		}
		if (mData == null) {
			return null;
		}
		int dataLength;
		if (removePos < 0 || removePos > (dataLength = getDataLenght())) {
			return null;
		}
		//
		int alterableLength = dataLength;
		int currentPos = removePos + 1;
		int unmoveableCount = 0;
		filter.onStart();
		for (int i = currentPos; i < alterableLength;) {
			T data = mData.get(i);
			if (!data.isMoveable()) {
				mData.remove(i);
				filter.put(data, currentPos);
				unmoveableCount++;
				alterableLength--;
			} else {
				i++;
			}
			data.setTag(TAG_KEY_DELETE_POSITION, currentPos);
			currentPos++;
		}
		filter.sort(mData.size());
		T removeData = mData.remove(removePos);
		int dataSize = mData.size();
		for (int i = 0; i < unmoveableCount; i++) {
			int pos = filter.getPosition(i);
			if (pos > dataSize) {
				pos = dataSize;
			}
			// Log.e("ttt", "restore array data to list collection: insert to:"
			// + pos + " name:"
			// + ((Cell<Source>) filter.getData(i)).getData().getName());
			mData.add(pos, filter.getData(i));
			dataSize++;
		}
		filter.onFinish();
		//
		locationsHolder.clear();
		locationsHolder.unmoveableCount = unmoveableCount;
		//
		dataLength = mData.size();
		for (int i = removePos; i < dataLength; i++) {
			T d = mData.get(i);
			int oldPos = d.getTag(TAG_KEY_DELETE_POSITION);
			d.setTag(TAG_KEY_DELETE_POSITION, -1);
			locationsHolder.put(oldPos, i);
			// Log.i("ttt", "insert value old:" + oldPos + " new pos:" + i);
		}
		return removeData;
	}

	private RemoveSortFilter<T> mRemoveSortFilter;
	private RemoveSortFilter<T> mDefRemoveSortFilter;

	@Override
	public void setRemoveSortFilter(RemoveSortFilter<T> filter) {
		mRemoveSortFilter = filter;
	}

	private RemoveSortFilter<T> getRemoveSortFilter() {
		if (mRemoveSortFilter == null) {
			if (mDefRemoveSortFilter == null) {
				mDefRemoveSortFilter = new RemoveSortFilterDefaultImp();
			}
			return mDefRemoveSortFilter;
		}
		return mRemoveSortFilter;
	}

	@Override
	public int indexOf(T d) {
		if (mData == null) {
			return -1;
		}
		return mData.indexOf(d);
	}

	@Override
	public int indexInGlobal(int segment, int indexInSegment) {
		int gIndex = 0;
		int segmentCount = mSegmentList.size();
		if (segment > segmentCount) {
			segment = segmentCount;
		}
		for (int i = 0; i < segment; i++) {
			Segment s = mSegmentList.get(i);
			if (s != null && s.length > 0) {
				gIndex += s.length;
			}
		}
		return gIndex + indexInSegment;
	}

	@Override
	public void indexInSegment(int indexInGlobal, int[] out) {
		if (out == null) {
			return;
		}
		if (out.length < 2) {
			throw new RuntimeException("int[] out array min length is 2");
		}
		int segmentCount = getSegmentCount();
		int targetSegIndex = -1;
		int targetIndexInSeg = -1;
		for (int i = 0; i < segmentCount; i++) {
			Segment s = mSegmentList.get(i);
			if (s != null && s.length > 0) {
				if (indexInGlobal <= s.end) {
					targetSegIndex = i;
					targetIndexInSeg = indexInGlobal - s.start;
					break;
				}
			}
		}
		out[0] = targetSegIndex;
		out[1] = targetIndexInSeg;
	}

	private SparseArray<SegmentListener> mSegmentListener = new SparseArray<SegmentListener>();

	@Override
	public void setSegmentListener(SegmentListener listener, int segment) {
		if (listener == null) {
			mSegmentListener.remove(segment);
		} else {
			mSegmentListener.put(segment, listener);
		}
	}

	List<DataObserver<T>> mDataObserver = new ArrayList<DataObserver<T>>();

	@Override
	public void registerDataObserver(DataObserver<T> observer) {
		if (observer != null && !mDataObserver.contains(observer)) {
			mDataObserver.add(observer);
		}
	}

	@Override
	public void unregisterDataObserver(DataObserver<T> observer) {
		if (observer != null) {
			mDataObserver.remove(observer);
		}
	}

	private void notifyDataSortChangedAfterDelete(int delGlobalIndex,
			Locations locations) {
		for (OnDataDeleteListener odd : mOnDataDeleteListeners) {
			odd.onLocationChanged(delGlobalIndex, locations);
		}
	}

	List<OnDataDeleteListener> mOnDataDeleteListeners = new ArrayList<OnDataDeleteListener>();

	@Override
	public void registerLocationChangedListener(OnDataDeleteListener listener) {
		if (listener == null || mOnDataDeleteListeners.contains(listener)) {
			return;
		}
		mOnDataDeleteListeners.add(listener);
	}

	@Override
	public void unregisterLocationChangedListener(OnDataDeleteListener listener) {
		if (listener != null) {
			mOnDataDeleteListeners.remove(listener);
		}
	}

	private void notifySegementCountChanged() {
		if (mSegmentCountListener != null) {
			mSegmentCountListener.onSegmentCountChanged(getSegmentCount());
		}
	}

	private SegmentCountListener mSegmentCountListener;

	@Override
	public void setSegmentCountListener(SegmentCountListener listener) {
		mSegmentCountListener = listener;
	}

	private OnSegmentDestroyListener mOnSegmentDestroyListener;

	@Override
	public void setOnSegmentDestroyListener(OnSegmentDestroyListener listener) {
		mOnSegmentDestroyListener = listener;
	}

	private void innerOnDataMove(int globalPosA, int globalPosB) {
		if (globalPosA == globalPosB) {
			return;
		}
		//
		int[] out = new int[2];
		//
		indexInSegment(globalPosA, out);
		int fromPageA = out[0];
		int itemAPos = out[1];
		//
		out[0] = out[1] = 0;
		indexInSegment(globalPosB, out);
		int toPageB = out[0];
		int itemBPos = out[1];
		//
		// 通知数据改变的页面刷新数据
		int start = Math.min(fromPageA, toPageB);
		int end = Math.max(fromPageA, toPageB) + 1;
		for (int i = start; i < end; i++) {
			notifyDataMove(i, globalPosA, globalPosB);
			if (DragConfig.DEBUG) {
				log("notifyPageDataMove pageIndex:" + i + " itemAPos:"
						+ itemAPos + " to itemBPos:" + itemBPos + "(" + (i + 1)
						+ "/" + end + ")");
			}
		}
		//
		onDataMove(fromPageA, itemAPos, toPageB, itemBPos, globalPosA,
				globalPosB);
		notifyDataObserverMove(globalPosA, globalPosB);
	}

	private void notifyDataMove(int segment, int fromGlobalPos,
			int moveToGlobalPos) {
		SegmentListener lintener = mSegmentListener.get(segment);
		if (lintener != null) {
			lintener.onDataMove(fromGlobalPos, moveToGlobalPos);
		}
	}

	private void notifyDataDelete(int notifySegmentIndex, int dellFromSegment,
			int delGlobalPos, Locations locations) {
		SegmentListener lintener = mSegmentListener.get(notifySegmentIndex);
		if (lintener != null) {
			lintener.onDeleteAffected(dellFromSegment, delGlobalPos, locations);
			if (DragConfig.DEBUG) {
				log("notifyDataDelete segment:" + notifySegmentIndex
						+ " delGlobalPos:" + delGlobalPos);
			}
		}
	}

	private void notifyDataObserverDelete(T d, int location, Locations mapping) {
		for (DataObserver<T> dos : mDataObserver) {
			dos.onDataDelete(d, location, mapping);
		}
	}

	private void notifyDataObserverMove(int fromLocation, int moveToLocation) {
		for (DataObserver<T> dos : mDataObserver) {
			dos.onDataMove(fromLocation, moveToLocation);
		}
	}

	protected void onDataMove(int fromPageA, int itemAPos, int toPageB,
			int itemBPos, int globalPosA, int globalPosB) {
	}

	public class SimpleReadOnlyList implements ReadOnlyList<T> {

		@Override
		public int getSize() {
			if (mData == null) {
				return 0;
			}
			return mData.size();
		}

		@Override
		public T get(int index) {
			if (mData == null) {
				return null;
			}
			return mData.get(index);
		}

	}

	class Segment {
		int index;
		int length;
		int start;
		int end;
	}

	class RemoveSortFilterDefaultImp implements RemoveSortFilter<T> {
		SparseArray<T> array;

		@Override
		public void onStart() {
			array = new SparseArray<T>();
		}

		@Override
		public void put(T d, int originPosition) {
			array.put(originPosition, d);
		}

		@Override
		public int getPosition(int index) {
			return array.keyAt(index);
		}

		@Override
		public T getData(int index) {
			return array.valueAt(index);
		}

		@Override
		public void onFinish() {
			if (array != null) {
				array.clear();
				array = null;
			}
		}

		@Override
		public void sort(int currentDataSize) {

		}

	}

	class LocationsImpl implements Locations {
		// old index
		List<Integer> indexs = new ArrayList<Integer>();
		// key:old index,value:new index
		Map<Integer, Integer> mapping = new HashMap<Integer, Integer>();
		int unmoveableCount;

		public void clear() {
			indexs.clear();
			mapping.clear();
			unmoveableCount = -1;
		}

		public void put(int oldIndex, int newIndex) {
			indexs.add(oldIndex);
			mapping.put(oldIndex, newIndex);
		}

		@Override
		public int getOldLocation(int index) {
			return indexs.get(index);
		}

		@Override
		public int getNewLocation(int index) {
			return mapping.get(indexs.get(index));
		}

		@Override
		public int size() {
			return indexs.size();
		}

		@Override
		public int getUnmoveableCount() {
			return unmoveableCount;
		}

	}

	protected void log(String msg) {
		Log.d(TAG, "" + msg);
	}
}