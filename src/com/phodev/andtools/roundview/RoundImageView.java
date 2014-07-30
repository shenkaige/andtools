package com.phodev.andtools.roundview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.phodev.andtools.roundview.RoundDrawer.SuperDrawer;

/**
 * 圆角ImageView
 * 
 * @author skg
 */
public class RoundImageView extends ImageView implements SuperDrawer {

	private RoundDrawer roundDrawer;

	public RoundImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		roundDrawer = new RoundDrawer(this, attrs);
		setScaleType(ScaleType.CENTER_CROP);
	}

	public RoundImageView(Context context) {
		super(context);
		roundDrawer = new RoundDrawer(this);
		setScaleType(ScaleType.CENTER_CROP);
	}

	@Override
	public View getView() {
		return this;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		roundDrawer.layout(changed, left, top, right, bottom);
	}

	@Override
	public void draw(Canvas canvas) {
		roundDrawer.draw(canvas);
	}

	@Override
	public void superDraw(Canvas canvas) {
		super.draw(canvas);
	}

}
