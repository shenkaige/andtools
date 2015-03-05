package com.phodev.andtools.samples;

import android.graphics.Path;
import android.graphics.Path.Direction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.phodev.andtools.R;
import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.samples.inner.InnerFragment;
import com.phodev.andtools.widget.SplitLayout;

/**
 * 切割效果View
 * 
 * @author sky
 * 
 */
@SimpleDesc(title = "View切割效果")
public class SplitLayoutFragment extends InnerFragment {
	SplitLayout v;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = new SplitLayout(getActivity());
		ImageView img = new ImageView(getActivity());
		v.addView(img);
		img.setBackgroundResource(R.drawable.car);
		v.post(rn);
		return v;
	}

	Runnable rn = new Runnable() {
		int i;
		Path path = new Path();
		{
			path.addCircle(280, 280, 280, Direction.CCW);
		}

		@Override
		public void run() {
			if (i < 0 || i > 4) {
				i = 0;
			}
			switch (i) {
			case 0://
				v.setDropAreaByPercentage(// 左上角三角形
						0, 0,// point 1
						1, 0,// point 2
						0, 1// point 3
				);
				break;
			case 1:
				v.setDropAreaByPercentage(// 右下角三角形
						1, 1, //
						1, 0,//
						0, 1);
				break;
			case 2:
				v.setDropAreaByPercentage(// 上部矩形
						0, 0, //
						1, 0,//
						1, 0.5f,//
						0, 0.5f);
				break;
			case 3:
				v.setDropAreaByPercentage(// 下部矩形
						0, 0.5f, //
						1, 0.5f,//
						1, 1,//
						0, 1);
				break;
			case 4:
			default:
				v.setDropArea(path);
				break;
			}
			i++;
			v.postDelayed(this, 2000);
		}
	};

}
