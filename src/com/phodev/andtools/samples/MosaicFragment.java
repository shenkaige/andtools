package com.phodev.andtools.samples;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.phodev.andtools.R;
import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.samples.inner.InnerFragment;
import com.phodev.andtools.utils.MosaicProcessor;

/**
 * 图片跑马灯
 * 
 * @author sky
 * 
 */
@SimpleDesc(title = "mosaic马赛克图片")
public class MosaicFragment extends InnerFragment {
	private ImageView img;
	private final int mosaic_block_size = 20;// px

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (img == null) {
			img = new ImageView(container.getContext());
			img.setBackgroundColor(Color.BLACK);
			//
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.girl);
			Rect rect = null;// null表示整个图片都处理
			// rect = new Rect(500, 500, 1000, 1000);// 指定区域
			img.setImageBitmap(MosaicProcessor.makeMosaic(bitmap, rect,
					mosaic_block_size));
		}
		return img;
	}

}
