package com.phodev.andtools.samples;

import com.phodev.andtools.R;
import com.phodev.andtools.adapter.InnerBaseAdapter;
import com.phodev.andtools.widget.RightSlidingMenu;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * RightSlidingMenu demo
 * 
 * @author sky
 * 
 */
public class RightSlidingMenuActivity extends Activity {
	private RightSlidingMenu rightSlidingMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_right_sliding_menu);
		rightSlidingMenu = (RightSlidingMenu) findViewById(R.id.right_sliding);
		//
		ListView lvContent = (ListView) findViewById(R.id.right_sliding_content_lv);
		ListView lvMenu = (ListView) findViewById(R.id.right_sliding_menu_lv);
		lvContent.setAdapter(new SampleAdapter("Content:", Color.WHITE, 14));
		lvMenu.setAdapter(new SampleAdapter("Menu:", Color.BLACK, 26));
		lvMenu.setOnItemClickListener(menuItemClickListener);
	}

	private OnItemClickListener menuItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			rightSlidingMenu.hideMenu(true);
		}

	};

	class SampleAdapter extends InnerBaseAdapter<String> {
		String tag;
		int color;
		int textSize;

		SampleAdapter(String tag, int color, int textSize) {
			this.tag = tag;
			this.color = color;
			this.textSize = textSize;
		}

		@Override
		public int getCount() {
			return 100;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = new TextView(parent.getContext());
			tv.setText(tag + position);
			tv.setTextColor(color);
			tv.setPadding(8, 5, 0, 5);
			tv.setTextSize(textSize);
			tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			return tv;
		}
	}

}
