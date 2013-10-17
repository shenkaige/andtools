package com.phodev.andtools.drag;

import java.util.List;

import com.phodev.andtools.drag.DataLine.Data;

/**
 * 数据线路
 * 
 * @author skg
 * 
 * @param <T>
 */
public interface DataLine<T extends Data> {
	/**
	 * 获取总更的数据长度
	 * 
	 * @return
	 */
	public int getDataLenght();

	/**
	 * 获取指定段的长度
	 * 
	 * @param segmentIndex
	 * @return
	 */
	public int getSegmentLength(int segmentIndex);

	public int getSegmentStart(int segmentIndex);

	public int getSegmentEnd(int segmentIndex);

	/**
	 * 获取所有段的数量
	 * 
	 * @return
	 */
	public int getSegmentCount();

	/**
	 * 获取指定位置的数据
	 * 
	 * @param index
	 * @return
	 */
	public T getData(int index);

	// /**
	// * 获取数据段
	// *
	// * @param segmentIndex
	// * @return
	// */
	// public Segment<D> getSegment(int segmentIndex);

	/**
	 * 从段落中的指定位置获取数据
	 * 
	 * @param segmentIndex
	 * @param index
	 * 
	 * @return
	 */
	public T getData(int segmentIndex, int index);

	/**
	 * 从指定的位置开始寻找第一个符合要求的数据
	 * 
	 * @param index
	 * @param moveable
	 * @param deletable
	 * @param desc
	 * @return
	 */
	public T getData(int index, int fliterFlag, boolean desc);

	public T getData(int segmentIndex, int index, int fliterFlag, boolean desc);

	/**
	 * 只要是可以删除的就满足条件
	 */
	public static final int FLITER_FLAG_DELETABLE = 1;
	/**
	 * 只要是可以移动的就满足条件
	 */
	public static final int FLITER_FLAG_MOVEABLE = 2;
	/**
	 * 必须同时具备可删除和移动的属性
	 */
	public static final int FLITER_FLAG_DELETABLE_AND_MOVEABLE = 3;

	/**
	 * 获取数据的起始位置
	 * 
	 * @param segmentIndex
	 * @return
	 */
	public int getDataStart(int segmentIndex);

	/**
	 * 获取数据的结束位置
	 * 
	 * @param segmentIndex
	 * @return
	 */
	public int getDataEnd(int segmentIndex);

	/**
	 * 拷贝指定的数据
	 * 
	 * @param array
	 * @param start
	 * @param end
	 * @return
	 */
	public int copyData(List<T> array, int start, int end);

	/**
	 * 拷贝指定的数据
	 * 
	 * @param array
	 * @param segmentIndex
	 * @return
	 */
	public int copySegmentData(List<T> array, int segmentIndex);

	/**
	 * 获取所有数据
	 * 
	 * @return
	 */
	public ReadOnlyList<T> getDataSource();

	/**
	 * 从指定的位置移动到指定的位置
	 * 
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	public boolean moveFromTo(int fromIndex, int toIndex);

	public boolean moveFromTo(int fromSegment, int fromIndex, int toSegment,
			int toIndex);

	/**
	 * 移除指定的数据
	 * 
	 * @param d
	 * @return
	 */
	public boolean remove(T d);

	/**
	 * 从全局的位置移除指定的数据
	 * 
	 * @param dataPosition
	 * @return
	 */
	public T removeAt(int dataPosition);

	/**
	 * 根据段落位置和内部数据位置，移除指定的数据
	 * 
	 * @param segment
	 * @param posInSegment
	 * @return
	 */
	public T removeAt(int segment, int posInSegment);

	public void setRemoveSortFilter(RemoveSortFilter<T> filter);

	public int indexOf(T d);

	/**
	 * 根据段落和段落内的index换取全局index
	 * 
	 * @param segment
	 * @param indexInSegment
	 * @return
	 */
	public int indexInGlobal(int segment, int indexInSegment);

	/**
	 * 根据全局的index获得在相应段落数据里的index
	 * 
	 * @param indexInGlobal
	 * @param out
	 *            0:segment index,1:segment inner index
	 * @return
	 */
	public void indexInSegment(int indexInGlobal, int[] out);

	public interface ReadOnlyList<DATA> {

		public int getSize();

		public DATA get(int index);
	}

	public void setSegmentListener(SegmentListener listener, int segment);

	/**
	 * 注册事件监听器，不管是哪一个Segment发生了便会都会得到通知
	 * 
	 * @param listener
	 */
	public void registerDataObserver(DataObserver<T> observer);

	public void unregisterDataObserver(DataObserver<T> observer);

	public interface SegmentListener extends OnSegmentDestroyListener {

		public void onDataMove(int fromGlobalPos, int moveToGlobalPos);

		public void onDeleteAffected(int fromSegment, int delGlobalPos,
				Locations mapping);

		public void onDataSetChanged();
	}

	public interface DataObserver<T> {
		public void onDataMove(int fromLocation, int moveToLocation);

		/**
		 * 有数据被删除了
		 * 
		 * @param d
		 * @param location
		 * @param newMapping
		 *            从被删除的position开始到数据结束的位置中间的数据位置变化
		 */
		public void onDataDelete(T d, int location, Locations newMapping);
	}

	public void setOnSegmentDestroyListener(OnSegmentDestroyListener listener);

	public interface OnSegmentDestroyListener {
		/**
		 * 该段落已经无效了
		 */
		public void onSegmentDestroy(int segmentIndex);
	}

	public interface Data {
		public boolean isDeletable();

		public boolean isMoveable();

		/**
		 * 使用的时候要确保key的唯一性，避免覆盖值
		 * 
		 * @param key
		 * @param tag
		 */
		public void setTag(int key, int tag);

		/**
		 * 使用的时候要确保key的唯一性
		 * 
		 * @param key
		 * @param tag
		 */
		public int getTag(int key);
	}

	public interface RemoveSortFilter<T> {
		public void onStart();

		public void put(T d, int originPosition);

		public int getPosition(int index);

		public T getData(int index);

		public void onFinish();

		public void sort(int currentDataSize);
	}

	public interface OnDataDeleteListener {
		public void onLocationChanged(int delGlobalIndex, Locations indexMapping);
	}

	public interface Locations {
		public int getOldLocation(int index);

		public int getNewLocation(int index);

		public int size();

		public int getUnmoveableCount();
	}

	public interface SegmentCountListener {
		public void onSegmentCountChanged(int segmentCount);
	}

	public void setSegmentCountListener(SegmentCountListener listener);

	public void registerLocationChangedListener(OnDataDeleteListener listener);

	public void unregisterLocationChangedListener(OnDataDeleteListener listener);
	// public interface Segment<D> {
	// public int getLength();
	//
	// public int getStart();
	//
	// public int getEnd();
	//
	// public D get(int index);
	//
	// public boolean isValide();
	//
	// }
}
