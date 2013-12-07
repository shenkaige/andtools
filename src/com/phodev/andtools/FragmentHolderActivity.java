package com.phodev.andtools;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.FrameLayout;

import com.phodev.andtools.samples.inner.InnerFragment;

/**
 * Only holder the Fragment
 * 
 * @author sky
 * 
 */
public class FragmentHolderActivity extends FragmentActivity {
	private static final String TAG = "FragmentHolderActivity";

	private static final String key_fragment_class_name = FragmentHolderActivity.class
			.getName() + "holder_f_class";

	public static boolean startFragment(Context ctx,
			Class<? extends InnerFragment> clz) {
		if (ctx == null || clz == null) {
			return false;

		}
		try {
			Intent intent = new Intent(ctx, FragmentHolderActivity.class);
			intent.putExtra(key_fragment_class_name, clz.getName());
			ctx.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private InnerFragment fragment;
	private FrameLayout rootView;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Intent intent = getIntent();
		Class<InnerFragment> clz = null;
		if (intent != null) {
			String clzName = intent.getStringExtra(key_fragment_class_name);
			try {
				clz = (Class<InnerFragment>) Class.forName(clzName);
			} catch (Exception e) {
				e.printStackTrace();
				clz = null;
			}
		}
		if (clz == null) {
			Log.e(TAG, "we need a InnerFragment class,but it was null");
			finish();
		}
		try {
			fragment = clz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if (fragment == null) {
			Log.e(TAG, "try to create Fragment instance failed,class:" + clz);
			finish();
		}
		//
		rootView = new FrameLayout(this);
		rootView.setId(rootView.hashCode());
		setContentView(rootView);
		//
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(rootView.getId(), fragment, clz.getCanonicalName());
		ft.commit();
	}

	void log(String msg) {
		Log.d(TAG, "" + msg);
	}
}
