package com.phodev.andtools.adapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.view.View;
import android.view.ViewGroup;

/**
 * 支持但条刷新Item，提高刷新效率
 * 
 * @author sky
 * 
 * @param <T>
 */
public abstract class RefreshableAdapter<T> extends InnerBaseAdapter<T> {
	private HashMap<View, T> viewMaping = new HashMap<View, T>();

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		View view = getItemView(position, convertView, parent);
		viewMaping.put(view, getData(position));
		return view;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		checkDataForViewMapping();
	}

	@Override
	public void notifyDataSetInvalidated() {
		super.notifyDataSetInvalidated();
		checkDataForViewMapping();
	}

	private void checkDataForViewMapping() {
		if (getCount() <= 0) {
			viewMaping.clear();
		}
	}

	protected abstract View getItemView(int position, View convertView,
			ViewGroup parent);

	protected abstract boolean refreshItemView(T t, View itemView);

	public void refreshItemViewByPosition(int position) {
		refreshItemViewByData(getData(position));
	}

	public void refreshItemViewByData(T t) {
		if (t == null) {
			return;
		}
		Iterator<Entry<View, T>> iter = viewMaping.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<View, T> en = iter.next();
			if (isSameItemData(en.getValue(), t)) {
				refreshItemView(t, en.getKey());
				break;
			}
		}
	}

	/**
	 * 判断两个数据是否是同一个条目的数据
	 * 
	 * <pre>
	 * Data d1;
	 * Data d2;
	 * boolean result = d1.getId() == d2.getId();// 用自己的形式判断即可
	 * </pre>
	 * 
	 * @param t
	 * @param t2
	 * @return
	 */
	protected abstract boolean isSameItemData(T t, T t2);
}
