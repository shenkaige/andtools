package com.phodev.andtools.samples;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.samples.inner.InnerFragment;
import com.phodev.andtools.widget.WaterWaveView;

/**
 * 水波效果View
 * 
 * @author sky
 * 
 */
@SimpleDesc(title = "水波效果View")
public class WaterWaveFragment extends InnerFragment {
	WaterWaveView v;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = new WaterWaveView(getActivity());
		v.setFillWaveSourceShapeRadius(30);
		return v;
	}
}
