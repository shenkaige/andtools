package com.phodev.andtools.samples;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.phodev.andtools.R;
import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.samples.inner.InnerFragment;
import com.phodev.andtools.utils.CommonUtils;
import com.phodev.andtools.widget.RoundDiscView;
import com.phodev.andtools.widget.RoundDiscView.DiscDecorater;
import com.phodev.andtools.widget.RoundDiscView.DiscListener;

/**
 * Round Disc
 * 
 * @author sky
 * 
 */
@SimpleDesc(title = "Round Disc")
public class RoundDiscFragment extends InnerFragment {
	RoundDiscView disc;
	private String[] discItemNames;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		disc = new RoundDiscView(getActivity());
		disc.setDiscListener(discListener);
		disc.setDiscDecorater(decorater);
		discItemNames = getResources().getStringArray(R.array.disk_names);
		return disc;
	}

	private DiscListener discListener = new DiscListener() {

		@Override
		public void onCenterClick(RoundDiscView rd) {
			CommonUtils.toast(getActivity(),
					"on disc center click,and current mapping:"
							+ getItemName(disc.getMappingIndex() - 1));

		}

		@Override
		public void onMappingChanged(RoundDiscView rd, int mappingIndex) {
			CommonUtils.toast(getActivity(), "On pointer papping changed:"
					+ getItemName(disc.getMappingIndex() - 1));
		}

		@Override
		public boolean onAngleItemClick(RoundDiscView rd, int degreesIndex) {
			CommonUtils.toast(getActivity(), "you clicked index:"
					+ getItemName(degreesIndex - 1));
			return false;
		}
	};

	private TextPaint paint = new TextPaint();
	private DiscDecorater decorater = new DiscDecorater() {

		@Override
		public void init(RoundDiscView disc) {
			paint.setTextSize(30);
			paint.setColor(Color.BLUE);
			paint.setAntiAlias(true);
		}

		@Override
		public void destroy(RoundDiscView disc) {

		}

		@Override
		public void decorate(RoundDiscView rd, Canvas canvas, float centerX,
				float centerY, int mapping, int layer) {
			if (layer == LAYER_LASTER) {
				String text = getItemName(mapping - 1);
				float textWidth = paint.measureText(text);
				float x = centerX - textWidth / 2;
				float y = centerY - rd.getRadius() + 20;
				canvas.drawText(text, x, y, paint);
				canvas.drawLine(centerX, 0, centerX, rd.getHeight(), paint);
				canvas.drawLine(0, centerY, rd.getWidth(), centerY, paint);
			}
		}
	};

	private String getItemName(int index) {
		return discItemNames[index];
	}
}
