package com.phodev.andtools.samples;

import com.phodev.andtools.widget.VerticalMarqueeTextview;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;

/**
 * 垂直滚动的TextView
 * 
 * @author sky
 * 
 */
public class VerticleMarqueeTextActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		VerticalMarqueeTextview tv = new VerticalMarqueeTextview(this);
		LayoutParams lp = new LayoutParams(-1, 300);
		tv.setText("1\n2\n3\n4\n5\n6\n7\n8\n9\n0\na\nb\nc\nd\ne\nf\ng\nh\ni\nj\nk\nl\nm\nn\no\np\nq\nr\ns\nt");
		tv.setBackgroundColor(Color.BLACK);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(20);
		setContentView(tv, lp);
	}
}
