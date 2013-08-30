package com.phodev.andtools.viewpage;

import android.view.View;
import android.view.ViewGroup;

/**
 * View Wrap Config
 * 
 * @author skg
 * 
 */
public interface ViewWraperConfig {
	public boolean needConfigWrapView();

	public boolean isWrapViewShowable();

	public boolean configWrapView(ViewGroup parent, View configView);
}
