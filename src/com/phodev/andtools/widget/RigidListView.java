package com.phodev.andtools.widget;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

/**
 * LinearLayout模仿ListView，解决ListView在ScrollView中的不兼容问题
 * 
 * @author skg
 * 
 */
public class RigidListView extends LinearLayout implements OnClickListener {

	private BaseAdapter mAdapter;
	private OnItemClickListener onItemClickListener;
	private Drawable dividerDrawable;
	private int dividerResId = android.R.drawable.divider_horizontal_dim_dark;
	private int itemSelectorDrawableResId = android.R.drawable.list_selector_background;

	/**
	 * 绑定布局
	 */
	private void bindLinearLayout() {
		removeAllChild();
		int count = mAdapter.getCount();
		for (int i = 0; i < count; i++) {
			View v = mAdapter.getView(i, null, null);
			v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
			//
			LinearLayout itemContainer = new LinearLayout(getContext());
			itemContainer.setLayoutParams(new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			// 添加Item的View
			itemContainer.addView(v);
			// 设置Divider
			itemContainer.setBackgroundResource(itemSelectorDrawableResId);

			itemContainer.setOrientation(LinearLayout.VERTICAL);
			itemContainer.setId(i);

			itemContainer.setClickable(true);
			if (onItemClickListener != null) {
				itemContainer.setOnClickListener(this);
			}

			itemContainer.setFocusable(true);

			Drawable divider = getDivider();
			if (divider != null) {
				ImageView line = new ImageView(getContext());
				line.setBackgroundDrawable(dividerDrawable);
				itemContainer.addView(line);
			}

			addView(itemContainer, i);
		}
	}

	public RigidListView(Context context) {
		super(context);
		init(context);
	}

	public RigidListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		this.setOrientation(LinearLayout.VERTICAL);
	}

	/**
	 * 不设置的话使用默认的,0表示不使用任何Divider
	 * 
	 * @param resId
	 */
	public void setDivider(int resId) {
		dividerResId = resId;
	}

	public void setItemSelector(int resId) {
		itemSelectorDrawableResId = resId;
	}

	private Drawable getDivider() {
		if (dividerDrawable == null && dividerResId != 0) {
			try {
				dividerDrawable = getContext().getResources().getDrawable(
						dividerResId);
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		}
		return dividerDrawable;
	}

	/**
	 * 获取Adapter
	 * 
	 * @return adapter
	 */
	public ListAdapter getAdpater() {
		return mAdapter;
	}

	/**
	 * 设置数据
	 * 
	 * @param adpater
	 */
	public void setAdapter(BaseAdapter adpater) {
		mAdapter = adpater;
		mAdapter.registerDataSetObserver(dataSetObserver);
		bindLinearLayout();
	}

	/**
	 * 设置点击事件,OnItemClickListener不同于ListView的监听，这个监听必须在setAdapter之前才有效
	 * 
	 * @param onItemClickListener
	 */
	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public interface OnItemClickListener {
		public void onItemClick(View view, ListAdapter adpater, int position);
	}

	@Override
	public void onClick(View v) {
		if (onItemClickListener != null) {
			onItemClickListener.onItemClick(v, mAdapter, v.getId());
		}
	}

	private DataSetObserver dataSetObserver = new RigidDataSetObserver();

	public class RigidDataSetObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			refreshUI();
		}

		@Override
		public void onInvalidated() {
			refreshUI();
		}

	}

	/**
	 * 刷新UI
	 */
	private void refreshUI() {
		bindLinearLayout();

	}

	private void removeAllChild() {
		if (this.getChildCount() > 0) {
			removeAllViews();

		}
	}
}
