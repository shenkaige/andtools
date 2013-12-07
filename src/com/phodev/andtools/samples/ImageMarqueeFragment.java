package com.phodev.andtools.samples;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.phodev.andtools.R;
import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.samples.inner.InnerFragment;
import com.phodev.andtools.utils.CommonUtils;
import com.phodev.andtools.widget.ImageMarqueeView;
import com.phodev.andtools.widget.ImageMarqueeView.ImageMarqueeListener;

/**
 * 图片跑马灯
 * 
 * @author sky
 * 
 */
@SimpleDesc(title = "图片跑马灯")
public class ImageMarqueeFragment extends InnerFragment {
	ImageMarqueeView img;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		img = new ImageMarqueeView(getActivity());
		img.setAutoStart(true);
		img.setPauseOnPressed(true);
		img.setListener(imageMarqueeListener);
		img.setImageResource(R.drawable.car);
		return img;
	}

	private ImageMarqueeListener imageMarqueeListener = new ImageMarqueeListener() {

		@Override
		public void onUpdateDrawable(ImageMarqueeView view,
				Drawable newDrawable, boolean isRecycleClear) {
		}

		@Override
		public void onFinishOneCycle(ImageMarqueeView view) {
			CommonUtils.toast(getActivity(), "one cycle finish");
		}
	};

}
