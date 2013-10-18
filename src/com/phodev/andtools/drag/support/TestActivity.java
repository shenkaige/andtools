package com.phodev.andtools.drag.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.phodev.andtools.drag.CacheManager;
import com.phodev.andtools.drag.CellModel;
import com.phodev.andtools.drag.CellLayout;
import com.phodev.andtools.drag.DragLayer;
import com.phodev.andtools.drag.DragWorkspace;
import com.phodev.andtools.drag.DraggableGridView.OnCellClickListener;

public class TestActivity extends Activity {
	// ViewGroup rootView;
	DragWorkspace workspace;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// rootView = new LinearLayout(this);
		// rootView.setPadding(5, 5, 5, 5);
		// rootView.setBackgroundColor(Color.RED);
		// setContentView(rootView);
		//
		//
		CacheManager<CellModel> cacheManager = new CacheManager<CellModel>();
		workspace = new DragWorkspace(this);
		DragLayer dragLayer = workspace.getDragLayer();
		Map<Integer, Integer> refer = new HashMap<Integer, Integer>();
		refer.put(0, 6);
		//
		List<CellModel> out = new ArrayList<CellModel>();
		Random random = new Random();
		for (int i = 0; i < 100; i++) {
			CellModel c = new CellModel();
			c.i=i;
			c.setMoveable(true);
			c.setDeletable(true);
			c.setData(random.nextInt());
			out.add(c);
		}
		PageViewAdapter adapter = new PageViewAdapter(dragLayer, refer,
				cacheManager, new OnCellClickListener() {

					@Override
					public void onItemClick(CellLayout cell) {
						Toast.makeText(TestActivity.this,
								cell.hashCode() + " onClick",
								Toast.LENGTH_SHORT).show();
					}

				});
		adapter.setData(out);
		//
		PageContainerImpl pg = (PageContainerImpl) workspace.getPageContainer();
		//
		pg.setAdapter(adapter);
		setContentView(workspace);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// if (keyCode == KeyEvent.KEYCODE_MENU) {
		// if (rootView.getChildCount() > 0) {
		// rootView.removeAllViews();
		// } else {
		// rootView.addView(workspace);
		// }
		// }
		return super.onKeyUp(keyCode, event);
	}

}
