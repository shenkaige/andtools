package com.phodev.andtools.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 由于ListView，Gridiew,等高度设置为wrap_content的时候会有问题，如果此时下面还有其他view，
 * 当AdapterView的item很多的上海，不仅scroll有问题,底部跟随的view也会跑到可是范围外
 * 
 * <pre>
 * <com.phodev.widgets.AutoFollowBox>
 *      <ListView
 *       android:layout_width="fill_parent"
 *       android:layout_height="wrap_content"// 必须是wrap content,否则没有必要使用该组建，直接布局即可
 *      />
 *      <OtherView/>//有切只有有一个OtherView如果底部是多个View，可以放到一个布局里面。总是AutoFollowBox有切职能有两个Child
 * <com.phodev.widgets.AutoFollowBox/>
 * </pre>
 * 
 * @author sky
 * 
 */
public class AutoFollowBox extends ViewGroup {

	public AutoFollowBox(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AutoFollowBox(Context context) {
		super(context);
	}

	private Rect marginInfo = new Rect();

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
		int maxW = r - l;
		int cl, ct, cr, cb;
		int heightOffset = 0;
		for (int i = 0; i < childCount; i++) {
			View v = getChildAt(i);
			getMargingInfo(v, marginInfo);
			cl = marginInfo.left;
			ct = marginInfo.top + heightOffset;
			cr = maxW - marginInfo.right;
			cb = ct + v.getMeasuredHeight();
			v.layout(cl, ct, cr, cb);
			heightOffset = cb + marginInfo.bottom;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int childCount = getChildCount();
		if (childCount != 2) {
			throw new RuntimeException(
					"mast only 2 views is AutoFoolowAdapterViewBox");
		}
		//
		View stretchView = getChildAt(0);
		View followView = getChildAt(1);
		// start measure
		int offsetBottom = 0;
		int followViewHeihgt = 0;
		if (followView.getVisibility() == View.VISIBLE) {
			LayoutParams lp = followView.getLayoutParams();
			measureChildWithMargins(followView, widthMeasureSpec, 0,
					heightMeasureSpec, 0);
			followViewHeihgt = followView.getMeasuredHeight();
			if (lp instanceof MarginLayoutParams) {
				MarginLayoutParams mlp = (MarginLayoutParams) lp;
				offsetBottom = mlp.topMargin + mlp.bottomMargin
						+ followViewHeihgt;
			}
		}
		//
		if (stretchView.getVisibility() == View.VISIBLE) {
			measureChildWithMargins(stretchView, widthMeasureSpec, 0,
					heightMeasureSpec, offsetBottom);
		}
		//
		int maxWantWitdh = Math.max(getMeasuerWidthWithMarging(followView),
				getMeasuerWidthWithMarging(stretchView));
		int maxWantHeight = getMeasuerHeightWithMarging(stretchView)
				+ getMeasuerHeightWithMarging(followView);
		//
		int measuredWidth = resolveSize(maxWantWitdh, widthMeasureSpec);
		int measuredHeight = resolveSize(maxWantHeight, heightMeasureSpec);
		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	private int getMeasuerHeightWithMarging(View v) {
		LayoutParams lp = v.getLayoutParams();
		int hAdd = 0;
		if (lp instanceof MarginLayoutParams) {
			MarginLayoutParams mlp = (MarginLayoutParams) lp;
			hAdd = mlp.topMargin + mlp.bottomMargin;
		}
		return v.getMeasuredHeight() + hAdd;
	}

	private int getMeasuerWidthWithMarging(View v) {
		LayoutParams lp = v.getLayoutParams();
		int wAdd = 0;
		if (lp instanceof MarginLayoutParams) {
			MarginLayoutParams mlp = (MarginLayoutParams) lp;
			wAdd = mlp.leftMargin + mlp.rightMargin;
		}
		return v.getMeasuredWidth() + wAdd;
	}

	private void getMargingInfo(View v, Rect out) {
		if (v != null && v.getLayoutParams() instanceof MarginLayoutParams) {
			MarginLayoutParams mlp = (MarginLayoutParams) v.getLayoutParams();
			//
			out.set(mlp.leftMargin,//
					mlp.topMargin,//
					mlp.rightMargin,//
					mlp.bottomMargin);
		} else {
			out.set(0, 0, 0, 0);
		}
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new MarginLayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new MarginLayoutParams(getContext(), attrs);
	}
}
