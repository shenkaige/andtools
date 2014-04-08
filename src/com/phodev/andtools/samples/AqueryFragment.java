package com.phodev.andtools.samples;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.phodev.andtools.R;
import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.samples.inner.InnerFragment;
import com.phodev.andtools.utils.ImageUtility;

/**
 * 图片跑马灯
 * 
 * @author sky
 * 
 */
@SimpleDesc(title = "Aquery load img")
public class AqueryFragment extends InnerFragment {
	private ImageView img;
	final String url ="http://static.oschina.net/uploads/img/201404/04073810_aick.jpg";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		img = new ImageView(getActivity());
		ImageUtility.loadImage(img,url , 0, R.drawable.ic_launcher, false);
		return img;
	}

}
