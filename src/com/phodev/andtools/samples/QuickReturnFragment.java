package com.phodev.andtools.samples;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.phodev.andtools.R;
import com.phodev.andtools.adapter.InnerBaseAdapter;
import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.samples.inner.InnerFragment;

/**
 * 图片跑马灯
 * 
 * @author sky
 * 
 */
@SimpleDesc(title = "QuickReturnView")
public class QuickReturnFragment extends InnerFragment {
	private ListView listview;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_quick_return, null);
		listview = (ListView) root.findViewById(R.id.quick_return_content);
		listview.setAdapter(new InnerBaseAdapter<String>() {
			@Override
			public int getCount() {
				return 500;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView tv = new TextView(parent.getContext());
				tv.setText("" + position);
				return tv;
			}

		});
		return root;
	}

}
