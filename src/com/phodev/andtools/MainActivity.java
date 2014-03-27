package com.phodev.andtools;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.phodev.andtools.adapter.InnerBaseAdapter;
import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.samples.ChartViewFragment;
import com.phodev.andtools.samples.DonwloadFragment;
import com.phodev.andtools.samples.HttpConnectionFragment;
import com.phodev.andtools.samples.ImageMarqueeFragment;
import com.phodev.andtools.samples.MosaicFragment;
import com.phodev.andtools.samples.QuickReturnFragment;
import com.phodev.andtools.samples.RightSlidingMenuFragment;
import com.phodev.andtools.samples.RoundDiscFragment;
import com.phodev.andtools.samples.VerticleMarqueeTextFragment;
import com.phodev.andtools.samples.WebPageDriverFragment;
import com.phodev.andtools.samples.inner.InnerFragment;

public class MainActivity extends Activity {
	private FragmentAdapter adapter;
	private ListView listView;
	private List<Class<? extends InnerFragment>> data = new ArrayList<Class<? extends InnerFragment>>();
	{
		adapter = new FragmentAdapter();
		adapter.setData(data, false);
		//
		data.add(QuickReturnFragment.class);
		data.add(ChartViewFragment.class);
		data.add(HttpConnectionFragment.class);
		data.add(RightSlidingMenuFragment.class);
		data.add(VerticleMarqueeTextFragment.class);
		data.add(RoundDiscFragment.class);
		data.add(ImageMarqueeFragment.class);
		data.add(DonwloadFragment.class);
		data.add(WebPageDriverFragment.class);
		data.add(MosaicFragment.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//
		listView = (ListView) findViewById(R.id.listview);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(onItemClickListener);
	}

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			FragmentHolderActivity.startFragment(MainActivity.this,
					data.get(position));
		}

	};

	class FragmentAdapter extends
			InnerBaseAdapter<Class<? extends InnerFragment>> {
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh;
			if (convertView == null) {
				vh = new ViewHolder();
				convertView = LayoutInflater.from(parent.getContext()).inflate(
						R.layout.list_item_demo, null);
				vh.title = (TextView) convertView.findViewById(R.id.title);
				vh.desc = (TextView) convertView.findViewById(R.id.desc);
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}
			Class<? extends InnerFragment> clz = getData(position);
			SimpleDesc simpleDesc = clz.getAnnotation(SimpleDesc.class);
			String title = null;
			String desc = null;
			if (simpleDesc != null) {
				title = simpleDesc.title();
				desc = simpleDesc.desc();
			}
			if (desc == null || desc.length() <= 0) {
				desc = title;
			}
			vh.title.setText(title);
			vh.desc.setText(desc);
			return convertView;
		}

		class ViewHolder {
			TextView title;
			TextView desc;
		}
	}
}
