package com.phodev.andtools.samples;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.phodev.andtools.R;
import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.samples.inner.InnerFragment;
import com.phodev.andtools.widget.SlideSelectView;
import com.phodev.andtools.widget.SlideSelectView.SlideSelectListener;

/**
 * slide select content
 * 
 * @author sky
 */
@SimpleDesc(title = "Slide select content")
public class SlideSelectViewFragment extends InnerFragment {
	private TextView resultTv;
	private SlideSelectView slideView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_slide_select, container,
				false);
		resultTv = (TextView) v.findViewById(R.id.slide_select_result);
		slideView = (SlideSelectView) v.findViewById(R.id.slide_select_view);
		slideView.setSlideSelectListener(new SlideSelectListener() {

			@Override
			public void onSlideSelectedChanged(String content, int index) {
				resultTv.setText(content);
			}

			@Override
			public void onSlideSelectStateChanged(boolean isSelecting) {
				resultTv.setText(isSelecting ? "滑动选择" : "选择结束:"+resultTv.getText());
			}
		});
		return v;
	}

}
