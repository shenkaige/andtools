package com.phodev.andtools.samples;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.phodev.andtools.R;
import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.samples.inner.InnerFragment;
import com.phodev.andtools.widget.CircleLogoutBox;

/**
 * 水波效果View
 * 
 * @author sky
 * 
 */
@SimpleDesc(title = "切换场景效果View")
public class CircleLogoutBoxFragment extends InnerFragment {
	CircleLogoutBox v;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = new CircleLogoutBox(getActivity());
		v.setBackgroundResource(R.drawable.car);
		FrameLayout g = new FrameLayout(getActivity());
		ImageView im = new ImageView(getActivity());
		im.setImageResource(R.drawable.girl);
		g.addView(im);
		g.addView(v);
		v.postDelayed(new Runnable() {
			@Override
			public void run() {
				v.startLogout();
			}
		}, 1000);
		return g;
	}
}
