package com.phodev.andtools.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.phodev.andtools.R;

/**
 * 页面指示器
 * 
 * @author skg
 * 
 */
public class PageIndicatorView extends LinearLayout {
	private int totalCount;
	private Drawable dotNormal;
	private Drawable dotSelected;
	private int dotSpcae;

	public PageIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.PageIndicatorView);
		dotNormal = a.getDrawable(R.styleable.PageIndicatorView_nromalDot);
		dotSelected = a.getDrawable(R.styleable.PageIndicatorView_selectedDot);
		dotSpcae = a.getDimensionPixelSize(
				R.styleable.PageIndicatorView_dotSpace, 0);
		if (dotNormal == null) {
			dotNormal = new ColorDrawable(Color.BLUE);
		}
		if (dotSelected == null) {
			dotSelected = new ColorDrawable(Color.YELLOW);
		}
		a.recycle();
		this.setOrientation(LinearLayout.HORIZONTAL);
	}

	private void initView() {
		int currentCount = this.getChildCount();
		if (currentCount == totalCount) {
			return;
		} else if (totalCount > currentCount) {
			fillDotView(totalCount - currentCount);
		} else if (totalCount < currentCount) {
			removeDot(currentCount - totalCount);
		}
	}

	private void fillDotView(int count) {
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				ImageView iv = new ImageView(getContext());
				iv.setPadding(dotSpcae, 0, dotSpcae, 0);
				LayoutParams params = new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				iv.setImageDrawable(dotNormal);
				addView(iv, params);
			}
		}
	}

	private void removeDot(int count) {
		if (count > 0) {
			if (getChildCount() < count)
				count = getChildCount();
			removeViews(0, count);
		}
	}

	public void setPageCount(int count) {
		this.totalCount = count;
		initView();
	}

	public void setSelect(int select) {
		for (int i = 0; i < totalCount; i++) {
			if (i == select) {
				ImageView iv_pre = (ImageView) getChildAt(i);
				updateDrawable(iv_pre, dotSelected);
			} else {
				ImageView iv_pre = (ImageView) getChildAt(i);
				updateDrawable(iv_pre, dotNormal);
			}

		}
	}

	public void changeSelectDot(int pre, int select) {
		for (int i = 0; i < totalCount; i++) {
			if (i == pre) {
				ImageView iv_pre = (ImageView) getChildAt(i);
				updateDrawable(iv_pre, dotNormal);
			} else if (i == select) {
				ImageView iv_select = (ImageView) getChildAt(i);
				updateDrawable(iv_select, dotSelected);
			}

		}
	}

	private void updateDrawable(ImageView view, Drawable drawable) {
		if (view == null) {
			return;
		}
		if (view.getDrawable() != drawable) {
			view.setImageDrawable(drawable);
		}
	}
}
