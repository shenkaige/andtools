package com.phodev.andtools.roundview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class RoundLiearLayout extends LinearLayout implements
		RoundDrawer.SuperDrawer {
	private RoundDrawer roundDrawer;

	public RoundLiearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		roundDrawer = new RoundDrawer(this, attrs);
	}

	public RoundLiearLayout(Context context) {
		super(context);
		roundDrawer = new RoundDrawer(this);
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
