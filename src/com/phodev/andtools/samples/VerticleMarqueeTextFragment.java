package com.phodev.andtools.samples;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.samples.inner.InnerFragment;
import com.phodev.andtools.widget.VerticalMarqueeTextview;

/**
 * 垂直滚动的TextView
 * 
 * @author sky
 * 
 */
@SimpleDesc(title = "垂直滚动的TextView")
public class VerticleMarqueeTextFragment extends InnerFragment {
	VerticalMarqueeTextview tv;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		tv = new VerticalMarqueeTextview(getActivity());

		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(-1, 300);
		//
		tv.setText("1\n2\n3\n4\n5\n6\n7\n8\n9\n0\na\nb\nc\nd\ne\nf\ng\nh\ni\nj\nk\nl\nm\nn\no\np\nq\nr\ns\nt");
		tv.setBackgroundColor(Color.BLACK);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(20);
		tv.setLayoutParams(lp);
		return tv;
	}
}
