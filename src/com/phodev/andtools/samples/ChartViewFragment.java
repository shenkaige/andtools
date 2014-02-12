package com.phodev.andtools.samples;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.samples.inner.InnerFragment;
import com.phodev.andtools.widget.SineWaveView;

/**
 * 图片跑马灯
 * 
 * @author sky
 * 
 */
@SimpleDesc(title = "ChartView")
public class ChartViewFragment extends InnerFragment {
	private TextView frequency = null;
	private TextView phase = null;
	private TextView amplifier = null;
	private Button btnwave = null;
	SineWaveView sw = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		sw = new SineWaveView(getActivity());
		return sw;
//		View root = inflater.inflate(R.layout.fragment_chat, null);
//		frequency = (TextView) root.findViewById(R.id.frequency);
//		phase = (TextView) root.findViewById(R.id.phase);
//		amplifier = (TextView) root.findViewById(R.id.amplifier);
//		btnwave = (Button) root.findViewById(R.id.wave);
//		sw = (SineWave) root.findViewById(R.id.sine_wave);
//		//
//		btnwave.setOnClickListener(new Button.OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				sw.Set(Float.parseFloat(amplifier.getText().toString()),
//						Float.parseFloat(frequency.getText().toString()),
//						Float.parseFloat(phase.getText().toString()));
//			}
//		});
//		//
//		frequency.setText(Float.toString(sw.GetFrequency()));
//		phase.setText(Float.toString(sw.GetPhase()));
//		amplifier.setText(Float.toString(sw.GetAmplifier()));
//		return root;
	}
	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// // TODO Auto-generated method stub
	// // float px = event.getX();
	// // float py = event.getY();
	// // sw.SetXY(px, py);
	// return super.onTouchEvent(event);
	// }

}
