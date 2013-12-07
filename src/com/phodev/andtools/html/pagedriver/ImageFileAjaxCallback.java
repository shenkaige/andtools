package com.phodev.andtools.html.pagedriver;

import java.io.File;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

/**
 * Load image file base on Aquery
 * 
 * @author sky
 * 
 */
public abstract class ImageFileAjaxCallback extends AjaxCallback<File> {
	private long mRequestId;

	public ImageFileAjaxCallback(long requestId) {
		mRequestId = requestId;
	}

	@Override
	public void callback(String url, File cachedFile, AjaxStatus callback) {
		String path = null;
		if (cachedFile != null) {
			path = cachedFile.getAbsolutePath();
		}
		callback(cachedFile, url, path, callback, mRequestId);
	}

	public abstract void callback(File cachedFile, String url,
			String fileCachedPath, AjaxStatus callback, long requestId);
}
