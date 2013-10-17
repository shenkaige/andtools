package com.phodev.andtools.drag.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;

import com.phodev.andtools.drag.BaseDataLine;
import com.phodev.andtools.drag.CacheManager;
import com.phodev.andtools.drag.CacheManager.CacheBounds;
import com.phodev.andtools.drag.CellModel;
import com.phodev.andtools.drag.DragConfig;

public class DataLineCellsImpl extends BaseDataLine<CellModel> implements
		CacheBounds<CellModel> {
	private int cacheStartSegment;
	private int cacheEndSegment;
	private int cacheMaxPos;
	private int cacheMinPos;
	private CacheManager<CellModel> cacheManager;

	public DataLineCellsImpl(int defSegmentLen,
			Map<Integer, Integer> segmentLenInfo) {
		super(defSegmentLen, segmentLenInfo);
		setRemoveSortFilter(new RemoveSortFilterImpl());
	}

	public void boundCacheManager(CacheManager<CellModel> manager) {
		if (cacheManager != null) {
			cacheManager.setCacheBounds(null);
			cacheManager.releaseAll();
		}
		cacheManager = manager;
		if (cacheManager != null) {
			cacheManager.setCacheBounds(this);
		}
	}

	public void setCacheBounds(int startSegment, int endSegment) {
		if (startSegment == cacheStartSegment && endSegment == cacheEndSegment) {
			return;
		}
		cacheStartSegment = startSegment;
		cacheEndSegment = endSegment;
		if (updateCacheBounds()) {
			if (cacheManager != null) {
				cacheManager.checkCacheaVailability();
			}
		}
	}

	@Override
	protected void updateDataLineSource() {
		super.updateDataLineSource();
		updateCacheBounds();
	}

	private boolean updateCacheBounds() {
		int minPos = indexInGlobal(cacheStartSegment, 0);
		int maxPos = indexInGlobal(cacheEndSegment, 0);
		maxPos += getSegmentLength(cacheEndSegment);
		if (minPos == cacheMinPos && maxPos == cacheMaxPos) {
			return false;
		}
		cacheMaxPos = maxPos;
		cacheMinPos = minPos;
		if (DragConfig.DEBUG) {
			log("updateCacheBounds-->cacheMinPos:" + cacheMinPos
					+ ",cacheMaxPos:" + cacheMaxPos);
		}
		return true;
	}

	@Override
	public boolean isNeedCache(CellModel key) {
		int index = indexOf(key);
		return index <= cacheMaxPos && index >= cacheMinPos;
	}

	@SuppressLint("UseSparseArrays")
	class RemoveSortFilterImpl implements RemoveSortFilter<CellModel> {
		List<CellModel> cellsIndex = new ArrayList<CellModel>();;
		Map<CellModel, Integer> cellsOriginPosMap = new HashMap<CellModel, Integer>();
		CellModel addTag;
		int addTagOrignPos = -1;

		@Override
		public void onStart() {
			cellsOriginPosMap.clear();
			cellsIndex.clear();
			addTag = null;
			addTagOrignPos = -1;
		}

		@Override
		public void put(CellModel d, int originPosition) {
			// Source source = (Source) d.getData();
			// if (source != null && source.getType() == Source.TYPE_CUSTOM_ADD)
			// {
			// addTag = d;
			// addTagOrignPos = originPosition;
			// }
			cellsOriginPosMap.put(d, originPosition);
			cellsIndex.add(d);
		}

		@Override
		public int getPosition(int index) {
			return cellsOriginPosMap.get(cellsIndex.get(index));
		}

		@Override
		public CellModel getData(int index) {
			return cellsIndex.get(index);
		}

		@Override
		public void onFinish() {
			cellsOriginPosMap.clear();
			cellsIndex.clear();
			addTag = null;
			addTagOrignPos = -1;
		}

		@Override
		public void sort(int currentDataSize) {
			if (addTag != null) {
				int lastPos = currentDataSize - 1;
				if (lastPos < 0) {
					lastPos = 0;
				}
				if (lastPos < addTagOrignPos) {
					cellsIndex.remove(addTag);
					int deltaC = addTagOrignPos - lastPos;
					if (cellsIndex.size() < deltaC) {
						// Log.e("ttt", "---> after sort--<<<--- targetPos:"
						// + deltaC + " list size:" + cellsIndex.size());
						cellsIndex.add(addTag);
					} else {
						// Log.e("ttt", "---> after sort-->>>+++ targetPos:"
						// + deltaC + " list size:" + cellsIndex.size());
						cellsIndex.add(deltaC, addTag);
					}
				}
			}
			// Log.e("ttt", "---> after sort");
			// for (int i = 0; i < cellsIndex.size(); i++) {
			// Log.e("ttt", "---> " + i + " -- " + getPosition(i) + "-"
			// + getData(i).getData().getName());
			// }
		}
	}
}
