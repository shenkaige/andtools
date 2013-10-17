package com.phodev.andtools.drag;

import android.util.SparseIntArray;

import com.phodev.andtools.drag.DataLine.Data;

/**
 * 
 * @author skg
 * 
 */
public class CellModel implements Data {
	public static final int CELL_STATUS_NORMAL = 0;
	public static final int CELL_STATUS_EDIT = 1;
	private int type;
	private boolean moveable = Boolean.TRUE;
	private boolean deletable = Boolean.TRUE;

	private Object data;

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	private Integer lastDataHashCode = null;

	public boolean isDataChanged() {
		if (lastDataHashCode == null) {
			if (data != null) {
				lastDataHashCode = data.hashCode();
			}
			return false;
		}
		int hashcode = data.hashCode();
		if (lastDataHashCode == hashcode) {
			return false;
		}
		lastDataHashCode = hashcode;
		return true;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public boolean isMoveable() {
		return moveable;
	}

	public void setMoveable(boolean moveable) {
		this.moveable = moveable;
	}

	@Override
	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	SparseIntArray tags;

	@Override
	public void setTag(int key, int tag) {
		if (tags == null) {
			tags = new SparseIntArray();
		}
		tags.put(key, tag);
	}

	@Override
	public int getTag(int key) {
		if (tags == null) {
			return -1;
		}
		return tags.get(key, -1);
	}

}
