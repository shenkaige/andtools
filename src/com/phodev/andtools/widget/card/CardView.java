package com.phodev.andtools.widget.card;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.phodev.andtools.R;

/**
 * 卡片View
 * 
 * @author skg
 * 
 */
public class CardView extends LinearLayout {

	public CardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CardView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setOrientation(VERTICAL);
		onCreate();
	}

	public void onCreate() {
	}

	public boolean isShowable() {
		return true;
	}

	public void setContentView(int layoutResId) {
		LayoutInflater.from(getContext()).inflate(layoutResId, this, true);
	}

	@Override
	public boolean dispatchTrackballEvent(MotionEvent event) {
		super.dispatchTrackballEvent(event);
		return true;
	}

	public View getTitleView() {
		return findViewById(R.id.card_title);
	}

	private boolean isExpanding = false;

	public boolean isExpanding() {
		return isExpanding;
	}

	public void setExpanding(boolean expanding) {
		isExpanding = expanding;
	}

	private int moveOffsetX;
	private int moveOffsetY;

	public void setMoveOffset(int offsetX, int offsetY) {
		moveOffsetX = offsetX;
		moveOffsetY = offsetY;
	}

	public int getMoveOffsetX() {
		return moveOffsetX;
	}

	public int getMoveOffsetY() {
		return moveOffsetY;
	}

	public void clearMoveOffset() {
		moveOffsetX = 0;
		moveOffsetY = 0;
	}

	public String getString(int resId) {
		String str = null;
		try {
			str = getResources().getString(resId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}
}