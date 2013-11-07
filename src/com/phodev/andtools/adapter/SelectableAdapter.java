package com.phodev.andtools.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支持选择的Adapter
 * 
 * @author sky
 * 
 * @param <K>
 * @param <T>
 */
public abstract class SelectableAdapter<K, T> extends InnerBaseAdapter<T> {
	public void setChecked(int position, boolean isChecked) {
		T t = getData(position);
		innerSetChecked(position, t, isChecked);
	}

	/**
	 * 设置Item被选中
	 * 
	 * @param t
	 * @param isChecked
	 */
	public void setCheckedById(T t, boolean isChecked) {
		int position = mData.indexOf(t);
		if (position < 0) {
			return;
		}
		innerSetChecked(position, t, isChecked);
	}

	private void innerSetChecked(int position, T t, boolean isChecked) {
		if (t != null) {
			if (isChecked) {
				selectmap.put(getItemCheckRecordKey(t), t);
			} else {
				selectmap.remove(getItemCheckRecordKey(t));
			}
			onSelectChanged(position, isChecked);
		}
	}

	/**
	 * 选中所有
	 */
	public void checkAll() {
		if (mData != null) {
			int size = mData.size();
			for (int i = 0; i < size; i++) {
				T t = mData.get(i);
				if (t != null) {
					selectmap.put(getItemCheckRecordKey(t), t);
					onSelectChanged(i, true);
				}
			}
		}
		notifyDataSetChanged();
	}

	/**
	 * 取消所有选中状态
	 * 
	 * @param notifySelectHolder
	 */
	public void unCheckAll(boolean notifySelectHolder) {
		if (notifySelectHolder && mData != null) {
			int size = mData.size();
			for (int i = 0; i < size; i++) {
				T t = mData.get(i);
				if (t != null) {
					onSelectChanged(i, false);
				}
			}
		}
		selectmap.clear();
		notifyDataSetChanged();
	}

	public List<T> getChecked() {
		if (mData == null || mData.size() <= 0 || selectmap.size() <= 0) {
			return null;
		}
		List<T> result = new ArrayList<T>();
		for (T t : mData) {
			if (selectmap.containsValue(t)) {
				result.add(t);
			}
		}
		return result;
	}

	public int getCheckedCount() {
		return selectmap.size();
	}

	/**
	 * 被选中的Item会被放到一个Map<Key,ItemData>,该方法返回之作为这个map中的key
	 * 
	 * @param t
	 * @return
	 */
	protected abstract K getItemCheckRecordKey(T t);

	protected final Map<K, T> selectmap = new HashMap<K, T>();

	/**
	 * 是否被选中
	 * 
	 * @param key
	 * @return
	 */
	public boolean isCheckedKey(K key) {
		return selectmap.get(key) != null;
	}

	public boolean isCheckedData(T t) {
		return selectmap.get(getItemCheckRecordKey(t)) != null;
	}

	protected void onSelectChanged(int position, boolean isSelected) {
	}
}
