package com.phodev.andtools.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.Region.Op;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 透明间距的TextView
 * 
 * @author skg
 * 
 */
public class AlphaLineSpaceTextView extends TextView {
	// as thin as possible
	private boolean thinStyle = false;

	public AlphaLineSpaceTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public AlphaLineSpaceTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlphaLineSpaceTextView(Context context) {
		super(context);
	}

	private final Path path = new Path();

	@Override
	public void draw(Canvas canvas) {
		Layout layout = getLayout();
		if (layout == null) {
			super.draw(canvas);
			return;
		}
		Paint paint = layout.getPaint();
		float spacingAdd = layout.getSpacingAdd();
		if (spacingAdd > 0 && paint != null) {
			int linesCount = getLineCount();
			int maxWith = getWidth();
			int saveCount = canvas.save();
			FontMetrics fm = paint.getFontMetrics();
			int descent = (int) (fm.descent + 0.5f);
			int topOffset;
			int bottomOffset;
			path.reset();
			if (thinStyle) {
				// clip first line top
				path.moveTo(0, 0);
				path.lineTo(maxWith, 0);
				path.lineTo(maxWith, descent);
				path.lineTo(0, descent);
				path.close();
				try {
					canvas.clipPath(path, Op.DIFFERENCE);
				} catch (Exception e) {
				}
				//
				topOffset = -descent;
				bottomOffset = 0;
			} else {
				topOffset = -descent - 2;
				bottomOffset = 0;
			}
			// clip every line bottom
			for (int i = 0; i < linesCount; i++) {
				int top = layout.getLineBaseline(i) - topOffset;
				int bottom = layout.getLineBottom(i) - bottomOffset;
				/*
				 * Log.d("ttt", "---- baseline:" + layout.getLineBaseline(i) +
				 * " bottom:" + layout.getLineBottom(i) + "descent:" + descent);
				 */
				path.reset();
				path.moveTo(0, top);
				path.lineTo(maxWith, top);
				path.lineTo(maxWith, bottom);
				path.lineTo(0, bottom);
				path.close();
				try {
					canvas.clipPath(path, Op.DIFFERENCE);
				} catch (Exception e) {
				}
			}
			//
			super.draw(canvas);
			canvas.restoreToCount(saveCount);
			return;
		}
		super.draw(canvas);
	}
}
