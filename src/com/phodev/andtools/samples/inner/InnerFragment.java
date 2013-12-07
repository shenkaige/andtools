package com.phodev.andtools.samples.inner;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * The base Fragment for this Project
 * 
 * @author sky
 * 
 */
public abstract class InnerFragment extends Fragment {
	private View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		int resId = obtainInitLayoutResId();
		if (obtainInitLayoutResId() > 0) {
			rootView = inflater.inflate(resId, container, false);
			initWidget();
			return rootView;
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	protected View findViewById(int id) {
		if (rootView == null) {
			return null;
		}
		return rootView.findViewById(id);
	}

	protected View getRootView() {
		return rootView;
	}

	/**
	 * use with {@code #initWidget()}
	 * 
	 * @return
	 */
	protected int obtainInitLayoutResId() {
		return 0;
	}

	/**
	 * use with {@code #obtainInitLayoutResId()}
	 */
	protected void initWidget() {
	}
}
